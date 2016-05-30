package captain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CaptainClient {

	private List<ServiceItem> origins = new ArrayList<ServiceItem>();
	private ServiceItem currentOrigin;
	private LocalService localServices = new LocalService();
	private LocalKv localKvs = new LocalKv();
	private Map<String, ServiceItem> providedServices = new HashMap<String, ServiceItem>();
	private Map<String, Boolean> watchedServices = new HashMap<String, Boolean>();
	private Set<String> watchedKvs = new HashSet<String>();
	private BeatKeeper keeper = new BeatKeeper(this);
	private List<ICaptainObserver> observers = new ArrayList<ICaptainObserver>(1);
	private BlockingQueue<Boolean> waiter;

	public CaptainClient(List<ServiceItem> origins) {
		this.origins.addAll(origins);
	}

	public CaptainClient(String host, int port) {
		origins.add(new ServiceItem(host, port));
	}

	public void shuffleOrigin() {
		Random r = new Random();
		int totalProbe = 0;
		for (ServiceItem origin : this.origins) {
			totalProbe += origin.probe();
		}
		int randomProbe = r.nextInt(totalProbe);
		int accProbe = 0;
		for (ServiceItem origin : this.origins) {
			accProbe += origin.probe();
			if (accProbe > randomProbe) {
				currentOrigin = origin;
				break;
			}
		}
	}

	public String urlRoot() {
		if (currentOrigin == null) {
			this.shuffleOrigin();
		}
		return currentOrigin.urlRoot();
	}

	public void onOriginFail() {
		if (currentOrigin.probe() > 1) {
			currentOrigin.probe(currentOrigin.probe() >> 1);
		}
	}
	
	public void onOriginSuccess() {
		currentOrigin.probe(ServiceItem.DEFAULT_PROBE);
	}

	public boolean[] checkDirty() throws UnirestException {
		JSONObject js = Unirest.get(urlRoot() + "/api/version").asJson().getBody().getObject();
		long serviceVersion = js.getLong("service.version");
		long kvVersion = js.getLong("kv.version");
		boolean[] flags = new boolean[2];
		if (serviceVersion != localServices.globalVersion()) {
			flags[0] = true;
		}
		if (kvVersion != localKvs.globalVersion()) {
			flags[1] = true;
		}
		return flags;
	}

	public Set<String> checkServiceVersions() throws UnirestException {
		if (this.watchedServices.isEmpty()) {
			return Collections.emptySet();
		}
		JSONObject js = Unirest.get(urlRoot() + "/api/service/version")
				.queryString("name", this.watchedServices.keySet()).asJson().getBody().getObject();
		JSONObject versions = js.getJSONObject("versions");
		Set<String> dirties = new HashSet<String>();
		for (String name : versions.keySet()) {
			if (localServices.version(name) != versions.getLong(name)) {
				dirties.add(name);
			}
		}
		return dirties;
	}

	public Set<String> checkKvVersions() throws UnirestException {
		if (this.watchedKvs.isEmpty()) {
			return Collections.emptySet();
		}
		JSONObject js = Unirest.get(urlRoot() + "/api/kv/version").queryString("key", this.watchedKvs).asJson()
				.getBody().getObject();
		JSONObject versions = js.getJSONObject("versions");
		Set<String> dirties = new HashSet<String>();
		for (String name : versions.keySet()) {
			if (localKvs.version(name) != versions.getLong(name)) {
				dirties.add(name);
			}
		}
		return dirties;
	}

	/**
	 * @param name
	 * @throws UnirestException
	 */
	public void reloadService(String name) throws UnirestException {
		JSONObject js = Unirest.get(urlRoot() + "/api/service/set").queryString("name", name).asJson().getBody()
				.getObject();
		long version = js.getLong("version");
		JSONArray services = js.getJSONArray("services");
		List<ServiceItem> addrs = new ArrayList<ServiceItem>();
		for (int i = 0; i < services.length(); i++) {
			js = services.getJSONObject(i);
			ServiceItem item = new ServiceItem(js.getString("host"), js.getInt("port"), js.getInt("ttl"));
			addrs.add(item);
		}
		localServices.version(name, version);
		localServices.replaceService(name, addrs);
		if (addrs.isEmpty() && this.healthy(name)) {
			this.offline(name);
		} else if (!addrs.isEmpty() && !this.healthy(name)) {
			this.online(name);
		}
	}

	public void reloadKv(String key) throws UnirestException {
		JSONObject js = Unirest.get(urlRoot() + "/api/kv/get").queryString("key", key).asJson().getBody().getObject();
		JSONObject kv = js.getJSONObject("kv");
		localKvs.version(key, kv.getLong("version"));
		localKvs.replaceKv(key, kv.getJSONObject("value"));
		this.kvUpdate(key);
	}

	public void keepService() throws UnirestException {
		for (String name : this.providedServices.keySet()) {
			ServiceItem service = this.providedServices.get(name);
			Unirest.get(urlRoot() + "/api/service/keep").queryString("name", name).queryString("host", service.host())
					.queryString("port", service.port()).queryString("ttl", service.ttl()).asJson();
		}
	}

	public void cancelService() throws UnirestException {
		for (String name : this.providedServices.keySet()) {
			ServiceItem service = this.providedServices.get(name);
			Unirest.get(urlRoot() + "/api/service/cancel").queryString("name", name).queryString("host", service.host())
					.queryString("port", service.port()).asJson();
		}
	}

	public CaptainClient watchService(String... serviceName) {
		for (String name : serviceName) {
			this.watchedServices.put(name, false);
			this.localServices.initService(name);
		}
		return this;
	}

	public CaptainClient watchKv(String... key) {
		for (String k : key) {
			this.watchedKvs.add(k);
			this.localKvs.initKv(k);
		}
		return this;
	}

	public CaptainClient failover(String name, ServiceItem... items) {
		List<ServiceItem> services = new ArrayList<ServiceItem>();
		for (ServiceItem item : items) {
			services.add(item);
		}
		this.localServices.failover(name, services);
		return this;
	}

	public CaptainClient provide(String name, ServiceItem service) {
		this.providedServices.put(name, service);
		return this;
	}

	public ServiceItem select(String name) {
		return localServices.randomService(name);
	}

	/**
	 * serviceCheckInterval in milliseconds
	 * 
	 * @param interval
	 * @return
	 */
	public CaptainClient checkInterval(int interval) {
		if (interval < 100 || interval > 5000) {
			throw new CaptainException("service check interval should be in range[100ms, 5000ms]");
		}
		this.keeper.checkInterval(interval);
		return this;
	}

	/**
	 * keepAlive in seconds
	 * 
	 * @param keepAlive
	 * @return
	 */
	public CaptainClient keepAlive(int keepAlive) {
		if (keepAlive < 5) {
			throw new CaptainException("keepalive must not be less than 5 seconds");
		}
		this.keeper.keepAlive(keepAlive);
		return this;
	}

	/**
	 * add observer
	 * 
	 * @param listener
	 * @return
	 */
	public CaptainClient observe(ICaptainObserver observer) {
		this.observers.add(observer);
		return this;
	}

	public void kvUpdate(String key) {
		for (ICaptainObserver observer : this.observers) {
			observer.kvUpdate(this, key);
		}
	}

	public JSONObject kv(String key) {
		return this.localKvs.kv(key);
	}

	public void online(String name) {
		boolean oldstate = allHealthy();
		this.watchedServices.put(name, true);
		for (ICaptainObserver observer : this.observers) {
			observer.online(this, name);
		}
		if (!oldstate && allHealthy()) {
			this.allOnline();
		}
	}

	public void allOnline() {
		for (ICaptainObserver observer : this.observers) {
			observer.allOnline(this);
		}
		BlockingQueue<Boolean> waiter = this.waiter;
		if (waiter != null) {
			waiter.offer(true);
		}
	}

	public void offline(String name) {
		this.watchedServices.put(name, false);
		for (ICaptainObserver observer : this.observers) {
			observer.offline(this, name);
		}
	}

	public boolean allHealthy() {
		for (Entry<String, Boolean> entry : this.watchedServices.entrySet()) {
			if (!entry.getValue()) {
				return false;
			}
		}
		return true;
	}

	public boolean healthy(String name) {
		return this.watchedServices.get(name);
	}

	public CaptainClient waitUntilAllOnline() {
		this.waiter = new ArrayBlockingQueue<Boolean>(1);
		return this;
	}

	public void start() {
		Unirest.setConcurrency(origins.size(), 1);
		Unirest.setTimeouts(2000, 1000);
		this.keeper.setDaemon(true);
		this.keeper.start();
		if (this.watchedServices.isEmpty()) {
			this.allOnline();
		}
		if (this.waiter != null) {
			try {
				this.waiter.take();
			} catch (InterruptedException e) {
			}
			this.waiter = null;
		}
	}

	public void hang() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public CaptainClient stopBeforeExit() {
		CaptainClient that = this;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				that.stop();
			}
		});
		return this;
	}

	public void stop() {
		try {
			this.cancelService();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		try {
			Unirest.shutdown();
		} catch (IOException e) {
		}
		this.keeper.quit();
	}

}

package captain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LocalService {

	private long globalVersion = -1;
	private Map<String, Long> versions = new ConcurrentHashMap<String, Long>();
	private Map<String, List<ServiceItem>> serviceLists = new ConcurrentHashMap<String, List<ServiceItem>>();
	private Map<String, List<ServiceItem>> failoverServices = new ConcurrentHashMap<String, List<ServiceItem>>();
	private ThreadLocal<Random> randoms = new ThreadLocal<Random>();

	public ServiceItem randomService(String name) {
		List<ServiceItem> services = serviceLists.get(name);
		List<ServiceItem> failovers = failoverServices.get(name);
		if (services == null || services.isEmpty()) {
			if (failovers == null || failovers.isEmpty()) {
				throw new CaptainException("no service provided for name=" + name);
			} else {
				services = failovers;
			}
		}
		Random random = randoms.get();
		if (random == null) {
			random = new Random();
			randoms.set(random);
		}
		return services.get(random.nextInt(services.size()));
	}
	
	public List<ServiceItem> allServices(String name) {
		return this.serviceLists.getOrDefault(name, Collections.emptyList());
	}

	public long globalVersion() {
		return globalVersion;
	}

	public void globalVersion(long version) {
		this.globalVersion = version;
	}

	public long version(String name) {
		return this.versions.getOrDefault(name, -1L);
	}

	public void version(String name, long version) {
		this.versions.put(name, version);
	}

	public void replaceService(String name, List<ServiceItem> addrs) {
		this.serviceLists.put(name, addrs);
	}

	public void initService(String name) {
		this.serviceLists.put(name, Collections.emptyList());
	}
	
	public void removeService(String name) {
		this.serviceLists.remove(name);
	}
	
	public void failover(String name, List<ServiceItem> services) {
		this.failoverServices.put(name, services);
	}

}

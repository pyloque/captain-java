package captain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LocalService {

	private long globalVersion = -1;
	private Map<String, Long> versions = new HashMap<String, Long>();
	private Map<String, List<ServiceItem>> serviceLists = new HashMap<String, List<ServiceItem>>();
	private Map<String, List<ServiceItem>> failoverServices = new HashMap<String, List<ServiceItem>>();
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
	
	public void failover(String name, List<ServiceItem> services) {
		this.failoverServices.put(name, services);
	}

}

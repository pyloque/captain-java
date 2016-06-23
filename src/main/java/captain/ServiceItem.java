package captain;

public class ServiceItem {

	private String host;
	private int port;
	private int ttl;
	private String payload;
	private int probe = DEFAULT_PROBE;

	public final static int DEFAULT_PROBE = 1024;

	public ServiceItem() {
	}

	public ServiceItem(String host, int port) {
		this(host, port, 30);
	}

	public ServiceItem(String host, int port, int ttl) {
		this(host, port, ttl, "");
	}

	public ServiceItem(String host, int port, int ttl, String payload) {
		this.host = host;
		this.port = port;
		this.ttl = ttl;
		this.payload = payload;
	}

	public static ServiceItem parse(String addr) {
		String[] pairs = addr.split(":");
		return new ServiceItem(pairs[0], Integer.parseInt(pairs[1]));
	}

	public String getKey() {
		return String.format("%s:%s", host, port);
	}

	public String getHost() {
		return host;
	}

	public ServiceItem setHost(String host) {
		this.host = host;
		return this;
	}

	public int getPort() {
		return port;
	}

	public ServiceItem setPort(int port) {
		this.port = port;
		return this;
	}

	public int getTtl() {
		return ttl;
	}

	public ServiceItem setTtl(int ttl) {
		this.ttl = ttl;
		return this;
	}

	public ServiceItem setProbe(int probe) {
		this.probe = probe;
		return this;
	}

	public int getProbe() {
		return probe;
	}

	public ServiceItem setPayload(String payload) {
		this.payload = payload;
		return this;
	}

	public String getPayload() {
		return payload;
	}

	public String urlRoot() {
		return String.format("http://%s:%s", host, port);
	}

	public String toString() {
		return String.format("%s:%s:%s", host, port, payload);
	}

	@Override
	public int hashCode() {
		return host.hashCode() & port;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ServiceItem)) {
			return false;
		}
		ServiceItem other = (ServiceItem) obj;
		return this.host.equals(other.host) && this.port == other.port;
	}

}

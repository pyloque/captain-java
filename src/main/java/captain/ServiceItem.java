package captain;

public class ServiceItem {

	private String host;
	private int port;
	private int ttl;
	private int probe = DEFAULT_PROBE;
	
	public final static int DEFAULT_PROBE = 1024;

	public ServiceItem() {
	}
	
	public ServiceItem(String host, int port) {
		this(host, port, 30);
	}

	public ServiceItem(String host, int port, int ttl) {
		this.host = host;
		this.port = port;
		this.ttl = ttl;
	}

	public String host() {
		return host;
	}

	public void host(String host) {
		this.host = host;
	}

	public int port() {
		return port;
	}

	public void port(int port) {
		this.port = port;
	}

	public int ttl() {
		return ttl;
	}

	public void ttl(int ttl) {
		this.ttl = ttl;
	}
	
	public void probe(int probe) {
		this.probe = probe;
	}
	
	public int probe() {
		return probe;
	}

	public String urlRoot() {
		return String.format("http://%s:%s", host, port);
	}

}

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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	
	public void setProbe(int probe) {
		this.probe = probe;
	}
	
	public int getProbe() {
		return probe;
	}

	public String urlRoot() {
		return String.format("http://%s:%s", host, port);
	}
	
	public String toString() {
		return String.format("%s:%s", host, port);
	}

}

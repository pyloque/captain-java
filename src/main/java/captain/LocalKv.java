package captain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

public class LocalKv {

	private long globalVersion = -1;
	private Map<String, JSONObject> kvs = new ConcurrentHashMap<String, JSONObject>();
	private Map<String, Long> versions = new ConcurrentHashMap<String, Long>();

	public long globalVersion() {
		return globalVersion;
	}

	public void globalVersion(long version) {
		this.globalVersion = version;
	}

	public long version(String key) {
		return this.versions.getOrDefault(key, -1L);
	}

	public void version(String key, long version) {
		this.versions.put(key, version);
	}

	public void initKv(String key) {
		this.kvs.put(key, new JSONObject());
	}
	
	public void removeKv(String key) {
		this.kvs.remove(key);
	}

	public void replaceKv(String key, JSONObject value) {
		this.kvs.put(key, value);
	}

	public JSONObject kv(String key) {
		return this.kvs.getOrDefault(key, new JSONObject());
	}

}

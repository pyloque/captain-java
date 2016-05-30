package captain;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class LocalKv {

	private long globalVersion = -1;
	private Map<String, JSONObject> kvs = new HashMap<String, JSONObject>();
	private Map<String, Long> versions = new HashMap<String, Long>();

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

	public void initKv(String name) {
		this.kvs.put(name, new JSONObject());
	}

	public void replaceKv(String name, JSONObject value) {
		this.kvs.put(name, value);
	}

	public JSONObject kv(String key) {
		return this.kvs.getOrDefault(key, new JSONObject());
	}

}

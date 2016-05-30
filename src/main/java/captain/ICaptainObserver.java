package captain;

public interface ICaptainObserver {

	/**
	 * service dependency ready called in async thread
	 */
	public void online(CaptainClient client, String name);
	
	/**
	 * all service dependencies ready called in async thread
	 */
	public void allOnline(CaptainClient client);
	
	/**
	 * kv update callback
	 */
	public void kvUpdate(CaptainClient client, String key);

	/**
	 * service dependency down called in async thread
	 */
	public void offline(CaptainClient client, String name);

}

package captain;

public class Service1 {

	public static void main(String[] args) throws Exception {
		CaptainClient client = new CaptainClient("localhost", 6789);
		client.provide("service1", new ServiceItem("localhost", 6100)).observe(new ICaptainObserver() {

			@Override
			public void online(CaptainClient client, String name) {

			}

			@Override
			public void allOnline(CaptainClient client) {
				System.out.println("service1 is ready");
			}

			@Override
			public void offline(CaptainClient client, String name) {
				System.out.println("service" + name + " is offline");
			}

			@Override
			public void kvUpdate(CaptainClient client, String key) {
				System.out.println(client.kv(key));
			}
		}).watchKv("project_settings_service1").stopBeforeExit().start();
		client.hang(); // hang just for test
	}
}

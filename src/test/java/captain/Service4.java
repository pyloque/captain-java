package captain;

public class Service4 {

	public static void main(String[] args) throws Exception {
		CaptainClient client = new CaptainClient("localhost", 6789);
		client.watch("service1", "service2", "service3").observe(new ICaptainObserver() {

			@Override
			public void online(CaptainClient client, String name) {
				System.out.println("ready:" + name);
			}

			@Override
			public void allOnline(CaptainClient client) {
				System.out.println("service4 is ready");
				System.out.println("service1:" + client.select("service1").urlRoot());
				System.out.println("service2:" + client.select("service2").urlRoot());
				System.out.println("service3:" + client.select("service3").urlRoot());
			}

			@Override
			public void offline(CaptainClient client, String name) {
				System.out.println("offline:" + name);
			}

			@Override
			public void kvUpdate(CaptainClient client, String key) {
				
			}

			@Override
			public void serviceUpdate(CaptainClient client, String name) {
				
			}

		}).stopBeforeExit().waitUntilAllOnline().start();
		client.hang(); // hang just for test
	}
}

package captain;

public class Service2 {

	public static void main(String[] args) throws Exception {
		CaptainClient client = new CaptainClient("localhost", 6789);
		client.provide("service2", new ServiceItem("localhost", 6200)).observe(new ICaptainObserver() {

			@Override
			public void online(CaptainClient client, String name) {

			}

			@Override
			public void allOnline(CaptainClient client) {
				System.out.println("service2 is ready");
			}

			@Override
			public void offline(CaptainClient client, String name) {

			}

			@Override
			public void kvUpdate(CaptainClient client, String key) {
				
			}
		}).stopBeforeExit().start();
		client.hang(); // hang just for test
	}
}

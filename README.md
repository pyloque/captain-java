Captain
--------------------------
Captain is yet another service discovery implementation based on redis.
Captain sacrifices a little high availability for simplicity and performance.
In most cases, we dont have so many machines as google/amazon.
The possibility of machine crashing is very low, high Availability is not so abviously important yet.
But the market only provides zookeeper/etcd/consul, they are complex, at least much complexer compared with captain.
https://github.com/pyloque/captain

Use Captain Java Client
-----------------------
```java
import java.util.List;
import java.util.ArrayList;
import captain.CaptainClient;

public class Service3 {

    public static void main(String[] args) throws Exception {
    	// connect to multiple captain servers
    	List<ServiceItem> origins = new ArrayList<ServiceItem>(2);
    	origins.add(new ServiceItem("localhost", 6789));
    	origins.add(new ServiceItem("localhost", 6790));
    	CaptainClient client = new CaptainClient(origins);
        // CaptainClient client = new CaptainClient("localhost", 6789); single captain server
        client.watch("service1", "service2") // define service dependencies
        	  .failover("service1", new ServiceItem("localhost", 6100), new ServiceItem("localhost", 6101)) // incase there's no service1 provided
        	  .provide("service3", new ServiceItem("localhost", 6300, 30)) // provide a service with expiring age of 30s
        	  .observe(new IServiceObserver() { // add event observer for service status change

		            @Override
		            public void online(CaptainClient client, String name) { // dependent service is ready for service $name
		
		            }
		
		            @Override
		            public void allOnline(CaptainClient client) {  // all dependent service is ready
		                System.out.println("service3 is ready");
                        System.out.println(client.select("service1").urlRoot()) // now select the service you want
                        System.out.println(client.select("service2").urlRoot()) // now select the service you want
		            }
		            
		            @Override
		            public void kvUpdate(CaptainClient client, String name) {
		            	System.out.println(client.kv(name));
		            }
		
		            @Override
		            public void offline(CaptainClient client, String name) { // for servcie downgrade
		
		            }
		        })
		        .keepAlive(5) // provided service heartbeat interval for keepalive
		        .checkInterval(1000) // check interval for watching dependent services
		        .stopBeforeExit() // cancel service before jvm quit
		        .watchKv("project_settings") // watch key value update
		        .waitUntilAllOnline() // let start method block until all dependent services are ready
		        .start(); // start connect to captain server
        client.hang(); // wait for ctrl+c
    }
}

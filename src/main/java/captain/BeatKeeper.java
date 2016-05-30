package captain;

import java.util.Set;

import com.mashape.unirest.http.exceptions.UnirestException;

public class BeatKeeper extends Thread {

	private CaptainClient client;
	private long lastKeepTs;
	private int keepAlive = 10;
	private int checkInterval = 1000;
	private boolean stop;

	public BeatKeeper(CaptainClient client) {
		this.client = client;
	}

	public BeatKeeper keepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
		return this;
	}

	public BeatKeeper checkInterval(int checkInterval) {
		this.checkInterval = checkInterval;
		return this;
	}

	public void run() {
		while (!stop) {
			this.client.shuffleOrigin(); // use same captain url per loop
			boolean success = true;
			try {
				this.watch();
			} catch (UnirestException e) {
				e.printStackTrace();
				success = false;
			}
			try {
				this.keepService();
			} catch (UnirestException e) {
				e.printStackTrace();
				success = false;
			}
			if (success) {
				this.client.onOriginSuccess();
			} else {
				this.client.onOriginFail();
			}
			try {
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				stop = true;
			}
		}
	}

	public void watch() throws UnirestException {
		boolean[] flags = client.checkDirty();
		if (flags[0]) {
			Set<String> dirties = client.checkServiceVersions();
			for (String name : dirties) {
				client.reloadService(name);
			}
		}
		if (flags[1]) {
			Set<String> dirties = client.checkKvVersions();
			for (String name : dirties) {
				client.reloadKv(name);
			}
		}
	}

	public void keepService() throws UnirestException {
		long now = System.currentTimeMillis();
		if (now - lastKeepTs > keepAlive * 1000) {
			client.keepService();
			lastKeepTs = now;
		}
	}

	public void quit() {
		this.interrupt();
		this.stop = true;
	}

}

package howToCodeSamples.services;

import java.util.Properties;

import howToCodeSamples.services.transports.mqtt.Mqtt;

public class Services {
	private static Mqtt mqtt;

	public static void initServices(Properties config) {
		mqtt = new Mqtt(config);
		if (mqtt.getConfigResult()) {
			System.out.println("Mqtt initialized successfully.");
		}
	}

	public static void logService(String message) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mqtt.getConfigResult()) {
					try {
						mqtt.publish(message);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("problem sending data to Mqtt Server");
					}
				} else {
					System.err.println("Missing Mqtt configuration.");
				}
			}
		});
		thread.run();
	}
}
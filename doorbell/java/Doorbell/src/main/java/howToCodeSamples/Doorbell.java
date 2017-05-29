package howToCodeSamples;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;

import howToCodeSamples.services.Services;
import mraa.Platform;
import mraa.mraa;
import upm_buzzer.Buzzer;
import upm_jhd1313m1.*;
import upm_ttp223.TTP223;

public class Doorbell {

	static short[] colour_white = { 255, 255, 255 };
	static short[] colour_green = { 0, 255, 0 };

	static boolean done = false;

	private static int screenBus = 0, touchPin = 0, buzzerPin = 0;

	private static Jhd1313m1 lcd = null;
	private static Buzzer buzzer = null;
	private static TTP223 touch = null;

	private static Properties config = new Properties();
	
	static class Dingdong implements Runnable {
		public void run() {
			if (touch.isPressed()) {

				Thread t = new Thread(incrementRunnable);
				t.start();

				message("dingdong", colour_green);
				synchronized (buzzer) {
					buzzer.playSound(2600, 0);
				}
				notifyService();
			} else {
				reset();
			}
		}
	}

	static void reset() {
		message("doorbot ready", null);

		synchronized (buzzer) {
			buzzer.setVolume(0.5f);
			buzzer.stopSound();
			buzzer.stopSound();
		}
	}

	static String server = "http://intel-examples.azurewebsites.net/logger/doorbell";
	static String auth = "s3cr3t";

	static Runnable incrementRunnable = new Runnable() {

		@Override
		public void run() {
			HttpClient client = HttpClients.createMinimal();
			HttpUriRequest request = RequestBuilder.put().setUri(server)
					.setHeader("X-Auth-Token", auth).build();
			try {
				client.execute(request);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	static synchronized void message(String message, short[] colour) {
		while (message.length() < 16)
			message += " ";
		lcd.setCursor(0, 0);
		lcd.write(message);
		if (colour == null || colour.length != 3)
			colour = colour_white;

		lcd.setColor(colour[0], colour[1], colour[2]);
	}

	public static void main(String[] args) {

		if (args.length >= 2) {
			server = args[0];
			auth = args[1];
		} else {
			String configuration = String.format(
					"Default configuration: SERVER = '%s', AUTH = '%s'",
					server, auth);
			System.out.println(configuration);
			System.out
					.println("Provide configuration as parameters: <server> <auth>");
		}

		Platform platform = mraa.getPlatformType();
		if (platform == Platform.INTEL_GALILEO_GEN1
				|| platform == Platform.INTEL_GALILEO_GEN2
				|| platform == Platform.INTEL_EDISON_FAB_C) {
			screenBus = 0;
			touchPin = 4;
			buzzerPin = 5;

		} else if (platform == Platform.INTEL_DE3815) {
			screenBus = 0 + 512;
			touchPin = 4 + 512;
			buzzerPin = 5 + 512;
		} else {
			System.err.println("Unsupported platform, exiting");
			return;
		}

		loadConfigurationFile();
		Services.initServices(config);
		lcd = new Jhd1313m1(screenBus);
		buzzer = new Buzzer(buzzerPin);
		touch = new TTP223(touchPin);
		lcd.displayOn();
		lcd.clear();

		reset();

		Dingdong dingdong = new Dingdong();
		touch.installISR(2, dingdong);
		while (!done) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
		}
	}
	
	/**
	 * load configuration file 
	 */
	private static void loadConfigurationFile() {
		// TODO Auto-generated method stub
		try {
			// Load configuration data from `config.properties` file. Edit this file
			// to change to correct values for your configuration
			config.load(Doorbell.class.getClassLoader().getResourceAsStream("resources/config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void notifyService() {
		String text = "{\"Doorbell pressed on " + new Date().toString() + "\"}";
		Services.logService(text);
	}
}
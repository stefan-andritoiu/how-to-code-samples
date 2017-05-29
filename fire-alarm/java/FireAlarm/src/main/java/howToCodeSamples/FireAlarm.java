package howToCodeSamples;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import howToCodeSamples.services.Services;
import mraa.Platform;
import mraa.mraa;
import upm_grove.GroveTemp;

public class FireAlarm {

	private static AlarmLcd lcdScreen;
	private static AlarmBuzzer buzzer;
	private static GroveTemp temperatureSensor;
	private static int lastTemperature = -1;
	private static  int temperatureThreshold;
	private static TimerTask alarmTask;
	private static Timer FireTimer;
	private static boolean alarmTick = true;
	private static boolean isAlertOn = false;
	private static Properties config = new Properties();
	private static int screenBus = 0, tempPin = 0, buzzerPin = 0;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Platform platform = mraa.getPlatformType();
		if (platform == Platform.INTEL_GALILEO_GEN1
				|| platform == Platform.INTEL_GALILEO_GEN2
				|| platform == Platform.INTEL_EDISON_FAB_C) {
			screenBus = 0;
			tempPin = 0;
			buzzerPin = 5;
		} else if (platform == Platform.INTEL_DE3815) {
			screenBus = 0 + 512;
			tempPin = 0 + 512;
			buzzerPin = 5 + 512;
		} else {
			System.err.println("Unsupported platform, exiting");
			return;
		}

		loadConfigurationFile();
		Services.initServices(config);
		initiateSensors();
		listenToTemperatureChanges();
	}


	/**
	 * load configuration file and retrieve temperature threshold
	 */
	private static void loadConfigurationFile() {
		// TODO Auto-generated method stub
		try {
			// Load configuration data from `config.properties` file. Edit this file
			// to change to correct values for your configuration
			config.load(FireAlarm.class.getClassLoader().getResourceAsStream("resources/config.properties"));
			temperatureThreshold =Integer.parseInt(config.getProperty("TEMPERATURE_THRESHOLD"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * when alarm is on this method will be called multiple times to simulate an alarm with the 
	 * LCD screen and buzzer.
	 */
	private static void handleFireAlertTick() {
		if(alarmTick){
			lcdScreen.setLcdColor("white");
			buzzer.stopBuzzing();
		}
		else{
			lcdScreen.setLcdColor("red");
			buzzer.buzz();
		}
		alarmTick = !alarmTick;
	}

	/**
	 * instantiate sensor helper objects
	 */
	private static void initiateSensors() {
		// TODO Auto-generated method stub
		lcdScreen = new AlarmLcd(screenBus);
		buzzer = new AlarmBuzzer(buzzerPin);
		temperatureSensor = new GroveTemp(tempPin);
	}

	/**
	 * run periodic task which checks change in temperature and alerts a fire if detected
	 * or stops the alarm if the fire is out. 
	 */
	private static void listenToTemperatureChanges() {
		// TODO Auto-generated method stub
		Timer temperatureCheckTimer = new Timer();
		temperatureCheckTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int currentTemperature = temperatureSensor.value();
				lcdScreen.displayMessageOnLcd("temperature: " + currentTemperature, 0);
				System.out.println("value: " +  currentTemperature);
				if(lastTemperature < temperatureThreshold && currentTemperature >= temperatureThreshold && !isAlertOn){
					alertFire();
				}
				if(lastTemperature >= temperatureThreshold && currentTemperature < temperatureThreshold && isAlertOn){
					stopAlertingFire();
				}
				lastTemperature = currentTemperature;
			}


		},0,2000);
	}

	/**
	 * start alerting a fire.
	 */
	private static void alertFire() {
		// TODO Auto-generated method stub
		Utils.sendMessageWithTwilio("fire alarm", config);
		Utils.notifyAzure((new Date().toString()), config);
		notifyService("Fire alarm!");
		System.out.println("fire!");
		isAlertOn = true;
		lcdScreen.displayMessageOnLcd("fire detected", 1);
		lcdScreen.setLcdColor("red");
		buzzer.buzz();
		alarmTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				handleFireAlertTick();
			}
		};
		FireTimer = new Timer();
		FireTimer.schedule(alarmTask, 0,250);
	}

	/**
	 * stop alerting a fire.
	 */
	private static void stopAlertingFire() {
		// TODO Auto-generated method stub
		isAlertOn = false;
		System.out.println("fire stopped!");
		lcdScreen.displayMessageOnLcd("fire stopped", 1);
		lcdScreen.setLcdColor("white");
		buzzer.stopBuzzing();
		notifyService("Fire stopped");
		FireTimer.cancel();
	}
	
	private static void notifyService(String message) {
		String text = "{\"State\": \""+ message + " on " + new Date().toString() + "\"}";
		Services.logService(text);
	}

}
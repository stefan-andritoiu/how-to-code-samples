package howToCodeSamples;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import howToCodeSamples.services.Services;
import mraa.Platform;
import mraa.mraa;
import upm_gas.TP401;
import upm_grovespeaker.GroveSpeaker;


/**
 * continuously checks the air quality for airborne contaminates;
 * sounds an audible warning when the air quality is unhealthy;
 * stores a record of each time the air quality sensor detects contaminates,
 * using cloud-based data storage.
 * 
 * @author rhassidi
 *
 */
public class AirQualitySensor {


    private static Properties config = new Properties();
    private static upm_grovespeaker.GroveSpeaker speaker;
    private static upm_gas.TP401 airQualitySensor;
    private static int prev = 0;
    private static int airPin = 0, speakerPin = 0;

    /**
     * Initializes sensors
     */
	private static void initSensors() {
		speaker = new GroveSpeaker(speakerPin);
		airQualitySensor = new TP401(airPin);
	}

    /**
     * Plays a chime sound using the Grove speaker
     */
    public static void chime(){
	speaker.playSound('a', true, "low");
	speaker.playSound('c', true, "low");
	speaker.playSound('g', true, "low");
    }

    
    /**
     * Alert user that the air quality level has exceeded
     * the allowed threshold of safety
     */
    public static void alertBadAirQuality(){
		Utils.NotifyAzure(config);
		notifyService();
		chime();
	}

    /**
     * Checks every 1sec if the air quality is higher than the threshold that is 
     * defined in the config file.
     */
    private static void checkAirQuality() {
	int threshold = Integer.parseInt(config.getProperty("THRESHOLD"));

	Timer alertTimer = new Timer();
	alertTimer.schedule(new TimerTask() {
	    public void run() {
		int quality = airQualitySensor.getSample();
		System.out.println("quality: " + quality);
		if(prev <= threshold && quality > threshold && prev != 0){
		    alertBadAirQuality();
		}
		prev = quality; 
	    }

	}, 1000, 1000);
    }
    
    private static void notifyService() {
    	String text = "{\"Air quality alert\": \"" + " on " + new Date().toString() + "\"}";
		Services.logService(text);
	}
    
    /**
     * Main function checks the air quality every 1 second,
     * then calls the `alert()` function if quality level has exceeded
     * the allowed threshold of safety
     */
    public static void main(String[] args) {
	System.out.println("Starting main");

		Platform platform = mraa.getPlatformType();
		if (platform == Platform.INTEL_GALILEO_GEN1
				|| platform == Platform.INTEL_GALILEO_GEN2
				|| platform == Platform.INTEL_EDISON_FAB_C) {
			speakerPin = 5;
			airPin = 0;
		} else if (platform == Platform.INTEL_DE3815) {
			speakerPin = 5 + 512;
			airPin = 0 + 512;
		} else {
			System.err.println("Unsupported platform, exiting");
			return;
		}

	try {
	    config.load(AirQualitySensor.class.getClassLoader().getResourceAsStream("resources/config.properties"));
	} catch (IOException e) {
	    e.printStackTrace();
	}
	Services.initServices(config);
	initSensors();
	checkAirQuality();
    }
}
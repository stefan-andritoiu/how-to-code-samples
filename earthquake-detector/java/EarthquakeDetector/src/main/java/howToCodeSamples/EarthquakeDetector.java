package howToCodeSamples;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import howToCodeSamples.services.Services;
import mraa.Platform;
import mraa.mraa;

/**
 * @author rhassidi
 *
 */
public class EarthquakeDetector {

    private static Properties config = new Properties();
    private static AccelerometerSensor accelerometer;
    private static LcdSensor lcd;
    private static float[] axis;
    private static boolean prev;
    private static boolean quake; 
    static int i = 0;
    static int screenBus = 0, accelPin = 0;

    /**
     * Initializing 3-axis and lcd sensors
     */
    private static void initSensors(){
	accelerometer = new AccelerometerSensor(accelPin);
	lcd = new LcdSensor(screenBus, config);
    }


    /**
     * loads the config file
     */
    private static void loadConfig(){
	try {
	    config.load(EarthquakeDetector.class.getClassLoader().getResourceAsStream("resources/config.properties"));
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }


    /**
     * Stops the verification message that is displayed on the lcd screen
     */
    private static void stopVerificationMeesage() {
	Timer stopVerificationTimer = new Timer();
	stopVerificationTimer.schedule(new TimerTask(){
	    public void run(){
		lcd.writeMessage("stopping");
	    }
	}, 15000);
    }


    /**
     * Verifies with the USGS API if an earthquake actually took place
     */
    private static void verify(){
	lcd.writeMessage("checking");
	String message="";
	try {
	    message = Utils.getRealEarthquakeStatus(config);
	} catch (IOException e) {
	    e.printStackTrace();

	}
	//message = "Earthquake!!!";
	//message = "No quake";

	if(!message.equals("")){
	    lcd.writeMessage(message);
	}

	stopVerificationMeesage();

    }


    /**
     * Every 100ms takes a sample from the 3-axis sensor to check if it detected an
     * earthquake
     */
    public static void checkQuake() {
	Timer alertTimer = new Timer();
	alertTimer.schedule(new TimerTask() {
	    public void run() {
		axis = accelerometer.getAccelerometer().getAcceleration();
		System.out.println("axis: {"+axis[0]+","+axis[1]+","+axis[2]+"}");
		quake = (axis[0] > 1);
		//if(i%10 == 0){
		//	quake = true;
		//}
		//i++;
		if(quake && !prev){
		    verify();
		}
		prev = quake;
//		quake = false;
	    }

	}, 100, 100);

    }
    
    public static void notifyService(String message) {
		String text = "{\""+ message + " on " + new Date().toString() + "\"}";
		Services.logService(text);
	}

    /**
     *  Main function 
     *  calls checkQuake that checks every 100ms to see if there has been motion detected
     *  by the accelerometer. If so, it calls verify to check the USGS API and see if
     *  an earthquake has actually occurred, and displays info on the display
     */
    public static void main(String[] args) {
		prev = false;

		Platform platform = mraa.getPlatformType();
		if (platform == Platform.INTEL_GALILEO_GEN1
				|| platform == Platform.INTEL_GALILEO_GEN2
				|| platform == Platform.INTEL_EDISON_FAB_C) {
			screenBus = 6;
			accelPin = 2;

		} else if (platform == Platform.INTEL_DE3815) {
			screenBus = 6 + 512;
			accelPin = 2 + 512;
		} else {
			System.err.println("Unsupported platform, exiting");
			return;
		}

		loadConfig();
		Services.initServices(config);
		initSensors();
		checkQuake();
    }

}
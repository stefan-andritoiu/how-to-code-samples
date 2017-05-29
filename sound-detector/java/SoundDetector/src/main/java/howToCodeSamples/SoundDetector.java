package howToCodeSamples;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import howToCodeSamples.services.Services;
import mraa.Platform;
import mraa.mraa;

/**
 * 
 * @author rhassidi
 *
 */
public class SoundDetector {

    private static upm_mic.Microphone mic;
    private static upm_jhd1313m1.Jhd1313m1 rgbLcd;
    private static int screenBus = 0, micPin = 0;
    
    private static final short WHITE[] = {0,0,0};
    private static final short YELLOW[] = {255,255,0};
    private static final short GREEN[] = {0,255,0};
    private static final short BLUE[] = {0,0,255};
    private static final short PURPEL[] = {255,0,255};
    private static final short RED[] = {255,0,0};
    
    private static short buffer[] = new short[128];
    
    /**
     * Initializes the lcd and microphone sensors
     */
	private static void initSensors() {
		rgbLcd = new upm_jhd1313m1.Jhd1313m1(screenBus);
		mic = new upm_mic.Microphone(micPin);
	}

    /**
     * Displays the current average volume on the lcd screen and colors the screen 
     * by the scale (from low to high volume): WHITE, YELLOW, GREEN, BLUE, PURPEL, RED
     * 
     * @param averageVolume The average volume to display on the lcd screen
     */
    private static void write(int averageVolume){
	int color = averageVolume / 100;
	short currColor[] = (color == 0)? WHITE : 
	    ((color == 1) ? YELLOW : 
		((color == 2) ? GREEN : 
		    ((color == 3) ? BLUE : 
			((color == 4) ? PURPEL : 
			    RED))));
	
	rgbLcd.clear();
	rgbLcd.setCursor(0, 0);
	rgbLcd.setColor(currColor[0], currColor[1], currColor[2]);
	rgbLcd.write("The average");
	rgbLcd.setCursor(1, 0);
	rgbLcd.write("volume is: " + averageVolume);
	
	Utils.NotifyAzure("The average volume is: " + averageVolume);
	notifyService(Integer.toString(averageVolume));
    }
    
    private static void notifyService(String message) {
		String text = "{\"Average volume\": \""+ message + " on " + new Date().toString() + "\"}";
		Services.logService(text);
	}
    
 
    /**
     * Main function checks the average volume every 1 second,
     * and writes the results to the lcd screen and to azure
     */
    public static void main(String[] args) {
		Platform platform = mraa.getPlatformType();
		if (platform == Platform.INTEL_GALILEO_GEN1
				|| platform == Platform.INTEL_GALILEO_GEN2
				|| platform == Platform.INTEL_EDISON_FAB_C) {
			screenBus = 0;
			micPin = 2;
		} else if (platform == Platform.INTEL_DE3815) {
			screenBus = 0 + 512;
			micPin = 2 + 512;
		} else {
			System.err.println("Unsupported platform, exiting");
			return;
		}
		Services.initServices(Utils.loadConfig());
		initSensors();
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				int len = mic.getSampledWindow(2, buffer);
				int averageVolume = 0;
				for (int i : buffer) {
					averageVolume += i;
				}
				averageVolume /= len;
				// System.out.println("decibel val = "+ average);
				write(averageVolume);
			}
		}, 1000, 1000);
	}
}
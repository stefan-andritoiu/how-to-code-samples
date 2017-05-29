package howToCodeSamples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import howToCodeSamples.services.Services;
import mraa.Platform;
import mraa.mraa;

public class AccessControl {

    private static Properties config;
    private static MotionActivityHandling motionActivityHandling;   
    private static int screenBus = 0, motionPin = 0;

    /**
     * Initializes motion activity handling object that constructs the lcd and the 
     * motion sensor instances
     */
    private static void initSensors() {
	motionActivityHandling = new MotionActivityHandling(screenBus, motionPin);
    }

    /**
     * Loads the main config file for future use
     */
    private static void loadConfigFile() {
	try {
	    config = Utils.loadConfig();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /** 
     * Starts the built-in web server that serves up the web page
     * used to enter code after triggering the motion sensor
     */
    public static void setupServer() {
	ServerSetup server= new ServerSetup();
	server.setupServer(8080);

	// Set new alarm time submitted by the web page using HTTP GET
	server.addServlet("/", new ServerSetup.GetCall(){
	    @Override
	    public void runCall(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (request.getRequestURI().matches("/")){

		    String sCurrentLine;
		    @SuppressWarnings("resource")
		    BufferedReader indexFile = new BufferedReader(new FileReader("/var/AccessControl/index.html"));
		    StringBuilder stringBuilder = new StringBuilder();
		    while ((sCurrentLine = indexFile.readLine()) != null) {
			stringBuilder.append(sCurrentLine).append("\n");
		    }
		    response.getWriter().println(stringBuilder.toString());
		}
	    }  
	});

	// Stop alarm if the given code matches the code in the config file
	server.addServlet("/alarm", new ServerSetup.GetCall(){
	    public void runCall(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//		System.out.println("motion.isExpectingCode(): "+motionSensor.isExpectingCode());
		//		System.out.println("req.getParam('code'): "+request.getParameter("code"));
		//		System.out.println("config.getProperty('CODE'): "+ codeConfig.getProperty("CODE"));
		if(motionActivityHandling.isExpectingCode() && 
			(request.getParameter("code").equals(config.getProperty("CODE")))){
		    motionActivityHandling.setValidatedTrue();
		}
	    }
	});

	server.run();
    }
    
    public static void notifyService(String message) {
    	String text = "{\"State\": \""+ message + " on " + new Date().toString() + "\"}";
		Services.logService(text);
	}

    /**
     *  The main function calls `server()` to start up
     *  the built-in web server used to enter the access code
     * after triggering the alarm.
     * It also calls the `lookForMotion()` function which monitors
     */
	public static void main(String[] args) {
		Platform platform = mraa.getPlatformType();
		if (platform == Platform.INTEL_GALILEO_GEN1
				|| platform == Platform.INTEL_GALILEO_GEN2
				|| platform == Platform.INTEL_EDISON_FAB_C) {
			screenBus = 0;
			motionPin = 4;
		} else if (platform == Platform.INTEL_DE3815) {
			screenBus = 0 + 512;
			motionPin = 4 + 512;
		} else {
			System.err.println("Unsupported platform, exiting");
			return;
		}
		loadConfigFile();
		Services.initServices(config);	
		initSensors();
		setupServer();
		motionActivityHandling.lookForMotion();
    }
}
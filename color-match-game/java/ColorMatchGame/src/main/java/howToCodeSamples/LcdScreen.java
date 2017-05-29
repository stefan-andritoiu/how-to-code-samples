package howToCodeSamples;

import upm_jhd1313m1.*;


public class LcdScreen {
	public static Jhd1313m1 lcdScreen;
	// Colors used for the RGB LCD
	private static final short[] redColor = {240,65,17}; 
	private static final short[] whiteColor = {255, 255, 255};
	private static final short[] greenColor = {17,240,76}; 
	private static final short[] blueColor = {0, 0, 255}; 
	private static final short[] yellowColor = {240,240,17}; 
	
	public static final String BLUE = "blue";
	public static final String RED = "red";
	public static final String GREEN = "green";
	public static final String YELLOW = "yellow";
	public static final String WHITE = "white";

	public LcdScreen(int bus) {
		lcdScreen = new Jhd1313m1(bus);
		displayMessageOnLcd("", 1);
	}

	// Sets the background color on the RGB LCD
	/**
	 * @param colorString color for the LCD background
	 */
	public void setLcdColor(String colorString) {
		short[] selectedColorNumbers = null;
		
		switch (colorString){
		case RED: 	
			selectedColorNumbers = redColor;
			break;
		case BLUE: 	
			selectedColorNumbers = blueColor;
			break;
		case GREEN: 	
			selectedColorNumbers = greenColor;
			break;
		case YELLOW: 	
			selectedColorNumbers = yellowColor;
			break;
		case WHITE: 	
			selectedColorNumbers = whiteColor;
			break;
		}
		lcdScreen.setColor(selectedColorNumbers[0], selectedColorNumbers[1], selectedColorNumbers[2]);
	}	
	
	/**
	 * Displays a message on the RGB LCD
	 * @param string- message
	 * @param line- which line on the LCD
	 */
	public void displayMessageOnLcd(String string, int line) {
		// pad string to avoid display issues
		while (string.length() < 16) { string += " "; }
		lcdScreen.setCursor(line, 0);
		lcdScreen.write(string);
	}
	



}

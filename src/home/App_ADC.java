package home;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import home.utils.Adc;

public class App_ADC {

	static Adc adc = new Adc();
	static boolean keepRunning = true;

    public static void main(String[] args) throws InterruptedException, UnsupportedBusNumberException, IOException
    {

		System.out.println("<--Pi4J--> ADS1115 GPIO Example ... started.");
		
		// arg1: event_threshold
		//	Define a threshold value for each pin for analog value change events to be raised.
		//	It is important to set this threshold high enough so that you don't overwhelm your program with change events for insignificant changes
		// arg2: monitor_interval
		//	Define the monitoring thread refresh interval (in milliseconds).
		//	This governs the rate at which the monitoring thread will read input values from the ADC chip
		//	(a value less than 50 ms is not permitted)
		adc.init(100, 100);
		
		adc.setGain_3v();

		// To start callback execution in response to events (changes in value greater than event_threshold set above)
		// Note: would probably have to modify how start() is defined to make it useful
		//adc.start();
		
		// Can also poll pins manually
		while(keepRunning)
		{
			System.out.println("Analog0:" + adc.getAnalog0_voltage());
			System.out.println("Analog1:" + adc.getAnalog1_voltage());
			System.out.println("Analog2:" + adc.getAnalog2_voltage());
			System.out.println("Analog3:" + adc.getAnalog3_voltage());
			System.out.println();
			
			// set to 1000 for this test, but 10-100 is better in practice
			Thread.sleep(1000); // slow the loop rate to keep cpu usage down
		}
		
		adc.kill();
	}


}

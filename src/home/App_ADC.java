package home;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import home.utils.Adc;

public class App_ADC {

	static Adc adc = new Adc();
	static boolean keepRunning = true;

    public static void main(String[] args) throws InterruptedException, UnsupportedBusNumberException, IOException
    {
    	
    	//========== SETUP ANALOG ================
		// arg1: event_threshold
		//	Define a threshold value for each pin for analog value change events to be raised.
		//	It is important to set this threshold high enough so that you don't overwhelm your program with change events for insignificant changes
		// arg2: monitor_interval
		//	Define the monitoring thread refresh interval (in milliseconds).
		//	This governs the rate at which the monitoring thread will read input values from the ADC chip
		//	(a value less than 50 ms is not permitted)
		adc.init(100, 100);
		
		adc.setGain_3_3v();

		// To start callback execution in response to events (changes in value greater than event_threshold set above)
		// Note: would probably have to modify how start() is defined to make it useful
		//adc.start();
		

		//========== SETUP DIGITAL ================
		// Create gpio controller
		final GpioController gpio = GpioFactory.getInstance();
		
		// Provision gpio pin #07 as an output pin and turn on
		final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "MyRelay", PinState.HIGH);
		
		// Set shutdown state for this pin
		pin.setShutdownOptions(true, PinState.LOW);
		
		// Can also poll pins manually
		while(keepRunning)
		{
			double volts = adc.getAnalog0_voltage();
			System.out.println("Analog0:" + volts);

			
			if (volts > 1.4)
			{
				System.out.println("Setting pin to low");
				pin.low();
			}else if (volts < 1.2)
			{
				System.out.println("Setting pin to high");
				pin.high();
			}
			
			System.out.println();
			
			// set to 1000 for this test, but 10-100 is better in practice
			Thread.sleep(1000); // slow the loop rate to keep cpu usage down
		}

		gpio.shutdown();
		adc.kill();
	}

}

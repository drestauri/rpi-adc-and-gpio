package home.utils;

import java.io.IOException;
import java.text.DecimalFormat;

import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

// How to use ADS1115:
//	https://www.best-microcontroller-projects.com/ads1115.html
// Code examples:
//	https://github.com/Pi4J/pi4j/blob/master/pi4j-example/src/main/java/ADS1115GpioExample.java


public class Adc {
	
	// number formatters
    final DecimalFormat df = new DecimalFormat("#.##");
    final DecimalFormat pdf = new DecimalFormat("###.#");

    // create gpio controller
    final GpioController gpio = GpioFactory.getInstance();

    // create custom ADS1115 GPIO provider
    ADS1115GpioProvider gpioProvider;

    // provision gpio analog input pins from ADS1115
    GpioPinAnalogInput myInputs[] = new GpioPinAnalogInput[4];
    
    public Adc(){

    }

    public void init(int event_threshold, int monitor_interval) throws UnsupportedBusNumberException, IOException {
    	gpioProvider = new ADS1115GpioProvider(I2CBus.BUS_1, ADS1115GpioProvider.ADS1115_ADDRESS_0x48);
    	
    	myInputs[0] = gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A0, "MyAnalogInput-A0");
    	myInputs[1] = gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A1, "MyAnalogInput-A1");
    	myInputs[2] = gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A2, "MyAnalogInput-A2");
    	myInputs[3] = gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A3, "MyAnalogInput-A3");
    	
    	// Define a threshold value for each pin for analog value change events to be raised.
		// It is important to set this threshold high enough so that you don't overwhelm your program with change events for insignificant changes
		gpioProvider.setEventThreshold(event_threshold, ADS1115Pin.ALL);
    	
		// Define the monitoring thread refresh interval (in milliseconds).
		// This governs the rate at which the monitoring thread will read input values from the ADC chip
		// (a value less than 50 ms is not permitted)
		gpioProvider.setMonitorInterval(monitor_interval);
    }
    
    public void setGain_3v()
    {
    	// ATTENTION !!
		// It is important to set the PGA (Programmable Gain Amplifier) for all analog input pins.
		// (You can optionally set each input to a different value)
		// You measured input voltage should never exceed this value!
		//
		// PGA value PGA_4_096V is a 1:1 scaled input,
		// so the output values are in direct proportion to the detected voltage on the input pins
		gpioProvider.setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_4_096V, ADS1115Pin.ALL);
    }
    
    public void setGain_5v()
    {
    	// ATTENTION !!
		// It is important to set the PGA (Programmable Gain Amplifier) for all analog input pins.
		// (You can optionally set each input to a different value)
		// You measured input voltage should never exceed this value!
		//
		// PGA value PGA_6_144V is for 5v inputs
		gpioProvider.setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_6_144V, ADS1115Pin.ALL);
    }
    
    public void start()
    {
		// create analog pin value change listener
		GpioPinListenerAnalog listener = new GpioPinListenerAnalog()
		{
			@Override
			public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event)
			{
				// RAW value
				double value = event.getValue();
				
				// percentage
				double percent = ((value * 100) / ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);
				
				// approximate voltage ( *scaled based on PGA setting )
				double voltage = gpioProvider.getProgrammableGainAmplifier(event.getPin()).getVoltage() * (percent/100);
				
				// display output
				System.out.println(" (" + event.getPin().getName() +") : VOLTS=" + df.format(voltage) + " | PERCENT=" + pdf.format(percent) + "% | RAW=" + value + " ");
				    
			}
		};
		
		myInputs[0].addListener(listener);
		myInputs[1].addListener(listener);
		myInputs[2].addListener(listener);
		myInputs[3].addListener(listener);
    }
    
    public void kill()
    {
		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
		gpio.shutdown();
    }
}

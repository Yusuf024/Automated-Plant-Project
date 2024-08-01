package eecs1021;
import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import java.io.IOException;
public class ButtonListener implements IODeviceEventListener {
    private Pin moisturePin;
    private final Pin buttonPin;
    // Constructor
    ButtonListener(Pin moisturePin, Pin buttonPin) {
        this.moisturePin = moisturePin;
        this.buttonPin = buttonPin;
    }
    // Handle pin change event
    @Override
    public void onPinChange(IOEvent event) {
        if (event.getPin().getIndex() != buttonPin.getIndex()) {
            return;
        }
        // What is the current LED output value?
        long currentValue = this.moisturePin.getValue();
        // Invert the LED.
        int newValue = (currentValue == 0) ? 1 : 0;
        try {
            // Set the new LED value
            this.moisturePin.setValue(newValue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // These methods are not used in this implementation
    @Override
    public void onStart(IOEvent event) {}
    @Override
    public void onStop(IOEvent event) {}
    @Override
    public void onMessageReceive(IOEvent event, String message) {}
}
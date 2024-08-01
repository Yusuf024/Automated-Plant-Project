package eecs1021;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.*;
public class plantProjectTest {
    @Test
    public void pinvalueTest() throws IOException {
        String port = "COM3";
        IODevice arduino = new FirmataDevice(port);
        try {
            // Start the Arduino device
            arduino.start();
            // Wait for the Arduino device to initialize
            arduino.ensureInitializationIsDone();
            // Write to pin 2
            plantProject.writePin(arduino,2,1);
            // Get the pin object corresponding to pin 2
            Pin pumpPin = arduino.getPin(2);
            // pin object is not null
            assertNotNull(pumpPin);
            // pin mode is set to OUTPUT
            assertEquals(Pin.Mode.OUTPUT, pumpPin.getMode());
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        } finally {
            // Stop the Grove board
            if (arduino != null) {
                arduino.stop();
            }
        }
    }
}
package eecs1021;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import java.io.IOException;
import org.firmata4j.I2CDevice;
import org.firmata4j.ssd1306.SSD1306;
import org.firmata4j.firmata.FirmataDevice;
import edu.princeton.cs.introcs.StdDraw;
import java.util.ArrayList;
public class plantProject {
    static final int maxGraphVals = 15000;
    static final int ButtonPin = 6;
    static final double dryMark = 3.4;
    static final double moistMark = 3.2;

    public static void main(String[] args) throws IOException, InterruptedException {
        String comPort = "COM3";
        IODevice ArduinoObject = new FirmataDevice(comPort);
        ArduinoObject.start();
        System.out.println("Starting...");
        ArduinoObject.ensureInitializationIsDone();
        // Use 0x3C for the Grove OLED
        I2CDevice i2cObject = ArduinoObject.getI2CDevice((byte) 0x3C);
        SSD1306 oledObject = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
        oledObject.init();
        Pin buttonPin = ArduinoObject.getPin(ButtonPin);
        Pin moisturePin = ArduinoObject.getPin(2);
        ArduinoObject.addEventListener(new ButtonListener(moisturePin, buttonPin));
        buttonPin.setMode(Pin.Mode.INPUT);
        StdDraw.setXscale(-10, 100);
        StdDraw.setYscale(-1, 5);
        StdDraw.setPenRadius(0.0065);
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.line(0,0,0,5);
        StdDraw.line(0,0,100,0);
        StdDraw.text(45,-0.5,"Time(s)");
        StdDraw.text(-8,2.5,"Moist(V)");
        StdDraw.text(50,5,"Moisture VS Time");
        ArrayList<Double> timeData = new ArrayList<>(maxGraphVals);
        ArrayList<Double> moistureData = new ArrayList<>(maxGraphVals);
        long startTime = System.currentTimeMillis();
        while (true) {
            double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            double moisture = readVoltage(ArduinoObject, 15);
            timeData.add(elapsedSeconds);
            moistureData.add(moisture);
            drawPlantStatus(moisture, oledObject);
            StdDraw.point(elapsedSeconds, moisture);
            StdDraw.show();
            int buttonState = readPin(ArduinoObject, ButtonPin);
            if (buttonState == 1) {
                writePin(ArduinoObject, 2, 1);
                Thread.sleep(3000);
            }
            if (moisture > dryMark) {
                writePin(ArduinoObject, 2, 1);
                Thread.sleep(300);
                writePin(ArduinoObject, 2, 0);
                System.out.println("Signal Received!");
                System.out.println("Dry soil!");
                System.out.println(moisture);
            } else if (moisture > moistMark) {
                writePin(ArduinoObject, 2, 1);
                Thread.sleep(300);
                writePin(ArduinoObject, 2, 0);
                System.out.println("Signal Received!");
                System.out.println("Wet Soil!");
                System.out.println(moisture);
            } else {
                writePin(ArduinoObject, 2, 0);
                System.out.println("Signal Received!");
                System.out.println("Pumping Terminated");
                System.out.println(moisture);
            }
            if (elapsedSeconds > maxGraphVals) {
                break;
            }
            Thread.sleep(50);
        }
    }
    static double readVoltage(IODevice ArduinoObject, int pin) {
        try {
            int pinNumber = Integer.parseInt(String.valueOf(pin));
            Pin analogPin = ArduinoObject.getPin(pinNumber);
            analogPin.setMode(Pin.Mode.ANALOG);
            double voltage = analogPin.getValue() * 5.0 / 1023.0;
            return voltage;
        } catch (Exception e) {
            System.err.println("Error from pin: " + pin + ": " + e.getMessage());
            return 0;
        }
    }
    static int readPin(IODevice ArduinoObject,int pin) {
        try {
            Pin digitPin = ArduinoObject.getPin(pin);
            digitPin.setMode(Pin.Mode.INPUT);
            return Math.toIntExact(digitPin.getValue());
        } catch (Exception e) {
            System.err.println("Error reading digital pin " + pin + ": " + e.getMessage());
            return 0;
        }
    }
    static void writePin(IODevice ArduinoObject, int pin, int value) {
        try {
            Pin digitPin = ArduinoObject.getPin(pin);
            digitPin.setMode(Pin.Mode.OUTPUT);
            digitPin.setValue(value);
        } catch (Exception e) {
            System.err.println("Error writing digital pin: " + pin + ": " + e.getMessage());
        }
    }
    private static void drawPlantStatus(double moistureReadings,SSD1306 oledObject) {
        final double voltageFactor = 5.0 / 1023.0;
        final double offSet = 0.2;
        oledObject.getCanvas().clear();
        double voltageReadings = (moistureReadings * voltageFactor) + offSet;
        int level = (int) (100 / 1.50 * voltageReadings);
        if (level <= 0) {
            oledObject.getCanvas().drawString(0,0,"Dry Soil: " + moistureReadings);
        } else if (level >= 100) {
            oledObject.getCanvas().drawString(0,0,"Wet Soil: " + moistureReadings);
        } else {
            oledObject.getCanvas().drawString(0,0,"Voltage: " + moistureReadings);
        }
        oledObject.display();
    }
}
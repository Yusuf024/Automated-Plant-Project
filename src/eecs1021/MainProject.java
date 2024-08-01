package eecs1021;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import java.io.IOException;
import org.firmata4j.I2CDevice;
import org.firmata4j.ssd1306.SSD1306;
import org.firmata4j.firmata.FirmataDevice;
import edu.princeton.cs.introcs.StdDraw;
public class MainProject {
    static final int maxGraphVals = 15000;
    static final int ButtonPin = 6;
    static final double dryMark = 3.4;
    static final double moistMark = 3.2;

    public static void main(String[] args) throws IOException, InterruptedException {
        String comPort = "COM3";
        IODevice arduinoObject = new FirmataDevice(comPort);
        arduinoObject.start();
        System.out.println("Starting...");
        arduinoObject.ensureInitializationIsDone();

        I2CDevice i2cObject = arduinoObject.getI2CDevice((byte) 0x3C);
        SSD1306 oledObject = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
        oledObject.init();

        Pin buttonPin = arduinoObject.getPin(ButtonPin);
        Pin moisturePin = arduinoObject.getPin(2); // Assuming the moisture sensor is connected to pin 7
        buttonPin.setMode(Pin.Mode.INPUT);
        arduinoObject.addEventListener(new ButtonListener(moisturePin, buttonPin));

        StdDraw.setXscale(-10, 100);
        StdDraw.setYscale(-1, 5);
        StdDraw.setPenRadius(0.0065);
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.line(0,0,0,5);
        StdDraw.line(0,0,100,0);
        StdDraw.text(45,-0.5,"Time(s)");
        StdDraw.text(-8,2.5,"Moist(V)");
        StdDraw.text(50,5,"Moisture VS Time");

        double[] timeData = new double[maxGraphVals];
        double[] moistureData = new double[maxGraphVals];
        long startTime = System.currentTimeMillis();

        while (true) {
            double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            double moisture = readVoltage(arduinoObject, 15); // Assuming the moisture sensor is connected to analog pin 15

            int lastIndex = timeData.length - 1;
            timeData[lastIndex] = elapsedSeconds;
            moistureData[lastIndex] = moisture;

            drawPlantStatus(moisture, oledObject);
            StdDraw.point(elapsedSeconds, moisture);
            StdDraw.show();

            int buttonState = readPin(arduinoObject, ButtonPin);
            if (buttonState == 1) {
                writePin(arduinoObject, 2, 1); // Assuming the pump is connected to pin 7
                Thread.sleep(3000); // Assuming 3 seconds pumping time
                writePin(arduinoObject, 2, 0); // Turn off the pump
            }

            if (moisture > dryMark) {
                // Activate the pump
                writePin(arduinoObject, 2, 1); // Assuming the pump is connected to pin 7
                Thread.sleep(200);
                writePin(arduinoObject, 2, 0); // Turn off the pump
                System.out.println("Soil is dry! Pumping Time!");
            } else if (moisture > moistMark) {
                System.out.println("Soil is moist!");
            } else {
                System.out.println("Soil is wet!");
            }

            if (elapsedSeconds > maxGraphVals) {
                break;
            }

            Thread.sleep(50);
        }
    }

    static double readVoltage(IODevice arduinoObject, int pin) {
        try {
            int pinNumber = Integer.parseInt(String.valueOf(pin));
            Pin analogPin = arduinoObject.getPin(pinNumber);
            analogPin.setMode(Pin.Mode.ANALOG);
            double voltage = analogPin.getValue() * 5.0 / 1023.0;
            return voltage;
        } catch (Exception e) {
            System.err.println("Error reading voltage: " + e.getMessage());
            return 0;
        }
    }

    static int readPin(IODevice arduinoObject, int pin) {
        try {
            Pin digitPin = arduinoObject.getPin(pin);
            digitPin.setMode(Pin.Mode.INPUT);
            return Math.toIntExact(digitPin.getValue());
        } catch (Exception e) {
            System.err.println("Error reading pin: " + e.getMessage());
            return 0;
        }
    }

    static void writePin(IODevice arduinoObject, int pin, int value) {
        try {
            Pin digitPin = arduinoObject.getPin(pin);
            digitPin.setMode(Pin.Mode.OUTPUT);
            digitPin.setValue(value);
        } catch (Exception e) {
            System.err.println("Error writing pin: " + e.getMessage());
        }
    }

    private static void drawPlantStatus(double moisture, SSD1306 oledObject) {
        final double voltageFactor = 5.0 / 1023.0;
        final double offSet = 0.4;
        oledObject.getCanvas().clear();
        double voltage = moisture * voltageFactor + offSet;
        int level = (int) (100 / 1.50 * voltage);
        if (level <= 0) {
            oledObject.getCanvas().drawString(0, 0, "Dry Soil: " + moisture);
        } else if (level >= 100) {
            oledObject.getCanvas().drawString(0, 0, "Wet Soil: " + moisture);
        } else {
            oledObject.getCanvas().drawString(0, 0, "Just right!: " + moisture);
        }
        oledObject.display();
    }
}

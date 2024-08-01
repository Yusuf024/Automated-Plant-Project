package eecs1021;
import org.firmata4j.firmata.*; // Import FirmataDevice from the correct package
import org.firmata4j.IODevice;
public class Trial_Run {
    public static void main(String[] args) {
        String myPort = "COM3";
        IODevice myGroveBoard = new FirmataDevice(myPort);
        try {
            myGroveBoard.start(); // start comms with board;
            System.out.println("Board started.");
            myGroveBoard.ensureInitializationIsDone(); // make sure connection is good to board.
            myGroveBoard.stop(); // finish with the board. Shut down the connection.
            System.out.println("Board stopped.");
        }
        catch (Exception ex){
            System.out.println("couldn't connect to board."); // message if the connection didn’t happen.
        }
    }

}

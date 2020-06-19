
/**
 *
 *  Run the program
 *  java AsyncJokeServer 3245
 *  java AsyncJokeServer 3246
 *  java AsyncJokeClient 3245 3246
 *  java AsyncJokeClientAdmin 3245 3246
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
 */

public class AsyncJokeClientAdmin {

    final static String IPADRESS_SERVER = "localhost";
    final static int FIRST_PORT_DEFAULT = 5245;
    final static int SECOND_PORT_DEFAULT = 5246;

    public static void main(String args[]) {

        int port1 = FIRST_PORT_DEFAULT;
        int port2 = SECOND_PORT_DEFAULT;
        String serverAdress = IPADRESS_SERVER;
        int port = port1;
        boolean isSecondServer = false;

        if (args.length > 0) {
            port1 = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            isSecondServer = true;
            port2 = Integer.parseInt(args[1]);
        }

        System.out.println("Asynchronous Joke ClientAdmin started up.");
        System.out.println("Frist sever start at port: " + port1);
        if (isSecondServer) {
            System.out.println("Second server start at port: " + port2);
        }
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));

        try {
            String inputLine = "";
            boolean isFirstServer = true;

            while (true) {
                System.out.println("Press <Enter> to toggle server mode.");
                System.out.println("Input 's' to change server, 'shutdown' for shutdown server, 'quit' to exit:");
                System.out.flush();

                inputLine = inputStream.readLine();
                if (inputLine == null)
                    continue;
                // Quit command
                if (inputLine.indexOf("quit") >= 0) {
                    System.out.println("Bye!!!!.");
                    break;
                }
                // Switch server
                if (inputLine.equals("s")) {
                    if (!isSecondServer)
                        System.out.println("No secondary server being used.");
                    else {
                        if(isFirstServer)
                            port = port2;
                        else
                            port = port1;
                        isFirstServer = !isFirstServer;
                        System.out.println("Now conntect to Server " + (isFirstServer ? "First" : "Second"));
                    }
                    continue;
                }
                // shutdown
                if (inputLine.equals("") || inputLine.equals("shutdown")) {
                    // Send shutdown signal to server
                    getResponseFromServer(serverAdress, port, inputLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send command and get resonse from server
     * 
     * @param server
     * @param port
     * @param input
     */
    static void getResponseFromServer(String server, int port, String input) {

        PrintStream outputStream; // sent to Server
        BufferedReader inputStream; // receive from server

        Socket socket;
        String inputLine;

        try {
            socket = new Socket(server, port); // Connecto to server
            outputStream = new PrintStream(socket.getOutputStream());
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream.println(input);
            outputStream.flush();

            // response from server
            inputLine = inputStream.readLine();
            if (inputLine != null) {
                String serverStr = (port == FIRST_PORT_DEFAULT) ? "Server First" : "Server Second";
                System.out.println(serverStr + inputLine.substring(6));
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

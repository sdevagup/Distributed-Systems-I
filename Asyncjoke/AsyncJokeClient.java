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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class AsyncJokeClient {
    
    final static String IPADRESS_SERVER = "localhost";
    final static int FIRST_PORT = 3245;
    final static int SECOND_PORT = 3246;
    public static int[] clientInfo = { 0, 0 };
    public static Queue<String> jokesQueue = new LinkedList<String>();

    public static void main(String args[]) {

        String server = IPADRESS_SERVER;
        int firstPort = FIRST_PORT;
        int secondPort = SECOND_PORT;
        boolean haveSecondSever = false;

        // Parsing agurment
        if (args.length > 0) {
            firstPort = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            secondPort = Integer.parseInt(args[1]);
            haveSecondSever = true;
        }

        // UDP socket
        DatagramSocket sockUDP1 = null, sockUDP2 = null;
        try {
            // avoid conflict port with system
            sockUDP1 = new DatagramSocket(firstPort + 1000);
            System.out.println("Asynchronous Joke Client started with bindings:");
            System.out.println("Start first server at " + firstPort);
            if (haveSecondSever) {
                sockUDP2 = new DatagramSocket(secondPort + 1000);
                System.out.println("Start second server at " + secondPort);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));
        try {
            String inputLine = "";
            boolean isFirstServer = true;
            while (true) {
                int info;
                if (!jokesQueue.isEmpty()) {
                    System.out.println("\n" + jokesQueue.poll());
                }
                // Prompt for user input
                System.out.print(
                        "\nEnter 1 or 2 to get a joke or proverb from first or second server, or two numbers for sum, quit to exit: ");
                System.out.flush();
                inputLine = inputStream.readLine(); // response from server
                if (inputLine.indexOf("quit") >= 0) {
                    System.out.println("Bye.");
                    break;
                } else if (inputLine.equals("2")) {
                    // Check secondary choice or not
                    if (!haveSecondSever)
                        System.out.println("Second server was not start!!!!");
                    else {
                        isFirstServer = false;
                        info = clientInfo[1];
                        getResponseMessage(server, secondPort, sockUDP2, info, isFirstServer);
                    }
                } else if (inputLine.equals("1")) {
                    isFirstServer = true;
                    info = clientInfo[0];
                    getResponseMessage(server, firstPort, sockUDP1, info, isFirstServer);
                } else {
                    String[] numList = inputLine.split("\\s+");
                    if (numList.length == 2) {
                        int first_number = Integer.parseInt(numList[0]);
                        int second_number = Integer.parseInt(numList[1]);
                        System.out.println("SUM is: " + (first_number + second_number));
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void getResponseMessage(String serverAddress, int serverPort, DatagramSocket sockUDP, int info,
            boolean isFirstServer) {

        PrintStream outputStream = null;
        Socket sockServer;

        try {
            sockServer = new Socket(serverAddress, serverPort);
            outputStream = new PrintStream(sockServer.getOutputStream());
            outputStream.println(info);
            outputStream.flush();
            sockServer.close();
            new Thread(new UDPClient(sockUDP, info, isFirstServer)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class UDPClient extends Thread {
    DatagramSocket udp_Socket;
    int info;
    boolean isFirstServer;

    UDPClient(DatagramSocket sock, int info, boolean isFirst) {
        this.udp_Socket = sock;
        this.info = info;
        this.isFirstServer = isFirst;
    }

    public void run() {
        DatagramPacket inputStream;

        try {
            byte[] readInput = new byte[1024];
            inputStream = new DatagramPacket(readInput, readInput.length);
            udp_Socket.receive(inputStream);
            // responses
            String response = new String(readInput, 0, inputStream.getLength());
            // Find pattern split
            String[] responses = response.split("xxxx");
            AsyncJokeClient.jokesQueue.add(responses[0]);
            info = Integer.parseInt(responses[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AsyncJokeClient.clientInfo[isFirstServer ? 0 : 1] = info;
    }

}

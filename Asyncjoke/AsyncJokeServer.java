
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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

 */
public class AsyncJokeServer {
    public static boolean isJokeMode = true;
    public static boolean isRunning = true;

    /**
     * Main function
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        int portClient1 = 3245;
        int portClient2 = 3246;
        int portAdmin1 = 5245;
        int portAdmin2 = 5246;
        boolean isSecondaryServer = false;
        int portClient = portClient1;
        int portAdmin = portAdmin1;

        // Check agrument
        if (args.length > 0) {
            portClient = Integer.parseInt(args[0]);
            portAdmin = portClient + 2000;
            isSecondaryServer = true;
        }
        System.out.println("Asynchronous Joke Server started up.");
        System.out.println("Starting server " + (isSecondaryServer ? "B" : "A"));
        System.out.println("Listening to Client at port " + portClient + ".");
        System.out.println("Listening to ClientAdmin at port " + portAdmin + ".");
        int maxRequestQueueLength = 6;
        Socket socket;
        ServerSocket servSock = new ServerSocket(portClient, maxRequestQueueLength);
        // UDP socket
        DatagramSocket sockUDP = new DatagramSocket();
        AdminServerHandler admin = new AdminServerHandler(portAdmin, servSock);
        Thread adminThread = new Thread(admin); // Thread handle client admin
        adminThread.start();

        // when new client connect. Create new handler
        while (isRunning) {
            try {
                socket = servSock.accept();
                new WorkerThread(socket, sockUDP, isJokeMode, portClient, isSecondaryServer)
                        .start();
            } catch (SocketException e) {
                System.out.println("Server stops listening to Clients.");
            }
        }
    }
}

class AdminServerHandler implements Runnable {

    int port = 5245;
    ServerSocket serverSocket; // Open this for listen from any client

    /**
     * Constructor
     * 
     * @param port
     * @param sock
     */
    AdminServerHandler(int port, ServerSocket sock) {
        this.port = port;
        this.serverSocket = sock;
    }

    public void run() {
        int maxRequestQueueLength = 6;
        Socket socket;

        try {
            @SuppressWarnings("resource")
            ServerSocket servsock = new ServerSocket(port, maxRequestQueueLength);
            while (AsyncJokeServer.isRunning) {
                // New connection come
                socket = servsock.accept();
                new AdminServerWorker(socket, serverSocket).start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

class AdminServerWorker extends Thread {

    Socket socket;
    ServerSocket serverSocket;

    /**
     * Constructor
     * 
     * @param sock
     * @param sockCS
     */
    AdminServerWorker(Socket sock, ServerSocket sockCS) {
        this.socket = sock;
        this.serverSocket = sockCS;
    }

    public void run() {

        BufferedReader inputStream = null; // receive via socket
        PrintStream outputStream = null; // Sent to client

        try {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintStream(socket.getOutputStream());
            String inputLine = "";
            boolean isRunning = false;
            try {
                inputLine = inputStream.readLine(); // read input
                String message = "";
                if (inputLine.equals("")) {
                    if (AsyncJokeServer.isJokeMode) {
                        message = "Server has been switched to Proverb Mode.";
                    } else {
                        message = "Server has been switched to Joke Mode.";
                    }
                    // Switch server type
                    AsyncJokeServer.isJokeMode = !AsyncJokeServer.isJokeMode;
                }
                // Receive shutdown command
                else if (inputLine.equals("shutdown")) {
                    AsyncJokeServer.isRunning = false; // Stop flag running
                    isRunning = true;
                    message = "Disconnect to client!!!";
                }
                System.out.println(message);
                outputStream.println(message);
            } catch (IOException e) {
                System.out.println("Read from Client Admin error.");
                e.printStackTrace();
            }
            socket.close();
            if (isRunning && (serverSocket != null))
                serverSocket.close();
        } catch (IOException e) {
            System.out.println("Socket error.");
            e.printStackTrace();
        }
    }
}

class WorkerThread extends Thread {

    Socket socket;
    DatagramSocket socketUDP; // UDP
    boolean isJokeMode = true;
    int port;
    boolean isSecondaryServer = false;

    WorkerThread(Socket s, DatagramSocket sU, boolean mode, int port, boolean second) {
        socket = s;
        socketUDP = sU;
        this.isJokeMode = mode;
        this.port = port + 1000;
        this.isSecondaryServer = second;
    }

    public void run() {

        BufferedReader inputStream = null; // input from client
        DatagramPacket outputStream = null; // output from client
        String inputLine;
        try {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            inputLine = inputStream.readLine();
            if (AsyncJokeServer.isRunning) {
                System.out.println("Receiving request from client.");
            }
            InetAddress address = socket.getInetAddress();
            // Break TCP/IP connection
            socket.close();
            try {
                // Sleep 70s in secondary server. And 40s if not yet
                sleep(isSecondaryServer ? 70000 : 40000);
            } catch (Exception e) {
            }
            String response = buildResponseMessage(inputLine, isJokeMode, isSecondaryServer);
            byte[] writeOut = response.getBytes();
            outputStream = new DatagramPacket(writeOut, writeOut.length, address, port);
            socketUDP.send(outputStream);
        } catch (IOException e) {
            System.out.println("Socket error.");
            e.printStackTrace();
        }
    }

    static String buildResponseMessage(String info, boolean isJokeMode, boolean secondaryServer) {
        // Jokes and Proverbs
        String[] jokes = {
                "Did you hear about the mathematician who's afraid of negative numbers? He'll stop at nothing to avoid them.",
                "Why do we tell actors to \"break a leg?\" Because every play has a cast.",
                "A bear walks into a bar and says, \"Give me a tea and a cola.\" \"Why the big pause?\" asks the bartender. The bear shrugged. \"I'm not sure; I was born with them.\"",
                "Did you hear about the actor who fell through the floorboards? He was just going through a stage.",
                "Did you hear about the claustrophobic astronaut? tHe just needed a little space." };

        String[] proverbs = { "A smile is an inexpensive way to change your looks.",
                "Failure is the path of least persistence.",
                "The pursuit of happiness is the chase of a life time.",
                "The future is purchased by the present.",
                "One thing you can't recycle is wasted time." };
        int signalRotate = 0;
        byte byteSize = (byte) Integer.parseInt(info);
        int index;
        char state;
        while (true) {
            index = (int) (Math.random() * 4);
            int bitIndex = index;
            if (!isJokeMode)
                bitIndex += 4;
            int bit = (byteSize >> bitIndex) & 0x1;
            if (bit == 1)
                continue; // generate another index
            byteSize = (byte) (byteSize | (1 << bitIndex));
            if (isJokeMode) {
                char cJ = (char) (byteSize & 0x0f);
                if (cJ == 0x0f) {
                    signalRotate = 1;
                    byteSize = (byte) (byteSize & 0xf0);
                }
            } else {
                char cP = (char) (byteSize & 0xf0);
                if (cP == 0xf0) {
                    signalRotate = 2;
                    byteSize = (byte) (byteSize & 0x0f);
                }
            }
            state = (char) (byteSize & 0xff);
            break;
        }

        // Build output message
        StringBuffer outputMessage = new StringBuffer();
        outputMessage.append("Server ");
        outputMessage.append(secondaryServer ? "B" : "A"); // secondary server?
        outputMessage.append(" responds: [");
        outputMessage.append(isJokeMode ? "Joke " : "Proverb "); // Joke or proverb?
        outputMessage.append((char) (index + 65)); // A, B, C, D
        outputMessage.append("] ");
        outputMessage.append(isJokeMode ? jokes[index] : proverbs[index]); // Joke/Proverb
        outputMessage.append("xxxx");
        int infoNew = (int) state;
        outputMessage.append(infoNew);
        System.out.println(
                "Sending " + (isJokeMode ? "J" : "P") + ((char) (index + 65)) + " to client.");
        if (signalRotate == 1) {
            System.out.println("JOKE CYCLE COMPLETED");
        } else if (signalRotate == 2) {
            System.out.println("PROVERB CYCLE COMPLETED");
        }
        return outputMessage.toString();
    }
}
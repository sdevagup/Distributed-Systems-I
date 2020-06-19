import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class McastDHT {
    public static void main(String args[]) {
        System.out.println("\nWelcome to McastDHT \n");
        MenuCreator menu = new MenuCreator();
        Thread menuThread = new Thread(menu);
        menuThread.start();
        try {
            menuThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

class McastThread extends Thread {
    private static DatagramSocket mcastDHTSocket; // UDP connection
    private static DatagramPacket mcastDHTPackage;
    private static byte[] rsize; // size of package
    private static byte[] ssize; // size string sent
    private static InetAddress mcastDHTAddress;
    private static final int MCAST_PORT = 40000;
    private static final int MCAST_ROOT_PORT = 40001;
    private static int run_port;
    private static int predecessor, successor;
    private static Group[] mcastGroup;
    private static int count;
    private static String text;
    private static int id;

    McastThread() {
        id = 1;
        count = 1;
        predecessor = 0;
        successor = 0;
        rsize = new byte[512];
        mcastGroup = new Group[1000];
    }

    static final class Group {
        int gID;
        boolean root, forwarder;

        Group(int g, boolean f, boolean r) {
            gID = g;
            forwarder = f;
            root = r;
        }
    }

    public void run() {
        try {
            mcastDHTAddress = InetAddress.getByName("localhost"); // Use localhost to run
            initializeNode();
            while (true) {
                try {
                    mcastDHTPackage = new DatagramPacket(rsize, rsize.length);
                    mcastDHTSocket.receive(mcastDHTPackage);
                    text = new String(mcastDHTPackage.getData(), 0, mcastDHTPackage.getLength());
                    if (text.contains("SendCommand"))
                        processcommand(text);
                    else
                        processMessage(text, mcastDHTPackage);
                } catch (SocketTimeoutException sto) {
                    continue;
                }
            }
        } catch (IOException io) {
        }
    }

    void processMessage(String message, DatagramPacket sport) {
        String ogrinalMessage = message;
        String loopingMessage, groupMessage;
        int p = sport.getPort();
        groupMessage = message;
        String[] splitMsg = message.split(" ");
        loopingMessage = message.replace(splitMsg[0], "").trim();
        message = message.toUpperCase();
        splitMsg = message.split(" ");
        String tmp;

        switch (splitMsg[0]) {
        case "ADD":
            if (run_port == 40001) {
                ogrinalMessage = (ogrinalMessage + " " + p);
                splitMsg = ogrinalMessage.split(" ");
            }
            if (successor == (Integer.parseInt(splitMsg[1]) + 40000)) {
                try {
                    ssize = ("No, node collision").getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            Integer.parseInt(splitMsg[2]));
                    mcastDHTSocket.send(mcastDHTPackage);
                } catch (Exception e) {
                    System.out.println("Unable to add Node.\n");
                }
                break;
            }
            if (successor < (Integer.parseInt(splitMsg[1]) + 40000)) {
                if (successor == run_port) {
                    p = (Integer.parseInt(splitMsg[1]) + 40000);
                    tmp = ("OK " + ogrinalMessage + " " + successor + " " + p);
                    splitMsg = tmp.split(" ");
                    ssize = tmp.getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            Integer.parseInt(splitMsg[3]));
                    successor = p;
                } else {
                    ssize = ogrinalMessage.getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            successor);
                }
            } else {
                if (id > Integer.parseInt(splitMsg[1])) {
                    p = run_port;
                    tmp = ("OK " + ogrinalMessage + " " + predecessor + " " + p);
                    splitMsg = tmp.split(" ");
                    ssize = tmp.getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            Integer.parseInt(splitMsg[3]));
                    predecessor = (Integer.parseInt(splitMsg[2]) + 40000);
                } else {
                    p = (Integer.parseInt(splitMsg[1]) + 40000);
                    ssize = ogrinalMessage.getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            successor);
                    successor = p;
                }
            }
            try {
                mcastDHTSocket.send(mcastDHTPackage);
            } catch (Exception e) {
                System.out.println("Unable to add Node.\n");
            }
            break;
        case "PING":
            try {
                ssize = ("Reply ping: Hi from Node: " + id).getBytes();
                mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress, p);
                mcastDHTSocket.send(mcastDHTPackage);
                System.out.println("Sending ping reply: \n" + ogrinalMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case "REPLY":
            System.out.println(ogrinalMessage);
            break;
        case "LOOPPING":
            if (successor == run_port) {
                System.out.println("LoopPing Message: " + loopingMessage);
                break;
            }
            try {
                ssize = ogrinalMessage.getBytes();
                mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                        successor);
                mcastDHTSocket.send(mcastDHTPackage);
                System.out.println("LoopPing Message: " + loopingMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case "SURVEY":
            if (successor == run_port) {
                try {
                    ssize = ("Done " + ogrinalMessage + " " + id).getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            Integer.parseInt(splitMsg[1]) + MCAST_PORT);
                    mcastDHTSocket.send(mcastDHTPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            try {
                if (Integer.parseInt(splitMsg[1]) != id)
                    ssize = (ogrinalMessage + " " + id).getBytes();
                else
                    ssize = ogrinalMessage.getBytes();
                mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                        successor);
                mcastDHTSocket.send(mcastDHTPackage);
            } catch (IOException e) {
                System.out.println("Can not collect survey\n");
            }
            break;
        case "DONE":
            System.out.print("Current Node: ");
            for (int i = 2; i < splitMsg.length; i++) {
                System.out.print(splitMsg[i] + " ");
            }
            System.out.println();
            break;
        case "FORWARDER":
            if (mcastGroup[(Integer.parseInt(splitMsg[1]))] == null) {
                mcastGroup[(Integer.parseInt(splitMsg[1]))] = new Group(0, true, false);
                System.out.println("Node: " + id + " is now a forwarder for Mcast Group: "
                        + Integer.parseInt(splitMsg[1]));
            }
            if (successor == run_port)
                break;
            try {
                ssize = ogrinalMessage.getBytes();
                mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                        successor);
                mcastDHTSocket.send(mcastDHTPackage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
        case "MCAST":
            switch (splitMsg[1]) {
            case "CREATE":
                if (Integer.parseInt(splitMsg[2]) <= id) {
                    mcastGroup[Integer.parseInt(splitMsg[2])] = new Group(
                            Integer.parseInt(splitMsg[2]), false, true);
                    System.out.println("MCAST CREAT: " + Integer.parseInt(splitMsg[2]));
                    break;
                } else if (successor == run_port) {
                    System.out.println("Unable to create group");
                    break;
                }
                try {
                    ssize = ogrinalMessage.getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            successor);
                    mcastDHTSocket.send(mcastDHTPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "ADD":
                if (Integer.parseInt(splitMsg[3]) == id) {
                    mcastGroup[Integer.parseInt(splitMsg[2])] = new Group(
                            Integer.parseInt(splitMsg[2]), false, false);
                    System.out.println("MCAST ADD: " + Integer.parseInt(splitMsg[3]) + " Group: "
                            + Integer.parseInt(splitMsg[2]));
                    try {
                        ssize = ("Forwarder " + splitMsg[2]).getBytes();
                        mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                                successor);
                        mcastDHTSocket.send(mcastDHTPackage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                try {
                    ssize = ogrinalMessage.getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            successor);
                    mcastDHTSocket.send(mcastDHTPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "SEND":
                splitMsg = groupMessage.split(" ");
                groupMessage = groupMessage
                        .replace(splitMsg[0] + " " + splitMsg[1] + " " + splitMsg[2], "");
                groupMessage = groupMessage.trim();
                // display
                if (mcastGroup[Integer.parseInt(splitMsg[2])] != null
                        && mcastGroup[Integer.parseInt(splitMsg[2])].gID != 0) {
                    System.out.println("SEND message: \n" + groupMessage + "\n");

                }
                if (successor == run_port)
                    break;
                try {
                    ssize = ogrinalMessage.getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            successor);
                    mcastDHTSocket.send(mcastDHTPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "REMOVE":
                if (Integer.parseInt(splitMsg[3]) == id) {
                    mcastGroup[Integer.parseInt(splitMsg[2])].gID = 0;
                    mcastGroup[Integer.parseInt(splitMsg[2])].forwarder = true;
                    mcastGroup[Integer.parseInt(splitMsg[2])].root = false;
                    System.out.println("Node: " + Integer.parseInt(splitMsg[3])
                            + " forwarder to Mcast Group: " + Integer.parseInt(splitMsg[2]));
                    break;
                }
                if (successor == run_port)
                    break;
                try {
                    ssize = ogrinalMessage.getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            successor);
                    mcastDHTSocket.send(mcastDHTPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            break;
        }
    }

    // initialize node
    private static void initializeNode() {
        String textFromServer;
        String[] split;
        // Init root node
        try {
            mcastDHTSocket = new DatagramSocket(id + MCAST_PORT, mcastDHTAddress);
            mcastDHTSocket.setSoTimeout(180);
            run_port = id + MCAST_PORT;
            predecessor = run_port;
            successor = run_port;
            System.out.println("Node root: " + id + " starting up at port " + run_port + "\n");
        } catch (SocketException se) {
            System.out.println("ERROR when create ROOT NODE.\n");
            se.printStackTrace();
            id = getRandomNewID();
        } // Root exist. Create new ID
          // in case create new node
        if (id != 1) {
            do {
                try {
                    mcastDHTSocket = new DatagramSocket();
                    mcastDHTSocket.setSoTimeout(180);
                    ssize = ("Add " + id).getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            MCAST_ROOT_PORT);
                    mcastDHTSocket.send(mcastDHTPackage);
                    mcastDHTPackage = new DatagramPacket(rsize, rsize.length);
                    mcastDHTSocket.receive(mcastDHTPackage);
                    count++;
                    textFromServer = new String(mcastDHTPackage.getData(), 0,
                            mcastDHTPackage.getLength());
                    split = textFromServer.split(" ");
                    if (split[0].equals("OK")) {
                        mcastDHTSocket.close();
                        mcastDHTSocket = new DatagramSocket(id + MCAST_PORT, mcastDHTAddress);
                        run_port = id + MCAST_PORT;
                        predecessor = Integer.parseInt(split[4]);
                        successor = Integer.parseInt(split[5]);
                        System.out
                                .println("Node: " + id + " start at port " + run_port + "\n");
                        mcastDHTSocket.setSoTimeout(180);
                        break;
                    } else {
                        mcastDHTSocket.close();
                        System.out.println("NodeID error, get new ID. \n");
                        id = getRandomNewID();
                    }
                } catch (SocketTimeoutException sto) {
                    sto.printStackTrace();
                    mcastDHTSocket.close();
                    id = getRandomNewID();
                } catch (Exception e) {
                    System.out.println("Socket error.");
                    mcastDHTSocket.close();
                    id = getRandomNewID();
                }
            } while (count < 1000);
        }
        if (count >= 1000)
            System.exit(1);

    }



    public void command(String commmand) {
        try {
            ssize = ("SendCommand " + commmand).getBytes();
            mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress, run_port);
            mcastDHTSocket.send(mcastDHTPackage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    private static int getRandomNewID() {
        Random random = new Random();
        return random.nextInt((999 - 2) + 1) + 2;
    }
    
    public void processcommand(String command) {
        int i = 0;
        String loopingMessage, message;
        command = command.replace("SendCommand ", "");
        message = command;
        String[] split = command.split(" ");
        loopingMessage = command.replace(split[0], "");
        command = command.toUpperCase();
        split = command.split(" ");
        switch (split[0]) {
        case "STATUS":
            StringBuilder builder = new StringBuilder();
            builder.append("NodeID: " + id + "\n");
            i = predecessor - MCAST_PORT;
            builder.append("Predecessor NodeID: " + i + "\n");
            i = successor - MCAST_PORT;
            builder.append("Successor NodeID: " + i + "\n\n");
            for (int t = 1; t < mcastGroup.length; t++) {
                if (mcastGroup[t] != null) {
                    builder.append("Mcast ID: " + mcastGroup[t].gID + "\n");
                    builder.append("Mcast Root: " + mcastGroup[t].root + "\n");
                    builder.append("Forwarder: " + mcastGroup[t].forwarder + " for Mcast group " + t
                            + "\n\n");
                }
            }
            System.out.println(builder.toString());
            break;
        case "PING":
            try {
                ssize = ("Ping by Node: " + id).getBytes();
                mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                        Integer.parseInt(split[1]));
                mcastDHTSocket.send(mcastDHTPackage);
                System.out.println("Pinging port: " + split[1]); // Display pinging to console
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case "LOOPPING":
            try {
                ssize = (split[0] + " " + loopingMessage).getBytes();
                mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                        MCAST_ROOT_PORT);
                mcastDHTSocket.send(mcastDHTPackage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case "SURVEY":
            try {
                ssize = (split[0] + " " + id).getBytes();
                mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                        MCAST_ROOT_PORT);
                mcastDHTSocket.send(mcastDHTPackage);

            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case "FILE":
            String commandLine;
            BufferedReader buffer;
            try {
                buffer = new BufferedReader(new FileReader(split[1].trim()));
                commandLine = buffer.readLine();
                while (commandLine != null) {
                    commandLine.trim();
                    if (!commandLine.equals(" ")) {
                        command(commandLine);
                    }
                    commandLine = buffer.readLine();
                }
                buffer.close();
            } catch (Exception t) {
                t.printStackTrace();
            }
            break;
        case "MCAST":
            switch (split[1]) {
            case "CREATE":
                try {
                    ssize = (split[0] + " " + split[1] + " " + split[2]).getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            MCAST_ROOT_PORT);
                    mcastDHTSocket.send(mcastDHTPackage);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "SEND":
                split = message.split(" ");
                message = message.replace(split[0] + " " + split[1] + " " + split[2], "");
                message = message.trim();
                try {
                    ssize = (split[0] + " " + split[1] + " " + split[2] + " " + message).getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            MCAST_ROOT_PORT);
                    mcastDHTSocket.send(mcastDHTPackage);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "ADD":
            case "REMOVE":
                try {
                    ssize = (split[0] + " " + split[1] + " " + split[2] + " " + split[3])
                            .getBytes();
                    mcastDHTPackage = new DatagramPacket(ssize, ssize.length, mcastDHTAddress,
                            MCAST_ROOT_PORT);
                    mcastDHTSocket.send(mcastDHTPackage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            break;
        }
    }
}

class MenuCreator implements Runnable {
    static boolean runningCommandChoice;

    static class Menu {
        final String option;
        final Action action;

        Menu(String o, Action r) {
            option = o;
            action = r;
        }
    }

    interface Action {
        public void run(McastThread status, BufferedReader in);
    }

    public void run() {
        final Menu[] menu;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        McastThread status = new McastThread();
        String cmd;
        Thread statusThread = new Thread(status);
        statusThread.start();
        try {
            // Try to sleep for print
            TimeUnit.MILLISECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        menu = new Menu[12];
        createMenu(menu);
        runningCommandChoice = true;
        int choice = 0;
        do {
            try {
                System.out
                        .println("\n1. Enter commandline \n" + "2. Command list \n" + "3. Quit\n");
                choice = Integer.parseInt(in.readLine());
                System.out.println();
                switch (choice) {
                case 1:
                    do {
                        System.out.print("Enter a command or quit: ");
                        cmd = in.readLine();
                        System.out.println("\n");
                        if (cmd.indexOf("quit") < 0) {
                            status.command(cmd);
                            try {
                                TimeUnit.MILLISECONDS.sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } while (cmd.indexOf("quit") < 0);
                    break;
                case 2:
                    while (runningCommandChoice) {
                        System.out.println("\nSelect an option \n");
                        for (int c = 1; c < menu.length; c++) {
                            System.out.println(menu[c].option);
                        }
                        System.out.println();

                        choice = Integer.parseInt(in.readLine());
                        System.out.println();

                        if (choice > 0 && choice < 12) {
                            menu[choice].action.run(status, in);
                            try {
                                TimeUnit.MILLISECONDS.sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("\n");
                    }
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Invalid input.");
                    break;
                }
            } catch (NumberFormatException | IOException e) {
                System.out.println("Invalid input. Type in a number ");
            }
            runningCommandChoice = true;
        } while (choice != 3);
        System.out.println("Node shutdown.\n");
    }

    /**
     * Create support menu
     * 
     * @param menu
     */
    static void createMenu(Menu[] menu) {
        menu[0] = new Menu(" 0. Invalid ", new Action() {
            public void run(McastThread status, BufferedReader in) {
                System.out.println("Invalid command.");
            }
        });
        menu[1] = new Menu(
                "1. Status % Display NodeID, predecessor NodeID, successor NodeID, Forwarder/Member Status for any Mcast group, and [McastID for any Mcast Root nodes managed by this node]",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {
                        status.command("status");
                    }
                });
        menu[2] = new Menu(
                "2. Ping [ComPort] % Send a ping to this ComPort, get a response, both nodes display ping information of sender on their console.",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {
                        String comPort;
                        System.out.print("Enter a ComPort: ");
                        try {
                            comPort = ("Ping " + in.readLine());
                            status.command(comPort);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        menu[3] = new Menu(
                "3. LoopPing [Msg] % Forward ping around DHT, all consoles display LoopPing Msg",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {
                        String message;
                        System.out.print("Enter a message: ");
                        try {
                            message = ("LoopPing " + in.readLine());
                            status.command(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        menu[4] = new Menu(
                "4. Survey % Display a list of all DHT nodes, in order, starting and ending with the current node",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {
                        status.command("survey");
                    }
                });
        menu[5] = new Menu("5. File [FileName] % Read this ascii file full of commands.",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {
                        String cmd;

                        System.out.print("Enter filename: ");
                        try {
                            cmd = ("File " + in.readLine());
                            status.command(cmd);
                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            System.out.println("Can not read file");
                        }

                    }
                });
        menu[6] = new Menu("6. Mcast Create [McastID] % Create Mcast group with this ID.",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {

                        String create;
                        System.out.print("Enter <McastID>: ");
                        try {
                            create = ("Mcast create " + in.readLine());
                            status.command(create);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
        menu[7] = new Menu(
                "7. Mcast Add [McastID] [NodeID] % Add this DHT node to Mcast group, possibly by changing from forwarder status",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {

                        String mID;
                        System.out.print("Enter <McastID> <NodeID>: ");
                        try {
                            mID = ("Mcast add " + in.readLine());
                            status.command(mID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
        menu[8] = new Menu("8. Mcast Send [McastID] [Msg] % Send Message to Mcast group",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {

                        String mID;
                        System.out.print("Enter <McastID> <Message>: ");
                        try {
                            mID = ("Mcast send " + in.readLine());
                            status.command(mID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
        menu[9] = new Menu(
                "9. Mcast Remove [McastID] [NodeID] % Remove DHT node frm Mcast group by changing to forwarder status",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {
                        String mID;
                        System.out.print("Enter <McastID> <NodeID>: ");
                        try {
                            mID = ("Mcast remove " + in.readLine());
                            status.command(mID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
        menu[10] = new Menu(
                "10. Mcast Destroy [McastID] % HARD! Bragging rights. Remove all traces of the Mcast group. Non-trivial.",
                new Action() {
                    public void run(McastThread status, BufferedReader in) {
                        System.out.println("I'm not implement this.\n ");
                    }
                });
        menu[11] = new Menu("11. Exit command options", new Action() {
            public void run(McastThread status, BufferedReader in) {
                runningCommandChoice = false;
            }
        });
    }
}

/** File is: JokeClient.java, Version 1.8
 *  A client for JokeServer. 

	Name :
	Date:
	Java version: jdk1.8.0_211
	command-line compilation instructions:
	e.g.:
		> javac JokeClient.java
	Instructions to run this program:
		> java JokeClient [<IPADDR>] [<IPADDR>]
		> java JokeClient localhost
		> java JokeClient localhost localhost
----------------------------------------------------------------------*/
import java.io.*; 				// Get the Input Output libraries
import java.net.*; 				// Get the Java networking libraries
import java.util.Scanner;		// Libarary to read input from console

/**
 * This is the main class which will be used for the starting the JokeClient
 * @author 
 *
 */
public class JokeClient {

	/** Create a new file output stream. **/
	static PrintStream fileOut;
    

    static void print(String data)
	{
		fileOut.print(data);
		System.out.print(data);
	}
    static void println(String data)
	{
		fileOut.println(data);
		System.out.println(data);
	}

	public static void init() {
		/** initialize the file out stream */
		try {
			if(fileOut == null)
				fileOut = new PrintStream("./JokeLog.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void println(IOException ioe) {
		fileOut.println(ioe.toString());
		System.out.println(ioe);
	}
	/**
	 * This is the main method and entry point for the JVM(Java virtual machine). 
	 * So whenever we run this program, this main method will be called first by the JVM.
	 * 
	 * @param args : String array of command line argument
	 * @throws FileNotFoundException 
	 */

	public static void main(String args[]) throws FileNotFoundException {

		/** Declare String object which will store the name of the server*/
		String serverName = null;
		
		/** Declare String object to store the default/primary and secondary
		 * JokeServer's address */
		String primaryServerName = null;
		String secondaryServerName = null;

		/** Default and Primary JokeServer's port */
		int primaryPort = 4545;

		/** Secondary JokeServer's port*/
		int secondaryPort = 4546;
		
		/** boolean variable to check whether to connect the  Secondary JokeServer
		 * or not. */
		boolean trySecondaryServer = false;

		/** Set port to default JokeServer Port*/
		int port = primaryPort;

		/** Set connected JokeServer. default is primary JokeServer */
		int connectedServer = 1;
		
		/** Declare String object to store the client name*/
		String clientName;
		
		init();
		
		/**Check if command line arguments are not provided*/ 
		if (args.length < 1)
		{
			/**assign the server name as 'localhost', that means it will run in local machine(Computer)*/
			serverName = "localhost";
		
			/** Set default address and port to JokeClient to connect.*/
			primaryServerName = serverName;
			port = primaryPort;
		}
			
		/**If command line argument is provided then take it from command line argument*/
		else
		{
			if (args.length == 1)
				primaryServerName = args[0];
			else if (args.length > 1)
			{
				/** Initialize the primary Server's address from first args.*/
				primaryServerName = args[0];

				/** Initialize the secondary Server's address from second args.*/
				secondaryServerName = args[1];
				
				/** Allow JokeClient to toggle from primary Server to Secondary server.*/
				trySecondaryServer = true;
				
				println("Server one: "+primaryServerName+", port "+primaryPort);
				println("Server two: "+secondaryServerName+", port "+secondaryPort);
			}
		}

		/** Set default address and port to JokeClient to connect.*/
		serverName = primaryServerName;
		port = primaryPort;
		
		
		
		/** Get JokeClient name from console.*/
		clientName = getClientName();
		
		/** If JokeClient name is not present then exit.*/
		if(clientName == null || clientName.length() == 0) {
			System.err.println("Invalid User name.\nExiting...");
			return;
		}
		/** Print information about the client*/
		println(clientName+"'s Joke Client, 1.0.\n");
		
		/**Print the server name and port number*/
		println("Using server: " + serverName + ", Port: "+port);
		
		/**
		 * Create the instance of the BufferedReader class to read the data from the input stream.
		 * 
		 * Buffered reader can read the data from any stream like command prompt or from some Socket.
		 * InputStreamReader will read the user input from the command line input stream.
		 */
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		/**
		 * Put the code in try block, so if some exception is generated by any of the code of within the try block then
		 * it can be catch and actual reason for exception can be traced.
		 */
		try {
			/**Declare the string object */
			String request;
			
			/** Loop for client to connect with server and get Jokes/ Proverb
			 * from Server. User can toggle in between JokeServers also.
			 * This loop will break only if user enters the "quit" string from console.
			 * To get next Joke/Proverb, user has to press "enter" button from
			 * console. 
			 * type "s" for toggle the JokeServer.*/
			while(true) 
			{
				/** If trySecondaryServer is true then give option to user
				 * to toggle from primary to secondary JokeServer. */
				if(trySecondaryServer)
					print("Enter \"s\" to switch to secondar joke server,(enter) to query server, (quit) to end: ");
				else
					print("Enter (enter) to query server, (quit) to end: ");
				
				/**flush the input steam before taking input from user*/
				System.out.flush();
				
				/**Read user input from the console*/
				request = in.readLine();
				
				/** If user types "quit" then exit the loop and exit the Jokeclient.*/
				if(request.indexOf("quit") >= 0)
					break;
				
				/**check user input for toggle of JokeServer.*/
				if (request.equals("s") && trySecondaryServer)
				{
					/** if Jokeclient was connected to primary JokeServer than
					 * connect with Secondary JokeServer. */
					if(connectedServer == 1)
					{
						serverName = secondaryServerName;
						port = secondaryPort;
						connectedServer = 2;
					}
					/** if Jokeclient was connected to Secondary JokeServer than
					 * connect with primary JokeServer. */
					else if(connectedServer == 2)
					{
						serverName = primaryServerName;
						port = primaryPort;
						connectedServer = 1;
					}
				}
				/**Call the method to get the connect the JokeServer*/
				connectWithJokeServer(clientName, serverName, port, connectedServer);
				
			}
			/**Print message that user cancelled the session*/
			println("Cancelled by user request.");
		} catch (IOException x) {
			/**If any exception is thrown by the any part of the code in the try block, then print the stack trace to get the reason for exception*/
			x.printStackTrace();
		}
	}

	/**
	 * This method is used to read the client's name from console.
	 * @return : String object
	 * */
	static public String getClientName() {
		
		/** String object to stroe the client's name*/
		String name;
		
		/** Scanner object to read from System.in */
		Scanner in = new Scanner(System.in);
		
		/** prompt for user input*/
		print("Enter user name: ");
		
		/** Read from console */
		name = in.nextLine();
		
		/** Return name string*/
		return name;
	}

	/**
	 * This method used to communicate with the JokeServer server 
	 * and sending the data and receiving the response from the JokeServer. 
	 * @param name : Name of the JokeClient who is making request 
	 * @param serverName : JokeServer's address for making connection
	 * @param port : JokeServer's port for making connection
	 * @param connectedServer : it indicate that Jokeclient is connected with Primary or
	 * Secondary JokeServer.
	 * @return : void
	 */
	static void connectWithJokeServer(String name, String serverName, int port, int connectedServer) {
		
		/**Create the instance of Socket class so that connection can be setup with the TCP server*/
		Socket sock;
		
		/**Create the instance of the read the any response from the TCP server
		 * When TCP server send any data to the TCP client then that data can be stored in BufferedReader and read by this client*/
		BufferedReader fromServer;
		
		/**Create instance of the PrintStream class to send data to TCP server,
		 *If client want to send some data the PrintStream can be used to send that data*/
		PrintStream toServer;
		
		/**Declare string object to hold the response from server*/
		String textFromServer;
		
		/**Put code in try block to handle any run time exception if generated by any code in the try block*/
		try {
			
			/**
			 * Open our connection to server port, choose your own port number..
			 */
			sock = new Socket(serverName, port);
			
			println("Now communicating with: "+ serverName+ ", port "+port);
			
			/** Initialize the BufferedReader instance with the input stream of the Socket, 
			 * So any data coming from the server can be stored in the this stream */
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			
			/** Create the initialize the printStream class to send the data to server*/ 
			toServer = new PrintStream(sock.getOutputStream());
			
			/** Send machine name or IP address to server*/
			toServer.println(name);
			
			/** flush the toStream object */
			toServer.flush();
			
			/** Read two or three lines of response from the server,
			and block while synchronously waiting */
			for (int i = 1; i <= 3; i++) {
				
				/**Read one line from the response which comes from server*/
				textFromServer = fromServer.readLine();
				
				/**Check if response from server received*/
				if (textFromServer != null)
				{	
					if(connectedServer == 2)
					{
						/** Prepend <S2> if response is coming from Secondary JokeServer. */
						print("<S2> ");
					}
					/**Print the response received from server*/
					println(textFromServer);
				}
			}
			
			/**Close the TCP socket*/
			sock.close();
		} 
		catch (IOException x) {
		
			/**If any exception is thrown then print the socket error message*/
			println("Socket connect error. Try again/ Check server is running or not");
//			
//			/**Print the stack trace for detail exception analysis*/
//			x.printStackTrace();
		}
	}

}

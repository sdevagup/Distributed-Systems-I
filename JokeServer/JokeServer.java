/** File is: JokeServer.java, Version 1.8 (small)
	A multithreaded JokeServer for JokeClient. 
	Name :
	Date:
	Java version: jdk1.8.0_211
	command-line compilation instructions:
	e.g.:
		> javac JokeServer.java
	Instructions to run this program:
		> java JokeServer
		or
		> java JokeServer  secondary  
----------------------------------------------------------------------*/
// For Serializing the client data
import java.io.*; 			// Get the Input Output libraries
import java.net.*; 			// Get the Java networking libraries
import java.util.*;			// Get the Java Utility libraries
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;  // Get the random int for Jokes and Proverbs
/**
 * Creating ClientState class which will store the JokeClient's state in memory
 * @ToDo: serialize the client data into file.
 */
class ClientState implements Serializable
{
	private static final long serialVersionUID = 1L;
			
	/** Class members, local to ClientState.*/
	/** JokeClient's name */
	String name;
	
	/** client's joke index counter which we will be reset after 4 counts. */
	int jokeIndex;

	/** client's proverb index counter which we will be reset after 4 counts. */
	int proverbIndex;
	
	/**
	 * This is the constructor of the ClientState class which used to initialize the ClientState class instance
	 * @param name2 : String variable having client's name.
	 * @param jIndex: int variable having Joke index initial value
	 * @param pIndex: int variable having proverb index initial value
	 */
	public ClientState(String name2, int jIndex, int pIndex) {
		name = name2;
		jokeIndex= jIndex;
		proverbIndex = pIndex;
	}
	/** getter & setter for class member variables*/
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getJokeIndex() {
		return jokeIndex;
	}
	public void setJokeIndex(int jokeIndex) {
		this.jokeIndex = jokeIndex;
	}
	public int getProverbIndex() {
		return proverbIndex;
	}
	public void setProverbIndex(int proverbIndex) {
		this.proverbIndex = proverbIndex;
	}
	
	/* This method will increment proverb index and will reset it's value if it is greater than 
	 * MAX_PROVERB_COUNT (4) */
	public void incrementProverbIndex() {
		
		proverbIndex++;
		proverbIndex = proverbIndex % ProcessRequest.MAX_PROVERB_COUNT;
	}

	/* This method will increment joke index and will reset it's value if it is greater than 
	 * MAX_JOKES_COUNT (4) */
	public void incrementJokeIndex() {
		
		jokeIndex++;
		jokeIndex = jokeIndex % ProcessRequest.MAX_JOKES_COUNT;
	}	
	
	@Override
	public String toString() {
		return "Name:" + name + "\njokeIndex: " + jokeIndex + "\nproverbIndex: " + proverbIndex;
	}
}

class Log
{
	/** Create a new file output stream. **/
    static PrintStream fileOut;
    
    Log() {
    	init();
    }

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

	public static void println(ClientState clientState) {

		System.out.println(clientState);
		fileOut.println(clientState.toString());
	}
}

/* This class ProcessRequest is a shared class among the threads.
 * This class is responsible for storing the all Jokes and Proverbs.
 * This class is also responsible for sending jokes and proverbs based on JokeServer state.
 * This class also maintains the JokeServer's state.
 * */
class ProcessRequest{
 
	/** MAX Proverb count constant */
	static final int MAX_PROVERB_COUNT = 4;
	
	/** max jokes count constant */
	static final int MAX_JOKES_COUNT = 4;
	
	/** class members to store the jokes and proverb value for all clients */
	static List<String> jokesList = new ArrayList<String>(); 		// contains the Jokes as JA, JB, JC...
	static List<String> proverbList = new ArrayList<String>(); 	// contains the Proverbs as PA, PB, PC...
	
	
	static List<String> jokes = new ArrayList<String>();			// stores the one liner jokes
	static List<String> proverbs = new ArrayList<String>();		// stores the one liner proverbs
	
	/** String object to store the client's state*/
	static String clientDataFile = "./clientData.txt";
	
	/** shared static hashmap.
	 * This stores the map combination of client's name and client's clientstate */
	static Map<String, ClientState> clientMap = new HashMap<String, ClientState>();

	/** shared resoures among thread for JokeServer.
	 * This value indicates that JokeServer is in Joke mode or in Proverb mode.
	 * */
	static int state = 0;
	
	/**
	 * stores the prev value of JokeServer state.
	 * 
	 * */
	static int prev_state = 0;

	/** constructor for ProcessRequest class.
	 * This is going to initialize all the jokes and proverb class members.
	 * @throws FileNotFoundException */
	static void initialize() throws FileNotFoundException
	{
		
		/** Initialize the jokeList. fill it with JA. JB JC...*/
		for(int i = 0; i < MAX_JOKES_COUNT; i++) {
			jokesList.add("J"+(char)('A'+i));
		}
		
		/** Initialize the proverbList. fill it with PA. PB PC...*/
		for(int i = 0; i < MAX_PROVERB_COUNT; i++) {
			proverbList.add("P"+(char)('A'+i));
		}
		
		/** Initialize the jokes with predefined value. */
		jokes.add("Did you hear about the mathematician who's afraid of negative numbers? He'll stop at nothing to avoid them.");
		jokes.add("Why do we tell actors to \"break a leg?\" Because every play has a cast.");
		jokes.add("A bear walks into a bar and says, \"Give me a tea and … cola.\" \"Why the big pause?\" asks the bartender. The bear shrugged. \"I’m not sure; I was born with them.\"");
		jokes.add("Did you hear about the actor who fell through the floorboards? He was just going through a stage.");
		jokes.add("Did you hear about the claustrophobic astronaut? tHe just needed a little space.");

		/** Initialize the proverb with predefined value. */
		proverbs.add("A smile is an inexpensive way to change your looks.");
		proverbs.add("Failure is the path of least persistence.");
		proverbs.add("The pursuit of happiness is the chase of a life time.");
		proverbs.add("The future is purchased by the present.");
		proverbs.add("One thing you can't recycle is wasted time.");
		
		retrieveClientStateFromDisk();
	}
	
	/**
	 * This method will save the JokeServer's all client's state in disk file.
	 * */
	static void saveClientStateOnDisk() 
	{
		try {
			/** Open Fileoutput stream to serialize the class data*/
			FileOutputStream f = new FileOutputStream(new File(clientDataFile));
			ObjectOutputStream o = new ObjectOutputStream(f);
			
			// using for-each loop for iteration over Map.entrySet() 
	        for (Entry<String, ClientState> client : clientMap.entrySet())  
	        {
	        	Log.println("Key = " + client.getKey() + ", Value = " + client.getValue()); 
	        	ClientState clientState = client.getValue();

	        	// Write objects to file
				o.writeObject(clientState);
	        }
	        
	        o.close();
			f.close();

		}catch (FileNotFoundException e) {
			Log.println("File not found");
		} catch (IOException e) {
			Log.println("Error initializing stream");
		} 
	}
	
	/**
	 * This method will restore the JokeServer's all client's state in disk file.
	 * */
	static void retrieveClientStateFromDisk()
	{
		try {
			FileInputStream  fi = new FileInputStream (new File(clientDataFile));
			ObjectInputStream  oi = new ObjectInputStream (fi);

			Log.println("Loading Client's state...");
			
			for (;;)
		    {
				ClientState clientState = (ClientState)oi.readObject();
				if(clientState == null)
					break;
				Log.println(clientState);
	
				/** save clienState in clientMap. */
				clientMap.put(clientState.getName(), clientState);	
		    }
			oi.close();
			fi.close();
		}catch (FileNotFoundException e) {
			Log.println("File not found");
		} catch (IOException e) {
			Log.println("Error initializing stream");
		}catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static void changeJokeServerState() 
	{
		prev_state = state;
		state++;
		state = state % 2;
	}
	
	/**
	 * This is the method to respond the remote client request and
	 * also store the client's name and state 
	 * @param name : name of client provided by the client 
	 * @param out : output stream of the Socket to send the response to the client
	 */
	static void sendReponseToClient(String name, PrintStream out) {
		
		/** Get client's saved state from th clientMAP.
		 *  if it not there then create new ClientState object 
		 *  and save it in clientMap. */
		ClientState clientState = (ClientState)clientMap.get(name);
		if(clientState == null)
		{
			/** Create new ClientState object and initialize it with
			 * client's name and default values. */
			clientState = new ClientState(name, 0, 0);
			
			/** save clienState in clientMap. */
			clientMap.put(name, clientState);	
		}
		
		int randomNum = ThreadLocalRandom.current().nextInt(0, 5);
		
		if(prev_state != state) 
		{
			/** Send Joke Server's new updated mode. */
			out.println("JokeServer's State changed. Current Mode is : "+getStateString());
		}
		
		/** Send Joke message to client if JokeServer is in Joke mode.*/
		if(state == 0)  // Joke State
		{
			/** Get client's current Joke Index*/
			int index = clientState.getJokeIndex();
			
			/** Prepare the Joke Response for client*/
			String response = jokesList.get(index)+ " "+name+" : "+ jokes.get(randomNum);
			
			/** Send Joke message to client on stream. */
			out.println(response);
			
			/** Increment the joke index for the client.*/
			clientState.incrementJokeIndex();
			
			/** Print if Joke Cycle is completed for this client. */
			if(clientState.getJokeIndex() == 0 ) 
			{
				Log.print("Client< "+name +" > :");
				Log.println("JOKE CYCLE COMPLETED");
			}
		}
		/** Send Proverb message to client if JokeServer is in Proverb mode.*/
		else
		{
			/** Get client's current Proverb Index*/
			int index = clientState.getProverbIndex();
			
			/** Prepare the Proverb Response for client*/
			String response = proverbList.get(index)+ " "+name+" : "+ proverbs.get(randomNum);

			/** Send Proverb message to client on stream. */
			out.println(response);
			
			/** Increment the proverb index for the client.*/
			clientState.incrementProverbIndex();
			
			/* Print if Proverb Cycle is completed for this client. */
			if(clientState.getProverbIndex() == 0 ) 
			{
				Log.print("Client< "+name +" > :");
				Log.println("PROVERB CYCLE COMPLETED");
			}
		}
		
		/** Update the clientMap with new status of client's clientState object.*/
		clientMap.put(name, clientState);	
	}

	/* This method prints the JokeServer's current state/mode in 
	 * String format.
	 * */
	public static String getStateString() {

		if(state == 0)
			return "Joke";
		else if(state == 1)
			return "Proverb";
		return "error";
	}
	
	/* This method returns the JokeServer's current state in integer.
	 * */
	public int getState() {
		return state;
	}
}

/**
 * Creating worker class which extends thread so that TCP program can be made Multi-threaded
 */
class Worker extends Thread { // Class definition
	
	/** Class member, socket, local to Worker.*/
	Socket sock; 
	
	/**
	 * This is the constructor of the Worker class which used to initialize the worker class instance
	 * @param s : Instance of the Socket class
	 */
	Worker(Socket s) {
		/**Assign the  s to sock instance variable*/
		sock = s;
	} 

	/**
	 * This is the run method if the Thread class and this will run when thread will be started
	 */
	public void run() {
	
		/**Create the instance of the PrintStream class and initialize with null.
		 * PrintStream can be used to send data to the connected TCP client*/
		PrintStream out = null;
		
		/**Create the instance of the BufferedReader class which will be used to read the request from the TCP client
		 * BufferedReader instance will be used to store the response from the TCP server and read by the program*/
		BufferedReader in = null;
		
		/**Put the code into the try block to catch any runtime exception by the any code of the try block*/
		try {
			
			/**initialize the instance of the BufferedReader class with the input stream of the socket*/
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			/**Create the output stream of the socket to post data to the client*/
			out = new PrintStream(sock.getOutputStream());
			
				/**Put the code in try block to catch the exception*/ 
				try {
					/**Declare the string object*/
					String name;
					
					/**Read data from the input stream of the socket*/
					name = in.readLine();
					
					/**Send responses to client. */
					ProcessRequest.sendReponseToClient(name, out);
				
				} 
				catch (IOException x) 
				{
					/**If IOException is generated then print the error message*/
					Log.println("Server read error");
					
					/**Print the detailed stack trace*/
					x.printStackTrace();
				}
//			}
			/**close the socket to free up the TCP connection, but serve will be still running to accept other requests*/
			sock.close(); 
		} catch (IOException ioe) {
			/**If any exception thrown then print the stack trace*/
			Log.println(ioe);
		}
	}
}

/**
 * Creating JokeServerAdminWorker class which extends thread so that TCP program can be made Multi-threaded
 */
class JokeServerAdminWorker extends Thread { // Class definition
	
	/** Class member, socket, local to Worker.*/
	Socket sock; 
	
	/**
	 * This is the constructor of the Worker class which used to initialize the worker class instance
	 * @param s : Instance of the Socket class
	 */
	JokeServerAdminWorker(Socket s) {
		/**Assign the  s to sock instance variable*/
		sock = s;
	} 

	/**
	 * This is the run method if the Thread class and this will run when thread will be started
	 */
	public void run() {
	
		/**Create the instance of the PrintStream class and initialize with null.
		 * PrintStream can be used to send data to the connected TCP client*/
		PrintStream out = null;
		
		/**Create the instance of the BufferedReader class which will be used to read the request from the TCP client
		 * BufferedReader instance will be used to store the response from the TCP server and read by the program*/
		BufferedReader in = null;
		
		/**Put the code into the try block to catch any runtime exception by the any code of the try block*/
		try {
			
			/**initialize the instance of the BufferedReader class with the input stream of the socket*/
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			/**Create the output stream of the socket to post data to the client*/
			out = new PrintStream(sock.getOutputStream());
			
			/**Put the code in try block to catch the exception*/ 
			try {
				/**Declare the string object*/
				String name;
				
				/**Read data from the input stream of the socket*/
				name = in.readLine();
				
				/**Print the data in the command prompt*/
				String stateString = ProcessRequest.getStateString();
				out.println("JokeServer's current status :" + stateString );
				Log.println("JokeServer's current status :" + stateString );
				ProcessRequest.changeJokeServerState();
				stateString = ProcessRequest.getStateString();
				out.println("JokeServer's status changed to" + stateString );
				Log.println("JokeServer's status changed to" + stateString );
			} catch (IOException x) 
			{
				/**If IOException is generated then print the error message*/
				Log.println("Server read error");
				
				/**Print the detailed stack trace*/
				x.printStackTrace();
			}
			/**close the socket to free up the TCP connection, but serve will be still running to accept other requests*/
			sock.close(); 
		} catch (IOException ioe) {
			/**If any exception thrown then print the stack trace*/
			Log.println(ioe);
		}
	}

}

/**
 * class JokeAdminServer creates Admin Server to handle the
 * JokeClientAdmin's request to toggle the state of JokeServer. 
 * */
class JokeAdminServer implements Runnable {
	
	  /** class member to break the loop. */
	  public static boolean adminControlSwitch = true;
	  
	  /** Joke Admin Server runs on this port*/
	  int port;
	  
	  /**
	   * Class JokeAdminServer's constructor. It initializes the port.
	   * @param : Set Admin Server's port
	   * */
	  JokeAdminServer(int _port)
	  {
		  port = _port;
	  }
	  
	  /**
	   * implementing the Runnable's run() thread method.
	   * */
	  public void run()
	  { 
	    int q_len = 6; /* Number of requests for OpSys to queue */
	    
	    /** JokeAdminClient's socket variable dedicated to this class. */
	    Socket sock;
	    Log.println("JokeAdminServer running at port: "+port);

	    try
	    {
	      /** Create a Server socket.*/
	      ServerSocket servsock = new ServerSocket(port, q_len);
	      
	      /** Infinite loop for  handling */
	      while (adminControlSwitch) 
	      {
			// wait for the next JokeClientAdmin client connection:
			sock = servsock.accept();
			
			/** Launch JokeServerAdminWorker thread to handle the client's request*/
			new JokeServerAdminWorker (sock).start(); 
	      }
	    }catch (IOException ioe) {Log.println(ioe);}
	  }
}

	 
/**
 * This is the main class which will be used for the starting the JokeServer
 * @author 
 *
 */
public class JokeServer {

    static boolean jokeServerRunning = true;
	/**
	 * This is main method and entry point for the JVM
	 * @param args : String array containing the command line argument
	 * @throws IOException : Through IO exception if any thrown by the code of main method
	 */

	public static void main(String[] args) throws IOException {
	
		/** Define the queue length*/
		int q_len = 6; 
		
		/** Define the port number in which JokeServer will run*/
		int port;
		/** Default and Primary JokeServer's port */
		int primaryPort = 4545;
		
		/** Secondary JokeServer's port*/
		int secondaryPort = 4546;
		
		/** Default and Primary JokeAdminServer's port*/
		int primaryAdminPort = 5050;
		
		/** Secondary JokeAdminServer's port*/
		int secondaryAdminPort = 5051;
		
		/** boolean variable to check if Secondary JokeServer option is available
		 * or not. If "secondary" command line argument is not there than
		 * it consider that no secondary fallback.*/
		boolean secondaryFallback = false;
		
		/**Check if command line arguments are not provided*/ 
		if (args.length < 1)
			secondaryFallback = false;
		else
		{
			if(args[0].equalsIgnoreCase("secondary"))
				secondaryFallback = true;
		}
		
		/**Create the instance of the Socket for JokeClient*/
		Socket sock;
		
		/** ServerSocket for JokeServer */
		ServerSocket servsock = null;
		
		/** Set port to default JokeServer Port*/
	    port = primaryPort;
	    
	    Log.init();
	    
	    ProcessRequest.initialize();
	    
	    Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                Log.println("W: interrupt received, killing server…");
                JokeAdminServer.adminControlSwitch = false;
                JokeServer.jokeServerRunning = false;
                ProcessRequest.saveClientStateOnDisk();
            }
        });
	    
	    /** Connect the JokeServer on default /primary port with queue len 6.*/
	    servsock = getServerSocket(port, q_len);
	    
	    /** if JokeServer is already running on primary port and secondary fallback is present
	     * then try to run on Secondary port. */
	    if(servsock == null && secondaryFallback )
	    {
	    	port = secondaryPort;
	    	servsock = getServerSocket(port, q_len);
	    }
	    
	    /** JokeServer fails to start on the given port then it will
	     * raise error and exit. */
	    if(servsock == null)
	    {
	    	System.err.println("Joke Server is already running on all ports. try different port");
	    	return;
	    }
	    
	    /** Based on above check, JokeServer running on Primary Port or Secondary port,
	     * JokeAdminServer will run on Primary Admin port or Secondary Admin port.*/
		if(port == primaryPort) 
		{
			
			/** Create JokeAdminServer object with primary Admin Port*/
			JokeAdminServer jokeAdminServer = new JokeAdminServer(primaryAdminPort);
			
			/** Create thread object of JokeAdminServer */
		    Thread t = new Thread(jokeAdminServer);
		    
		    /** Start the thread. */
		    t.start();  
		} 
		else 
		{
			/** Create JokeAdminServer object with secondary Admin Port*/
			JokeAdminServer jokeAdminServer = new JokeAdminServer(secondaryAdminPort);
			
			/** Create thread object of JokeAdminServer */
		    Thread t = new Thread(jokeAdminServer);
		    
		    /** Start the thread. */
		    t.start();  
		}
	    
		/**Print the message in the console*/
		Log.println("Joke Server 1.0 starting up, listening at port "+port+".\n");
		
		/**Run the while loop indefinitely*/
		while (jokeServerRunning) 
		{
			/**Accept any incoming connection from JokeClient*/
			sock = servsock.accept(); // wait for the next client connection
		
			/**Start the worker thread so that for each incoming request separate thread will be created */
			new Worker(sock).start(); // Spawn worker to handle it
		}

	}

	/**
	 * This method, getServerSocket, will create the new Server socket. If server is already
	 * running on the given port or throws any error then it
	 * will return the null.
	 * @param: port: port number on which server has to connect.
	 * @param: q_len: Queue length of server
	 * @return: It returns the ServerSocket object on successful connect otherwise null
	 * @exception: throws IOException
	 * */
	private static ServerSocket getServerSocket(int port, int q_len) throws IOException {
		
		/** ServerSocket variable */
		ServerSocket servsock = null;
		try {
			/**Create the server Socket and to run the TCP IP server*/
			servsock = new ServerSocket(port, q_len);
		} catch (java.net.BindException x) {
		
			/**If any exception is thrown then print the socket error message*/
			Log.println("Socket error : port "+ port +" already in use.");
			
		}

		return servsock;
	}

}

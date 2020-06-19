
/** File is: InetServer.java, Version 1.8 (small)
A multithreaded server for InetClient. Elliott, after Hughes, Shoffner, Winslow
This will not run unless TCP/IP is loaded on your machine.
----------------------------------------------------------------------*/
import java.io.*; // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries

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
				
				/**Print the data in the command prompt*/
				System.out.println("Looking up " + name);
				
				/**Call method to print the remote address*/
				printRemoteAddress(name, out);
			} catch (IOException x) 
			{
				/**If IOException is generated then print the error message*/
				System.out.println("Server read error");
				
				/**Print the detailed stack trace*/
				x.printStackTrace();
			}
			/**close the socket to free up the TCP connection, but serve will be still running to accept other requests*/
			sock.close(); 
		} catch (IOException ioe) {
			/**If any exception thrown then print the stack trace*/
			System.out.println(ioe);
		}
	}

	/**
	 * This is the method to get the remote client request and based on the post the data to the client 
	 * @param name : name of server provided by the client 
	 * @param out : output stream of the Socket to send the response to the server
	 */
	static void printRemoteAddress(String name, PrintStream out) {
		/**Put the code in try block to catch any runtime exception*/
		try {
			/**Send the data to the client*/
			out.println("Looking up " + name + "...");
			/**Create the instance of the InetAddress class using the server name provided by the client*/
			InetAddress machine = InetAddress.getByName(name);
			/**Send the host name to the client*/
			out.println("Host name : " + machine.getHostName()); 
			/**Send the Host IP address to the client*/
			out.println("Host IP : " + toText(machine.getAddress()));
		} catch (UnknownHostException ex) {
			/**if UnknownHostException is thrown then send the error message to client*/
			out.println("Failed in atempt to look up " + name);
		}
	}

	/**
	 * This method used to convert the IP address stored into the byte array to the string
	 * @param ip : IP stored in the byte array
	 * @return
	 */
	static String toText(byte ip[]) { /* Make portable for 128 bit format */
		
		/**Create the instance of the StringBuffer class*/
		StringBuffer result = new StringBuffer();
		
		/**Iterate through each by of the IP address */
		for (int i = 0; i < ip.length; ++i) {
			/**If IP data is after first byte*/
			if (i > 0)
				/**Append dot(.) after each byte data*/
				result.append(".");
			/**Append IP byte into the StringBuffer object*/ 
			result.append(0xff & ip[i]);
		}
		/**Return the IP address in string for format*/
		return result.toString();
	}
}

/**
 * This is the main class which will be used for the starting the TCP server
 * @author 
 *
 */
public class InetServer {
	/**
	 * This is main method and entry point for the JVM
	 * @param a : String array containing the command line argument
	 * @throws IOException : Through IO exception if any thrown by the code of main method
	 */
	public static void main(String a[]) throws IOException {
		
		/**Define the queue length*/
		int q_len = 6; 
		
		/**Define the post number in which TCP server will run*/
		int port = 1565;
		
		/**Create the instance of the Socket*/
		Socket sock;
		
		/**Create the server Socket and to run the TCP IP server*/
		ServerSocket servsock = new ServerSocket(port, q_len);
		
		/**Print the message in the console*/
		System.out.println("Clark Elliott's Inet server 1.8 starting up, listening at port 1565.\n");
		
		/**Run the while loop indefinitely*/
		while (true) {
			/**Accept any incoming connection*/
			sock = servsock.accept(); // wait for the next client connection
			
			/**Start the worker thread so that for each incoming request separate thread will be created */
			new Worker(sock).start(); // Spawn worker to handle it
		}
	}
}
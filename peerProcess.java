import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.IOException;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.BitSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class peerProcess implements RequiredConstants {

	/**
	 * 1. Spawn new nodes
	 * 2. connectivity
	 * Currently: 2 nodes two threads each . make it work !
	 * @param args
	 */
	// Has to be symmetric , and synchronised
	public static String mypeerID;
	private Thread listeningThread;
	private Thread connectThread;

	public static Vector<Thread> ConnectionThread = new Vector<Thread>();
	private ServerSocket serverSocket;
	public Vector<Connection> conn ;
	public static Map<String,Connection> cMap = new ConcurrentHashMap<String,Connection>();
	public static Map<String,BitSet> bitMap = new ConcurrentHashMap<String,BitSet>();
	public static HashMap<String,Integer> downloadRate = new  HashMap<String,Integer>();
	public static Map<String,Socket> socketsMap = new  HashMap<String,Socket>();
	public static Vector<Connection> peersInterestedVec = new Vector<Connection>();
	public static volatile Vector<String> unchokedVec = new Vector<String>();
	public static Map<String,Vector<Integer>> requestedMap = new ConcurrentHashMap<String,Vector<Integer>>();
	public static volatile Vector<String> validRequestList = new Vector<String>();
	public static Vector<String> prefNbr = new Vector<String>(); 
	private static int NumberOfPreferredNeighbors;
	private static int UnchokingInterval;
	private static int OptimisticUnchokingInterval;
	private static String FileName;
	private static int FileSize;
	private static int PieceSize;
	public static int TotalPieces;	
	public static BitSet mybitmap;
	public static volatile Boolean theEnd    = false;
	public static volatile Boolean iHaveFile = false;
	public static volatile Boolean iComp     = false;
	public static Boolean done               = false;
	public static Object lock = new Object();
	public static volatile Connection currOptNbr = null;
	
	private ArrayList<Connection> prefNeighborList;
	public static int myPieceCount = 0;
	public String tempDirectory;
	public static Logger logObj = null;
	int  portNumber;

	public peerProcess() {

	}

	public static void main(String args[]) throws IOException {
		final peerProcess peerobj = new peerProcess();
		peerobj.starting(args);
	}

	public void starting(String args[]){
		/*
		 * 1. Read configure from the file connection
		 * 2. Make connections list from peerinfo.cfg
		 * 3. then do processing .
		 */

		startTimerThreads();
		loadConfig();
		mypeerID = args[0];
		
		new File("peer_" + mypeerID).mkdir();
		logObj = new Logger(mypeerID);
		
		try{
			BufferedReader inp2 = new BufferedReader(new FileReader("PeerInfo.cfg"));
			String st2 = inp2.readLine();
			final String[] tokens2 = st2.split("\\s+");
			portNumber = Integer.parseInt(tokens2[2]);
			inp2.close();
		} catch(Exception ex) {}
		
		
		//Implement a listening thread which can accept incoming connections
		// try and contact the peers which are above the line no.
		String st;
		int nop = 0;
		Boolean connectFlag = false;
		int listenTo = 0,bindTo,my_position = 0 ;
		try {
		    final ServerSocket serSock = new ServerSocket(portNumber);
			BufferedReader inp = new BufferedReader(new FileReader("PeerInfo.cfg"));
			while((st = inp.readLine()) != null) {
				final String[] tokens = st.split("\\s+");
				//if(Integer.parseInt(tokens[3])==1){

				//}
				int lessthan = tokens[0].compareTo(mypeerID);
		
				if(lessthan == 0){
					if (tokens[3].equals("1")) {
						mybitmap.set(0,TotalPieces,true);
						iHaveFile = true;
						System.out.println("Thisdsdasdadadasdasd  " + mybitmap.nextClearBit(0) + "total length = " + TotalPieces);
						// start filesplitter 
						fileSplitter();
						fileJoiner();
					} else {									
					}

					bitMap.put(mypeerID,mybitmap);
					continue;
				}
				if(lessthan > 0) {
					try {
						Thread.sleep(1000);
					} catch(InterruptedException ex) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!1          24");
						System.out.println("!!!!!!!!!!!!!!!!!!!!1          1");
						Thread.currentThread().interrupt();
					}
					System.out.println("I'm Here");
					// start accepting sockets, put it in connection list
					// start a thread for doing this .

					Thread t1 = new Thread(new Runnable(){

						public void run(){
							Socket bindingSocket = null;
							try {
								bindingSocket = serSock.accept();
								synchronized(this){									
									socketsMap.put(tokens[0],bindingSocket);
								}
								//								
								final Connection c = new Connection(tokens[0],tokens[1],Integer.parseInt(tokens[2]));
								c.setPeerSocket(bindingSocket);
								c.setOutputStream(new ObjectOutputStream(bindingSocket.getOutputStream()));
								c.setInputStream(new ObjectInputStream(bindingSocket.getInputStream()));


								logObj.logFrom(tokens[0]);
								HandShake hs = new HandShake(Integer.parseInt(mypeerID));
								System.out.println("Here 1 s");
								/* Sending HandShake */
								try{
									c.getOutputStream().writeObject(hs);
									c.getOutputStream().flush();
								}
								catch(Exception ex){
									System.out.println("!!!!!!!!!!!!!!!!!!!!1          2");
									ex.printStackTrace();
								}
								HandShake recievedHandShake = null;
								System.out.println("Here 2 s");
								/* Receiving HandShake */

								System.out.println("  This was needed:" +  Integer.parseInt(c.getPeerID()) );
								while(recievedHandShake==null){
									try {
										recievedHandShake  = (HandShake)c.getInputStream().readObject();
									} catch(Exception ex){
										System.out.println("!!!!!!!!!!!!!!!!!!!!1          3");
										ex.printStackTrace();						
									}
								}
								System.out.println("Server Came Here");
								if(HS_HEADER.equals(recievedHandShake.getHeader())){
									if(recievedHandShake.getPeerID()==Integer.parseInt(c.getPeerID())){
										System.out.println("Server Got coreect header!");
										System.out.println("Server Sending Bitmap!");
										BitField  bf = new BitField(mybitmap);
										write(c.getOutputStream(),bf);
									}  else {
										System.out.println("I got This " + recievedHandShake.getPeerID() + "  This was needed:" +  Integer.parseInt(c.getPeerID()) );
									}
								} else {
									System.out.println("I also got This");
								}
								synchronized(this){									
									cMap.put(tokens[0],c);
									downloadRate.put(tokens[0],0);
								}
								/* Starting Continuosly Listening for messages*/
								final Connection SafeConn = c;
								Thread Listen = new Thread(new Runnable(){
									public void run(){
										try {
											while(!theEnd){
												try {
													Container clientMessage = read(SafeConn.getInputStream());
													HandleMessages mp = new HandleMessages(clientMessage,SafeConn.getPeerID());
													Thread msgProcessor = new Thread(mp);
													msgProcessor.start();
												} catch(Exception ex){
												//	Thread.sleep(1000);
													System.out.println("!!!!!!!!!!!!!!!!!!!!1          4");
													if (Thread.interrupted()) {
														throw new InterruptedException();
													}
													else {
														throw ex;
													}						
												} 
											}
										} catch (InterruptedException exception) {
											System.out.println("!!!!!!!!!!!!!!!!!!!!1          5");
										}
									}
								});
								Listen.start();

							} catch (IOException e) {
								System.out.println("!!!!!!!!!!!!!!!!!!!!1          6");
								System.out.println("Here 33333333333333333333333333333333333333333");
								e.printStackTrace();
							}
						}
					});
					t1.start();
					ConnectionThread.add(t1);
				} else {
					// start connecting sockets
					Thread t2 = new Thread(new Runnable(){

						public void run(){
							Socket conSock = null;
							try {							    
								conSock = new Socket(tokens[1],portNumber);
								synchronized(this){									
									socketsMap.put(tokens[0],conSock);
								}
								final Connection c = new Connection(tokens[0],tokens[1],Integer.parseInt(tokens[2]));								
								logObj.logTo(tokens[0]);
								c.setPeerSocket(conSock);
								System.out.println("Client Here 1");
								c.setOutputStream(new ObjectOutputStream(conSock.getOutputStream()));								
								System.out.println("Client Here 2");
								c.setInputStream(new ObjectInputStream(conSock.getInputStream()));
								System.out.println("Connecting to " + tokens[0] + "  token1 " + tokens[1] + " token 2 " + tokens[2]);
								HandShake hs = new HandShake(Integer.parseInt(mypeerID)); 
								/* Sending HandShake */
								try{
									c.getOutputStream().writeObject(hs); 
									c.getOutputStream().flush();
									System.out.println("Here 1");
								}
								catch(Exception ex){
									System.out.println("!!!!!!!!!!!!!!!!!!!!1          7");
									ex.printStackTrace();						
								}
								System.out.println("Here 2");
								HandShake recievedHandShake = null;
								/* Receiving HandShake */
								while(recievedHandShake==null){
									try {
										recievedHandShake  = (HandShake)c.getInputStream().readObject();
									} catch(Exception ex){
										System.out.println("!!!!!!!!!!!!!!!!!!!!1          8");
										ex.printStackTrace();						
									}
								}
								System.out.println("recievedHandShake.getPeerID() = " + recievedHandShake.getPeerID() + " & " + 
										"Integer.parseInt(c.getPeerID()) == " + Integer.parseInt(c.getPeerID()));
								if(HS_HEADER.equals(recievedHandShake.getHeader())){
									if(recievedHandShake.getPeerID()==Integer.parseInt(c.getPeerID())){
										System.out.println("Client Got correct header!");
										System.out.println("Client Sending Bitmap!");
										BitField  bf = new BitField(mybitmap);
										write(c.getOutputStream(),bf);
									}

								}
								synchronized(this){									
									cMap.put(tokens[0],c);
								}
								final Connection SafeConn = c;
								/* Starting Continuosly Listening for messages*/
								Thread Listen = new Thread(new Runnable(){
									public void run(){
										while(!theEnd){
											try{
												Container clientMessage = read(SafeConn.getInputStream());
												HandleMessages mp = new HandleMessages(clientMessage,SafeConn.getPeerID());
												Thread msgProcessor = new Thread(mp);
												msgProcessor.start();
											} catch(Exception ex){
												System.out.println("!!!!!!!!!!!!!!!!!!!!1          8");
												ex.printStackTrace();						
											}
										} 
									}
								});
								Listen.start();
							} catch (UnknownHostException e) {
								System.out.println("!!!!!!!!!!!!!!!!!!!!1          9");
								System.out.println("Here 1111111111111111111111111111111111111");
								e.printStackTrace();
							} catch (IOException e) {
								System.out.println("!!!!!!!!!!!!!!!!!!!!1          10");
								System.out.println("Here 22222222222222222222222222222222");
								e.printStackTrace();
							}
						}
					});
					t2.start();
					ConnectionThread.add(t2);
				}
			}
		} catch (Exception ex) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!1          11");
			System.out.println(ex.toString());
		}

	}
	private static void loadConfig(){

		Properties prop = new Properties();

		InputStream is;
		try {
			is = new FileInputStream("Common.cfg");
			prop.load(is);
			NumberOfPreferredNeighbors  = Integer.parseInt(prop.getProperty("NumberOfPreferredNeighbors"));
			UnchokingInterval           = Integer.parseInt(prop.getProperty("UnchokingInterval"));
			OptimisticUnchokingInterval = Integer.parseInt(prop.getProperty("OptimisticUnchokingInterval"));
			FileName                    = prop.getProperty("FileName");
			FileSize                    = Integer.parseInt(prop.getProperty("FileSize"));
			PieceSize                   = Integer.parseInt(prop.getProperty("PieceSize"));


			if(FileSize%PieceSize == 0){
				TotalPieces = FileSize/PieceSize;
			} else {
				TotalPieces = (FileSize/PieceSize) + 1;
			}
			mybitmap = new BitSet(TotalPieces);
		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!1          12");
		}
		/* Set maximum number of connections that are available to a peer */
		//synchronized(this) {
		//			allConnections = new Vector<Connection>(NumberOfPreferredNeighbors);
		//}

	}

	/**
	 * This function is called only by the peer having THE_FILE
	 */

	public static void fileSplitter() {
		try {
			int SPLIT_SIZE = PieceSize;
			FileInputStream fis = new FileInputStream("./peer_1000/" + FileName);
			String padding;
			byte buffer[] = new byte[SPLIT_SIZE];
			int count = 10000;
			while (true) {
				int i = fis.read(buffer, 0, SPLIT_SIZE);

				if (i == -1) 	break;

				String filename = "peer_" + mypeerID + "/"  +  count;
				FileOutputStream fos = new FileOutputStream(filename);
				fos.write(buffer, 0, i);
				fos.flush();
				fos.close();
				++count;
			}
		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!1          13");
		}
	}



	public synchronized static void  fileJoiner() throws FileNotFoundException {	
		File f = new File("./" + "peer_" + mypeerID + "/" + FileName);
		if(f.exists()) return;
		int SPLIT_SIZE = PieceSize;
		File file = new File("peer_" + mypeerID);
		File[] files = file.listFiles();
		Arrays.sort(files);

		FileOutputStream fout = new FileOutputStream("./" + "peer_" + mypeerID + "/" + FileName);
		FileInputStream segment;
		int length;
		try {

			for (int i = 0; i < files.length; i++) {
				segment = new FileInputStream(files[i].getPath()); 
				byte[] buff = new byte[SPLIT_SIZE];
				while ( (length = segment.read(buff)) > 0){
					fout.write(buff, 0, length);
				}
				segment.close();				 
			}
			fout.close();
		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!1          14");
			e.printStackTrace();
		}
		System.out.println("IIIIIIIIIIIIIIIII AM COMINGGGGGGGGGGGGG  HERER     "); 
	}

	public static synchronized void write(ObjectOutputStream outputStream, Container message){
		try {
			try {
				outputStream.writeObject(message);
				outputStream.flush();
			} 
			catch (IOException e) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!1          15");
				Thread.sleep(1000);
		      	logObj.stop();
				try {
					for (Connection c : cMap.values()) {
						c.getOutputStream().close();
						c.getInputStream().close();
					}
					for (Socket s : socketsMap.values()) {
						s.close();
					}

				} catch (IOException exio) {
				}
				System.exit(0);
				throw new InterruptedException();
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		} 
	}

	public Container read(ObjectInputStream inputStream){
		Container inputMessage = null;
		try {
			try {
				if(!theEnd) {
					inputMessage  = (Container) inputStream.readObject();
					return inputMessage;
				}
			} catch (IOException ex) {
				System.out.println("theEnd = " + theEnd);
				try {
					for (Connection c : cMap.values()) {
						c.getOutputStream().close();
						c.getInputStream().close();
					}
					for (Socket s : socketsMap.values()) {
						s.close();
					}

				} catch (IOException exio) {
				}
				//Thread.sleep(10000);
				logObj.stop();
				
				System.exit(0);
				System.out.println("!!!!!!!!!!!!!!!!!!!!1          16");
				throw new InterruptedException();
			} catch (ClassNotFoundException ex) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!1          17");
			}			
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
		return inputMessage;
	}

	public void startTimerThreads() {
		Thread taskScheduler = new Thread(new Runnable(){
			public void run(){
				while(cMap.size()==0){
					try {
						Thread.sleep(2000);
					} 
					catch (InterruptedException e) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!1          19");
					}

				}
				

				final Timer optNbrTimer = new Timer();
				optNbrTimer.schedule(new TimerTask(){
					public void run(){
					// call rate calculator
					  System.out.println(prefNbr);
						send_choke();
						downloadRateCalc();
						send_unchoke();
						getOptNbr();
					}
				}, 0, OptimisticUnchokingInterval*1000);

				final Timer finishTimer = new Timer();
				finishTimer.schedule(new TimerTask(){
					public void run() {
						if(completionChecker() == true){
							System.out.println("SSSSSSSSSSSSTTTTTTTTGGGGGGGGGGGG!!");
							optNbrTimer.cancel();
							theEnd = true;
							finishTimer.cancel();
						}
					}
				},0,10*1000);
			}
		});
		taskScheduler.start();
	}

	public boolean completionChecker() {
		/* this guy returns if the program is complete */
		//bitMap ..check all bitsets are set 
		//for all the peers int the list 
		int count_peers = 0;
		Iterator itr = this.cMap.entrySet().iterator();
		while(itr.hasNext()) {			   			  
			Map.Entry entry = (Map.Entry) itr.next();
			String p = (String)entry.getKey();
			BitSet tempBS = this.bitMap.get(p);
			if(tempBS == null) {
				return false;
			}
			if(tempBS.nextClearBit(0) >= TotalPieces) {
				count_peers++;
			} else {

			}
		}
		
		if(count_peers == cMap.size()) {
			try {
				Thread.sleep(2000);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			done = true;
			System.out.println("count_peers == cMap.size()" + count_peers  + "    " +cMap.size());
			Iterator vecItr = this.ConnectionThread.iterator();
			try {
				for (Connection c : peerProcess.cMap.values()) {					
					(c.getOutputStream()).close();
					(c.getInputStream()).close();
				} 
			} catch (Exception e) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!1          20");
			}

			while(vecItr.hasNext()) {				
				Thread temp = (Thread) vecItr.next();
				temp.stop();
			}
			return true;
		}
		return false;
	}

	public void getOptNbr() {
		if(peersInterestedVec.size() == 0){
			return;
		}
		if( currOptNbr != null ) {
			Choke chokeOptimistic = new Choke();
			write(currOptNbr.getOutputStream(),chokeOptimistic);
			unchokedVec.remove(currOptNbr.getPeerID());
		}

		Connection newOptNbr = peersInterestedVec.get(new Random().nextInt(peersInterestedVec.size()));
		
		currOptNbr = newOptNbr;
		Unchoke unchokeOptimistic = new Unchoke();
		logObj.logUnchoked(newOptNbr.getPeerID());
		write(newOptNbr.getOutputStream(), unchokeOptimistic);
		unchokedVec.add(newOptNbr.getPeerID());
	}
	
	public void send_choke() {
	
	for(int i=0; i< prefNbr.size(); i++)
{
String temp = prefNbr.elementAt(i);
Choke chokeOptimistic = new Choke();
			Connection c = this.cMap.get(temp);
			write(c.getOutputStream(),chokeOptimistic);
			prefNbr.remove(temp);
}
	prefNbr.clear();

	}
	
	public void send_unchoke() {
	
			for(int i=0; i< prefNbr.size(); i++) {
	String temp = prefNbr.elementAt(i);
	Unchoke UnchokeOptimistic = new Unchoke();
			Connection c = this.cMap.get(temp);
			write(c.getOutputStream(),UnchokeOptimistic);
			}	
		
	}

	public static synchronized int  compareBitmap( BitSet BitMapIn){
		ArrayList<Integer> missingChunks = new ArrayList<Integer>();
		if(iHaveFile){
			return -1;
		}
		int index = 0;
		while( index < TotalPieces){
			index = mybitmap.nextClearBit(index);
			if(BitMapIn.get(index)) {
				missingChunks.add(index);
			}
			index++;
		}
		if(missingChunks.size() == 0) {
			return -1;
		} else {
			return missingChunks.get(new Random().nextInt(missingChunks.size()));
		}
	}
	
	public synchronized ArrayList<Connection> getInterested() {
		ArrayList<Connection> interestedList = new ArrayList<Connection>();

		for (Connection c : cMap.values()) {
			if(c.getInterested() == true) {
				interestedList.add(c);
			}
		}
		return interestedList;
	}


	public static synchronized boolean isRequested(int PieceID) {
		Collection<Vector<Integer>> pieceIDs =  requestedMap.values();
		Iterator<Vector<Integer>> valueIterator = pieceIDs.iterator();
		while(valueIterator.hasNext()) {
			Vector<Integer> requested = (Vector<Integer>) valueIterator.next();
			if(requested.contains(PieceID)) {
				return true;
			}
		}
		return false;
	}

	public static synchronized void removeRequested(int PieceID){
		Collection<Vector<Integer>> pieceIDs =  requestedMap.values();
		Iterator<Vector<Integer>> valueIterator = pieceIDs.iterator();
		while(valueIterator.hasNext()) {
			Vector<Integer> requested = (Vector<Integer>) valueIterator.next();
			if(requested.contains(PieceID)) {
				int index = requested.indexOf(PieceID);

				requested.remove(index);
				return;
			}
		}
	}

	public static synchronized int get_currentPieceCount() {
		return myPieceCount;
	}
	public static synchronized void set_currentPieceCount() {
		myPieceCount++;
	}

	public static synchronized void putPiece(byte[] BytesIn, int pieceNumber){
		try {
			pieceNumber += 10000;
			OutputStream out = new FileOutputStream("peer_" + mypeerID + "/" + pieceNumber);
			out.write(BytesIn,0,BytesIn.length);
			out.close();
		} catch (Exception e) {  
			System.out.println("!!!!!!!!!!!!!!!!!!!!1          21");
		}
	}

	public static synchronized byte[] getPiece(int pieceNumber){
		pieceNumber += 10000;
		File pieceFile = new File("peer_" + mypeerID + "/" + pieceNumber);
		if(!pieceFile.exists()){
			System.out.println("Piece requested does not exist. PieceNumber = " + pieceNumber);
			return null;
		}
		int pieceFileSize     = (int) pieceFile.length();
		byte[] pieceFileBytes = new byte[(int) pieceFileSize];
		int bytesRead = 0;
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(pieceFile));
			bytesRead = dis.read(pieceFileBytes,0,pieceFileSize);
		} catch (FileNotFoundException e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!1          22");
			System.out.println("FNOF Execp");
		} catch (IOException e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!1          23");
			System.out.println("IOE Execp");
		}

		if(bytesRead<pieceFileSize){
			System.out.println("Could not read piece" + pieceNumber + " Completely");
			return null;
		}
		return pieceFileBytes;
	}
	
	
	 public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
	        List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
	        Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

	            @Override
	            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
	                return o1.getValue().compareTo(o2.getValue());
	            }
	        });
	     
	        //LinkedHashMap will keep the keys in the order they are inserted
	        //which is currently sorted on natural ordering
	        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
	     
	        for(Map.Entry<K,V> entry: entries){
	            sortedMap.put(entry.getKey(), entry.getValue());
	        }
	     
	        return sortedMap;
	    }
	    
	     private static Map sortByComparator(Map unsortMap) {
		 
			List list = new LinkedList(unsortMap.entrySet());
	 
			// sort list based on comparator
			Collections.sort(list, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((Comparable) ((Map.Entry) (o1)).getValue())
	                                       .compareTo(((Map.Entry) (o2)).getValue());
				}
			});
	 
			// put sorted list into map again
	                //LinkedHashMap make sure order in which keys were inserted
			Map sortedMap = new LinkedHashMap();
			for (Iterator it = list.iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				sortedMap.put(entry.getKey(), entry.getValue());
			}
			return sortedMap;
		}
		
		public static void downloadRateCalc() {
			Map<String, Integer> tempMap = sortByComparator(peerProcess.downloadRate);
			int temp = NumberOfPreferredNeighbors;	
			for ( String key : downloadRate.keySet()  ) {
			if(temp==0)break;     
			temp--;
			peerProcess.prefNbr.add(key);
		}
		//while(peerProcess.prefNbr.size() < 2) {
		//	String gg = new Random().nextString(peerProcess.downloadRate.keySet());
			//peerProcess.prefNbr.add(peerProcess.downloadRate.keySet().get( new Random().nextInt(peerProcess.downloadRate.size()) ));
		//	peerProcess.prefNbr.add(gg);
		//}
		//System.out.println("Prefered neighbor list are : " + prefNbr);
		peerProcess.logObj.logPrefNeighbor(" "+prefNbr);
		
	//	return Map;
		}
	
}

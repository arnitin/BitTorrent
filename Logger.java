import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class Logger {
	
	String PeerID ;
	PrintWriter pw = null; 
	String timeStamp ;
	public Logger(String pid) {
		this.PeerID = pid;
		try {
			this.pw = new PrintWriter("log_peer_" + this.PeerID +".log");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void getTime(){
		this.timeStamp = new SimpleDateFormat(" MM/dd/yyyy  H:m:s ").format(Calendar.getInstance().getTime());
	}
	//[Time]: Peer [peer_ID 1] makes a connection to Peer [peer_ID 2].

	public void logTo(String pid) {
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ "makes a connection to Peer[" + pid+"]");
	}
	
	//[Time]: Peer [peer_ID 1] is connected from Peer [peer_ID 2].
	
	public void logFrom(String pid) {
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ "makes a connection from Peer[" + pid+"]");
	}
	
//	[Time]: Peer [peer_ID] has the preferred neighbors [preferred neighbor ID list].

	public void logPrefNeighbor(String prefNeigh){
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ "has the preferred neighbors : "+ prefNeigh);
		}
	
	public void stuff(String pid) {
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+pid);
	}
	
//	[Time]: Peer [peer_ID] has the optimistically unchoked neighbor [optimistically unchoked neighbor ID].
	
	public void logUnchoked(String gg) {
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"] " + gg +"");
	}
	
//	[Time]: Peer [peer_ID 1] is unchoked by [peer_ID 2].

	public void logUnchokedBy (String pid) {
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ " is unchoked by Peer[" + pid+"]");
		}
	
//	[Time]: Peer [peer_ID 1] is choked by [peer_ID 2].

	public void logChoked(String pid) {
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ " is choked by Peer[" + pid+"]");
	}
	
	//[Time]: Peer [peer_ID 1] received the ‘have’ message from [peer_ID 2] for the piece [piece index]

	public void logHave(String pid, int pindex) {
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ "received the ‘have’ message from Peer[" + pid+"]" + 
		"for the piece "+pindex);
	}
	
//	[Time]: Peer [peer_ID 1] received the ‘interested’ message from [peer_ID 2].
	public void logInterested(String pid){
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ "  received the ‘interested’ message from Peer[" + pid+"]");
	}
	
	//[Time]: Peer [peer_ID 1] received the ‘not interested’ message from [peer_ID 2].

	public void logNotInterested(String pid){
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ "  received the ‘ not interested’ message from Peer[" + pid+"]");
	}
	
	//[Time]: Peer [peer_ID 1] has downloaded the piece [piece index] from [peer_ID 2].
	//Now the number of pieces it has is [number of pieces]

	public void logDownloaded(String pid, int pindex,int no_of_pieces) {
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ " has downloaded the piece" + pindex + " from " + "Peer[" + pid+"]");
		pw.println("Now the number of pieces it has is " + no_of_pieces);
	}
	
	// 	[Time]: Peer [peer_ID] has downloaded the complete file.

	public void logFinished() {
		getTime();
		pw.println(this.timeStamp + ": Peer["+this.PeerID+"]"+ " has downloaded the complete file");
	}
	
	// closing the file descriptor
	
	public void stop(){
		pw.close();
	}
	
}

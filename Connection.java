import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public final class Connection {

	private final String     myPeerID; 
	private int              myUplRate;
	private int              myDlRate;
	private Socket           myPeerSocket;
	private Boolean          choked;
	private final String     myHost;   
	private final int        port;    
	private Boolean          fullFile;
	private ObjectInputStream  inputStream;
	private ObjectOutputStream outputStream;
	private Boolean          interested;
    
    /***********************************************************************************/
	
	synchronized Boolean getInterested() {
		return interested;
	}

	/***********************************************************************************/
	
	synchronized void setInterested(Boolean interested) {
		this.interested = interested;
	}
	
	/***********************************************************************************/
	
	public Connection(String peerIDin, String hostIn, int port) {
		this.myPeerID     = peerIDin;
		this.myHost       = hostIn;
		this.port         = port;
	}
	//public Connection(){}
	/***********************************************************************************/
	
	public String getPeerID() {
		return myPeerID;
	}

	/***********************************************************************************/
	
	public int getUploadRate() {
		return myUplRate;
	}

	/***********************************************************************************/
	
	public void setUploadRate(int UplRateIn) {
		myUplRate = UplRateIn;
	}

	/***********************************************************************************/
	
	public int getDownloadRate() {
		return myDlRate;
	}

	/***********************************************************************************/
	
	public void set_downloadRate(int DlRateIn) {
		myDlRate = DlRateIn;
	}

	/***********************************************************************************/
	
	public Socket getPeerSocket() {
		System.out.println("Returning");
		return myPeerSocket;
	}

	/***********************************************************************************/
	
	public void setPeerSocket(Socket peerSocketIn) {
		myPeerSocket = peerSocketIn;
	}

	/***********************************************************************************/
	
	public Boolean getChoked() {
		return choked;
	}

	/***********************************************************************************/
	
	public void setChoked(Boolean chokedIn) {
		this.choked = choked;
	}

	/***********************************************************************************/
	
	public String getHost() {
		return myHost;
	}
	
	/***********************************************************************************/
	
	public int getPort() {
		return port;
	}
	
	/***********************************************************************************/
	
	public Boolean getFullFileStatus() {
		return fullFile;
	}

	/***********************************************************************************/
	
	public void setFullFileStatus(Boolean fullFileIn) {
		fullFile = fullFileIn;
	}

	/***********************************************************************************/
	
	public synchronized ObjectInputStream getInputStream() {
		return inputStream;
	}

	/***********************************************************************************/
	
	public synchronized void setInputStream(ObjectInputStream inputStreamIn) {
		this.inputStream = inputStreamIn;
	}

	/***********************************************************************************/
	
	public	synchronized ObjectOutputStream getOutputStream() {
		return outputStream;
	}

	/***********************************************************************************/
	
	public 	synchronized void setOutputStream(ObjectOutputStream outputStreamIn) {
		this.outputStream = outputStreamIn;
	}	
}

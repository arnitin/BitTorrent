import java.io.Serializable;

public class HandShake implements Serializable{
    

	private String header = "CNT5106C2013SPRING";
	private int peerID;
	private byte[] zeroBits = new byte[10];

	public HandShake(int IDin) {
		this.peerID = IDin;
	}

	public String getHeader() {
		return header;
	}

	public int getPeerID() {
		return peerID;
	}
}
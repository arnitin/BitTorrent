/**
 * 
 * Interface to define the constants.
 *
 */
public interface RequiredConstants {
	
	// Port no.
	//public static final int portNumber = 6345;
	// For Handshake messages
	public static final int HANDSHAKE_MSG = 32;
	public static final int HANDSHAKE_HEADER = 18;
	public static final int HANDSHAKE_ZEROBITS = 10;
	public static final int HANDSHAKE_PEERID = 4;
	public static final String HS_HEADER = "CNT5106C2013SPRING";
	
	// For Data messages
	public static final int DATA_MESSAGE_LENGTH = 4;
	public static final int DATA_MESSAGE_TYPE = 1;
	// public static final int DATA_MESSAGE_PAYLOAD is variable size
	
	// For message type 

	public static final int MSG_TYPE_CHOKE = 0;
	public static final int MSG_TYPE_UNCHOKE = 1;
	public static final int MSG_TYPE_INTERESTED = 2;
	public static final int MSG_TYPE_NOT_INTERESTED = 3;
	public static final int MSG_TYPE_HAVE = 4;
	public static final int MSG_TYPE_BITFIELD = 5;
	public static final int MSG_TYPE_REQUEST = 6;
	public static final int MSG_TYPE_PIECE = 7;
	
	// For 'have' messages
	public static final int PIECE_INDEX_FIELD = 4;
	
	//File Constants
	public static int  SPLIT_SIZE = 16384;
	
	public static int numOfPreferredNeighbr = 0;
	public static int unchokingInterval = 0;
	public static int optUnchokingInterval = 0;
	public static String fileName = "testfile.txt";
	public static int fileSize = 0;
	public static int pieceSize = 0;
	
	// For type of bytes transmitted
	public static final String MSG_CHARSET_NAME = "UTF8";
}

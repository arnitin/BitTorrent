import java.io.Serializable;
public class Piece extends Container implements Serializable,RequiredConstants{
 
   public int pieceIndex;
   public byte[] pieceBytes;
   public Integer type = MSG_TYPE_PIECE;

	public Piece(byte[] pieceBytes, int pieceIndex) {
		super(MSG_TYPE_PIECE);
		this.pieceIndex = pieceIndex ;
		this.pieceBytes = pieceBytes ;
	}
}
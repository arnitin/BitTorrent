public class Request  extends Container  implements RequiredConstants{
   public int pieceIndex;
   public Integer type = MSG_TYPE_REQUEST;
   
   public Request(int pieceIndex) {
	   super(MSG_TYPE_REQUEST);
	   this.pieceIndex = pieceIndex;
   }
}
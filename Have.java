public class Have extends Container  implements RequiredConstants{

	public int  pieceIndex;
	public Integer type = MSG_TYPE_HAVE;

	public Have(int peiceIndexIn) {
		super(MSG_TYPE_HAVE);
		this.pieceIndex = peiceIndexIn;
	}
}
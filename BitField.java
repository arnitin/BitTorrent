import java.util.BitSet;


public class BitField extends Container implements RequiredConstants{

	public BitSet myBitMap;
	public Integer type = 5;//MSG_TYPE_BITFIELD;

	public BitField(BitSet bitMapIn){
		super(MSG_TYPE_BITFIELD);
		this.myBitMap = bitMapIn;
	}
}
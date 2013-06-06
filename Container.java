import java.io.Serializable;

public class Container implements Serializable{

	public int length;
	public Integer type;
	
	Container(Integer type){
		this.type = type;
	}	
}

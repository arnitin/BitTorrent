import java.util.BitSet;
import java.util.Vector;
import java.io.File;
import java.io.FileNotFoundException;

//Logger Defined in peerProcess
public class HandleMessages implements Runnable,RequiredConstants{    
	Container input;	
	public String peerID;	
	public HandleMessages(Container input,String peerID) {
		this.input = input;
		this.peerID = peerID;
	}

	@Override
	public void run() {	
		if(input.type == null) {
			return ;
		}
		switch(input.type){
			case MSG_TYPE_CHOKE:
				handle_choke();
				break;
			case MSG_TYPE_UNCHOKE: 
				handle_unchoke();
				break;
			case MSG_TYPE_INTERESTED:
				handle_interested();
				break;
			case MSG_TYPE_NOT_INTERESTED: 
				handle_not_interested();
				break;
			case MSG_TYPE_HAVE: 
				handle_have();
				break;
			case MSG_TYPE_BITFIELD: 
				handle_bitfield();
				break;
			case MSG_TYPE_REQUEST: 
				try {
					handle_request();
					break;
				} catch (OutOfMemoryError ex) {
					return;
				}
			case MSG_TYPE_PIECE: 
				handle_piece();
				break;			 
		}
	}


	private void handle_choke(){
		if (peerProcess.validRequestList == null ) {
			return;
		}
		peerProcess.validRequestList.remove(peerID);
		Vector<Integer> requestedList = peerProcess.requestedMap.get(peerID);
		if (requestedList == null ) {
			return;
		}
		requestedList.clear();
		
		
		peerProcess.logObj.logChoked(peerID);  
	}

	private void handle_unchoke(){
		if(peerProcess.theEnd) {
			return;
		}
		if(!peerProcess.validRequestList.contains(peerID)) {
			peerProcess.validRequestList.add(peerID);
		}
		
		BitSet connectionBitSet = peerProcess.bitMap.get(peerID);
		while(peerProcess.validRequestList.contains(peerID)) { // peerProcess.prefNbr.contains(peerID)){
			//if(!peerProcess.prefNbr.contains(peerID)){ break;}
			int pieceId = peerProcess.compareBitmap(connectionBitSet);
			if(pieceId == -1) {
				continue;
			}
			if(!peerProcess.isRequested(pieceId)){
				if(peerProcess.requestedMap.get(peerID) == null){
					Vector<Integer> pieceRequested = new Vector<Integer>();
					pieceRequested.add(pieceId);
					peerProcess.requestedMap.put(peerID, pieceRequested);
				} else {
					Vector<Integer> pieceRequested = peerProcess.requestedMap.get(peerID);
					pieceRequested.add(pieceId); 
				}
				Request reqPiece = new Request(pieceId);
				Connection intrConn = peerProcess.cMap.get(peerID);
				peerProcess.write(intrConn.getOutputStream(),reqPiece);
			}
		}
		peerProcess.logObj.logUnchokedBy(peerID);
	}

	private void handle_interested(){
		peerProcess.logObj.logInterested(this.peerID);
		// Get the connection 
		Connection tempConn = peerProcess.cMap.get(this.peerID);
		tempConn.setInterested(true);
		// If not included in the interested list, then include it 
		if(!peerProcess.peersInterestedVec.contains(tempConn)) {
			peerProcess.peersInterestedVec.add(tempConn);
		}
	}


	private void handle_not_interested(){
		peerProcess.logObj.logNotInterested(this.peerID);
		Connection tempConn = peerProcess.cMap.get(this.peerID);
		tempConn.setInterested(false);
		// If included in the interested list, then remove it
		if(peerProcess.peersInterestedVec.contains(tempConn)) {
			peerProcess.peersInterestedVec.remove(tempConn);
		}
	}

	private void handle_have(){
		//Lock the bitfield so that other threads cannot access it
		peerProcess.logObj.logHave(peerID,((Have)input).pieceIndex);	 
		synchronized(peerProcess.lock) {
			BitSet connectionBitSet = peerProcess.bitMap.get(peerID);
			if(connectionBitSet == null) {
				return;
			}
			connectionBitSet.set(((Have)input).pieceIndex, true);

			if(peerProcess.mybitmap.get(((Have)input).pieceIndex) == false){		
				Interested iNeed = new Interested();
				Connection intrConn = peerProcess.cMap.get(peerID);
				peerProcess.write(intrConn.getOutputStream(),iNeed);
			} else {
				NotInterested iDontNeed = new NotInterested();
				Connection notIntrConn = peerProcess.cMap.get(peerID);
				peerProcess.write(notIntrConn.getOutputStream(),iDontNeed);
			}
		}
	}

	private void handle_bitfield(){
		synchronized(peerProcess.lock) {
			BitSet connectionBitSet = peerProcess.bitMap.get(peerID);
			if(connectionBitSet == null ){
				peerProcess.bitMap.put(peerID, ((BitField)input).myBitMap);
				System.out.println("Putting BitfieldIn");
			} else {
				connectionBitSet = ((BitField)input).myBitMap;
			}		

			int missing = peerProcess.compareBitmap(((BitField)input).myBitMap);
			System.out.println("MY missin : " + missing);
			if(missing != -1) {			
				System.out.println("I DO!!!!");
				Interested unchokeOptimistic = new Interested();
				Connection intrConn = peerProcess.cMap.get(peerID);
				peerProcess.write(intrConn.getOutputStream(),unchokeOptimistic);
			} else {
				System.out.println("I don't need!!");
			}
		} 
	}



	private void  handle_request(){
		BitSet connectionBitSet = peerProcess.bitMap.get(peerID);
		if(connectionBitSet != null){
			if(connectionBitSet.get(((Request)input).pieceIndex) == true) {
				System.out.println("##################");
				return;
			}
		}
		byte[] pieceByte = null;
		//if(peerProcess.mybitmap.get(((Piece)input).pieceIndex) == true)  { 
	//	peerProcess.logObj.stuff("dup request returning from  handle_request & piece index is "+  ((Piece)input).pieceIndex);
	//	return;
	//	 } 
		synchronized(peerProcess.lock){
					pieceByte = peerProcess.getPiece(((Request)input).pieceIndex);
		}
		
		Piece msgPiece = new Piece(pieceByte,((Request)input).pieceIndex);
		Connection tempConn = peerProcess.cMap.get(peerID);
		peerProcess.write(tempConn.getOutputStream(),msgPiece);
		
		

	}

	private void  handle_piece(){
		int count_piece ;
		int totalDown = new File("peer_" + peerProcess.mypeerID).list().length;	
		if(peerProcess.iComp) {
			return;
		}
		
		if(peerProcess.mybitmap.get(((Piece)input).pieceIndex) == true) { 
		peerProcess.logObj.stuff("dup request return from  handle_piece & piece index is "+  ((Piece)input).pieceIndex);
		return;
		 } 
		synchronized(peerProcess.lock){
 peerProcess.logObj.stuff("current piece count: " +peerProcess.get_currentPieceCount()+ "Total piece count "+peerProcess.TotalPieces); 
			peerProcess.putPiece(((Piece)input).pieceBytes,((Piece)input).pieceIndex);
			peerProcess.mybitmap.set(((Piece)input).pieceIndex,true);
			peerProcess.bitMap.put(peerProcess.mypeerID,peerProcess.mybitmap);

			totalDown = new File("peer_" + peerProcess.mypeerID).list().length;
			if(peerProcess.iComp != true)  {
				if(totalDown == peerProcess.TotalPieces){
					peerProcess.iComp = true;
					try{
						peerProcess.fileJoiner();	
					} catch (FileNotFoundException ex){
					}
					peerProcess.logObj.logFinished();
				} 
			}
			try{
			if(peerProcess.downloadRate.get(peerID)==null) peerProcess.downloadRate.put(peerID,0);
			else {
			int temp = peerProcess.downloadRate.get(peerID);
			temp++;
			peerProcess.downloadRate.put(peerID,temp);
			}
			} catch (NullPointerException ex ) { System.out.println("Download rate thingy");}
			peerProcess.set_currentPieceCount();
		}
		Have haveMsg = new Have(((Piece)input).pieceIndex);
		for (Connection c : peerProcess.cMap.values()) {
			peerProcess.write(c.getOutputStream(),haveMsg);
		}
		peerProcess.logObj.logDownloaded(peerID,((Piece)input).pieceIndex , totalDown);
	}
}

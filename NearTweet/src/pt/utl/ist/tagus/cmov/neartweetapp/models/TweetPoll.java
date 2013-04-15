package pt.utl.ist.tagus.cmov.neartweetapp.models;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.PollResponseDTO;

public class TweetPoll extends Tweet{

//	private HashMap<String,ArrayList<String>> mOptions;

	private ArrayList<String> options = new ArrayList<String>();
	private ArrayList<PollResponseDTO> responses = new ArrayList<PollResponseDTO>();
	private boolean updated = false;
	public TweetPoll(String texto, String mUsername, String mDeviceID, Long mTweetId){
		super( texto, mUsername, mDeviceID,mTweetId); 
		
		//mOptions = new HashMap<String,ArrayList<String>>();
	}

	public TweetPoll(){
		super();
	}
	
	
	public ArrayList<String> getOptions() {
		return options;
	}

	public void setOptions(ArrayList<String> options) {
		this.options = options;
	}

	
	public ArrayList<PollResponseDTO> getAllResponses() {
		return responses;
	}

	public void addResponse(PollResponseDTO resp) {
		
		for(PollResponseDTO p : responses){
			if(p.getSrcDeviceID().equals(resp.getSrcDeviceID())){
				return;
			}
		}
		responses.add(resp);
		updated = true;
	}
	
	public boolean hasPollUpdates(){
		return updated;
	}
	
	public void setPollRead(){
		updated = false;
	}
	
//	public void addOptions(String answer){
//		mOptions.put(answer, new ArrayList<String>());	
//	}

//	public boolean addVote(String user,String answer){
//		if (mOptions.get(answer).contains(user)){
//			return false;
//		}
//		else{
//			mOptions.get(answer).add(user);
//			return true;
//		}
//	}

//	public ArrayList<String> getVoters(String answer){
//		return mOptions.get(answer);
//	}
	
//	// formato -> [resposta, [user_que voutou1, user_que_votou_2,....]]
//	public HashMap<String,ArrayList<String>> getOptions(){
//		return mOptions;
//	}

//	//gera comentarios dummy para usar na interface NAO APGAR
//	public HashMap<String,ArrayList<String>> generateDummyAnswers(){
//		HashMap<String, ArrayList<String>> dummyAnswers = new HashMap<String,ArrayList<String>>();
//		
//		ArrayList<String> dummyVoters1 = new ArrayList<String>();
//		ArrayList<String> dummyVoters2 = new ArrayList<String>();
//		ArrayList<String> dummyVoters3 = new ArrayList<String>();
//		
//		String answ1 = "Hoje chover?";
//		String answ2 = "Hoje nao vai chover?";
//		String answ3 = "O David é BUE lindo?";
//		String voter_balanuta = "Balanuta";
//		String voter_tufa = "Tufa";
//		String voter_david = "David";
//		
//		dummyVoters1.add(voter_david);
//		dummyVoters1.add(voter_balanuta);
//		dummyVoters1.add(voter_tufa);
//		
//		dummyVoters3.add(voter_balanuta);
//		
//		dummyAnswers.put(answ1, dummyVoters1);
//		dummyAnswers.put(answ2, dummyVoters2);
//		dummyAnswers.put(answ3, dummyVoters3);
//		
//		return dummyAnswers;
//	}
}
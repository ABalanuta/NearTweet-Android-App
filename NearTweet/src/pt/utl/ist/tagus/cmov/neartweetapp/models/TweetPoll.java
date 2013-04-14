package pt.utl.ist.tagus.cmov.neartweetapp.models;

import java.util.ArrayList;
import java.util.HashMap;

public class TweetPoll extends Tweet{

	private HashMap<String,ArrayList<String>> mAnswers;

	public TweetPoll(String texto, String uId, String macAddress,Long mTweetId){
		super( texto, uId, macAddress,mTweetId);
		mAnswers = new HashMap<String,ArrayList<String>>();
	}

	public TweetPoll(){
		super();
	}
	
	public void addAnswer(String answer){
		ArrayList<String> voters = new ArrayList<String>();
		mAnswers.put(answer, voters);	
	}

	public boolean addVote(String user,String answer){
		if (mAnswers.get(answer).contains(user)){
			return false;
		}
		else{
			mAnswers.get(answer).add(user);
			return true;
		}
	}

	public ArrayList<String> getVoters(String answer){
		return mAnswers.get(answer);
	}
	
	// formato -> [resposta, [user_que voutou1, user_que_votou_2,....]]
	public HashMap<String,ArrayList<String>> getAnswers(String answer){
		return mAnswers;
	}

	//gera comentarios dummy para usar na interface NAO APGAR
	public HashMap<String,ArrayList<String>> generateDummyAnswers(){
		HashMap<String, ArrayList<String>> dummyAnswers = new HashMap<String,ArrayList<String>>();
		
		ArrayList<String> dummyVoters1 = new ArrayList<String>();
		ArrayList<String> dummyVoters2 = new ArrayList<String>();
		ArrayList<String> dummyVoters3 = new ArrayList<String>();
		
		String answ1 = "Hoje chover?";
		String answ2 = "Hoje nao vai chover?";
		String answ3 = "O David � lindo?";
		String voter_balanuta = "Balanuta";
		String voter_tufa = "Tufa";
		String voter_david = "David";
		
		dummyVoters1.add(voter_david);
		dummyVoters1.add(voter_balanuta);
		dummyVoters1.add(voter_tufa);
		
		dummyVoters3.add(voter_balanuta);
		
		dummyAnswers.put(answ1, dummyVoters1);
		dummyAnswers.put(answ2, dummyVoters2);
		dummyAnswers.put(answ3, dummyVoters3);
		
		return dummyAnswers;
	}
}
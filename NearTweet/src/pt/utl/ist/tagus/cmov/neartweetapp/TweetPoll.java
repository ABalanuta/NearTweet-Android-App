package pt.utl.ist.tagus.cmov.neartweetapp;

import java.util.ArrayList;
import java.util.HashMap;

public class TweetPoll extends Tweet{

	private HashMap<String,ArrayList<String>> mAnswers;

	public TweetPoll(String texto, String uId, String macAddress){
		super( texto,  uId,  macAddress);
		mAnswers = new HashMap<String,ArrayList<String>>();
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
	
	public HashMap<String,ArrayList<String>> getAnswers(String answer){
		return mAnswers;
	}
	
}

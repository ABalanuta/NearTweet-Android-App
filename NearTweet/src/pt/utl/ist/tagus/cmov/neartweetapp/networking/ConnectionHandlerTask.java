package pt.utl.ist.tagus.cmov.neartweetapp.networking;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.tagus.cmov.neartweetapp.MainActivity;
import pt.utl.ist.tagus.cmov.neartweetapp.Tweet;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TypeofDTO;
import android.os.AsyncTask;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ConnectionHandlerTask extends AsyncTask<String,BasicDTO,Tweet> {

	//private	final static String serverIP = "10.0.2.2";
	protected final String KEY_TEXT = "texto";
	protected final String KEY_TWEETER = "utilizador";
	ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
	private	final static String serverIP = "172.20.81.13";
	private	final static int serverPort = 4444;

	@Override
	protected Tweet doInBackground(String... message) {

		InetAddress serverAddr = null;
		try {
			serverAddr = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		}

		Socket localSock = null;
		ConnectionHandler ch = null;


		// Contacting the Server , Retry if error
		while(true){
			try{
				localSock = new Socket(serverAddr, serverPort);
				break;
			}catch(Exception e){
				System.out.println("TCP " + " Sleeping 5s");
				System.out.println(e.toString());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {}
			}
		}
		
		MainActivity.connectionHandler = new ConnectionHandler(localSock);
		MainActivity.connectionHandler.start();
		MainActivity.mProgressBar.setVisibility(View.INVISIBLE);

		while(true){

			if(MainActivity.connectionHandler.recevedObjects()){
				ArrayList<BasicDTO> objects  = MainActivity.connectionHandler.receve();
				for(BasicDTO oo : objects){
					publishProgress(oo);
				}
			}else{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	protected void onProgressUpdate(BasicDTO... values) {

		if(values[0].getType().equals(TypeofDTO.TWEET_DTO)){
			TweetDTO t = (TweetDTO) values[0];
			
			// Cenas do Tufa para actualizar a lista de tweets
			HashMap<String,String> tweetInterface = new HashMap<String,String>();
			tweetInterface.put(KEY_TEXT,t.getTweet());
			tweetInterface.put(KEY_TWEETER,"Balanuta");
			tweets.add(tweetInterface);
			
			int[] ids = {android.R.id.text1, android.R.id.text2};
			String[] keys = {KEY_TEXT,KEY_TWEETER };
	//		SimpleAdapter adapter = new SimpleAdapter(MainActivity, tweets, android.R.layout.simple_list_item_2, keys, ids);
		//	setListAdapter(adapter);

			
			
			
			
//			for (Tweet tweet : mTweetsArray){
//				String text = tweet.getText();
//				String userId = tweet.getUId();
//
//				HashMap<String,String> tweetInterface = new HashMap<String,String>();
//				tweetInterface.put(KEY_TEXT,text);
//				tweetInterface.put(KEY_TWEETER,userId);
//				tweets.add(tweetInterface);
//			}
//
//			String[] keys = {KEY_TEXT,KEY_TWEETER };
//			int[] ids = {android.R.id.text1, android.R.id.text2};
//			SimpleAdapter adapter = new SimpleAdapter(this, tweets,
//					android.R.layout.simple_list_item_2, keys, ids);
//			setListAdapter(adapter);
			
			//MainActivity.receveTextBox.append(t.getTweet()+"\n");
		}

	}
}

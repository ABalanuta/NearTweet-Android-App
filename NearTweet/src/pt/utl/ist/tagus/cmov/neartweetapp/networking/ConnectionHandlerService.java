package pt.utl.ist.tagus.cmov.neartweetapp.networking;


import java.util.ArrayList;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ConnectionHandlerService extends Service {

	  private ConnectionHandler mConectionHandler;
	  private final IBinder mBinder = new LocalBinder();

	  @Override
	  public void onCreate() {
		  super.onCreate();
		  
		  this.mConectionHandler = new ConnectionHandler();
		  mConectionHandler.start();
		  Log.e("ServiceP", "Created");
		  
	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
		  
	      Log.e("ServiceP", "Started");
	      
	      // If we get killed, after returning from here, restart
	      return START_NOT_STICKY;
	  }
	  
	  @Override
	  public IBinder onBind(Intent arg0) {
	    return mBinder;
	  }

	  
	  @Override
	  public void onDestroy() {
		  
		  Log.e("ServiceP", "Destroing");
		  
		  this.mConectionHandler.close();
		  
	  }
	  
	  public class LocalBinder extends Binder {
		  
		  public ConnectionHandlerService getService() {
	            // Return this instance of LocalService so clients can call public methods
	            return ConnectionHandlerService.this;
	        }
	    }
	  
	  
	  public ArrayList<BasicDTO> receveNewTweets(){
		  
		  if(mConectionHandler.recevedObjects()){
			  return null;
		  }
		  
		  return this.mConectionHandler.receve();
	  }
	  
	  
	  public void sendTweet(BasicDTO tweet){
		  mConectionHandler.send(tweet);
	  }
	  
	}
package pt.utl.ist.tagus.cmov.neartweetapp;

import java.util.HashMap;
import java.util.Map;

import pt.utl.ist.cmov.neartweet.wifidirect.WifiDirectBroadcastReceiver;

import android.app.Activity;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Bundle;
import android.util.Log;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class GroupSelectionActivity extends Activity {

	static WifiP2pManager mManager;
	static Channel mChannel;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_g);
		
		
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		

	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//		getMenuInflater().inflate(R.menu, menu);
//		return true;
//	}

	
	
	@Override
	public void finish() {
		
		super.finish();
	}
	
	
	@SuppressWarnings("unchecked")
	private static void RegisterService() {
		
		Map record = new HashMap();
		record.put("listenport", String.valueOf(4444));
		record.put("available", "visible");

		WifiP2pDnsSdServiceInfo serviceInfo =
				WifiP2pDnsSdServiceInfo.newInstance("NearTweetService", "_presence._tcp", record);

		mManager.addLocalService(mChannel, serviceInfo, new android.net.wifi.p2p.WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
				Log.e("ServiceP", "startRegistration SUCCESS");
			}

			@Override
			public void onFailure(int arg0) {
				Log.e("ServiceP", "startRegistration FAIL");
			}
		});
	}
	
	
	
	
}

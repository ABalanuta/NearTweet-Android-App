package pt.ist.utl.cmov.neartweet.wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.widget.Toast;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

	private WifiP2pManager mManager;
	private Channel mChannel;
	private Activity mActivity;
	



	public WifiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
			Activity activity) {
		super();
		this.mManager = manager;
		this.mChannel = channel;
		this.mActivity = activity;
	}





	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();


		PeerListListener myPeerListListener = new PeerListListener() {			
			@Override
			public void onPeersAvailable(WifiP2pDeviceList peers) { //este callback é chamado quando o requestPeers é chamado
				// WifiP2pDeviceList peers - contém todos os peers que encontrar :)
				Toast.makeText(mActivity.getApplicationContext(), "There are peers available", Toast.LENGTH_LONG).show();
				

				//Iterar e ligar-me a todos os peers que encontrar :)
				for (WifiP2pDevice d : peers.getDeviceList()){
					//obtain a peer from the WifiP2pDeviceList
					WifiP2pDevice device = d;
					WifiP2pConfig config = new WifiP2pConfig();
					config.deviceAddress = device.deviceAddress;
					mManager.connect(mChannel, config, new ActionListener() {
						@Override
						public void onSuccess() {
							//success logic
							Toast.makeText(mActivity.getApplicationContext(), "Connected to a Peer", Toast.LENGTH_LONG).show();
			        		
						}

						@Override
						public void onFailure(int reason) {
							//failure logic
						}
					});
				}
			}
		};
		
		
		


		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// Wifi Direct is enabled
			} else {
				// Wi-Fi Direct is not enabled
			}


		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Call WifiP2pManager.requestPeers() to get a list of current peers

			// request available peers from the wifi p2p manager. This is an
			// asynchronous call and the calling activity is notified with a
			// callback on PeerListListener.onPeersAvailable()
			//if (mManager != null) {
			//	mManager.requestPeers(mChannel, myPeerListListener);
			//}



		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			// Respond to new connection or disconnections

			Toast.makeText(mActivity.getApplicationContext(), "Received Connection Request", Toast.LENGTH_LONG).show();
    		
			// HERE IS WHERE WE GET NOTIFIED THAT THERE WAS NEW CONNECTIONS TO US :)
			if (mManager == null) {
				return;
			}
			NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

			if (networkInfo.isConnected()) {
				// we are connected with the other device, request connection
				// info to find group owner IP
				mManager.requestConnectionInfo(mChannel,(ConnectionInfoListener) mActivity);
			} else {
				// It's a disconnect
			}





		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			// Respond to this device's wifi state changing
			
			//Isto é se o Wifi for com os porcos
		}
	}
}

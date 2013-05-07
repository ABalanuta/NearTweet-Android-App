package pt.utl.ist.cmov.neartweet.wifidirect;

import java.util.HashMap;
import java.util.Map;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.MainActivity;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class GroupSelectionActivity extends Activity {

	static WifiP2pManager mManager;
	static Channel mChannel;
	ListView mListView;
	Button mScanButton;
	Button mCreateGroupButton;
	TextView mCreateGroupTextBox;
	
	CmovPreferences mMyPreferences ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_group_selection);
		
		//		setContentView(R.layout.activity_g);
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = MainActivity.mChannel;
		

		mListView = (ListView) findViewById(R.id.groupsList);
		mScanButton = (Button) findViewById(R.id.scanGroups);
		mCreateGroupButton = (Button) findViewById(R.id.createGroupButton);
		mCreateGroupTextBox = (TextView) findViewById(R.id.createGroupTextBox);
		
		mMyPreferences = new CmovPreferences(getApplicationContext());
		
		//ListView listView = getListView();
		//listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		
		//discoverService();
		
		
		mScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//SCAN ALL THE SERVICES =)
				//discoverService();
				
				reloadSearch();
			}
		});
		
		mCreateGroupButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//UNREGISTER LAST GROUP
				mManager.removeGroup(mChannel, new ActionListener(){
					@Override
					public void onFailure(int reason) {Log.d("UNREGISTER GROUP", "COULD NOT NOT");}
					@Override
					public void onSuccess() {Log.d("UNREGISTER GROUP", "SUCCESS");}
				});
				//REGISTER NEW GROUP
				String text = mCreateGroupTextBox.getText().toString();
				if (text == null){
					Toast.makeText(getApplicationContext(), "Cannot create groups with empty names :)" , Toast.LENGTH_LONG).show();
					return;
				}
				startRegistration(text);
			}
		});
		
		
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		mManager = null;
		mChannel = null;
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









	/***************************************************************************************
	 * 
	 * 							 WIFI Direct Methods
	 * 
	 ***************************************************************************************/

	@SuppressWarnings("unchecked")
	private static void startRegistration(String groupName) {
		//  Create a string map containing information about your service.
		Map record = new HashMap();
		record.put("listenport", String.valueOf(4444));
		record.put("buddyname", MainActivity.mUsername + (int) (Math.random() * 1000));
		record.put("available", "visible");

		WifiP2pDnsSdServiceInfo serviceInfo =
				WifiP2pDnsSdServiceInfo.newInstance(groupName, "_presence._tcp", record);

		mManager.addLocalService(mChannel, serviceInfo, new android.net.wifi.p2p.WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() { Log.i("ServiceP", "startRegistration SUCCESS"); }
			@Override
			public void onFailure(int arg0) { Log.i("ServiceP", "startRegistration FAIL"); }
		});
	}
	
	
	static void discoverService() {
		Log.e("ServiceP", "discoverService init");
		DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
			@Override
			public void onDnsSdTxtRecordAvailable(String fullDomain, Map record, WifiP2pDevice device) {
				Log.e("ServiceP", "DnsSdTxtRecord available -" + record.toString());
			}
		};

		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
			@Override
			public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {
				//	                    // Update the device name with the human-friendly version from
				//	                    // the DnsTxtRecord, assuming one arrived.
				//	                    resourceType.deviceName = buddies
				//	                            .containsKey(resourceType.deviceAddress) ? buddies
				//	                            .get(resourceType.deviceAddress) : resourceType.deviceName;
				//
				//	                    // Add to the custom adapter defined specifically for showing
				//	                    // wifi devices.
				//	                    WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
				//	                            .findFragmentById(R.id.frag_peerlist);
				//	                    WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
				//	                            .getListAdapter());
				//
				//	                    adapter.add(resourceType);
				//	                    adapter.notifyDataSetChanged();
				Log.i("ServiceP", "I FOUND SERVICES =) it is called : " + instanceName);
				//Toast.makeText(getApplicationContext(), "Discovered service called" + instanceName , Toast.LENGTH_LONG).show();
			}
		};

		mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);
		WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

		mManager.addServiceRequest(mChannel,serviceRequest, new  WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() { Log.e("ServiceP", "addServiceRequest  onSuccess"); }
			@Override
			public void onFailure(int code) {
				if (code == WifiP2pManager.P2P_UNSUPPORTED) {
					Log.e	("ServiceP", "P2P isn't supported on this device.");
				}
				Log.e("ServiceP", "addServiceRequest  onFailure");
			}
		});
		Log.e("ServiceP", "discoverService final");

	}


	

	private static void reloadSearch(){

		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
			@Override
			public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {
				//	                    // Update the device name with the human-friendly version from
				//	                    // the DnsTxtRecord, assuming one arrived.
				//	                    resourceType.deviceName = buddies
				//	                            .containsKey(resourceType.deviceAddress) ? buddies
				//	                            .get(resourceType.deviceAddress) : resourceType.deviceName;
				//
				//	                    // Add to the custom adapter defined specifically for showing
				//	                    // wifi devices.
				//	                    WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
				//	                            .findFragmentById(R.id.frag_peerlist);
				//	                    WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
				//	                            .getListAdapter());
				//
				//	                    adapter.add(resourceType);
				//	                    adapter.notifyDataSetChanged();
				Log.i("ServiceP", "I FOUND SERVICES =) it is called : " + instanceName);
				//Toast.makeText(getApplicationContext(), "Discovered service called" + instanceName , Toast.LENGTH_LONG).show();
			}
		};
		
		
		
		mManager.discoverServices(mChannel, new  WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
					
				Log.e("ServiceP", "discoverServices success");
			}

			@Override
			public void onFailure(int code) {
				// Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
				if (code == WifiP2pManager.P2P_UNSUPPORTED) {
					Log.d("group coisa", "P2P isn't supported on this device.");
				}
			}
		});
	}















}

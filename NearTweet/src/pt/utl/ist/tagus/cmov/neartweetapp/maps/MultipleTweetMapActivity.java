/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pt.utl.ist.tagus.cmov.neartweetapp.maps;

import pt.utl.ist.tagus.cmov.neartweet.R;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * This shows how to create a simple activity with a map and a marker on the map.
 * <p>
 * Notice how we deal with the possibility that the Google Play services APK is not
 * installed/enabled/updated on a user's device.
 */
public class MultipleTweetMapActivity extends FragmentActivity {
    


	
	
	/**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;
    private Double mlat = 0.0;
    private Double mlng = 0.0;
    private String mTweetText = "Marker";
    
    private String[] latArray;
    private String[] lngArray;    
    private String[] textArray;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);
        
        if(!(com.google.android.gms.common.ConnectionResult.SUCCESS == 
         com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()))){
        	finish();
        }
        

        Bundle bundle = getIntent().getExtras();
		
        //bundle will have Array of LAT, Array of LNG, Array of TweetText
        
        latArray = bundle.getStringArray("latArray");
        lngArray = bundle.getStringArray("lngArray");
        textArray = bundle.getStringArray("textArray");
        
		
       
        //mlat = Double.parseDouble(bundle.getString("gps_location_lat"));
		//mlng = Double.parseDouble(bundle.getString("gps_location_lng"));
		
		//mTweetText = bundle.getString("tweet_text");
        
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	//mMap = null;
    	//mlat = 0.0;
    	//mlng = 0.0;
    }
    

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        for(int i=0; i< latArray.length; i++){
        	mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latArray[i]), Double.parseDouble(lngArray[i]))).title(textArray[i]));
        }
 
    	//mMap.addMarker(new MarkerOptions().position(new LatLng(mlat, mlng)).title(mTweetText));
    }
}

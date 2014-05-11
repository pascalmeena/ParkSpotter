/**
 * Author: Pascal & Usman
 * References: http://www.androidhive.info/2013/08/android-working-with-google-maps-v2/
 * http://www.vogella.com/tutorials/AndroidLocationAPI/article.html
 * http://wptrafficanalyzer.in/blog/customizing-infowindow-contents-in-google-map-android-api-v2-using-infowindowadapter/
 */
package com.parking.parkspotter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class ShowTheMap extends FragmentActivity  implements LocationListener{
	// Google Map
    private GoogleMap googleMap;
    private LatLng gp, currentLoc;
    MarkerOptions startMO;//start position marker options
    //Database Access
    private SQLiteDatabase db;
	private DBManager dbManager;
	
	  private LocationManager locationManager;
	  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showthemap);
       // googleMap.setTrafficEnabled(true);
        
        try {
        	//check for google play
        	googlePlay();
            // Loading map
            initilizeMap();
            //save the current location of the user
            getCurrentLocation();
            
            
            //Display current user location 
            //googleMap.setMyLocationEnabled(true);
            //Display zoom gestures 
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
                        
            //Initialize some data
            dbManager= new DBManager(this);
            db = dbManager.getReadableDatabase();
            
            String theStory = null;
            Intent startingIntent = getIntent();
            
           //Set the center of the map and move to it
            if (savedInstanceState == null) {
                CameraUpdate center=
                    CameraUpdateFactory.newLatLng(new LatLng(51.045325, -114.058101));
                CameraUpdate zoom=CameraUpdateFactory.zoomTo(10);

                googleMap.moveCamera(center);
                googleMap.animateCamera(zoom);
            }
            
            //Get the bundle from the previous activity
            if (startingIntent != null)
            {
            	 Bundle b = startingIntent.getBundleExtra("android.intent.extra.INTENT");
            
            	//check if the bundle is exists
            	if(b == null)
        		{
            		theStory = "bad string bundle?";
        		}else{
                    String title = b.getString("title");
                    String category = b.getString("category");
                    String click = b.getString("click");
                  
                    //Check to place markers for all the listed items on the RSSReader activity
                    if(click.equals("nextbutton")){
                    	
                    	//look in the database and return categoryid of the specified category term
                    	String query = "select * from "+DBManager.DB_TABLE+" where "+DBManager.C_DESCRIPTION+" = ?";
                    	
                    	Cursor categoryCursor = db.rawQuery(query, new String[] { category });
                    	
                		String categoryid = "";
                        if (categoryCursor.moveToFirst()) 
                        {
                        	  categoryid = categoryCursor.getString(categoryCursor.getColumnIndex(DBManager.C_SENDER));
                        } else {
                          System.out.println("NOTHING WAS FOUND");
                        } 
                        	
                     
                    	//Use the retrieved categoryid to return a specified item
                    	Cursor parkadeCursor = db.rawQuery("select * from "+DBManager.DB_TABLE_PARKADE+" where "+DBManager.CATEGORYID+" = ?", new String[] { categoryid });
                    	
                    //check cursor
                    if(parkadeCursor.getCount() > 0){
                    		parkadeCursor.moveToFirst();
                    	    do{
                    	    	
                            	String myLocation = parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.ADDRESS));
                    	    	try{
	                            	//Using Async class to do network connection in the background
	                            	CreateAddressJsonObject task  = new CreateAddressJsonObject();
	                            	task.execute(myLocation).get();
	                            	
	                    	    	//Get my preferred location(s) coordinates by address
	                            	gp = getLatLng(task.get());
	                            	//String parkTitle =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.TITLE));
                        			//googleMap.addMarker(new MarkerOptions().position(gp).title(parkTitle).snippet(myLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                   		       
	                            	
                    	    	}catch(Exception e){
                    	    		Toast.makeText(this, "ERROR 4 GeoPoint: " +e, Toast.LENGTH_LONG).show();
                    	    		e.printStackTrace();
                    	    	}
                            	
                    	    	//make sure the geopoint is not null
                    	    	if(gp == null){
                   		    	 Toast.makeText(this,"No matches were found! for address", Toast.LENGTH_LONG).show();
                   		        }else{
                   		        	
                   		        	final String pktitle =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.TITLE));
                        			final String pkaddress =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.ADDRESS));
                        			final String counter =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.PARKINGCOUNTER));
                        	   
                	                // Adding marker on the GoogleMap
                	                // Creating an instance of MarkerOptions to set position
                        			googleMap.addMarker(new MarkerOptions().position(gp).title(pktitle).snippet(pkaddress).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))).showInfoWindow();

                	                // Setting position on the MarkerOptions
                	                CameraUpdate zoom=CameraUpdateFactory.zoomTo(10);
                	                googleMap.moveCamera(CameraUpdateFactory.newLatLng(gp));
                	                
                	                // Animating to the currently touched position
                	                googleMap.animateCamera(zoom);
                	 
                	 
                   		        }
                        	    	
                        	        
                        	    }while(parkadeCursor.moveToNext());
                    		
                    		
                    		
                    	}else{
                    	    Toast.makeText(this,"There are no parkades under this category. Try another category to proceed", Toast.LENGTH_LONG).show();
                    	}//end cursor check
                    	
                	    
                      click="clicked";
            	        
                    }
                   //check to place a marker of the only clicked item on the map
                    if(click.equals("itemclick")){
                    	//Use the title to uniquely identify your item
                    	Cursor parkadeCursor = db.rawQuery("select * from "+DBManager.DB_TABLE_PARKADE+" where "+DBManager.TITLE+" = ?", new String[] { title });
                    	
                		
                		//check cursor
                    	if(parkadeCursor.getCount() > 0){
                    		
                        	parkadeCursor.moveToFirst();
                    	    do{
                    	    	
                            	String myLocation = parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.ADDRESS));
                    	    	
                            	try{
                                	//Using Async class to do network connection in the background
                                	CreateAddressJsonObject task  = new CreateAddressJsonObject();
                                	task.execute(myLocation).get();
                                	
                        	    	//Get my preferred location(s) coordinates by address
                                	gp = getLatLng(task.get());
                        	    	}catch(Exception e){
                        	    		Toast.makeText(this, "ERROR 4 GeoPoint: " +e, Toast.LENGTH_LONG).show();
                        	    		e.printStackTrace();
                        	    	}
                            	
                            		//make sure the geopoint is not null
                            		if(gp == null){
                            			Toast.makeText(this,"No matches were found! for address", Toast.LENGTH_LONG).show();
                            		}else{
                            			final String pktitle =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.TITLE));
                            			final String pkaddress =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.ADDRESS));
                            			final String counter =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.PARKINGCOUNTER));
                            		
                            			//This method will override all the markers info window, we need to use hashmap to fix the problem
                            	   /*     googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
                            	 
                            	            // Use default InfoWindow frame
                            	            @Override
                            	            public View getInfoWindow(Marker arg0) {
                            	                return null;
                            	            }
                            	 
                            	            // Defines the contents of the InfoWindow
                            	            @Override
                            	            public View getInfoContents(Marker arg0) {
                            	 
                            	                // Getting view from the layout file info_window_layout
                            	                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                            	 
                            	                // Getting reference to the TextView to set title
                            	                TextView parkTitle = (TextView) v.findViewById(R.id.title);
                            	 
                            	                // Getting reference to the TextView to set address
                            	                TextView parkAddress = (TextView) v.findViewById(R.id.address);
                            	                
                            	                // Getting reference to the TextView to set counter
                            	                TextView parkCounter = (TextView) v.findViewById(R.id.counter);
                            	 
                            	                parkTitle.setText(pktitle);
                            	                parkAddress.setText("Address:"+ pkaddress);
                            	                parkCounter.setText(counter);
                            	 
                            	                // Returning the view containing InfoWindow contents
                            	                return v;
                            	 
                            	            }
                            	        });*/
                            	        
                            			googleMap.addMarker(new MarkerOptions().position(gp).title(pktitle).snippet(pkaddress).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))).showInfoWindow();

                    	                // Creating an instance of MarkerOptions to set position
                    	                MarkerOptions markerOptions = new MarkerOptions();
                    	 
                    	                // Setting position on the MarkerOptions
                    	                markerOptions.position(gp);
                    	                CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);
                    	                googleMap.moveCamera(CameraUpdateFactory.newLatLng(gp));
                    	                
                    	                // Animating to the currently touched position
                    	                googleMap.animateCamera(zoom);
                    	 
                    	                // Adding marker on the GoogleMap
                    	                Marker marker = googleMap.addMarker(markerOptions);
                    	 
                    	                // Showing InfoWindow on the GoogleMap
                    	                marker.showInfoWindow();
                    	                
                    	                String url = getDirectionsUrl(currentLoc,gp);
                    	                DownloadTask downloadtask = new DownloadTask();
                    	                //download json data from Google Directions API
                    	                downloadtask.execute(url);
                            	    }
                            			
                            		                        	
                    	    }while(parkadeCursor.moveToNext());
                    	}else{
                    		 Toast.makeText(this,"There are no parkades under this category. Try another category to proceed", Toast.LENGTH_LONG).show();
                    	}//end cursor check
                    	
                    	
            	        click = "clicked"; 
                    }
        			
                  //This selects all parkades in FavoriteParkade activity and displays them on the map
                    if(click.equals("fromAll")){
                    	                     
                    	//Use the retrieved categoryid to return a specified item
                    	Cursor parkadeCursor = db.rawQuery("select * from "+DBManager.DB_TABLE_FAVPARKADE, new String [] {});
                    	
                    //check cursor
                    if(parkadeCursor.getCount() > 0){
                    		parkadeCursor.moveToFirst();
                    	    do{
                    	    	
                            	String myLocation = parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.ADDRESS));
                    	    	try{
	                            	//Using Async class to do network connection in the background
	                            	CreateAddressJsonObject task  = new CreateAddressJsonObject();
	                            	task.execute(myLocation).get();
	                            	
	                    	    	//Get my preferred location(s) coordinates by address
	                            	gp = getLatLng(task.get());
	                            	//String parkTitle =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.TITLE));
                        			//googleMap.addMarker(new MarkerOptions().position(gp).title(parkTitle).snippet(myLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                   		       
	                            	
                    	    	}catch(Exception e){
                    	    		Toast.makeText(this, "ERROR 4 GeoPoint: " +e, Toast.LENGTH_LONG).show();
                    	    		e.printStackTrace();
                    	    	}
                            	
                    	    	//make sure the geopoint is not null
                    	    	if(gp == null){
                   		    	 Toast.makeText(this,"No matches were found! for address", Toast.LENGTH_LONG).show();
                   		        }else{
                   		        	
                   		        	final String pktitle =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.TITLE));
                        			final String pkaddress =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.ADDRESS));
                        			final String counter =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.PARKINGCOUNTER));
                        	   
                	                // Adding marker on the GoogleMap
                	                // Creating an instance of MarkerOptions to set position
                        			googleMap.addMarker(new MarkerOptions().position(gp).title(pktitle).snippet(pkaddress).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))).showInfoWindow();

                	                // Setting position on the MarkerOptions
                	                CameraUpdate zoom=CameraUpdateFactory.zoomTo(10);
                	                googleMap.moveCamera(CameraUpdateFactory.newLatLng(gp));
                	                
                	                // Animating to the currently touched position
                	                googleMap.animateCamera(zoom);
                	 
                	 
                   		        }
                        	    	
                        	        
                        	    }while(parkadeCursor.moveToNext());
                    		
                    		
                    		
                    	}else{
                    	    Toast.makeText(this,"There are no parkades added. Plase add a category first!", Toast.LENGTH_LONG).show();
                    	}//end cursor check
                    	
                	    
                      click="clicked";
            	        
                    }
                    
                  //When Map button is clicked on Main Activity, it displays all parkades in the database
                    if(click.equals("fromAllParkades")){
                    	                     
                    	//Use the retrieved categoryid to return a specified item
                    	Cursor parkadeCursor = db.rawQuery("select * from "+DBManager.DB_TABLE_PARKADE, new String [] {});
                    	
                    //check cursor
                    if(parkadeCursor.getCount() > 0){
                    		parkadeCursor.moveToFirst();
                    	    do{
                    	    	
                            	String myLocation = parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.ADDRESS));
                    	    	try{
	                            	//Using Async class to do network connection in the background
	                            	CreateAddressJsonObject task  = new CreateAddressJsonObject();
	                            	task.execute(myLocation).get();
	                            	
	                    	    	//Get my preferred location(s) coordinates by address
	                            	gp = getLatLng(task.get());
	                            	//String parkTitle =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.TITLE));
                        			//googleMap.addMarker(new MarkerOptions().position(gp).title(parkTitle).snippet(myLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                   		       
	                            	
                    	    	}catch(Exception e){
                    	    		Toast.makeText(this, "ERROR 4 GeoPoint: " +e, Toast.LENGTH_LONG).show();
                    	    		e.printStackTrace();
                    	    	}
                            	
                    	    	//make sure the geopoint is not null
                    	    	if(gp == null){
                   		    	 Toast.makeText(this,"No matches were found! for address", Toast.LENGTH_LONG).show();
                   		        }else{
                   		        	
                   		        	final String pktitle =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.TITLE));
                        			final String pkaddress =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.ADDRESS));
                        			final String counter =parkadeCursor.getString(parkadeCursor.getColumnIndex(DBManager.PARKINGCOUNTER));
                        	   
                	                // Adding marker on the GoogleMap
                	                // Creating an instance of MarkerOptions to set position
                        			googleMap.addMarker(new MarkerOptions().position(gp).title(pktitle).snippet(pkaddress).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))).showInfoWindow();

                	                // Setting position on the MarkerOptions
                	                CameraUpdate zoom=CameraUpdateFactory.zoomTo(10);
                	                googleMap.moveCamera(CameraUpdateFactory.newLatLng(gp));
                	                
                	                // Animating to the currently touched position
                	                googleMap.animateCamera(zoom);
                	 
                	 
                   		        }
                        	    	
                        	        
                        	    }while(parkadeCursor.moveToNext());
                    		
                    		
                    		
                    	}else{
                    	    Toast.makeText(this,"There are no parkades in the database!", Toast.LENGTH_LONG).show();
                    	}//end cursor check
                    	
                	    
                      click="clicked";
            	        
                    }//end
        		}
            }
            else
            {
            	theStory = "Information Not Found.";
            
            }
            
            
          System.out.println("THE STORTY IS:"+theStory);
            
       
            
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
 
    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
 
    
    
  

  	
  //pass and read the returned JSON object from Google service and return location's GeoPoint
  	public LatLng getLatLng(JSONObject jsonObject) {
  	  LatLng mypoint = null;
      Double longitude;
      Double latitude;
          try {

              longitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                  .getJSONObject("geometry").getJSONObject("location")
                  .getDouble("lng");

              latitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                  .getJSONObject("geometry").getJSONObject("location")
                  .getDouble("lat");
              
              mypoint = new LatLng(latitude, longitude);

          } catch (JSONException e) {
          	 e.printStackTrace();
  			  Toast.makeText(this,
  			    "The network is unavailable or any other I/O problem occurs!",
  			    Toast.LENGTH_LONG).show();

          }

         return mypoint;
      }
  	//pass and read the returned JSON object from Google service and return location's GeoPoint
  	public GeoPoint getGeoPoint(JSONObject jsonObject) {
      GeoPoint mypoint = null;
      Double longitude;
      Double latitude;
          try {

              longitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                  .getJSONObject("geometry").getJSONObject("location")
                  .getDouble("lng");

              latitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                  .getJSONObject("geometry").getJSONObject("location")
                  .getDouble("lat");
              System.out.println("<<<<<<<<<LATITUDE:"+latitude);
              System.out.println("<<<<<<<<<LONGITUDE:"+longitude);
              
              mypoint = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));

          } catch (JSONException e) {
          	 e.printStackTrace();
  			  Toast.makeText(this,
  			    "The network is unavailable or any other I/O problem occurs!",
  			    Toast.LENGTH_LONG).show();

          }

         return mypoint;
      }
    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
        googleMap.setMyLocationEnabled(true);
    }
 
   
    
    
    
    /*
	 * This function is used to parse the rss feed url on a different thread 
	 * so Android won't complain		
	 */
	private class CreateAddressJsonObject extends AsyncTask	<String, Void, JSONObject >
	{
		@Override
		protected JSONObject doInBackground(String... urls) {
			String address;
			StringBuilder stringBuilder = new StringBuilder();
	          try {

	          address = urls[0].replaceAll(" ","%20");    

	          HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
	          HttpClient client = new DefaultHttpClient();
	          HttpResponse response;
	          stringBuilder = new StringBuilder();


	              response = client.execute(httppost);
	              HttpEntity entity = response.getEntity();
	              InputStream stream = entity.getContent();
	              int b;
	              while ((b = stream.read()) != -1) {
	                  stringBuilder.append((char) b);
	                  
	              }
	          } catch (ClientProtocolException e) {
	          } catch (IOException e) {
	          }

	          JSONObject jsonObject = new JSONObject();
	          try {
	              jsonObject = new JSONObject(stringBuilder.toString());
	          } catch (JSONException e) {
	              // TODO Auto-generated catch block
	              e.printStackTrace();
	          }

	          return jsonObject;
		}
		
		
		
		@Override
		protected void onPostExecute(JSONObject result) {
			
			Log.d("result count: ", result.toString());
			//feed = result;
			
			
		}
		
	}



	  /* Remove the locationlistener updates when Activity is paused */
	  @Override
	  protected void onPause() {
	    super.onPause();
	    locationManager.removeUpdates(this);
	  }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		int lat = (int) (location.getLatitude());
	    int lng = (int) (location.getLongitude());
	    System.out.print("Printout: \n"+ lat +".."+lng);
	    currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
	    startMO = new MarkerOptions().position(currentLoc)
	    		.title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
	    googleMap.addMarker(startMO);
	    
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Disabled provider " + provider,
		        Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Enabled new provider " + provider,
		        Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
    
    //This method checks if google play is available on the device, it's need for Google Maps
	public void googlePlay() {
	    // ---- Google Play

	    // Getting Google Play availability status
	    int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

	    if (status != ConnectionResult.SUCCESS) { // Google Play Services are
	                                                    // not available

	        int requestCode = 10;
	        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,requestCode);
	        dialog.show();

	    } else { // Google Play Services are available

	        //Toast.makeText(getApplicationContext(),"Services Launched Fo  Sho'",Toast.LENGTH_SHORT).show();
	    }
	}
	
	//This method gets the current device location
	public void getCurrentLocation(){
	    // Getting LocationManager object from System Service LOCATION_SERVICE
	    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	    boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    boolean enabledWiFi = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	    // Check if enabled and if not send user to the GPS settings
	    if (!enabledGPS && !enabledWiFi) {
	        Toast.makeText(getApplicationContext(), "GPS signal not found",
	                Toast.LENGTH_LONG).show();
	        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	        startActivity(intent);
	    }
	    // Creating a criteria object to retrieve provider
	    Criteria criteria = new Criteria();

	    // Getting the name of the best provider
	    String provider = locationManager.getBestProvider(criteria, true);

	    // Getting Current Location From GPS
	    Location oLc = locationManager.getLastKnownLocation(provider);
	    if (oLc != null) {
	        onLocationChanged(oLc);
	    }
	    locationManager.requestLocationUpdates(provider, 20000, 0, this);

	}
	
	private String getDirectionsUrl(LatLng updatedLatLng, LatLng dest) {
	    // Origin of route
	    String str_updatedLatLng = "origin=" + updatedLatLng.latitude + ","
	            + updatedLatLng.longitude;

	    // Destination of route
	    String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

	    // Sensor enabled
	    String sensor = "sensor=false";

	    // Building the parameters to the web service
	    String parameters = str_updatedLatLng + "&" + str_dest + "&" + sensor;

	    // Output format
	    String output = "json";

	    // Building the url to the web service
	    String url = "https://maps.googleapis.com/maps/api/directions/"
	            + output + "?" + parameters;

	    return url;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
	    String data = "";
	    InputStream iStream = null;
	    HttpURLConnection urlConnection = null;
	    try {
	        URL url = new URL(strUrl);

	        // Creating an http connection to communicate with url
	        urlConnection = (HttpURLConnection) url.openConnection();

	        // Connecting to url
	        urlConnection.connect();

	        // Reading data from url
	        iStream = urlConnection.getInputStream();

	        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

	        StringBuffer sb = new StringBuffer();

	        String line = "";
	        while ((line = br.readLine()) != null) {
	            sb.append(line);
	        }

	        data = sb.toString();

	        br.close();

	    } catch (Exception e) {
	        Log.d("Exception while downloading url", e.toString());
	    } finally {
	        iStream.close();
	        urlConnection.disconnect();
	    }
	    return data;
	}
	
	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String> {
	    // Downloading data in non-ui thread
	    @Override
	    protected String doInBackground(String... url) {

	        // For storing data from web service
	        String data = "";

	        try {
	            // Fetching the data from web service
	            data = downloadUrl(url[0]);
	        } catch (Exception e) {
	            Log.d("Background Task", e.toString());
	        }
	        return data;
	    }

	    // Executes in UI thread, after the execution of
	    // doInBackground()
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);

	        ParserTask parserTask = new ParserTask();

	        // Invokes the thread for parsing the JSON data
	        parserTask.execute(result);

	    }
	}
	
	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
	        AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

	    // Parsing the data in non-ui thread
	    @Override
	    protected List<List<HashMap<String, String>>> doInBackground(
	            String... jsonData) {
	        JSONObject jObject;
	        List<List<HashMap<String, String>>> routes = null;

	        try {
	            jObject = new JSONObject(jsonData[0]);
	            DirectionsJSONParser parser = new DirectionsJSONParser();

	            // Starts parsing data
	            routes = parser.parse(jObject);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return routes;
	    }

	    // Executes in UI thread, after the parsing process
	    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
	        ArrayList<LatLng> points = null;
	        PolylineOptions lineOptions = null;
	        MarkerOptions markerOptions = new MarkerOptions();
	        String distance = "";
	        String duration = "";

	        if (result.size() < 1) {
	            Toast.makeText(getBaseContext(), "No Points",
	                    Toast.LENGTH_SHORT).show();
	            return;
	        }

	        // Traversing through all the routes
	        for (int i = 0; i < result.size(); i++) {
	            points = new ArrayList<LatLng>();
	            lineOptions = new PolylineOptions();

	            // Fetching i-th route
	            List<HashMap<String, String>> path = result.get(i);

	            // Fetching all the points in i-th route
	            for (int j = 0; j < path.size(); j++) {
	                HashMap<String, String> point = path.get(j);

	                if (j == 0) { // Get distance from the list
	                    distance = point.get("distance");
	                    continue;
	                } else if (j == 1) { // Get duration from the list
	                    duration = point.get("duration");
	                    continue;
	                }
	                double lat = Double.parseDouble(point.get("lat"));
	                double lng = Double.parseDouble(point.get("lng"));
	                LatLng position = new LatLng(lat, lng);
	                points.add(position);
	            }

	            // Adding all the points in the route to LineOptions
	            lineOptions.addAll(points);
	            lineOptions.width(4);
	            lineOptions.color(Color.RED);
	        }
	        //Add a textview to display distance and duration HERE
	        
	        // Drawing polyline in the Google Map for the route
	        googleMap.addPolyline(lineOptions);
	    }
	}
}
 

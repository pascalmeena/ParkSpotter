/*
 * Author: Pascal & Usman
 */
package com.parking.parkspotter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements OnClickListener {

	private List<RSSItem> feed;

	//My Entities variables
	private Parkade _parkade;
	private List<Parkade> Parkades = null;
	
	//Database Variables
	private SQLiteDatabase db;
	private DBManager dbManager;
	String fromAllParkades = null;
	//Here we start call the main layout, and set the view
	//Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	

   
    //We create a view for the Favorite Parkade button
    View firstButton = findViewById(R.id.button_fav_park);
    firstButton.setOnClickListener(this); 
    
    //We create a view for the Categories button
    View secondButton = findViewById(R.id.button_categories);
    secondButton.setOnClickListener(this);
    
    //We create a view for the Map button
    View thirdButton = findViewById(R.id.button_map);
    thirdButton.setOnClickListener(this);
            
    populateParkades();
	}


	//This method tells what to be done when button is clicked
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		//Depending on what is click, do the following..
		switch(v.getId()){
		
		//When Favorite button is clicked, call FavoriteParkades activity
		case R.id.button_fav_park:		
			Intent i1 = new Intent(this, FavoriteParkades.class);
			startActivity(i1);        
	    break;		
	    
	    //When Categories button is clicked, call Categories Activity 
		case R.id.button_categories:		
			Intent i2 = new Intent(this, Categories.class); 
			startActivity(i2);
	    break;	
	    
	    //When Map button is clicked, call ShowTheMap activity and display all the parkades
		case R.id.button_map:	
			Intent itemintent = new Intent(getApplicationContext(), ShowTheMap.class);
  	         Bundle b = new Bundle();
  	         fromAllParkades = "fromallparkades";
  	        	 b.putString("fromallparkades", fromAllParkades);
  		 	     b.putString("click", "fromAllParkades");
  	   	         itemintent.putExtra("android.intent.extra.INTENT", b);
  	   	         itemintent.putExtras(b);
  	             startActivity(itemintent);   
	    break;
	    
		}
	}
	
	//This function reads the Json file and creates parkades object from the data 
	private void populateParkades(){

		JSONObject json = getJSONFromFile("parkades.txt");
    	//String url = "http://www....";   //need to be json
		//JSONObject json = getJSONfromURL(url);
		Parkades = new Vector<Parkade>();	 
	    try{
	   
	    
	    JSONArray  nodes = json.getJSONArray("nodes");
	 
        //Loop the Array
        for(int i=0;i < nodes.length();i++){                      
 
            //Create an instance of a Parkade object to store and later retrieving information from it. Its probably
        	//more efficient to use SQLite ContentValues to do the same this quicker.
            _parkade = new Parkade();
            JSONObject node = nodes.getJSONObject(i).getJSONObject("node");
            
            _parkade.set_parkd_name(node.getString("title"));
            _parkade.set_parkd_nid(node.getInt("nid"));
			_parkade.set_parkd_address(node.getString("address"));
			_parkade.set_parkd_category_id(node.getInt("categoryid"));
		    _parkade.set_parkd_rssfeed_url(node.getString("rssfeedurl"));
		    		    
		    
		    //_parkade.set_parkadecount(feed.toString());
	         Parkades.add(_parkade);
	         
            }
        replaceParkades();
	
	    }catch(JSONException e)        {
	         Log.e("log_tag", "Error parsing data "+e.toString());
	    }
	}
    
    //This function adds the Parkade objects into the database
	private void replaceParkades() {
		dbManager = new DBManager(this);
		try{
			db=dbManager.getWritableDatabase();
	    	ContentValues cv=new ContentValues();

	    	for(int i=0; i< Parkades.size();i++){
	    	
	    		System.out.println("PARKADE REPLACED:======"+Parkades.get(i).get_parkd_name());
	    		cv.put(DBManager.PNID, Parkades.get(i).get_parkd_nid());
	    		cv.put(DBManager.TITLE, Parkades.get(i).get_parkd_name());
	    		cv.put(DBManager.ADDRESS, Parkades.get(i).get_parkd_address());
	    		cv.put(DBManager.CATEGORYID, Parkades.get(i).get_parkd_category_id());
	    		cv.put(DBManager.RSSFEEDURL, Parkades.get(i).get_parkd_rssfeed_url());
	    		
	    		//Additional or Extended Parkade Information as retrieved from the RSSFeed
	    		if(!Parkades.get(i).get_parkd_rssfeed_url().isEmpty() || Parkades.get(i).get_parkd_rssfeed_url() != null ){
	    			//Toast.makeText(this, "Park Feed: "+ Parkades.get(i).get_parkd_rssfeed_url(), Toast.LENGTH_LONG).show();
	    			
	    			replaceParkadeFeeds(Parkades.get(i).get_parkd_rssfeed_url());
	    			
	    			cv.put(DBManager.PARKINGCOUNTER, feed.toString());
	    			//Toast.makeText(this, feed.toString(),Toast.LENGTH_LONG).show();
	    		}
	    		else
	    		{
	    			System.out.println("RSSFEED url is empty and It didn't run the rssfeed");
	    		}
	    		db.replaceOrThrow(DBManager.DB_TABLE_PARKADE, null, cv);
	    		//System.out.println("PARKADE COUNTER:======"+Parkades.get(i).get_parkd_name() +"Count: " +Parkades.get(i).get_parkadecount());

	    	}
	    	
		}catch(Exception e){
			Toast.makeText(this, "ERROR2:"+e,
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
    	
    	 	
	}
		
	//This function fetches more information from the rssfeed
	private void replaceParkadeFeeds(String feedUrl) {
		
		try{
			
			DownloadRssFeed task = new DownloadRssFeed();
		    
			task.execute(feedUrl).get();
			//Toast.makeText(this, task.get().toString(),Toast.LENGTH_LONG).show();
			feed = task.get();

			// Debug the thread name
			Log.d("ParkadeRSSReader", Thread.currentThread().getName());
			
		}catch(Exception e){
			Toast.makeText(this, "ERROR3:"+e,
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	 
  	
	/*
	 * This function is used to parse the rss feed url on a different thread 
	 * so Android won't complain		
	 */
	private class DownloadRssFeed extends AsyncTask	<String, Void, List<RSSItem> >
	{
		@Override
		protected List<RSSItem> doInBackground(String... urls) {
			
			// Debug the task thread name
			Log.d("ParkadeRSSReader", Thread.currentThread().getName());
			
			try {
				// Create RSS reader
				RSSFeed rssReader = new RSSFeed(urls[0]);
				//Log.d("here is url string: ", rssReader.getItems().toString());
				// Parse RSS, get items
				return rssReader.getItems();
			
			} catch (Exception e) {
				Log.e("ParkadeRSSReader", e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(List<RSSItem> result) {
			
			Log.d("FEED count: ", result.toString());
			//feed = result;
			
			
		}
		
	}
	
	//Currently read JSON string from a text file but you can pass customise it as appropriate for service URL 
		private JSONObject getJSONFromFile(String file){
	    	
	    	AssetManager am = this.getAssets();
	    	InputStream is;
	    	
	    	JSONObject jArray = null;
	    	String result = "";


	    	try {
	    	    
	    	    is =  am.open(file);
	    	  
	    	    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
		        StringBuilder sb = new StringBuilder();
		        String line = null;
	    	    
		        while ((line = reader.readLine()) != null) {
		            sb.append(line + "\n");
		            System.out.println("LINE : "+line);
		        }
		      
		        result=sb.toString();
	    	    
	    	    reader.close();
	    	    is.close();
	    	}
	    	catch  (Exception e) {  
	    		 Log.e("log_tag", "Error converting result "+e.toString());
	    	}
			
	    	  //try parse the string to a JSON object
		    try{
		            jArray = new JSONObject(result);
		    }catch(JSONException e){
		        Log.e("log_tag", "Error parsing data "+e.toString());
		    }
		 
		    return jArray;
	    	
	    }
	
}

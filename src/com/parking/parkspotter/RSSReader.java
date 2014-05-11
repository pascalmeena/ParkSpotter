package com.parking.parkspotter;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RSSReader extends BaseActivity implements OnItemClickListener 
{
			
	//Database Variables
	private SQLiteDatabase db;
	private DBManager dbManager;
	
	// Service related variables
	Cursor cursor;
	ShowAdapter myAdapter;
	ListView listView;
	
	//add IntentFilter and ShowReciever class level variables to Show Activity
	ShowReceiver receiver;
	IntentFilter filter;

	String theCategory = null;
	Button addButton;

	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_reader);
     
   	 // Get the data from the database
        dbManager = new DBManager(this);
        db = dbManager.getWritableDatabase();
        listView = (ListView) findViewById(R.id.itemlist);
        listView.setTextFilterEnabled(true);
        
        receiver = new ShowReceiver();
        filter = new IntentFilter(BroadcastService.INTENT);
        
        /* retrieves data that is passed from other intents */
        Intent startingIntent = getIntent();
        
        if (startingIntent != null)
        {
        	Bundle b = startingIntent.getBundleExtra("android.intent.extra.INTENT");
        	if (b == null)
        	{
        		theCategory = "bad bundle?";
        	}
        	else
    		{
        		//Initialize the received category
        		theCategory = b.getString("category");
    		}

            //Toast.makeText(this, "From RSSReader_Category passed:" +theCategory, Toast.LENGTH_LONG).show();
	     }
	     else
	     {
	     	theCategory = "Information Not Found.";
	     
	     }
     //Back button onclick action
     Button backbutton = (Button) findViewById(R.id.previous_button);   
     backbutton.setOnClickListener(new Button.OnClickListener() 
     {
         public void onClick(View v) 
         {
         	finish();
         }
     });  
     
     /*
      * When a user doesn't select a parkade, just clicks next button
      */
     Button nextbutton = (Button) findViewById(R.id.next_button);
     nextbutton.setOnClickListener(new Button.OnClickListener() 
     {
         public void onClick(View v) 
         {
        	 Intent itemintent = new Intent(getApplicationContext(), ShowTheMap.class);
   	         Bundle b = new Bundle();
   	         System.out.println("Second pass of Category: " + theCategory);
   	         if(theCategory.isEmpty() || theCategory == null){
   	        	//Message or do nothing
   	        	 System.out.println("Select Category");
   	         }else{
   	        	 b.putString("category", theCategory);
   		 	     b.putString("click", "nextbutton");
   	   	         itemintent.putExtra("android.intent.extra.INTENT", b);
   	   	         itemintent.putExtras(b);
   	             startActivity(itemintent);   	        	 
   	         }   	     
         }
     });  
    }

    public void onDestroy(){
		 db.close();
		 super.onDestroy();
	 }
    
	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
			
			String query = "select * from "+DBManager.DB_TABLE+" where "+DBManager.C_DESCRIPTION+" = ?";
            //Toast.makeText(this, "parkade query result:" +query, Toast.LENGTH_LONG).show();
        	Cursor categoryCursor = db.rawQuery(query, new String[] { theCategory });
        	String categoryid = "";
            if (categoryCursor.moveToFirst()) {
            	  categoryid = categoryCursor.getString(categoryCursor.getColumnIndex(DBManager.C_SENDER));
      			System.out.println("category id:" +categoryid); 
      			
                } else {
                  System.out.println("NOTHING WAS FOUND");
                } 
        	 categoryCursor.close();
        	cursor = db.rawQuery("select * from "+DBManager.DB_TABLE_PARKADE+" where "+DBManager.CATEGORYID+" = ?", new String[] { categoryid });
        	//Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));
  			
			 startManagingCursor(cursor);
			 
			 myAdapter = new ShowAdapter(this, cursor);
			 listView.setAdapter(myAdapter);
		     
			listView.setOnItemClickListener(this);
		    listView.setSelection(0);
			 //cursor.close();       
		    //db.close();
			 registerReceiver(receiver, filter);
			 super.onResume();
	}
	
	@Override
	public void onPause() {
			 unregisterReceiver(receiver);
			 super.onPause();
	}	
	
  
    class ShowReceiver extends BroadcastReceiver{

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			cursor.requery();
			myAdapter.notifyDataSetChanged();

		}
	 }

    /*
     * When a user clicks on the parkade
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// When clicked, show a toast with the TextView text
	      Toast.makeText(getApplicationContext(), ((TextView) view.findViewById(R.id.parkade_title)).getText(),Toast.LENGTH_SHORT).show();
	      String title = (String) ((TextView) view.findViewById(R.id.parkade_title)).getText();
	      
	      //Create the bundle
	      Intent i = new Intent(this, ShowTheMap.class);
	      Bundle b = new Bundle();
	 	  b.putString("title", title);
	 	  b.putString("category", theCategory);
	 	  b.putString("click", "itemclick");
	 	  
     	  i.putExtra("android.intent.extra.INTENT", b);
     	  startActivity(i);
	}
	
	
	
	
    public void myClickHandler(View v) 
    {
       
        //get the row the clicked button is in
        LinearLayout vwParentRow = (LinearLayout)v.getParent();
         
        TextView title_child = (TextView)vwParentRow.getChildAt(0);
        Button btnChild = (Button)vwParentRow.getChildAt(3);
        btnChild.setText(title_child.getText());
       
        if(!title_child.getText().equals(null) || !(title_child.getText() == "")){
        	
			   addParkadeToLocalDB(title_child.getText().toString());
			   
			   btnChild.setText("Parkade Added!");
		       btnChild.setEnabled(false);
		       
			}else{
				Toast.makeText(this, "Null parkades are not allowed. Select another Parkade to proceed",
						Toast.LENGTH_LONG).show();
			}
           
    }

	/*
	 * This function adds a parkade into the Favorite Parkade table
	 * its parkade id, title, address, 
	 */
	
	public void addParkadeToLocalDB(String sender){

		String the_sender = sender;
		dbManager = new DBManager(this);
		
		try{
			//to get 
			ContentValues values = new ContentValues();
			
			Boolean exists = false;
			//Favorite parkade table is empty
			if(listParkades().length ==0)
			{
				//insert the parkade information
				db = dbManager.getWritableDatabase();
				values  = getParkadeValues(the_sender);
				db.replaceOrThrow(DBManager.DB_TABLE_FAVPARKADE, null, values);
				
			}
			else
			{
				//check to see if this parkade already exists
				for(int i=0; i<listParkades().length; i++)
				{
					if(listParkades()[i].contains(the_sender))
					{	
						exists = true;
						i = listParkades().length;	//it found, no need to continue
					}
					else
					{
						exists = false;
					}
				}
				
			
				//If the category exists abandon the insert all together
				if(!exists ==  true){
					Log.e("Bool check",exists.toString());
					db = dbManager.getWritableDatabase();
					values  = getParkadeValues(the_sender);
					db.replaceOrThrow(DBManager.DB_TABLE_FAVPARKADE, null, values);
	
					Toast.makeText(this, ""+the_sender+" Parkade is added in the Favorite Parkades.",
							Toast.LENGTH_LONG).show();
					//db.close();
				}else{
					//Display a message only when is not a default parkade
					
						Toast.makeText(this, ""+the_sender+" Parkade already exists in the Favorite Parkades.",
								Toast.LENGTH_LONG).show();
					
					}
				
				}
			
		}catch(Exception e){
			Toast.makeText(this, "ERROR:"+e,
					Toast.LENGTH_LONG).show();
		}
		
	}
    
	/*
	 * This function returns values of a single parkade as contentvalues from the database
	 */
	private ContentValues getParkadeValues(String title) {
		
		ContentValues parkingValues = new ContentValues();
		dbManager = new DBManager(this);
		db = dbManager.getWritableDatabase();
		
		//query to run
		Cursor cursor = db.rawQuery("select * from parkade where title = '"+title+"'", null);
		//get the results
		if(cursor.getCount()!=0)
		{
			if(cursor.moveToFirst())
			{
				do{
					parkingValues.put(DBManager.PARKD_ID, cursor.getString(cursor.getColumnIndex("nid")));
					parkingValues.put(DBManager.FP_TITLE, cursor.getString(cursor.getColumnIndex("title")));
					parkingValues.put(DBManager.FP_ADDRESS, cursor.getString(cursor.getColumnIndex("address")));
					parkingValues.put(DBManager.FP_CATEGORYID, cursor.getString(cursor.getColumnIndex("categoryid")));
					parkingValues.put(DBManager.FP_RSSFEEDURL, cursor.getString(cursor.getColumnIndex("rssfeedurl")));
					parkingValues.put(DBManager.FP_PARKINGCOUNTER, cursor.getString(cursor.getColumnIndex("parkingcounter")));
					
				}while(cursor.moveToNext());
			}
		}
		cursor.close();
		//db.close();
		return parkingValues;
	}

	/*
	 * Get list of parkades in Favorite parkade table
	 */
	public String[] listParkades()
	{
		dbManager = new DBManager(this);
		
		int columnIndex = 2; // Whichever column your float is in.
		    
		String[] myparkades = new String[]{};
		try{
			db = dbManager.getWritableDatabase();
			
			String query = "select * from favoriteparkade";
			cursor = db.rawQuery(query, null);

			myparkades = new String[cursor.getCount()];
			
			if (cursor.moveToFirst())
		    {                       
		        for (int i = 0; i < cursor.getCount(); i++)
		        {
		        	myparkades[i] = cursor.getString(columnIndex);
		            cursor.moveToNext();
		        }           
		    }
			cursor.close();
		    db.close();
			
	
		}catch(Exception e){
			Toast.makeText(this, "ERROR:"+e,
					Toast.LENGTH_LONG).show();
		}
		
		return myparkades;
	}
}

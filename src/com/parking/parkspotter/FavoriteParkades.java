/*
 * Author: Pascal & Usman
 */
package com.parking.parkspotter;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.TextView;

public class FavoriteParkades extends BaseActivity implements OnItemClickListener{
	
	//Database Variables
	private SQLiteDatabase db;
	private DBManager dbManager;
	
	// Service related variables
	Cursor cursor;
	FavoriteParkadeAdapter myAdapter;
	ListView listView; 
	String fromAllCategories = null;	
	Button deleteButton;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_parkades);
		
		// Get the data from the database
        dbManager = new DBManager(this);
        db = dbManager.getWritableDatabase();
        listView = (ListView) findViewById(R.id.itemlist2);
        listView.setTextFilterEnabled(true);
        
        //Back button onclick action
		Button backbutton = (Button) findViewById(R.id.previous_button);
		     backbutton.setOnClickListener(new Button.OnClickListener() 
		     {
		            public void onClick(View v) 
		            {
		            	finish();
		            }
		        });
		//Next button onclick action
	     Button nextbutton = (Button) findViewById(R.id.continue_button);
	     nextbutton.setOnClickListener(new Button.OnClickListener() 
	     {
	         public void onClick(View v) 
	         {
	        	 Intent itemintent = new Intent(getApplicationContext(), ShowTheMap.class);
	   	         Bundle b = new Bundle();
	   	         fromAllCategories = "fromall";
	   	        	 b.putString("fromall", fromAllCategories);
	   		 	     b.putString("click", "fromAll");
	   	   	         itemintent.putExtra("android.intent.extra.INTENT", b);
	   	   	         itemintent.putExtras(b);
	   	             startActivity(itemintent);    	        	 
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
		cursor = db.rawQuery("select * from "+DBManager.DB_TABLE_FAVPARKADE,null);
		
		 startManagingCursor(cursor);
		 
		 myAdapter = new FavoriteParkadeAdapter(this, cursor);
		 listView.setAdapter(myAdapter);
	     
		listView.setOnItemClickListener(this);
	    listView.setSelection(0);
		//db.close();          
	    
		 super.onResume();
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
        //Toast.makeText(this, "Button value: "+ title_child.getText().toString(), Toast.LENGTH_LONG).show();
        
        if(!title_child.getText().equals(null) || !(title_child.getText() == "")){
        	
        	deleteParkadeFromLocalDB(title_child.getText().toString());
			   
			btnChild.setText("Parkade Deleted!");
		    btnChild.setEnabled(false);
		    
			}else{
				Toast.makeText(this, "Null parkades are not allowed. Select another Parkade to proceed",
						Toast.LENGTH_LONG).show();
			}
           
    }
	/*
	 * Deletes a parkade from the database
	 */
	public void deleteParkadeFromLocalDB(String parkade) {

		dbManager = new DBManager(this);
		db = dbManager.getWritableDatabase();
		
		
		String[] whereClauseArgument = new String[1];
		whereClauseArgument[0] = parkade;

		db.delete(DBManager.DB_TABLE_FAVPARKADE, DBManager.FP_TITLE+"=?", whereClauseArgument);

		//db.close();
	}
	
}
/**
 * Author: Pascal & Usman
 * 
 * References:
 * 	
 * 	http://www.androidaspect.com/2013/02/android-radio-buttons-tutorial.html
 */
package com.parking.parkspotter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class Categories extends BaseActivity implements OnClickListener {

	static Context context;
	RadioGroup rg;
	LinearLayout selectCategory;
	static SharedPreferences myPrefs;
	public static final String PREFS_NAME = "GlobalValues";
	static String default_url;
	int rbid = 0;
	OnClickListener first_radio_listener;
	DBManager dbManager;
	SQLiteDatabase database;
	Cursor cursor;
	
	//This is an array containing hash objects with pair values key and value
	ArrayList <HashMap<String, String>> Categories = new ArrayList <HashMap<String, String>>();
			
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_list_layout);
        
        // Get application context for later use
        context = getApplicationContext();
        
        View secondButton = findViewById(R.id.continue_button);
        secondButton.setOnClickListener(this);
        
     // Creates the back button
     		Button backbutton = (Button) findViewById(R.id.previous_button);
             backbutton.setOnClickListener(new Button.OnClickListener() 
             {
                 public void onClick(View v) 
                 {
                 	finish();
                 }
             }); 
             
        View LinearLayout = findViewById(R.id.radioButtons);
        LinearLayout.setBackgroundColor(Color.TRANSPARENT);
        
        
        
    	rg = (RadioGroup) findViewById(R.id.category_radio_group);
		rg.setOnClickListener(this);

		myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		default_url = myPrefs.getString("url", "No url was entered");

		dbManager = new DBManager(this);
		
		populateList();
		//displayCategories(); 
		createRadioButton();
		
		//Adding an action for performing while user click on radio button  
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// checkedId is the RadioButton selected

				RadioButton clickedrb = (RadioButton) findViewById(checkedId);
				clickedrb.getTag();
				rbid = checkedId;
				
		//settings for global preferences to the all app, concantation of category to url
		myPrefs = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = myPrefs.edit();
			try {
				editor.putString(
						"url",
						default_url
								+ "?CATEGORY="
								+ URLEncoder.encode(clickedrb.getTag()
										.toString(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				editor.commit();

			}
		});
        
    }

    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		//Once the user selects continue, start the RSSReader class
		case R.id.continue_button:		
			Intent i1 = new Intent(this, RSSReader.class);
			String message = "start";
	         if(selectedCategory(message) != null && selectedCategory(message) != ""){
	        	 Bundle b = new Bundle();
	        	 message = "continueSuccess";
	        	 b.putString("category", selectedCategory(message));
	        	 i1.putExtra("android.intent.extra.INTENT", b);
	        	 startActivity(i1);
	         }else{
	        	 message="continueFail";
	        	 selectedCategory(message);
	         }
	         break;	
		}	
	}
	
	//this function returns a category name
	public String selectedCategory(String message) {
		rg = (RadioGroup) findViewById(R.id.category_radio_group);
		rbid = rg.getCheckedRadioButtonId();
		RadioButton rb = (RadioButton) findViewById(rbid);
		String theMessage = message;
		String category = null;
		
		if(rb != null){
			category = (String) rb.getTag();
			//rb.getTag();
			//Toast.makeText(this, "Category: " +category, Toast.LENGTH_LONG).show();
		}else{			    
			if(theMessage == "continueFail"){
				Toast.makeText(this, "Please select at least one category to proceed ", Toast.LENGTH_LONG).show();
			}
		}
		return category;
	}
	
	// Create a static method to show toasts (not presently used but included
		// as an example)
		
		public static void showToast(String text){
			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
		}
		
		/*
		 * This method creates radio buttons based on the list of categories \
		 * then adds to the view
		 */
private void createRadioButton() 
{		
		String[] RADIO_BUTTONS_LABELS = new String[] {};
		RADIO_BUTTONS_LABELS = listCategories();
			
		//create buttons in the radiogroup view
		rg = (RadioGroup) findViewById(R.id.category_radio_group);
		rg.removeAllViews();		  

		for (int i = 0; i < RADIO_BUTTONS_LABELS.length; i++) {
			RadioButton radioButton = new RadioButton(this);

			radioButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			radioButton.setText(RADIO_BUTTONS_LABELS[i]);
			radioButton.setTag(RADIO_BUTTONS_LABELS[i]);
			radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40);
			radioButton.setPadding(50, 0, 0, 10);
			radioButton.setId(i);
			radioButton.setTextColor(Color.WHITE);
			rg.addView(radioButton, i);				
		}
			rg.check(0);
		
}		

//Method for storing categories from json file to the dabatase
public void addCategoryToLocalDB(String sender, String category) {
		String the_sender = sender;
		String the_category = category;
		dbManager = new DBManager(this);
			
		try{
			database = dbManager.getWritableDatabase();
			ContentValues values = new ContentValues();
			
			values.put(DBManager.C_SENDER, the_sender);
			values.put(DBManager.C_DESCRIPTION, the_category);
				
			database.replaceOrThrow(DBManager.DB_TABLE, null, values);				
			database.close();
				
			}catch(Exception e){
				Toast.makeText(this, "ERROR:"+e,
						Toast.LENGTH_LONG).show();
			}
}
		
//returns an array list of all categories in the database
public String[] listCategories() 
{
		dbManager = new DBManager(this);
			
		int columnIndex = 2; // Whichever column your float is in.
			    
		//cursor = database.getAllMyFloats();
		String[] mycategories = new String[]{};
		try{
			database = dbManager.getWritableDatabase();
			
			String query = "select * from categories";
			cursor = database.rawQuery(query, null);
			mycategories = new String[cursor.getCount()];
				
			if (cursor.moveToFirst())
		    {                       
		        for (int i = 0; i < cursor.getCount(); i++)
		        {
		            mycategories[i] = cursor.getString(columnIndex);
		            cursor.moveToNext();
		        }           
		    }
			cursor.close();
		    database.close();
				
		
			}catch(Exception e){
				Toast.makeText(this, "ERROR:"+e,
						Toast.LENGTH_LONG).show();
			}
		return mycategories;
}
	
//This function parses the json file then adds them into a Hash array	
private void populateList() 
{
			
	JSONObject json = getJSONFromFile("categories.txt");
				 
	    try{
		    //Get the element that holds the earthquakes ( JSONArray )
		    JSONArray  nodes = json.getJSONArray("nodes");
		    //JSONObject node = nodes.optJSONArray("node");
		    //.out.println("The size of nodes: "+nodes.length());
						    
			System.out.println("SIZE:"+nodes.length());
						    
			//Loop the Array
			for(int i=0;i < nodes.length();i++){                      
						 
			HashMap<String, String> map = new HashMap<String, String>();
			JSONObject node = nodes.getJSONObject(i).getJSONObject("node");
			//JSONObject node = c.getJSONObject("node");
						            
			map.put("category", node.getString("name"));
			map.put("catid", Integer.toString(node.getInt("Term ID")));
			// Integer.toString(node.getInt("Term ID"))
			
			System.out.println("My name value:"+node.getString("name"));
			addCategoryToLocalDB(Integer.toString(node.getInt("Term ID")),node.getString("name"));				            
			// map.put("termid",  c.getString("Term ID"));
			Categories.add(map);
			//mylist.add(map);
			}
						 
						    
			}catch(JSONException e)        {
			      Log.e("log_tag", "Error parsing data "+e.toString());
			}			
}
		
//This function parses the json file and returns a jsonobject array
private JSONObject getJSONFromFile(String file){
	    	
	    	AssetManager am = this.getAssets();
	    	InputStream is;
	    	
	    	JSONObject jArray = null;
	    	String result = "";
	    	
	    	try {
	    	    //fis = openFileInput(filepath);
	    	    is =  am.open(file);
	    	    //DataInputStream dataIO = new DataInputStream(fis);
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

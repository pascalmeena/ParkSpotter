/*
 * Author: Pascal & Usman
 */
package com.parking.parkspotter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class BaseActivity extends Activity {
	
	static final String TAG = "BaseActivity-Helper";	
	//private Menu menu;
	String fromAllParkades = null;
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		//super.onCreateOptionsMenu(menu); 
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.appmenu, menu);
		//this.menu = menu;
		return true;
	}

	//Creates the app menu
	 @Override
		public boolean onOptionsItemSelected(MenuItem item) {
	    	
		    switch((item.getItemId()))
		    {		    
			    case R.id.itemFavoriteParkades:{			    
			    	Intent view =  new Intent(this, FavoriteParkades.class);
			    	startActivity(view);
			    	break;
			    }
			    case R.id.itemViewCategories:
			    {
			    	Intent view =  new Intent(this, Categories.class);
			    	startActivity(view);
			    	break;
			    }
			    
			    case R.id.Home:
			    {
			    	Intent view =  new Intent(this, MainActivity.class);
			    	startActivity(view);
			    	break;
			    }
			    
			    case R.id.itemShowMap:
			    {
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
		    return true;
		}
	 
}


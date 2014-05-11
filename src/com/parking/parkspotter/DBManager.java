/*
 * Author: Usman & Pascal
 * Reference: http://developer.android.com/reference/android/database/sqlite/package-summary.html
 */

package com.parking.parkspotter;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBManager extends SQLiteOpenHelper{

	
	static final String TAG = "DBManager";
	static final String DB_NAME = "parkspotterDatabase.db";
	static final int DB_VERSION = 7;
	
	//Category Table
	static final String DB_TABLE = "categories";
	static final String C_ID = BaseColumns._ID;
	static final String C_DESCRIPTION = "category_description";
	static final String C_SENDER = "sender";
	
	
	//Parkade Table
		static final String DB_TABLE_PARKADE = "parkade";
		static final String P_ID = BaseColumns._ID;
		static final String PNID = "nid";
		static final String TITLE = "title";
		static final String ADDRESS = "address";
		static final String CATEGORYID = "categoryid";
		static final String RSSFEEDURL = "rssfeedurl";
		static final String PARKINGCOUNTER = "parkingcounter";
	//Parkade Entrance
		//For later implementation if we want to add an image to a parkade or different entrances
		static final String DB_TABLE_ENTRANCE = "entrance";
		static final String E_ID = BaseColumns._ID;
		static final String E_NID = "nid";
		static final String IMAGEPATH = "imagepath";
		static final String LATITUDE = "latitude";
		static final String LONGITUDE = "longitude";
		static final String PARKADEID = "parkadeid";
		
		
    //Parkade RSSFeed 
		
		static final String DB_TABLE_FAVPARKADE = "favoriteparkade";
		static final String PK_ID = BaseColumns._ID;
		static final String PARKD_ID = "parkadedid";
		static final String FP_TITLE = "title";
		static final String FP_ADDRESS = "address";
		static final String FP_CATEGORYID = "categoryid";
		static final String FP_RSSFEEDURL = "rssfeedurl";
		static final String FP_PARKINGCOUNTER = "parkingcounter";
	
	
	Context context;
	
	public DBManager(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		//Create Categories
		String categories_table = "create table "+DB_TABLE+ "("+C_ID +" int, " +C_SENDER+ " text primary key, "+C_DESCRIPTION+" text);";
		
		//Create Parkade Table 
		String parkade_table = "create table "+DB_TABLE_PARKADE+ "("+P_ID+" int, " + PNID +" int primary key, "+TITLE+" text, "+ADDRESS+" text, "+CATEGORYID+" int, "+RSSFEEDURL+" text, "+PARKINGCOUNTER+" text);";		
		
		
		//Create Parkade Entrance Table
		
		String entrance_table = "create table "+DB_TABLE_ENTRANCE+"("+E_ID+" int primary key, "+E_NID+" int, "+IMAGEPATH+" text, "+LATITUDE+" real, "+LONGITUDE+" real, "+PARKADEID+" int);";
		
			
		//Create Favorite Parkade Table
		
		String favoriteparkade_table = "create table "+DB_TABLE_FAVPARKADE+"("+PK_ID+" int primary key, "+PARKD_ID+" int, "+FP_TITLE+" text, "+FP_ADDRESS+" text, "+FP_CATEGORYID+" int, "+FP_RSSFEEDURL+" text, "+PARKINGCOUNTER+" int);";
		
		
		Log.d(TAG, categories_table);
		db.execSQL(categories_table);
		
		Log.d(TAG, parkade_table);
		db.execSQL(parkade_table);
		
		Log.d(TAG, entrance_table);
		db.execSQL(entrance_table);
		
		Log.d(TAG, favoriteparkade_table);
		db.execSQL(favoriteparkade_table);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		
		db.execSQL("drop table if exists " + DB_TABLE);
		
        db.execSQL("drop table if exists " + DB_TABLE_ENTRANCE);
		
		db.execSQL("drop table if exists " + DB_TABLE_PARKADE);
				
		db.execSQL("drop table if exists " + DB_TABLE_FAVPARKADE);
		
		Log.d(TAG, "Database upgraded");
		onCreate(db);
	}

}


/*
 * Author: Pascal & Usman
 */
package com.parking.parkspotter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

//This class is to handle the display of the parkades
public class FavoriteParkadeAdapter extends SimpleCursorAdapter {
	
	static final String[] FROM = {DBManager.TITLE, DBManager.ADDRESS, DBManager.PARKINGCOUNTER};
	static final int[] TO = {R.id.parkade_title, R.id.parkade_address, R.id.parkade_counter};
	
	@SuppressWarnings("deprecation")
	public FavoriteParkadeAdapter(Context context, Cursor cursor){
		
		super(context, R.layout.row_favorite_parkades, cursor, FROM, TO);
		
	}
	
	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		
		super.bindView(row, context, cursor);
		TextView tvTitle = (TextView)row.findViewById(R.id.parkade_title);
		
	}
}

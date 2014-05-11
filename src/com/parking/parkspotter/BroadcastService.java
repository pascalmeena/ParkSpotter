/*	
 * Author: Pascal & Usman
 * Reference: http://www.websmithing.com/2011/02/01/how-to-update-the-ui-in-an-android-activity-using-data-from-a-background-service/
 *
 */

/*This class is used just for the purposes of the app menu, most of the methods are moved to the MainActivity
 *  Because we had to use the Async class to run internet connections on another thread
 *  We will have to remodify this class
*/
package com.parking.parkspotter;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class BroadcastService  extends Service {
	
	//New Service Variables
	static final String TAG = "BroadcastService";
    public static boolean bRun = false;
    public static final String INTENT = "NEW_DATA";
	public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
	

	Intent intent;
	int counter = 0;
	
	//My Services variables
	String default_url;
	static SharedPreferences myPrefs;
	//Insert checks
	Boolean isParkadeInsertedAtleastOnce = false;   
    
    @Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG,"============================================onCreate ============================================================");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}

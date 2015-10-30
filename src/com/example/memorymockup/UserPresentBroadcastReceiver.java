package com.example.memorymockup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class UserPresentBroadcastReceiver extends BroadcastReceiver {
	public SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {

        /*Sent when the user is present after 
         * device wakes up (e.g when the keyguard is gone)
         * */
 
        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
        	sharedPreferences = context.getSharedPreferences(ListActivity.AUTHENTICATORFILE, Context.MODE_PRIVATE);
        	String authenticatorName = sharedPreferences.getString(ListActivity.AUTHENTICATORNAME, "");
        	String authenticatorMode = sharedPreferences.getString(ListActivity.AUTHENTICATORMODE, "");
        	String authenticatorInputMethod = sharedPreferences.getString(ListActivity.AUTHENTICATORINPUTMETHOD, "");
        	
        	if (!authenticatorName.equals("")) {
	        	intent = new Intent(context, MainActivity.class);
	        	intent.putExtra(StartActivity.MODE, authenticatorMode);
				intent.putExtra(SetupActivity.TASK, SetupActivity.AUTHENTICATE);
				intent.putExtra(ListActivity.PATHNAME, authenticatorName);
				intent.putExtra(SetupActivity.INPUTMETHOD, authenticatorInputMethod);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            context.startActivity(intent);
        	}
        }
    }

}
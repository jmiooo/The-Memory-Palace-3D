package com.example.memorymockup;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ListActivity extends Activity {
	public static String PATHNAME = "path-name";
	public static String AUTHENTICATORNAME = "authenticator-name";
	public static String AUTHENTICATORMODE = "authenticator-mode";
	public static String AUTHENTICATORINPUTMETHOD = "authenticator-input-method";
	
	public static String IMPOSSIBLEFILE = "impossible_paths";
	public static String NONIMPOSSIBLEFILE = "nonimpossible_paths";
	public static String AUTHENTICATORFILE = "authenticator_paths";
	
	public static String PATHLIST = "path_list";
	
	public Intent intent;
	public String username;
	public String mode;
	public String task;
	public String inputMethod;
	
	public PathAdapter listAdapter;
	public ListView listView;
	
	public SharedPreferences sharedPreferences;
	public Editor editor;
	public SharedPreferences authenticatorSharedPreferences;
	public String authenticatorName;
	public String authenticatorMode;
	public Editor authenticatorEditor;
	public Gson gson;
	
	public int pathIndex;

	public class PathAdapter extends ArrayAdapter<String> {
		 
	    // Keeping the currently selected item
		private List<String> data;
	 
	    public PathAdapter(Context context, int textViewResourceId, List<String> data) {
	        super(context, textViewResourceId, data);
	        this.data = data;
	    }
	    
	    public List<String> getData() {
	    	return this.data;
	    }
	    
	    public String getDataAtPosition(int i) {
	    	return this.data.get(i);
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		intent = getIntent();
		username = intent.getStringExtra(IDActivity.ID);
		mode = intent.getStringExtra(StartActivity.MODE);
		task = intent.getStringExtra(SetupActivity.TASK);
		inputMethod = SetupActivity.SLOW;
		
		if (task.equals(SetupActivity.DELETE)) {
			setContentView(R.layout.activity_list_delete);
		}
		else {
			setContentView(R.layout.activity_list);
		}
	}
	
	protected void onStart() {
		super.onStart();
		
		String fileName = "";
		if (mode.equals(StartActivity.IMPOSSIBLE)) {
			fileName = username + "_" + IMPOSSIBLEFILE;
		}
		else if (mode.equals(StartActivity.NONIMPOSSIBLE)) {
			fileName = username + "_" + NONIMPOSSIBLEFILE;
		}

		sharedPreferences = this.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		
		authenticatorSharedPreferences = this.getSharedPreferences(username + "_" + AUTHENTICATORFILE, Context.MODE_PRIVATE);
		authenticatorEditor = authenticatorSharedPreferences.edit();
		authenticatorName = authenticatorSharedPreferences.getString(ListActivity.AUTHENTICATORNAME, "");
    	authenticatorMode = authenticatorSharedPreferences.getString(ListActivity.AUTHENTICATORMODE, "");
		
    	gson = new Gson();
		
		List<String> pathList = new ArrayList<String>();
		if (sharedPreferences.contains(PATHLIST)) {
			String json = sharedPreferences.getString(PATHLIST, "");
			Type type = new TypeToken<List<String>>(){}.getType();
			pathList = gson.fromJson(json, type);
		}
		
		listView = (ListView) findViewById(R.id.save_list);
	    listAdapter = new PathAdapter(this, R.layout.setup_row, pathList);  
	    listView.setAdapter(listAdapter);
	    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    
	    listView.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    		listView.setItemChecked(position, true);
	    		pathIndex = position;
	    		
	    		if (task.equals(SetupActivity.MATCH)) {
	    			startMatch(view);
	    		}
	    		else if (task.equals(SetupActivity.DELETE)) {
	    			deletePath(view);
	    		}
	    		else if (task.equals(SetupActivity.SELECT)) {
	    			selectPath(view);
	    		}
	    		else {
	    		}
	    	}
	    });
	    
	    pathIndex = -1;
	}
	
	public void onInputMethodSwitchPressed(View view) {
		boolean fast = ((Switch) view).isChecked();
		
		if (fast) {
			inputMethod = SetupActivity.FAST;
		}
		else {
			inputMethod = SetupActivity.SLOW;
		}
	}
	
	public void startMatch(View view) {
		if (pathIndex >= 0) {
			intent = new Intent(this, MainActivity.class);
			intent.putExtra(IDActivity.ID, username);
			intent.putExtra(StartActivity.MODE, mode);
			intent.putExtra(SetupActivity.TASK, SetupActivity.MATCH);
			intent.putExtra(PATHNAME, listAdapter.getDataAtPosition(pathIndex));
			intent.putExtra(SetupActivity.INPUTMETHOD, inputMethod);
			
			startActivity(intent);
		}
	}
	
	
	public void startEntry(View view) {
		intent = new Intent(this, MainActivity.class);
		intent.putExtra(IDActivity.ID, username);
		intent.putExtra(StartActivity.MODE, mode);
		intent.putExtra(SetupActivity.TASK, SetupActivity.ENTRY);
		intent.putExtra(SetupActivity.INPUTMETHOD, SetupActivity.SLOW);
		
		startActivity(intent);
	}
	
	public void deletePath(View view) {
		if (pathIndex >= 0) {
			List<String> pathList = listAdapter.getData();
			String pathName = pathList.remove(pathIndex);
			editor.putString(PATHLIST, gson.toJson(pathList));
			editor.remove(pathName);
			editor.commit();
        	
        	if (pathName.equals(authenticatorName) && mode.equals(authenticatorMode)) {
        		authenticatorEditor.putString(AUTHENTICATORNAME, "");
        		authenticatorEditor.putString(AUTHENTICATORMODE, "");
        		authenticatorEditor.putString(AUTHENTICATORINPUTMETHOD, "");
        		authenticatorEditor.commit();
        	}
        	
			onStart();
		}
	}
	
	// Select to be the authenticator path
	public void selectPath(View view) {
		if (pathIndex >= 0) {
			intent = new Intent(this, MainActivity.class);
			intent.putExtra(IDActivity.ID, username);
			intent.putExtra(StartActivity.MODE, mode);
			intent.putExtra(SetupActivity.TASK, SetupActivity.SELECT);
			intent.putExtra(PATHNAME, listAdapter.getDataAtPosition(pathIndex));
			intent.putExtra(SetupActivity.INPUTMETHOD, inputMethod);
			
			startActivity(intent);
		}
	}
}

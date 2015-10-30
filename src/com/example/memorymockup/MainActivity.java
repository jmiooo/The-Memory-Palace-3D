package com.example.memorymockup;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity {
	public static final int LEFT = 0;
	public static final int UP = 1;
	public static final int RIGHT = 2;
	public static final int DOWN = 3;
	
	public static final int SWIPELENGTH = 200;
	
	//public static String SAVE_KEY = "memory-view";
	
	public static final String[] promptLocations =
		{ "kitchen", "living room", "bathroom", "bedroom", "front door" };
	public static final int howManyPrompts = 3;
	
	public String[] allPrompts;
	
	public Intent intent;
	public String username;
	public String mode;
	public String task;
	public String pathName;
	public String inputMethod;
	
	public MemoryView memoryView;
	public MapView mapView;
	public ViewFlipper minimapViewFlipper;
	public HorizontalScrollView minimapScroll;
	public LinearLayout minimap;
	public TextView statusText;
	public TextView promptText;
	public EditText editText;
	//public RandomPlayer randomPlayer;
	
	public String[] prompts;
	public int promptIndex;
	
	public SharedPreferences sharedPreferences;
	public Editor editor;
	public SharedPreferences authenticatorSharedPreferences;
	public Editor authenticatorEditor;
	public Gson gson;
	
	public boolean inTransition;
	public boolean inBlur;
	public boolean inShow;

	public boolean inSwipe;
	public int[] swipeStart;
	
	/*public class RandomPlayer {
		private boolean memoryInited, mapInited, mainInited;
		private boolean firstShowDone;
		private MainActivity mainActivity;
		private Random random;
		private int[] path;
		private int index; //Which leg of the path are we to show next
		
		public RandomPlayer(Context context) {
			memoryInited = false;
			mapInited = false;
			memoryInited = false;
			
			firstShowDone = false;
			
			mainActivity = (MainActivity) context;
			
			random = new Random();
			
			int length = random.nextInt(3) + 6;
			path = new int[length];
			for (int i = 0; i < length; i++) {
				path[i] = random.nextInt(4);
			}
			
			index = 0;
		}
		
		public void showCallback() {
			if (index != path.length) {
				mainActivity.memoryView.tryMoveSlow(path[index]);
				index += 1;
			}
			else {
				List<Integer> pathToFollow = mainActivity.memoryView.roomManager.getPath();
				List<Integer> newPathToFollow = new ArrayList<Integer> ();
				
				for (int i = 0; i < pathToFollow.size(); i++)
					newPathToFollow.add(pathToFollow.get(i));
				
				mainActivity.memoryView.pathToFollow = newPathToFollow;
						
				index = 0;
				mainActivity.inShow = false;
			}
		}
		
		public void show() {
			mainActivity.inShow = true;
			showCallback();
		}
		
		public void doFirstShow () {
			if (memoryInited && mapInited && mainInited) {
				firstShowDone = true;
				show();
			}
		}
		
		public void updateMemoryInited (boolean memoryInited) {
			this.memoryInited = memoryInited;
			if (!firstShowDone) doFirstShow();
		}
		
		public void updateMapInited (boolean mapInited) {
			this.mapInited = mapInited;
			if (!firstShowDone) doFirstShow();
		}
		
		public void updateMainInited (boolean mainInited) {
			this.mainInited = mainInited;
			if (!firstShowDone) doFirstShow();
		}
	}*/
	
	public String[] generatePrompts() {
		String[] allPromptsTemp = new String[allPrompts.length];
		for (int i = 0; i < allPrompts.length; i++) {
			allPromptsTemp[i] = allPrompts[i];
		}
		
		Random random = new Random();
		String[] prompts = new String[howManyPrompts];
		int endIndex = allPromptsTemp.length;
		
		for (int i = 0; i < howManyPrompts; i++){
			int index = random.nextInt(endIndex);
			prompts[i] = allPromptsTemp[index];
			allPromptsTemp[index] = allPromptsTemp[endIndex - 1];
			endIndex -= 1;
		}
		
		return prompts;
	}
	
	public void setPromptText () {
		if (prompts.length > 0)
			promptText.setText(prompts[promptIndex]);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		allPrompts = new String[promptLocations.length * (promptLocations.length - 1) / 2];
		int tempIndex = 0;
		for (int i = 0; i < promptLocations.length; i++) {
			for (int j = i + 1; j < promptLocations.length; j++) {
				allPrompts[tempIndex] = "Draw the path from your "
										+ promptLocations[i]
										+ " to your "
										+ promptLocations[j]
										+ ".";
				tempIndex += 1;
			}
		}
		
		intent = getIntent();
		username = intent.getStringExtra(IDActivity.ID);
		mode = intent.getStringExtra(StartActivity.MODE);
		task = intent.getStringExtra(SetupActivity.TASK);
		inputMethod = intent.getStringExtra(SetupActivity.INPUTMETHOD);
		System.out.println(inputMethod);
		
		if (task.equals(SetupActivity.MATCH)) {
			setContentView(R.layout.activity_main_match);
			statusText = (TextView) findViewById(R.id.statusText);
			promptText = (TextView) findViewById(R.id.promptText);
		}
		else if (task.equals(SetupActivity.ENTRY)) {
			setContentView(R.layout.activity_main_entry);
			editText = (EditText) findViewById(R.id.enter_path_name);
		}
		else if (task.equals(SetupActivity.SELECT)) {
			setContentView(R.layout.activity_main_match);
			statusText = (TextView) findViewById(R.id.statusText);
			promptText = (TextView) findViewById(R.id.promptText);
		}
		else if (task.equals(SetupActivity.AUTHENTICATE)) {
			setContentView(R.layout.activity_main_authenticate);
			statusText = (TextView) findViewById(R.id.statusText);
			promptText = (TextView) findViewById(R.id.promptText);
		}
		else if (task.equals(SetupActivity.RANDOM)) {
			setContentView(R.layout.activity_main_random);
			//randomPlayer = new RandomPlayer(this);
			statusText = (TextView) findViewById(R.id.statusText);
		}
		else if (task.equals(SetupActivity.PROMPT)) {
			setContentView(R.layout.activity_main_prompt);
			promptText = (TextView) findViewById(R.id.promptText);
			editText = (EditText) findViewById(R.id.enter_path_name);
		}
		
		memoryView = (MemoryView) findViewById(R.id.memory);
		if (memoryView.getViewTreeObserver().isAlive()) {
		    memoryView.getViewTreeObserver().addOnGlobalLayoutListener( 
		    	    new OnGlobalLayoutListener(){
		    	        @Override
		    	        public void onGlobalLayout() {
		    	            // gets called after layout has been done
		    	        	if (memoryView.getViewTreeObserver().isAlive()) {
		    	        		memoryView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		    	        	}
		    	            //memoryView.initDrawables();
		    	            if (task.equals(SetupActivity.RANDOM)) {
		    	    			//randomPlayer.updateMemoryInited(true);
		    	    		}
		    	        }
		    	});
		}
		
		memoryView.setMode(mode);
		memoryView.setTask(task);
		
		mapView = (MapView) findViewById(R.id.map);
		if (mapView.getViewTreeObserver().isAlive()) {
		    mapView.getViewTreeObserver().addOnGlobalLayoutListener( 
		    	    new OnGlobalLayoutListener(){
		    	        @Override
		    	        public void onGlobalLayout() {
		    	            // gets called after layout has been done
		    	        	if (mapView.getViewTreeObserver().isAlive()) {
		    	        		mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		    	        	}
		    	            mapView.initDrawables();
		    	            if (task.equals(SetupActivity.RANDOM)) {
		    	    			//randomPlayer.updateMapInited(true);
		    	    		} 
		    	        }
		    	});
		}
		
		minimapViewFlipper = (ViewFlipper) findViewById(R.id.minimapViewFlipper);
		minimapScroll = (HorizontalScrollView) findViewById(R.id.minimapScroll);
	
		minimap = (LinearLayout) findViewById(R.id.minimap);
		
		String fileName = "";
		if (mode.equals(StartActivity.IMPOSSIBLE)) {
			fileName = username + "_" + ListActivity.IMPOSSIBLEFILE;
		}
		else if (mode.equals(StartActivity.NONIMPOSSIBLE)) {
			fileName = username + "_" + ListActivity.NONIMPOSSIBLEFILE;
		}
		sharedPreferences = this.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		gson = new Gson();
		
		pathName = "";
		prompts = new String[] {};
		promptIndex = 0;
		
		if (task.equals(SetupActivity.MATCH) || 
			task.equals(SetupActivity.AUTHENTICATE) ||
			task.equals(SetupActivity.SELECT)) {
			pathName = intent.getStringExtra(ListActivity.PATHNAME);
			memoryView.setRoomManager(sharedPreferences.getString(pathName, ""));
			prompts = memoryView.roomManager.getPrompts();
			setPromptText();
		}
		
		if (task.equals(SetupActivity.SELECT)) {
			authenticatorSharedPreferences = this.getSharedPreferences(
					username + "_" + ListActivity.AUTHENTICATORFILE, Context.MODE_PRIVATE);
			authenticatorEditor = authenticatorSharedPreferences.edit();
		}
		
		if (task.equals(SetupActivity.PROMPT)) {
			prompts = generatePrompts();
			memoryView.roomManager.setPrompts(prompts);
			setPromptText();
			memoryView.showPromptText();
		}
		
		inTransition = false;
		inBlur = false;
		inShow = false;
		
		inSwipe = false;
		swipeStart = new int[2];
		
		if (task.equals(SetupActivity.RANDOM)) {
			//inShow = true;
			//randomPlayer.updateMainInited(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void matchPath(View view) {
		if (!inTransition) {
			boolean result = memoryView.matchPath();
			
			if (result) {
				if (task.equals(SetupActivity.AUTHENTICATE)) {
					exit(view);
				}
				if (task.equals(SetupActivity.SELECT)) {
					authenticatorEditor.putString(ListActivity.AUTHENTICATORNAME, pathName);
					authenticatorEditor.putString(ListActivity.AUTHENTICATORMODE, mode);
					authenticatorEditor.putString(ListActivity.AUTHENTICATORINPUTMETHOD, inputMethod);
					authenticatorEditor.commit();
					finish();
				}
			}
		}
	}
	
	public void enterPath(View view) {
		if (!inTransition) {
			if (task.equals(SetupActivity.PROMPT) && promptIndex !=  howManyPrompts - 1) {
				promptIndex += 1;
				setPromptText();
				memoryView.showPromptText();
			}
			else {
				String pathName = editText.getText().toString();
				
				if (!pathName.equals("") && !pathName.contains("_")) {
					String roomInfo = memoryView.getRoomInfoStringified();
					System.out.println(roomInfo);
					editor.putString(pathName, roomInfo);
					
					List<String> pathList = new ArrayList<String>();
					if (sharedPreferences.contains(ListActivity.PATHLIST)) {
						String json = sharedPreferences.getString(ListActivity.PATHLIST, "");
						Type type = new TypeToken<List<String>>(){}.getType();
						pathList = gson.fromJson(json, type);
					}
					pathList.add(pathName);
					editor.putString(ListActivity.PATHLIST, gson.toJson(pathList));
					editor.commit();
					this.finish();
				}
			}
		}
	}
	
	public void resetPath(View view) {
		if (!inTransition) {
			memoryView.resetPath();
			promptIndex = 0;
			setPromptText();
		}
	}
	
	public void showPath(View view) {
		if (!inTransition) {
			memoryView.resetPath();
			//randomPlayer.show();
		}
	}
	
	public void showPromptForMatching(View view) {
		if (minimapViewFlipper.getDisplayedChild() == 2) {
			promptIndex = (promptIndex + 1) % prompts.length;
			setPromptText();
		}
		else {
			memoryView.showPromptText();
		}
	}
	
	public void showPromptForEntering(View view) {
		if (minimapViewFlipper.getDisplayedChild() == 2)
			memoryView.showMinimap();
		else {
			memoryView.showPromptText();
		}
	}
	
	public void exit(View view) {
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}
	
	/*public void processTouchEventSlow(MotionEvent event) {
		int x = (int)event.getX();
	    int y = (int)event.getY();

        switch (event.getAction()) {
        	case MotionEvent.ACTION_DOWN:
                inSwipe = true;
                swipeStart[0] = x;
                swipeStart[1] = y;
                break;
        	case MotionEvent.ACTION_MOVE:
                break;
        	case MotionEvent.ACTION_UP:
        		if (inSwipe) {
	            	int deltaX = x - swipeStart[0];
	            	int deltaY = y - swipeStart[1];
	            	
	            	if (Math.abs(deltaX) <= 100 &&
	            		Math.abs(deltaY) <= 100) {
	            		memoryView.tryMoveSlow(swipeStart[0], swipeStart[1] - minimapViewFlipper.getHeight());
	            	}
        		}
        		
            	inSwipe = false;
                break;
            default:
            	inSwipe = false;
                break;
        }
	}*/
	
	public void processTouchEventFast(MotionEvent event) {
		int x = (int)event.getX();
	    int y = (int)event.getY();

        switch (event.getAction()) {
        	case MotionEvent.ACTION_DOWN:
                inSwipe = true;
                swipeStart[0] = x;
                swipeStart[1] = y;
                break;
        	case MotionEvent.ACTION_MOVE:
        		if (inSwipe) {
	            	int deltaX = x - swipeStart[0];
	            	int deltaY = y - swipeStart[1];
	            	
	       
	            	
	            	if (Math.abs(deltaX) >= SWIPELENGTH ||
	            		Math.abs(deltaY) >= SWIPELENGTH) {
	            		if (deltaX <= -SWIPELENGTH)
	            			memoryView.tryMoveFast(LEFT);
	            		else if (deltaY <= -SWIPELENGTH)
	            			memoryView.tryMoveFast(UP);
	            		else if (deltaX >= SWIPELENGTH)
	            			memoryView.tryMoveFast(RIGHT);
	            		else if (deltaY >= SWIPELENGTH)
	            			memoryView.tryMoveFast(DOWN);
	            		
	            		memoryView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
	            										 HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
	            		
	            		swipeStart[0] = x;
	            		swipeStart[1] = y;
	            	}
        		}
                break;
        	case MotionEvent.ACTION_UP:
            	inSwipe = false;
                break;
            default:
            	inSwipe = false;
                break;
        }
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		// Method to get motion with swipes instead of presses
		
		if (task.equals(SetupActivity.ENTRY) && editText.isFocused()) {
			editText.clearFocus();
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		}
		
		if (!inTransition) {
			processTouchEventFast(event);
			/*if (inputMethod.equals(SetupActivity.SLOW)) {
				processTouchEventSlow(event);
			}
			else if (inputMethod.equals(SetupActivity.FAST)) {
				// Fast is actually both fast and slow
				if (!inBlur) {
					processTouchEventSlow(event);
					processTouchEventFast(event);
				}
				else {
					processTouchEventFast(event);
				}
			}*/
		}

        return super.onTouchEvent(event);
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (task.equals(SetupActivity.AUTHENTICATE)) {
			return true;
		}
	    
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (task.equals(SetupActivity.AUTHENTICATE)) {
			return true;
		}
		
	    return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    /*for (int i = 0; i < memoryView.roomSprites.length; i++)
	    	memoryView.roomSprites[i].bitmap.recycle();
	    for (int i = 0; i < memoryView.doorSprites.length; i++)
	    	memoryView.doorSprites[i].bitmap.recycle();
	    memoryView.playerSprite.bitmap.recycle();
	    Runtime.getRuntime().gc();*/
	}
}

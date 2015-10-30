package com.example.memorymockup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class IDActivity extends Activity {
	public static String ID = "id";
	
	public Intent intent;
	
	public EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_id);
		editText = (EditText) findViewById(R.id.enter_id);
	}
	
	public void startStart(View view) {
		String username = editText.getText().toString();
		
		if (!username.equals("") && !username.contains("_")) {
			intent = new Intent(this, StartActivity.class);
			intent.putExtra(ID, username);
			startActivity(intent);
		}
	}
	
}
package com.example.androcommandline;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final int BUFF_LEN = 4096;
	EditText commandWindow;
	TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		commandWindow = (EditText) findViewById(R.id.editText1);
		tv = (TextView) findViewById(R.id.textView1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClickExecute(View v){
		String command  = commandWindow.getText().toString();
		try {
			tv.setText(doCmd(command));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	String doCmd(String command) throws IOException{
		Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "system/bin/sh"});
		DataOutputStream stdin = new DataOutputStream(p.getOutputStream());
		
		//from here all commands are executed with su permissions
		stdin.writeBytes(command+"\n"); // \n executes the command
		InputStream stdout = p.getInputStream();
		byte[] buffer = new byte[BUFF_LEN];
				stdout.read(buffer);
		return new String(buffer);
	}

}

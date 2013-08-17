package pl.xt.jokii.inventory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class NewCategoryDialog extends Activity{
	private EditText mEditTextName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_dialog);
		
		mEditTextName = (EditText) findViewById(R.id.editTextNewCategoryName);
	}
	
	public void onClickOK(View v){
		String name = mEditTextName.getText().toString();
		
		if(TextUtils.isEmpty(name)){
			Toast.makeText(getApplicationContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
		}else{
			Intent data = new Intent();
			data.putExtra("new_category_name", name);
			setResult(RESULT_OK, data);
			finish();
		}
	}
	
	public void onClickCancel(View v){
		setResult(RESULT_CANCELED);
		finish();
	}
}

package pl.xt.jokii.inventory;

import java.util.ArrayList;

import pl.xt.jokii.db.DbUtils;
import pl.xt.jokii.db.InventoryEntry;
import pl.xt.jokii.db.InventoryResultsSet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddEditActivity extends Activity{
	private EditText mEditTextAmount;
	private Spinner  mSpinnerCategory;
	private EditText mEditTextName;
	private int mAmount = 0;
	private int mSelectedCategoryPos = 0;
	private InventoryResultsSet mResultSet;
	private ArrayList<String> mCategories;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.add_edit_entry);
	    
	    mEditTextAmount  = (EditText) findViewById(R.id.editTextAmount);
	    mSpinnerCategory = (Spinner)  findViewById(R.id.spinnerCategory);
	    mEditTextName    = (EditText) findViewById(R.id.editTextName);
	    
	    mResultSet = DbUtils.retrieveResultSet(getApplicationContext());
	    mCategories = (ArrayList<String>) mResultSet.getCategories().clone();
	    mCategories.add("Add new category");
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, mCategories);
	    mSpinnerCategory.setAdapter(adapter);
	    mSpinnerCategory.setOnItemSelectedListener(onCategorySelectedListener);
	}
	
	OnItemSelectedListener onCategorySelectedListener = new OnItemSelectedListener() {

		@Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
			if(position == (mCategories.size() - 1)){			// If last element was chosen "Add new Entry"
//				Toast.makeText(getApplicationContext(), "New entry", Toast.LENGTH_SHORT).show();
				startActivityForResult(new Intent(getApplicationContext(), NewCategoryDialog.class), R.id.add_new_category_req_id);
			}else{
				mSelectedCategoryPos = position;
			}
        }

		@Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
	};
	
	public void onClickPlusAddEdit(View v){
		mAmount++;
		mEditTextAmount.setText(String.valueOf(mAmount));
	}
	
	public void onClickMinusAddEdit(View v){
		mAmount = (mAmount > 0) ? (mAmount - 1) : mAmount;
		mEditTextAmount.setText(String.valueOf(mAmount));
	}
	
	public void onClickCancelAddEdit(View v){
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onClickSaveAddEdit(View v){
		String category = mCategories.get(mSpinnerCategory.getSelectedItemPosition());
		String name 	= mEditTextName.getText().toString();
		String amount 	= mEditTextAmount.getText().toString();

		if(TextUtils.isEmpty(name)){
			Toast.makeText(getApplicationContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
		}else{
//			Toast.makeText(getApplicationContext(), category + ", n: " + name + ", a: " + amount, Toast.LENGTH_SHORT).show();
			
			InventoryEntry entry = new InventoryEntry();
			entry.setCategory(category);
			entry.setName(name);
			entry.setAmount(Integer.valueOf(amount));
			DbUtils.insertEntryDB(getApplicationContext(), entry);
			
			setResult(RESULT_OK);
			finish();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    if(requestCode == R.id.add_new_category_req_id){
	    	if(resultCode == RESULT_OK){
	    		String newCategoryName = data.getStringExtra("new_category_name");
	    		mCategories = (ArrayList<String>) mResultSet.getCategories().clone();
	    	    mCategories.add(newCategoryName);
	    	    mCategories.add("Add new category");
	    	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, mCategories);
	    	    mSpinnerCategory.setAdapter(adapter);
	    	    mSelectedCategoryPos = mCategories.size()-2;
	    	    mSpinnerCategory.setSelection(mSelectedCategoryPos);
	    	}else{
	    		mSpinnerCategory.setSelection(mSelectedCategoryPos);
	    	}
	    }
	}
}

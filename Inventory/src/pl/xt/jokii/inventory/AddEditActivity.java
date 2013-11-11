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
	private static final long NO_ENTRY_TO_EDIT = -1L;
	private EditText mEditTextAmount;
	private Spinner  mSpinnerCategory;
	private EditText mEditTextName;
	private int mAmount = 0;
	private int mSelectedCategoryPos = 0;
	private ArrayList<String> mCategories;
	/**
	 * Indicate if Activity is in edit mode. 
	 * If -1 then none entry is edited, but added new
	 */
	private long entryIdToEdit = NO_ENTRY_TO_EDIT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.add_edit_entry);
	    
	    mEditTextAmount  = (EditText) findViewById(R.id.editTextAmount);
	    mSpinnerCategory = (Spinner)  findViewById(R.id.spinnerCategory);
	    mEditTextName    = (EditText) findViewById(R.id.editTextName);
	    
	    mCategories = getListOfExistingCategories();
	    mCategories.add(getString(R.string.add_new_category));
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, mCategories);
	    mSpinnerCategory.setAdapter(adapter);
	    mSpinnerCategory.setOnItemSelectedListener(onCategorySelectedListener);
	    
	    // Check if is in entry edit mode, if yes then fulfill screen with entry data
	    Intent intent = getIntent();
	    if(intent != null){
	    	entryIdToEdit = intent.getLongExtra(Const.EXTRA_ENTRY_DB_ID_TO_EDIT, NO_ENTRY_TO_EDIT);
	    	if(entryIdToEdit != NO_ENTRY_TO_EDIT){
	    		prepareEntryToEdit(entryIdToEdit);
	    	}
	    }
	}
	
	private void prepareEntryToEdit(long id){
		InventoryResultsSet resultSet = DbUtils.retrieveResultSet(getApplicationContext(), null);
		InventoryEntry entry = resultSet.getEntryByDbId(id);
		
		mEditTextAmount.setText(String.valueOf(entry.getAmount()));
		mEditTextName.setText(entry.getName());
		mSpinnerCategory.setSelection(getSpinnerSelectionPos(entry.getCategory()));
	}
	
	/**
	 * Retrieve position for Spinner to mark as selected
	 * @param nameOfCategory
	 * @return selected position on Spinner list or -1 if not found 
	 */
	private int getSpinnerSelectionPos(String nameOfCategory){
		int i = 0;
		if(mCategories == null){
			mCategories = getListOfExistingCategories();
		}
		for(String category : mCategories){
			if(category.equals(nameOfCategory)){
				return i;
			}
			i++;
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
    private ArrayList<String> getListOfExistingCategories(){
		return (ArrayList<String>) DbUtils.retrieveResultSet(getApplicationContext(), null).getCategories().clone();
	}
	
	OnItemSelectedListener onCategorySelectedListener = new OnItemSelectedListener() {

		@Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
			if(position == (mCategories.size() - 1)){			// If last element was chosen "Add new Entry"
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
			Toast.makeText(getApplicationContext(), getString(R.string.name_empty_warning), Toast.LENGTH_SHORT).show();
		}else{
			InventoryEntry entry = new InventoryEntry();
			entry.setCategory(category);
			entry.setName(name);
			entry.setAmount(Integer.valueOf(amount));
			
			if(entryIdToEdit == NO_ENTRY_TO_EDIT){		// new entry
				DbUtils.insertEntryDB(getApplicationContext(), entry);
				
			}else{										// existing entry is edited
				entry.setId(entryIdToEdit);
				DbUtils.updateEntryDB(getApplicationContext(), entry);
			}

			setResult(RESULT_OK);
			finish();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    if(requestCode == R.id.add_new_category_req_id){
	    	if(resultCode == RESULT_OK){
	    		String newCategoryName = data.getStringExtra(Const.EXTRA_NEW_CATEGORY_NAME);
	    		mCategories = getListOfExistingCategories();
	    	    mCategories.add(newCategoryName);
	    	    mCategories.add(getString(R.string.add_new_category));
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

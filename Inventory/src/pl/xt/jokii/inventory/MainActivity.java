package pl.xt.jokii.inventory;

import pl.xt.jokii.adapter.InventoryAdapter;
import pl.xt.jokii.db.DbUtils;
import pl.xt.jokii.db.InventoryEntry;
import pl.xt.jokii.db.InventoryResultsSet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {
	private ListView mListView = null;
	InventoryAdapter mInventoryAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		mListView = (ListView)findViewById(R.id.listView1); 
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
			}
		});
		
		configureList();
     	
	}
	
	private void configureList(){
		InventoryResultsSet resultSet = new InventoryResultsSet();
		
		resultSet = DbUtils.retrieveResultSet(getApplicationContext());
		
     	mInventoryAdapter = new InventoryAdapter(resultSet, getLayoutInflater());
     	mInventoryAdapter.setOnButtonPlusClickListener(onPlusClickListener);
     	mInventoryAdapter.setOnButtonMinusClickListener(onMinusClickListener);
     	mListView.setAdapter(mInventoryAdapter);
     	mListView.setOnItemLongClickListener(onListItemLongClickListener);
	}
	
	private OnItemLongClickListener onListItemLongClickListener = new OnItemLongClickListener() {
		@Override
        public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
			Intent intent = new Intent(getApplicationContext(), AddEditActivity.class);
			intent.putExtra(Const.EXTRA_ENTRY_DB_ID_TO_EDIT, (Long)v.getTag());
			startActivityForResult(intent, R.id.add_entry_req_id);
            return true;
        }
	};
	
	private OnClickListener onPlusClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Button btn = (Button)v;
			Integer position = (Integer) btn.getTag();
			int amount = mInventoryAdapter.getItem(position).getAmount();
			amount++;
			InventoryEntry entry = mInventoryAdapter.getItem(position);
			entry.setAmount(amount);
			DbUtils.updateEntryDB(getApplicationContext(), entry);
			mListView.invalidateViews();
		}
	};
	
	private OnClickListener onMinusClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Button btn = (Button)v;
			Integer position = (Integer) btn.getTag();
			int amount = mInventoryAdapter.getItem(position).getAmount();
			amount = (amount > 0) ? (amount - 1) : amount;
			InventoryEntry entry = mInventoryAdapter.getItem(position);
			entry.setAmount(amount);
			DbUtils.updateEntryDB(getApplicationContext(), entry);
			mListView.invalidateViews();
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClickAdd(View v){
		startActivityForResult(new Intent(getApplicationContext(), AddEditActivity.class), R.id.add_entry_req_id);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    if(requestCode == R.id.add_entry_req_id){
	    	if(resultCode == Activity.RESULT_OK){
	    		// recreate list from DB
	    		configureList();
	    		mListView.invalidateViews();
	    	}
	    }
	}

}

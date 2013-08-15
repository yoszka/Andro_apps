package pl.xt.jokii.inventory;

import pl.xt.jokii.adapter.InventoryAdapter;
import pl.xt.jokii.db.DbUtils;
import pl.xt.jokii.db.InventoryEntry;
import pl.xt.jokii.db.InventoryResultsSet;
import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ListView mListView = null;
	InventoryResultsSet resultSet;
	InventoryAdapter mInventoryAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		resultSet = new InventoryResultsSet();
		
		mListView = (ListView)findViewById(R.id.listView1); 
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
			}
		});
		
//		InventoryEntry entry = new InventoryEntry();
//		entry.setCategory("Przyprawy");
//		entry.setName("Przyprawa do kurczaka");
//		entry.setAmount(0);
//		resultSet.addEntry(entry);
////		DbUtils.insertEntryDB(getApplicationContext(), entry);
//		
//		entry = new InventoryEntry();
//		entry.setCategory("Œrodki czystoœci");
//		entry.setName("Myd³o");
//		entry.setAmount(5);
//		resultSet.addEntry(entry);
////		DbUtils.insertEntryDB(getApplicationContext(), entry);
//		
//		entry = new InventoryEntry();
//		entry.setCategory("Przyprawy");
//		entry.setName("Majeranek");
//		entry.setAmount(2);
//		resultSet.addEntry(entry);
////		DbUtils.insertEntryDB(getApplicationContext(), entry);
//		
//		entry = new InventoryEntry();
//		entry.setCategory("Przyprawy");
//		entry.setName("Sól");
//		entry.setAmount(1);
//		resultSet.addEntry(entry);
////		DbUtils.insertEntryDB(getApplicationContext(), entry);
//		
//		entry = new InventoryEntry();
//		entry.setCategory("Jedzenie");
//		entry.setName("Fasolka");
//		entry.setAmount(1);
//		resultSet.addEntry(entry);
////		DbUtils.insertEntryDB(getApplicationContext(), entry);
//		
//		entry = new InventoryEntry();
//		entry.setCategory("Œrodki czystoœci");
//		entry.setName("P³yn do naczyñ");
//		entry.setAmount(0);
//		resultSet.addEntry(entry);
////		DbUtils.insertEntryDB(getApplicationContext(), entry);
		
		resultSet = DbUtils.retrieveResultSet(getApplicationContext());
		
     	mInventoryAdapter = new InventoryAdapter(resultSet, getLayoutInflater());
     	mInventoryAdapter.setResource(getResources());
     	mInventoryAdapter.setOnButtonPlusClickListener(onPlusClickListener);
     	mInventoryAdapter.setOnButtonMinusClickListener(onMinusClickListener);
     	mListView.setAdapter(mInventoryAdapter);
     	mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                    int position, long arg3) {
				Toast.makeText(getApplicationContext(), "Edit item " + position + ", id: " + (Long)v.getTag(), 0).show();
	            return true;
            }
		});
	}
	
	private OnClickListener onPlusClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Button btn = (Button)v;
			Integer position = (Integer) btn.getTag();
//			Toast.makeText(getApplicationContext(), "Plus " + position, 0).show();
			int amount = mInventoryAdapter.getItem(position).getAmount();
			amount++;
//			InventoryEntry entry = mInventoryAdapter.getItem(position);
//			entry.setAmount(amount);
//			DbUtils.updateEntryDB(getApplicationContext(), entry);
			
			mInventoryAdapter.getItem(position).setAmount(amount);
			DbUtils.updateEntryDB(getApplicationContext(), mInventoryAdapter.getItem(position));
			mListView.invalidateViews();
		}
	};
	
	private OnClickListener onMinusClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Button btn = (Button)v;
			Integer position = (Integer) btn.getTag();
//			Toast.makeText(getApplicationContext(), "Minus " + position, 0).show();
			int amount = mInventoryAdapter.getItem(position).getAmount();
			amount = (amount > 0) ? (amount - 1) : amount;
			mInventoryAdapter.getItem(position).setAmount(amount);
			mListView.invalidateViews();
		}
	};
	
//	private OnLongClickListener mOnItemLongClickListener = new OnLongClickListener() {
//		@Override
//		public boolean onLongClick(View v) {
//			Integer position = (Integer) v.getTag();
//			Toast.makeText(getApplicationContext(), "Edit item " + position, 0).show();
//			return true;
//		}
//	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

}

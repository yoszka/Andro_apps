package pl.xt.jokii.adapter;

import java.util.ArrayList;
import java.util.List;

import pl.xt.jokii.db.InventoryEntry;
import pl.xt.jokii.db.InventoryEntry.EntryState;
import pl.xt.jokii.db.InventoryResultsSet;
import pl.xt.jokii.inventory.R;

import android.content.res.Resources;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InventoryAdapter extends BaseAdapter {
	final LayoutInflater inflater;
	private List<InventoryEntry> mEntries;
	private OnClickListener mOnButtonPlusClickListener;
	private OnClickListener mOnButtonMinusClickListener;
	private OnClickListener mOnButtonDismissOkClickListener;
	private OnClickListener mOnButtonDismissCancelClickListener;
	private Resources mAppResources;
	
	public InventoryAdapter(Resources resources, InventoryResultsSet resultSet, LayoutInflater inflater)
	{
		mEntries =  resultSet.getEntriesWithCategories();
		this.inflater = inflater;
		mAppResources = resources;
	}
	
	public void setResultSet(InventoryResultsSet resultSet){
		mEntries =  resultSet.getEntriesWithCategories();
	}
	
	/**
	 * Use adapter entries with given {@code resultSet} and update it.
	 * Entries state will be taken from original entries set.
	 * @param resultSet
	 */
	public void updateResultSet(InventoryResultsSet resultSet){
		List<InventoryEntry> dismissedEntries = getDismissedEntries();
		for(InventoryEntry entry : dismissedEntries){
			InventoryEntry e = resultSet.getEntryByDbId(entry.getId());
			if(e != null){
				e.setEntryState(EntryState.DISMISSED);
			}
		}
		setResultSet(resultSet);
	}
	
	public void setOnButtonPlusClickListener(OnClickListener listener){
		mOnButtonPlusClickListener = listener;
	}
	public void setOnButtonMinusClickListener(OnClickListener listener){
		mOnButtonMinusClickListener = listener;
	}
	
	public void setOnButtonDismissOkClickListener(OnClickListener listener){
		mOnButtonDismissOkClickListener = listener;
	}
	public void setOnButtonDismissCancelClickListener(OnClickListener listener){
		mOnButtonDismissCancelClickListener = listener;
	}
	
	public int getCount() {
		return mEntries.size();
	}

	public InventoryEntry getItem(int position) {
		return mEntries.get(position);
	}

	public long getItemId(int position) {
		return getItem(position).getId();
	}
	

	public View getView(int position, View rowView, ViewGroup parent) {
		final EntryState entryState = getItem(position).getEntryState();
//		Log.v("ADAPTER", "pos: " + position + ", rowView: " + rowView + ", tag: " + ((rowView != null)? rowView.getTag() : "null") + ", entryState: " + entryState + ", type: " + getItemViewType(position)+ ", n: " + getItem(position).getName());
		if(getItemViewType(position) == InventoryEntry.TYPE_ENTRY){
			if ((rowView == null) || (rowView.getTag() == null) || (entryState == EntryState.REMOVED)) {
				rowView = inflater.inflate(R.layout.list_item, null);
			}
			
			// Set default items visibility
			if(entryState == EntryState.DISMISSED){
				rowView.findViewById(R.id.list_item_dismiss_layout).setVisibility(View.VISIBLE);
				rowView.findViewById(R.id.list_item_content_layout).setVisibility(View.GONE);
			}else{
				rowView.findViewById(R.id.list_item_dismiss_layout).setVisibility(View.GONE);
				rowView.findViewById(R.id.list_item_content_layout).setVisibility(View.VISIBLE);
			}
			
			// NAME
			TextView textViewName = (TextView)rowView.findViewById(R.id.textViewName);
			textViewName.setText(getItem(position).getName());
			// dismissed name (same like NAME)
			TextView textViewNameDismissed = (TextView)rowView.findViewById(R.id.textViewNameDismissed);
//			textViewNameDismissed.setText("Delete: \"" + getItem(position).getName()+"\"?");
			textViewNameDismissed.setText(
					String.format(mAppResources.getString(R.string.delete_confirm), getItem(position).getName()));
			// AMOUNT
			EditText editTextAmount = (EditText) rowView.findViewById(R.id.editTextAmount);
			editTextAmount.setText(String.valueOf(getItem(position).getAmount()));
			// BUTTONS
			Button buttonPlus  = (Button) rowView.findViewById(R.id.buttonPlus);
			buttonPlus.setTag((Integer)position);
			buttonPlus.setOnClickListener(mOnButtonPlusClickListener);
			
			Button buttonMinus = (Button) rowView.findViewById(R.id.buttonMinus);
			buttonMinus.setTag((Integer)position);
			buttonMinus.setOnClickListener(mOnButtonMinusClickListener);
			
			Button buttonDismissOk = (Button) rowView.findViewById(R.id.buttonDismissOk);
			buttonDismissOk.setTag((Integer)position);
			buttonDismissOk.setOnClickListener(mOnButtonDismissOkClickListener);
			
			Button buttonDismissCancel = (Button) rowView.findViewById(R.id.buttonDismissCancel);
			buttonDismissCancel.setTag((Integer)position);
			buttonDismissCancel.setOnClickListener(mOnButtonDismissCancelClickListener);
			
			rowView.setTag((Long)getItem(position).getId());
		}else{
			if (rowView == null) {
				rowView = inflater.inflate(R.layout.list_item_category, null);
			}
			// CATEGORY
			TextView textViewCategory = ((TextView)rowView.findViewById(R.id.textViewCategory));
			textViewCategory.setText(getItem(position).getCategory());
			rowView.setClickable(false);
		}
		
		return rowView;
	}
	
	@Override
	public int getViewTypeCount() {
	    return 2;
	}
	

	@Override
	public int getItemViewType(int position) {
	    return getItem(position).getType();
	}
	
	public List<InventoryEntry> getDismissedEntries(){
		List<InventoryEntry> dismissedEntriesList = new ArrayList<InventoryEntry>();
		for(InventoryEntry entry : mEntries){
			if(entry.getEntryState() == EntryState.DISMISSED){
				dismissedEntriesList.add(entry);
			}
		}
		return dismissedEntriesList;
	}

}

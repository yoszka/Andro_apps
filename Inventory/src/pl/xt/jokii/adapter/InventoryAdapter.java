package pl.xt.jokii.adapter;

import java.util.List;

import pl.xt.jokii.db.InventoryEntry;
import pl.xt.jokii.db.InventoryResultsSet;
import pl.xt.jokii.inventory.R;

import android.content.res.Resources;
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
//	private Resources appResource = null;
	private OnClickListener mOnButtonPlusClickListener;
	private OnClickListener mOnButtonMinusClickListener;
//	private InventoryResultsSet mResultSet;
//	List<InventoryEntry> mEntriesWithCategories;
//	private OnLongClickListener mOnItemLongClickListener;
	
	public InventoryAdapter(InventoryResultsSet resultSet, LayoutInflater inflater)
	{
		mEntries =  resultSet.getEntriesWithCategories();
		this.inflater = inflater;
	}
	
	public void setOnButtonPlusClickListener(OnClickListener listener){
		mOnButtonPlusClickListener = listener;
	}
	public void setOnButtonMinusClickListener(OnClickListener listener){
		mOnButtonMinusClickListener = listener;
	}
	
//	public void setOnItemLongClickListener(OnLongClickListener listener){
//		mOnItemLongClickListener = listener;
//	}
	
//	public void setResource(Resources res)
//	{
//		this.appResource = res;
//	}

	public int getCount() {
		return mEntries.size();
	}

	public InventoryEntry getItem(int position) {
		return mEntries.get(position);
	}

	public long getItemId(int position) {
		return getItem(position).getId();
		//return this.entries.get(position).getId();
	}
	

	public View getView(int position, View rowView, ViewGroup parent) {
//		String previousCategory = null;
//		String currentCategory  = null;
//		
//		if(position == 0){
//			currentCategory = getItem(position).getCategory();
//		}else{
//			currentCategory = getItem(position).getCategory();
//			previousCategory = getItem(position-1).getCategory();
//
//			if((previousCategory == null)
//					||
//					(currentCategory != null) && (previousCategory != null) && (currentCategory.equals(previousCategory))){
//				currentCategory = null;
//			}
//		}
		
		
		
		
//		if(this.appResource == null)
//		{
//			throw new NullPointerException("Resource was used but never set before");
//		}
		
		
		if(getItemViewType(position) == InventoryEntry.TYPE_ENTRY){
			if (rowView == null) {
				rowView = inflater.inflate(R.layout.list_item, null);
			}
			
			//wierszView.setPadding(10,10, 10,10);
			
			// NAME
			TextView textViewName = (TextView)rowView.findViewById(R.id.textViewName);
			textViewName.setText(getItem(position).getName());
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
	
	

}

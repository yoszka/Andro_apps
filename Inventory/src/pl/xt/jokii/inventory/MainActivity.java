package pl.xt.jokii.inventory;


import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.Collections;
import java.util.List;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import pl.xt.jokii.adapter.InventoryAdapter;
import pl.xt.jokii.db.DbUtils;
import pl.xt.jokii.db.InventoryEntry;
import pl.xt.jokii.db.InventoryResultsSet;
import pl.xt.jokii.db.InventoryEntry.EntryState;
import pl.xt.jokii.slidetodismiss.SwipeDismissListViewTouchListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	protected static final String TAG = "Inventory";
	private ListView mListView = null;
	InventoryAdapter mInventoryAdapter;
	SwipeDismissListViewTouchListener mTouchListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mListView = (ListView)findViewById(R.id.listView1); 
//		mListView.setOnItemClickListener(new OnItemClickListener() {
//			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
//			}
//		});
		
		// Create a ListView-specific touch listener. ListViews are given special treatment because
		// by default they handle touches for their list items... i.e. they're in charge of drawing
		// the pressed state (the list selector), handling list item clicks, etc.
		mTouchListener = new SwipeDismissListViewTouchListener(
				mListView,
				new SwipeDismissListViewTouchListener.OnDismissCallback() {
					@Override
					public void onDismiss(ListView listView, int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							// Do nothing, just wait for confirm or cancel
							// mInventoryAdapter.remove(mInventoryAdapter.getItem(position));
//									DbUtils.deleteEntryDB(getApplicationContext(), dbId)
							
//							long dbId = ((InventoryAdapter)mListView.getAdapter()).getItem(position).getId();
//							DbUtils.deleteEntryDB(getApplicationContext(), dbId);
							getListItemEntryByPosition(position).setEntryState(EntryState.DISMISSED);
						}
//						mInventoryAdapter.notifyDataSetChanged();
			    		// recreate list from DB
//			    		mListView.invalidateViews();

//						configureList();
//			    		mInventoryAdapter.notifyDataSetChanged();
					}
				});
		configureList();
     	
		mListView.setOnItemLongClickListener(onListItemLongClickListener);
        mListView.setOnTouchListener(mTouchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        mListView.setOnScrollListener(mTouchListener.makeScrollListener());
        
	}
	
	private void configureList(){
		InventoryResultsSet resultSet = new InventoryResultsSet();
		
		resultSet = DbUtils.retrieveResultSet(getApplicationContext());
		
		mInventoryAdapter = (InventoryAdapter) mListView.getAdapter();
		
		if(mInventoryAdapter == null){
			mInventoryAdapter = new InventoryAdapter(getResources(), resultSet, getLayoutInflater());
			mListView.setAdapter(mInventoryAdapter);
		}else{
			mInventoryAdapter.updateResultSet(resultSet);
		}
		
     	mInventoryAdapter.setOnButtonPlusClickListener(onPlusClickListener);
     	mInventoryAdapter.setOnButtonMinusClickListener(onMinusClickListener);
     	mInventoryAdapter.setOnButtonDismissOkClickListener(onDismissOkClickListener);
     	mInventoryAdapter.setOnButtonDismissCancelClickListener(onDismissCancelClickListener);
     	
//     	mListView.setAdapter(mInventoryAdapter);
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
	
	private OnClickListener onDismissOkClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Button btn = (Button)v;
			Integer position = (Integer) btn.getTag();
			long dbId = ((InventoryAdapter)mListView.getAdapter()).getItem(position).getId();
			DbUtils.deleteEntryDB(getApplicationContext(), dbId);
			getListItemEntryByPosition(position).setEntryState(EntryState.REMOVED);
			//*
//			final View dismissingListItem = mListView.getChildAt(position);
			final View dismissingListItem = (View) v.getParent().getParent().getParent().getParent(); // Get overlying FrameLayout
//			Log.v("ADAPTER M", "pos: " + position + ", view: " + dismissingListItem + ", tag: " + ((dismissingListItem != null)? dismissingListItem.getTag() : "null") + ", entryState: " + getListItemEntryByPosition(position).getEntryState() + ", n: " + getListItemEntryByPosition(position).getName());
			dismissingListItem.setTag(null);
//			Log.v("ADAPTER M2", "pos: " + position + ", view: " + dismissingListItem + ", tag: " + ((dismissingListItem != null)? dismissingListItem.getTag() : "null") + ", entryState: " + getListItemEntryByPosition(position).getEntryState() + ", n: " + getListItemEntryByPosition(position).getName());
			removeItem(dismissingListItem, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
				    super.onAnimationEnd(animation);
				    configureList();
				    mInventoryAdapter.notifyDataSetChanged();
				}
			});
			
		}
	};
	
	private OnClickListener onDismissCancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Button btn = (Button)v;
			Integer position = (Integer) btn.getTag();
			getListItemEntryByPosition(position).setEntryState(EntryState.NORMAL);
			final View dismissingListItem = (View) v.getParent().getParent().getParent(); // Get overlying FrameLayout
			
//			Log.v(TAG, "parent parenta parenta: "+  v.getParent().getParent().getParent());

//			final View dismissingListItem = mListView.getChildAt(position); 
//			Log.v(TAG, "dismissingListItem.getTag() =  " + dismissingListItem.getTag());
//			String name = ((InventoryEntry)mListView.getAdapter().getItem(position)).getName();
//			String name = ((TextView)dismissingListItem.findViewById(R.id.textViewName)).getText().toString();
//			Log.v(TAG, "position: " + position + ", name: "+name+ ", id= "+Integer.toHexString(dismissingListItem.getId())+", dismissingListItem: " + dismissingListItem);
			animateCancelDismiss(dismissingListItem, null);

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
	
	
	/**
	 * Animate the dismissed list item to zero-height and fire the dismiss callback when
	 * dismissed item animations have completed.
	 * @param dismissView list view to dismiss
	 * @param listener listener for animation end
	 */
	private void removeItem(final View dismissView, AnimatorListener listener) {
		final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
		final int originalHeight = dismissView.getHeight();
		
		int ANIMATION_TIME = 500;
		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(ANIMATION_TIME);
		animator.addListener(listener);
		
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});
		
		
		animator.start();
		
	}
	
	/**
	 * Animate the dismissed list item "delete confirm" back to list item content by fade out-fade in animation
	 * @param dismissView list view to dismiss
	 * @param listener listener for animation end
	 */
    private void animateCancelDismiss(final View dismissView, final AnimatorListener listener) {
        final int ANIMATION_TIME = 250;
        final Handler handler = new Handler();
        
        animate(dismissView)
		        .alpha(0.01f)
		        .setDuration(ANIMATION_TIME)
		        .setListener(new AnimatorListenerAdapter() {
		        	@Override
		        	public void onAnimationEnd(Animator animation) {
		        		handler.post( new Runnable() {
	                        public void run() {
	                        	Log.v(TAG, "dismisView = " + dismissView +", id= "+Integer.toHexString(dismissView.getId()));
	                        	Log.v(TAG, "list_item_dismiss_layout = " + dismissView.findViewById(R.id.list_item_dismiss_layout) +((dismissView.findViewById(R.id.list_item_dismiss_layout) != null)?", id= "+Integer.toHexString(dismissView.findViewById(R.id.list_item_dismiss_layout).getId()):""));
	                        	Log.v(TAG, "list_item_content_layout = " + dismissView.findViewById(R.id.list_item_content_layout) +((dismissView.findViewById(R.id.list_item_content_layout) != null)?", id= "+Integer.toHexString(dismissView.findViewById(R.id.list_item_content_layout).getId()):""));
	                        	dismissView.findViewById(R.id.list_item_dismiss_layout).setVisibility(View.GONE);
	                        	dismissView.findViewById(R.id.list_item_content_layout).setVisibility(View.VISIBLE);
	                        	animate(dismissView)
	                        	.alpha(1f)
	                        	.setDuration(ANIMATION_TIME)
	                        	.setListener(listener);
	                        }
                        });
		        	    super.onAnimationEnd(animation);
		        	}
				});
        
    }
    
    private InventoryEntry getListItemEntryByPosition(int position){
    	return ((InventoryAdapter)mListView.getAdapter()).getItem(position);
    }

}

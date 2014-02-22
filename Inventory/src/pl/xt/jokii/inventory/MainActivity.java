package pl.xt.jokii.inventory;


import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.Locale;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import pl.xt.jokii.adapter.InventoryAdapter;
import pl.xt.jokii.db.DbUtils;
import pl.xt.jokii.db.InventoryEntry;
import pl.xt.jokii.db.InventoryResultsSet;
import pl.xt.jokii.db.InventoryEntry.EntryState;
import pl.xt.jokii.slidetodismiss.SwipeDismissListViewTouchListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final String TAG = "Inventory";
    private static final int LIGHT_RED   = Color.RED   + 0x0000cccc;    // make lighter red
    private static final int LIGHT_GREEN = Color.GREEN + 0x00cc00cc;    // make lighter green
    private static final boolean DEVELOPER_MODE = true;
    private static final boolean DRAWER = true;
    private static final int DISPLAY_ALL   = 0;
    private static final int DISPLAY_EMPTY = 1;
    private EditText mEditTextSearchView;
	private ImageButton   mButtonSearchView;
	private ListView mListView;
	InventoryAdapter mInventoryAdapter;
	SwipeDismissListViewTouchListener mTouchListener;
	
	// Drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerOptionsTitles;
    
    private DisplayMode mDisplayMode = DisplayMode.ALL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	     if (DEVELOPER_MODE) {
	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//	                 .detectDiskReads()
//	                 .detectDiskWrites()
//	                 .detectNetwork()
	                 .detectAll()       // for all detectable problems
	                 .penaltyLog()
	                 .build());
	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	                 .detectLeakedSqlLiteObjects()
	                 .detectLeakedClosableObjects()
	                 .penaltyLog()
	                 .penaltyDeath()
	                 .build());
	     }
		super.onCreate(savedInstanceState);

		if (DRAWER) {
	        setContentView(R.layout.activity_main);

	        mTitle = mDrawerTitle = getTitle();
	        mDrawerOptionsTitles = getResources().getStringArray(R.array.drawer_options_array);
	        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	        mDrawerList   = (ListView)     findViewById(R.id.left_drawer);

	        // set a custom shadow that overlays the main content when the drawer opens
	        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
	        // set up the drawer's list view with items and click listener
	        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
	                R.layout.drawer_list_item, mDrawerOptionsTitles));
	        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

	        // enable ActionBar app icon to behave as action to toggle nav drawer
	        getActionBar().setDisplayHomeAsUpEnabled(true);
	        getActionBar().setHomeButtonEnabled(true);

	        // ActionBarDrawerToggle ties together the the proper interactions
	        // between the sliding drawer and the action bar app icon
	        mDrawerToggle = new ActionBarDrawerToggle(
	                this,                  // host Activity 
	                mDrawerLayout,         // DrawerLayout object
	                R.drawable.ic_drawer,  // nav drawer image to replace 'Up' caret
	                R.string.drawer_open,  // "open drawer" description for accessibility
	                R.string.drawer_close  // "close drawer" description for accessibility
	                ) {
	            public void onDrawerClosed(View view) {
	                getActionBar().setTitle(mTitle);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }

	            public void onDrawerOpened(View drawerView) {
	                getActionBar().setTitle(mDrawerTitle);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	        };
	        mDrawerLayout.setDrawerListener(mDrawerToggle);

	        // **********************************************
            mListView           = (ListView)    findViewById(R.id.listView1); 
            mEditTextSearchView = (EditText)    findViewById(R.id.editTextsearchView);
            mButtonSearchView   = (ImageButton) findViewById(R.id.buttonSearchView);
            mButtonSearchView.setEnabled(false);
            
//        mListView.setOnItemClickListener(new OnItemClickListener() {
//            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
//            }
//        });
            
            // Create a ListView-specific touch listener. ListViews are given special treatment because
            // by default they handle touches for their list items... i.e. they're in charge of drawing
            // the pressed state (the list selector), handling list item clicks, etc.
            mTouchListener = new SwipeDismissListViewTouchListener(
                    mListView,
                    new SwipeDismissListViewTouchListener.OnDismissCallback() {
                        @Override
                        public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                            Debug.log("onDismiss", listView + ", " + reverseSortedPositions);
                            updateSearchBarColor(true);                          // Now search bar is enabled again
//                        mListView.setOnTouchListener(mTouchListener);        // bring back touches

                            for (int position : reverseSortedPositions) {
                                // Do nothing, just wait for confirm or cancel
                                // mInventoryAdapter.remove(mInventoryAdapter.getItem(position));
//                                    DbUtils.deleteEntryDB(getApplicationContext(), dbId)
                                
//                            long dbId = ((InventoryAdapter)mListView.getAdapter()).getItem(position).getId();
//                            DbUtils.deleteEntryDB(getApplicationContext(), dbId);
                                getListItemEntryByPosition(position).setEntryState(EntryState.DISMISSED);
                            }
//                        mInventoryAdapter.notifyDataSetChanged();
                            // recreate list from DB
//                        mListView.invalidateViews();

//                        configureList(mDisplayMode);
//                        mInventoryAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onUserActionStart() {
                            updateSearchBarColor(false);                         // Block search bar when dismiss is in progress
//                          mListView.setOnTouchListener(null);                  // Disable touches on list item
                        }
                    });
            configureList(mDisplayMode);
            
            mListView.setOnItemLongClickListener(onListItemLongClickListener);
            mListView.setOnTouchListener(mTouchListener);
            // Setting this scroll listener is required to ensure that during ListView scrolling,
            // we don't look for swipes.
            mListView.setOnScrollListener(mTouchListener.makeScrollListener());


            mEditTextSearchView.addTextChangedListener(mTextWatcher);
		} else {
		    setContentView(R.layout.main);
		}

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

	private TextWatcher mTextWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(TextUtils.isEmpty(s)){
                mButtonSearchView.setEnabled(false);
            }else{
                mButtonSearchView.setEnabled(true);
            }
            refreshList();
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    };

    /**
     * Configure and reload data to list
     */
	private void configureList(DisplayMode displayMode){
		InventoryResultsSet resultSet = new InventoryResultsSet();

//		Toast.makeText(getApplicationContext(), ""+displayMode, 1).show();
		resultSet = DbUtils.retrieveResultSet(getApplicationContext(), mEditTextSearchView.getText().toString(), (displayMode == DisplayMode.EMPTY));

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
				    configureList(mDisplayMode);
				    mInventoryAdapter.notifyDataSetChanged();
				}
			});
			
		}
	};
	
	private OnClickListener onDismissCancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
		    Debug.log("onClick", v);
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

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch(item.getItemId()){
        case R.id.action_add:
            startActivityForResult(new Intent(getApplicationContext(), AddEditActivity.class), R.id.add_entry_req_id);
            return true;

        default:
            return super.onMenuItemSelected(featureId, item);
        }
    }
    
    public void onClickAdd(View v){
        startActivityForResult(new Intent(getApplicationContext(), AddEditActivity.class), R.id.add_entry_req_id);
    }

    public void onClickClearSearch(View v){
        mEditTextSearchView.setText("");
        refreshList();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    if(requestCode == R.id.add_entry_req_id){
	    	if(resultCode == Activity.RESULT_OK){
	    	    refreshList();
	    	}
	    }
	}
	
    private DisplayMode getCurrentMode() {
        return DisplayMode.ALL;
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
        Debug.log("getListItemEntryByPosition", position);
        return ((InventoryAdapter)mListView.getAdapter()).getItem(position);
    }

    /**
     * Recreate list from DB. Entries retrieved with pattern from search bar.
     */
    private void refreshList(){
        Debug.log("refreshList");
        configureList(mDisplayMode);
//        mInventoryAdapter.notifyDataSetChanged();
        mListView.invalidateViews();
        updateSearchBarColor(null);
    }
    
    /**
     * Update search bar UI
     * @param inputEnabled determine new input state, if {@code null} then use current
     */
    private void updateSearchBarColor(Boolean inputEnabled){
        if(inputEnabled != null){
            mEditTextSearchView.setEnabled(inputEnabled);
            mButtonSearchView.setEnabled(inputEnabled);
        }else{
            inputEnabled = mEditTextSearchView.isEnabled();
        }

        if(inputEnabled){
            if(TextUtils.isEmpty(mEditTextSearchView.getText())){
                mEditTextSearchView.setBackgroundColor(Color.WHITE);
            }else{
                if(mInventoryAdapter.getCount() == 0){
                    mEditTextSearchView.setBackgroundColor(LIGHT_RED);
                }else{
                    mEditTextSearchView.setBackgroundColor(LIGHT_GREEN);
                }
            }
        }else{
            mEditTextSearchView.setBackgroundColor(Color.LTGRAY);
        }
    }
    
    
    // ********************************************************************************************************
    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_item_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
//        // Handle action buttons
//        switch(item.getItemId()) {
//        case R.id.action_item_search:
//            // create intent to perform web search for this planet
//            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
//            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
//            // catch event that there's no activity to handle intent
//            Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
////            if (intent.resolveActivity(getPackageManager()) != null) {
////                startActivity(intent);
////            } else {
////                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
////            }
//            return true;
//        default:
//            return super.onOptionsItemSelected(item);
//        }
    }

    // The click listener for ListView in the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
//        Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
//
//        // update selected item and title, then close the drawer
        mDisplayMode = ((position == DISPLAY_ALL) ? DisplayMode.ALL : DisplayMode.EMPTY);
        refreshList();
        mDrawerList.setItemChecked(position, true);
//        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

//    /**
//     * Fragment that appears in the "content_frame", shows a planet
//     */
//    public static class PlanetFragment extends Fragment {
//        public static final String ARG_PLANET_NUMBER = "planet_number";
//
//        public PlanetFragment() {
//            // Empty constructor required for fragment subclasses
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_planet, container, false);
//            int i = getArguments().getInt(ARG_PLANET_NUMBER);
//            String planet = getResources().getStringArray(R.array.planets_array)[i];
//
//            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
//                            "drawable", getActivity().getPackageName());
//            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
//            getActivity().setTitle(planet);
//            return rootView;
//        }
//    }
    
//    public static class MainListFragment extends Fragment {
//        
//    }
    
    private enum DisplayMode {
        ALL, EMPTY
    }
}

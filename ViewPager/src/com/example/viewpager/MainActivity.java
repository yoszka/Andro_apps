package com.example.viewpager;

import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;

public class MainActivity extends FragmentActivity  {
	protected static final int SCREEN_LEFT   = 0;
	protected static final int SCREEN_CENTER = 1;
	protected static final int SCREEN_RIGHT  = 2;
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == ((View) arg1);
			}
			@Override
			public int getCount() {
				return 3;
			}
			
			@Override
			public Object instantiateItem(View container, int position) {
				
				LayoutInflater inflater = (LayoutInflater) container.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				int resId = 0;
				switch (position) {
				case SCREEN_LEFT:
					resId = R.layout.left;
					break;
				case SCREEN_CENTER:
					resId = R.layout.list_item;
					break;
				case SCREEN_RIGHT:
					resId = R.layout.right;
					break;
				}
				
				View view = inflater.inflate(resId, null);
				((ViewPager) container).addView(view, 0);

				return view; 
			}
			
			@Override
			public void destroyItem(View arg0, int arg1, Object arg2) {
				((ViewPager) arg0).removeView((View) arg2);

			}


		};
		mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(SCREEN_CENTER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}

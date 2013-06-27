package com.example.buttonpreference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Implementation of custom preference with own layout.
 * Layout of this preference has two centered buttons.
 * @author Tomasz Jokel
 *
 */
public class ButtonPreference extends Preference {
	Button button1;
	Button button2;

	/**
	 * This type of constructor is must.
	 * @param context
	 * @param attrs
	 */
	public ButtonPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		// Get LayoutInflater 
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		
		// get view of desired layout
		View view = inflater.inflate(R.layout.button_preference_layout, null );
		
		// Find particular elements of view 
		button1 = (Button) view.findViewById(R.id.button1);
		button2 = (Button) view.findViewById(R.id.button2);
		
		button1.setOnClickListener(clickListener);
		button2.setOnClickListener(clickListener);
		
		return view;
	}
	
	
	/**
	 * Buttons click listener
	 */
	OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(v == button1){
				Toast.makeText(getContext(), "Button 1", Toast.LENGTH_SHORT).show();
			}
			if(v == button2){
				Toast.makeText(getContext(), "Button 2", Toast.LENGTH_SHORT).show();
			}
		}
	};

}

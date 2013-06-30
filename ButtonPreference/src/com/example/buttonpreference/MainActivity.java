package com.example.buttonpreference;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        // Custom divider to better see where dividers are
        getListView().setDivider(getResources().getDrawable(R.drawable.custom_divider));
        getListView().setDividerHeight(2);

        // Turn off printing divider after last element
        getListView().setFooterDividersEnabled(false);
    }

}

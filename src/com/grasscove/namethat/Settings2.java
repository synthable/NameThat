package com.grasscove.namethat;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings2 extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }
}

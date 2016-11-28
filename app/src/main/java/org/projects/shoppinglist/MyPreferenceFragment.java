package org.projects.shoppinglist;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class MyPreferenceFragment extends PreferenceFragment {

	//These values are specifed in the prefs.xml file
	private static String SETTINGS_PERSONALLISTKEY = "personal_list";
	private static String SETTINGS_NAMEKEY = "name";

	public static boolean isPersonalized(Context context)
	{

			return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_PERSONALLISTKEY, false);
	}

	public static String getName(Context context)
	{

		return PreferenceManager.getDefaultSharedPreferences(context).getString(SETTINGS_NAMEKEY, "");
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}

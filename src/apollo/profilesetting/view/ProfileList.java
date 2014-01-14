package apollo.profilesetting.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.view.Menu;
import android.view.MenuItem;
import apollo.profilesetting.model.Profile;
import apollo.profilesetting.util.ProfileApplyUtil;
import apollo.profilesetting.util.ProfileConstants;
import apollo.profilesetting.util.ProfileUtil;
import apollo.profilesetting.util.Profiles;

public class ProfileList extends PreferenceActivity implements
		OnPreferenceChangeListener {

	class ProfileIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = null;

			action = intent.getAction();
			if (ProfileConstants.INTENT_ACTION_DELETE.equals(action)) {
				fillList();
			}
		}

	}

	private static final int ID_INDEX = 0;
	private static final int MENU_BACKUP = 2;
	private static final int MENU_IMPORT = 5;
	private static final int MENU_MULTIDELETE = 4;
	private static final int MENU_NEW = 1;
	private static final int MENU_RESTORE = 3;
	private static final int NAME_INDEX = 1;
	  
	private ProfilePreference mCurrentProfilePref;
	private BroadcastReceiver mProfileIntentReceiver;
	private Profile activatedProfile;
	
	public ProfileList() {
		mProfileIntentReceiver = new ProfileIntentReceiver();
		activatedProfile = null;
		mCurrentProfilePref = null;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.addPreferencesFromResource(R.xml.profile_list);
		super.getListView().setItemsCanFocus(true);
		registerIntentReceivers();
        if(ProfileUtil.getActivatedProfileId(this) == -1) {
            ProfileUtil.setActivatedProfileId(this, 1);
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fillList();
	}

	protected void onDestory() {
		super.onDestroy();
		unregisterReceiver(this.mProfileIntentReceiver);
	}

	public void onNewIntent(Intent intent) {
		super.setIntent(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ProfileList.MENU_NEW, 0, R.string.menu_new).setIcon(R.drawable.ic_menu_add);
		menu.add(0, ProfileList.MENU_MULTIDELETE, 0, R.string.menu_multidelete).setIcon(R.drawable.ic_menu_delete);
		//menu.add(0, 2, 0, R.string.menu_backup).setIcon(R.drawable.ic_menu_backup);
		//menu.add(0, 3, 0, R.string.menu_restore).setIcon(R.drawable.ic_menu_recover);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case ProfileList.MENU_NEW:
			addNewProfile();
			break;
		case ProfileList.MENU_BACKUP:
			break;
		case ProfileList.MENU_RESTORE:
			break;
		case ProfileList.MENU_MULTIDELETE:
			Intent intent = new Intent(ProfileConstants.INTENT_ACTION_MULTIDELETE);
		    startActivity(intent);
			break;
		case ProfileList.MENU_IMPORT:
		}
		    
	    return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		int id = -1;

		if (newValue instanceof String) {
			id = Integer.parseInt((String) newValue);
			ProfileUtil.startApplyProfile(this, id);
			return true;
		} else {
			return false;
		}
	}

	private void addNewProfile() {
		Intent intent = new Intent(ProfileConstants.INTENT_ACTION_NEW);
		intent.putExtra(ProfileConstants.PROFILE_ID_EXTRA, -1);
		intent.setPackage(ProfileConstants.PACKAGE_NAME);
		super.startActivity(intent);
	}

	private void initCurrentPreference() {
		this.mCurrentProfilePref = new ProfilePreference(this);
		this.mCurrentProfilePref.setKey("0");
		this.mCurrentProfilePref.setTitle(R.string.Current_unsaved);
		this.mCurrentProfilePref.setSummary(R.string.create_new_with_current);
		this.mCurrentProfilePref.setPersistent(false);
		this.mCurrentProfilePref.setOnPreferenceChangeListener(this);
	}

	private void fillList() {
		boolean profile_unchanged = false;
		int activated_profile_id = -1;
		PreferenceGroup preferencegroup = null;
		Uri uri = null;
		String[] cols = null;
		Cursor cursor = null;
		
		activated_profile_id = ProfileUtil.getActivatedProfileId(this);
		if (activated_profile_id > 0) {
			this.activatedProfile = Profiles.getProfile(getContentResolver(), activated_profile_id);
		}
		
		if (activatedProfile != null) {
			Profile sys_profile = ProfileApplyUtil.getCurrentSystemProfile(this);
			profile_unchanged = sys_profile.equals(activatedProfile);//compareProfile(sys_profile, this.activatedProfile);
		} else {
			profile_unchanged = true;
		}
		
		uri = Profile.ProfileColumns.CONTENT_URI;
		
		cols = new String[6];
		cols[0] = Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_ID];
		cols[1] = Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME];
		cols[2] = Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_APPLIED];
		cols[3] = Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_HOUR];
		cols[4] = Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_MINITUE];
		cols[5] = Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_REPEAT];
		
		cursor = this.managedQuery(uri, cols, null, null, null);
		preferencegroup = (PreferenceGroup)this.findPreference("profile_list");
		preferencegroup.removeAll();
		ProfilePreference.clearSelected();
		
		if (cursor != null) {
			int col;
			int id, rule_repeat, rule_time_hour, rule_time_minutes;
			boolean rule_applied;
			String name;
			ProfilePreference pref;
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				col = cursor.getColumnIndex(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_ID]);
				id = cursor.getInt(col);
				
				col = cursor.getColumnIndex(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME]);
				name = cursor.getString(col);
				Profiles.localizeProfileName(name, this);
				
				col = cursor.getColumnIndex(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_APPLIED]);
				rule_applied = cursor.getInt(col) == 1;
						        
		        col = cursor.getColumnIndex(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_HOUR]);
		        rule_time_hour = cursor.getInt(col);
		        
		        col = cursor.getColumnIndex(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_MINITUE]);
		        rule_time_minutes = cursor.getInt(col);
		        
				col = cursor.getColumnIndex(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_REPEAT]);
				rule_repeat = cursor.getInt(col);
		        
				pref = new ProfilePreference(this, rule_applied, rule_repeat, rule_time_hour, rule_time_minutes);
				pref.setKey(Integer.toString(id));
				pref.setTitle(name);
				pref.setPersistent(false);
				pref.setOnPreferenceChangeListener(this);
		        if(activated_profile_id > 0 && activated_profile_id == id && profile_unchanged) {
		        	pref.setChecked();
		        }
		        preferencegroup.addPreference(pref);
			}
			cursor.close();
		}
		if (profile_unchanged == false) {
			initCurrentPreference();
			preferencegroup.addPreference(this.mCurrentProfilePref);
			this.mCurrentProfilePref.setChecked();
		}
	}
	
	private void registerIntentReceivers() {
	    IntentFilter filter = new IntentFilter(ProfileConstants.INTENT_ACTION_DELETE);
	    registerReceiver(this.mProfileIntentReceiver, filter);
	}
}
package apollo.profilesetting.view;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import apollo.profilesetting.model.Profile;
import apollo.profilesetting.util.ProfileConstants;
import apollo.profilesetting.util.ProfileUtil;
import apollo.profilesetting.util.Profiles;

public class ProfileView extends PreferenceActivity {
	private static final String GSM_RINGTONE_KEY = "gsm_ringtone_settings";
	static final String KEY_GSM_RINGTONE_TYPE = "gsm_preferences_ringtone_type";
	private static final int MENU_APPLY = 2;
	private static final int MENU_DELETE = 3;
	private static final int MENU_EDIT = 1;
	private static final int[] SOUND_PREFERENCES = new int[] { R.string.ignore, R.string.sound_ringtones, R.string.sound_music };
	private static String mEnableDisableStrings[] = null;
	private static String mOnOffStrings[] = null;
	private static String mOnOffValues[] = null;
	private static String mVibrateModeEntries[] = null;
	private static final int mVibrateModeValueSize = 5;
	private static String mVibrateModeValues[] = null;
	private Preference mAirplaneMode;
	private ProfileApplyRulePreference mAutoApply;
	private Preference mBluetooth;
	private Preference mGps;
	private int mId;
	private PreferenceCategory mLocationCategory;
	private Preference mNoSetting;
	private final int mOnOffEntrySize = 3;
	Profile mProfile;
	private PreferenceScreen mProfileView;
	private Preference mRingerVolume;
	private int mRingerVolumeSize;
	private Preference mSilentMode;
	private PreferenceCategory mSoundCategory;
	private Preference mVibrateMode;
	private PreferenceCategory mWirelessCategory;
	private Preference mGSMPreference;
	private Preference mWlan;
	private Uri mGSMRingtone;
	  private int mGSMRingtoneType;
	public ProfileView() {
		mRingerVolumeSize = 9;
	}

	private void deleteProfile() {
		final Intent intent = new Intent(ProfileConstants.INTENT_ACTION_DELETE);
		AlertDialog.Builder builder = null;
		

		builder = new android.app.AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.delete_profile));
		builder.setMessage(getResources().getString(R.string.delete_profile_confirm));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).setNegativeButton(android.R.string.cancel, null).show();
	}

	private void initEntryValues() {
		Resources resources = getResources();
		mOnOffStrings = resources.getStringArray(R.array.on_off_entries);
		mEnableDisableStrings = resources.getStringArray(R.array.enable_disable_entries);
		mOnOffValues = resources.getStringArray(R.array.on_off_values);
		mVibrateModeEntries = resources.getStringArray(R.array.vibrate_entries);
		mVibrateModeValues = resources.getStringArray(R.array.vibrate_values);
	}

	private void initPreference() {
		mProfileView = (PreferenceScreen) findPreference("profile_view");
		mWirelessCategory = (PreferenceCategory) findPreference("wireless_category");
		mLocationCategory = (PreferenceCategory) findPreference("location_category");
		mSoundCategory = (PreferenceCategory) findPreference("sound_category");
		mNoSetting = findPreference("no_setting");
		mAirplaneMode = findPreference(ProfileConstants.KEY_AIRPLANE);
		mWlan = findPreference(ProfileConstants.KEY_WLAN);
		mBluetooth = findPreference(ProfileConstants.KEY_BLUETOOTH);
		mGps = findPreference(ProfileConstants.KEY_GPS);
		mSilentMode = findPreference(ProfileConstants.KEY_SILENT_MODE);
		mRingerVolume = findPreference(ProfileConstants.KEY_RINGER_VOLUME);
		mVibrateMode = findPreference(ProfileConstants.KEY_VIBRATE_MODE);
		mGSMPreference = findPreference("gsm_ringtone_settings");
	}

	private void removeIgnoreItem(Profile profile) {
		int j = 4;
		int k = 3;
		if (profile.airplaneMode == -1) {
			mWirelessCategory.removePreference(mAirplaneMode);
			k--;
		}
		if (profile.wifi == -1 || profile.airplaneMode == 1) {
			mWirelessCategory.removePreference(mWlan);
			k--;
		}
		if (profile.bluetooth == -1 || profile.airplaneMode == 1) {
			mWirelessCategory.removePreference(mBluetooth);
			k--;
		}
		if (k == 0) {
			PreferenceScreen preferencescreen = mProfileView;
			PreferenceCategory preferencecategory4 = mWirelessCategory;
			preferencescreen.removePreference(preferencecategory4);
			mWirelessCategory = null;
			j--;
		}
		k = 1;
		if (profile.gps == -1 || profile.airplaneMode == 1) {
			mLocationCategory.removePreference(mGps);
			k--;
		}
		if (k == 0) {
			mProfileView.removePreference(mLocationCategory);
			mLocationCategory = null;
			j--;
		}
		k = 4;
		if (profile.silent == -1) {
			mSoundCategory.removePreference(mSilentMode);
			k--;
		}
		if (profile.silent != 0 || profile.ringerVolume == -1) {
			mSoundCategory.removePreference(mRingerVolume);
			k--;
		}
		if (profile.vibrate == -1) {
			mSoundCategory.removePreference(mVibrateMode);
			k--;
		}
		if (profile.GSMRingtoneType == -1) {
			mSoundCategory.removePreference(mGSMPreference);
			k--;
		}
		if (k == 0) {
			mProfileView.removePreference(mSoundCategory);
			mSoundCategory = null;
			j--;
		}
		if (!profile.ruleApplyEnabled || !profile.daysOfWeek.isRepeatSet())
			j--;
		if (j > 0) {
			mProfileView.removePreference(mNoSetting);
		}
	}

	private void updatePreference(Profile profile) {
		if (profile == null) {
			finish();
			return;
		}
		setTitle(profile.name);
		if (profile.ruleApplyEnabled && profile.daysOfWeek.isRepeatSet()) {
			mAutoApply = new ProfileApplyRulePreference(
					this, profile.ruleApplyEnabled, profile.daysOfWeek,
					profile.hour, profile.minutes);
			mProfileView.addPreference(mAutoApply);
			mAutoApply.setOrder(0);
		}
		ProfileUtil.setSummary(mAirplaneMode, profile.airplaneMode, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		ProfileUtil.setSummary(mWlan, profile.wifi, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		ProfileUtil.setSummary(mBluetooth, profile.bluetooth, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		ProfileUtil.setSummary(mGps, profile.gps, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		ProfileUtil.setSummary(mSilentMode, profile.silent, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		if (profile.ringerVolume == -1) {
			mRingerVolume.setSummary(getResources().getString(R.string.ignore));
		} else {
			mRingerVolume.setSummary(Integer.toString(profile.ringerVolume));
		}
		ProfileUtil.setSummary(mVibrateMode, profile.vibrate, mVibrateModeEntries, mVibrateModeValues, mVibrateModeValueSize);
		if ((profile.GSMRingtoneType != -1) && (profile.GSMRingtoneURIString != null)) {
			String type = getString(SOUND_PREFERENCES[profile.GSMRingtoneType]);
			mGSMPreference.setSummary(type + ":" + Profiles.getMediaTitle(Uri.parse(profile.GSMRingtoneURIString), this));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mId = getIntent().getIntExtra(ProfileConstants.PROFILE_ID_EXTRA, -1);
		initEntryValues();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_EDIT, 0, getResources().getString(R.string.menu_edit)).setIcon(android.R.drawable.ic_menu_edit);
		menu.add(0, MENU_APPLY, 0, getResources().getString(R.string.menu_apply)).setIcon(R.drawable.ic_menu_profile_manager);
		menu.add(0, MENU_DELETE, 0, getResources().getString(R.string.menu_delete)).setIcon(android.R.drawable.ic_menu_delete);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem)
    {
        Intent intent;
        
        intent = new Intent();
        intent.putExtra(ProfileConstants.PROFILE_ID_EXTRA, mId);
        switch(menuitem.getItemId()) {
        case MENU_EDIT:
            intent.setAction(ProfileConstants.INTENT_ACTION_EDIT);
            startActivity(intent);
        	break;
        case MENU_APPLY:
            intent.setAction(ProfileConstants.INTENT_ACTION_APPLY);
            startActivity(intent);
        	break;
        case MENU_DELETE:
            deleteProfile();
        	break;
        }
		return super.onOptionsItemSelected(menuitem);
    }

	@Override
	protected void onResume() {
		super.onResume();
		if (mProfileView != null && mAutoApply != null) {
			mProfileView.removePreference(mAutoApply);
		}
		if (mProfileView != null && mWirelessCategory != null) {
			mProfileView.removePreference(mWirelessCategory);
		}
		if (mProfileView != null && mLocationCategory != null) {
			mProfileView.removePreference(mLocationCategory);
		}
		if (mProfileView != null && mSoundCategory != null) {
			mProfileView.removePreference(mSoundCategory);
		} 
		if (mProfileView != null && mNoSetting != null) {
			mProfileView.removePreference(mNoSetting);
		}
		ContentResolver resolver = getContentResolver();
		mProfile = Profiles.getProfile(resolver, mId);
		addPreferencesFromResource(R.layout.profile_view);
		initPreference();
		updatePreference(mProfile);
		removeIgnoreItem(mProfile);
	}
}

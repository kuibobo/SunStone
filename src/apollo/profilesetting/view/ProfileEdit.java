package apollo.profilesetting.view;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import apollo.profilesetting.model.Profile;
import apollo.profilesetting.util.ProfileApplyUtil;
import apollo.profilesetting.util.ProfileConstants;
import apollo.profilesetting.util.ProfileUtil;
import apollo.profilesetting.util.Profiles;

public class ProfileEdit extends PreferenceActivity
	implements View.OnClickListener, Preference.OnPreferenceChangeListener, TimePickerDialog.OnTimeSetListener, 
	SoundPreference.OnSoundChangedListener, ChooseSoundDialog.OnSoundSelectedListener {


	enum RUNNING_MODE {
		MODE_NEW, MODE_EDIT;
	}

	private static final int MENU_GET_CURRENT = 1;
	private static final int[] SOUND_PREFERENCES = new int[] { R.string.ignore, R.string.sound_ringtones, R.string.sound_music };
	private static String[] mEnableDisableStrings;
	private static String[] mOnOffStrings = null;
	private static String[] mOnOffValues = null;
	private static String[] mVibrateModeEntries;
	private static final int mVibrateModeValueSize = 5;
	private static String[] mVibrateModeValues;
	private int lastOrient = 0;
	private ListPreference mAirplaneMode = null;
	private CheckBoxPreference mAutoApplyEnablePref = null;
	private AbstractSetAlarm.OnActivityResultListener mActivityResultListener = null;
	private ListPreference mBluetooth = null;
	private ListPreference mGps = null;
	private EditText mName;
	private final int mOnOffEntrySize = 3;
	private Profile mProfile = null;
	private int mProfileId = 0;
	private RepeatPreference mRepeatPref = null;
	private RingerVolumePreference mRingerVolume = null;
	private SoundPreference mGSMPreference;
	private PreferenceCategory mSoundCategory;
    private ChooseSoundDialog mSoundDlg;
    private String mTempSound;
	private Uri mGSMRingtone = null;
	private int mGSMRingtoneType;
	private RUNNING_MODE mRunningMode = null;
	private ListPreference mSilentMode = null;
	private Preference mTimePref = null;
	private ListPreference mVibrateMode = null;
	private ListPreference mWlan = null;
	private boolean mProfileSettingChanged = false;
	
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mProfile.hour = hourOfDay;
		mProfile.minutes = minute;
		mTimePref.setSummary(Profiles.formatTime(this, hourOfDay, minute, mRepeatPref.getDaysOfWeek()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (mRunningMode == RUNNING_MODE.MODE_NEW) {
			menu.add(0, ProfileEdit.MENU_GET_CURRENT, 0, getResources().getString(R.string.menu_get_current)).setIcon(0x1080049);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem) {
		if (mRunningMode == RUNNING_MODE.MODE_NEW) {
			menuitem.getItemId();
		} else {
			mProfile = ProfileApplyUtil.getCurrentSystemProfile(this);
			if (mProfile != null) {
				updatePreference(mProfile);
			}
		}
		return super.onOptionsItemSelected(menuitem);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object obj) {
		String key = preference.getKey();
		int i = 0;

		if (key.equals(ProfileConstants.KEY_REPEAT)) {
			mProfile.daysOfWeek = mRepeatPref.getDaysOfWeek();
		} else if (key.equals(ProfileConstants.KEY_RINGER_VOLUME)) {
			 mProfile.ringerVolume = mRingerVolume.mVolume;
			if (mProfile.ringerVolume == -1) {
				mRingerVolume.setSummary(getResources().getString(
						R.string.ignore));
			} else {
				mRingerVolume.setSummary(Integer
						.toString(mProfile.ringerVolume));
			}
		} else {
			try {
				i = Integer.parseInt((String) obj);
			} catch (NumberFormatException numberformatexception) {
			}
			updateSummary(preference, i);
		}
		mProfileSettingChanged = true;
		return true;
	}

	public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
		if (preference == this.mAutoApplyEnablePref) {
			if (this.mAutoApplyEnablePref.isChecked()) {
				Toast.makeText(this, getResources().getString(R.string.apply_profile_invalid), 1).show();
			}
			this.mProfile.ruleApplyEnabled = this.mAutoApplyEnablePref.isChecked();
		} else if (preference == this.mTimePref) {
			boolean is_format = DateFormat.is24HourFormat(this);
			new TimePickerDialog(this, this, this.mProfile.hour, this.mProfile.minutes,
					is_format).show();
		}
		return super.onPreferenceTreeClick(screen, preference);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.save_profile:
			doSaveAction();
			break;
		case R.id.discard_profile:
			doRevertAction();
			break;
		}
	}

	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);
		if (lastOrient == configuration.orientation) {
			onCreate(null);
		} else {
			lastOrient = configuration.orientation;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//FrameLayout framelayout = null;
		ListView listview = null;
		LinearLayout linearlayout = null;
		LinearLayout.LayoutParams layoutparams = null;
		View view = null;
		
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.profile_edit);
		//framelayout = (FrameLayout) getWindow().getDecorView().findViewById(16908290);
		listview = getListView();

	//	framelayout.removeView(listview);
		((ViewGroup)listview.getParent()).removeView(listview);
		linearlayout = new LinearLayout(this);
		linearlayout.setOrientation(1);
		view = LayoutInflater.from(this).inflate(R.layout.profile_edit_header, linearlayout);
		layoutparams = new LinearLayout.LayoutParams(-1, -1);
		layoutparams.weight = 1065353216;
		linearlayout.addView(listview, layoutparams);

		view = LayoutInflater.from(this).inflate(R.layout.profile_edit_save_discard, linearlayout);
		view.findViewById(R.id.save_profile).setOnClickListener(this);
		view.findViewById(R.id.discard_profile).setOnClickListener(this);
		setContentView(linearlayout);
		initEntryValues();
		initPreference();
		mName = (EditText) view.findViewById(R.id.edit_text_profile_name);
		mProfileId = getIntent().getIntExtra(ProfileConstants.PROFILE_ID_EXTRA, -1);
		if (mProfileId == -1 || mProfileId == 0) {
			mRunningMode = RUNNING_MODE.MODE_NEW;
			setTitle(R.string.title_add_profile);
		} else {
			mRunningMode = RUNNING_MODE.MODE_EDIT;
			setTitle(R.string.title_edit_profile);
		}
		if (mProfile == null)
			if (mProfileId == -1) {
				mProfile = new Profile();
			} else if (mProfileId == 0) {
				mProfile = ProfileApplyUtil.getCurrentSystemProfile(this);
			} else {
				mProfile = Profiles.getProfile(getContentResolver(), mProfileId);
			}
		if (mProfile != null) 
			updatePreference(mProfile);

		lastOrient = getResources().getConfiguration().orientation;
	}

	private void doRevertAction() {
		if (mProfileSettingChanged == true) {
			AlertDialog.Builder builder = (new AlertDialog.Builder(this))
					.setTitle(R.string.discard_confirmation_title)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(R.string.discard_confirmation)
					.setNegativeButton(android.R.string.cancel, null);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).show();
		} else {
			finish();
		}
	}

	private void showAlertDialog(Context context) {
		(new AlertDialog.Builder(context)).setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(R.string.no_profile_name)
				.setPositiveButton(android.R.string.yes, null).create().show();
	}

	private void initEntryValues() {
		Resources resources = getResources();
		mOnOffStrings = resources.getStringArray(R.array.on_off_entries);
		mOnOffValues = resources.getStringArray(R.array.on_off_values);
		mEnableDisableStrings = resources.getStringArray(R.array.enable_disable_entries);
		mVibrateModeEntries = resources.getStringArray(R.array.vibrate_entries);
		mVibrateModeValues = resources.getStringArray(R.array.vibrate_values);
	}

	private void initPreference() {
		mAutoApplyEnablePref = (CheckBoxPreference) findPreference("auto_apply_enable");
		mTimePref = findPreference("time");
		mRepeatPref = (RepeatPreference) findPreference(ProfileConstants.KEY_REPEAT);
		mAirplaneMode = (ListPreference) findPreference(ProfileConstants.KEY_AIRPLANE);
		mWlan = (ListPreference) findPreference(ProfileConstants.KEY_WLAN);
		mBluetooth = (ListPreference) findPreference(ProfileConstants.KEY_BLUETOOTH);
		mGps = (ListPreference) findPreference(ProfileConstants.KEY_GPS);
		mSilentMode = (ListPreference) findPreference(ProfileConstants.KEY_SILENT_MODE);
		mRingerVolume = (RingerVolumePreference) findPreference(ProfileConstants.KEY_RINGER_VOLUME);
		mVibrateMode = (ListPreference) findPreference(ProfileConstants.KEY_VIBRATE_MODE);
		mGSMPreference = (SoundPreference) findPreference(ProfileConstants.KEY_GSM_RINGSTONE);
		mSoundCategory = (PreferenceCategory)findPreference("sound_category");
		mRepeatPref.setOnPreferenceChangeListener(this);
		mAirplaneMode.setOnPreferenceChangeListener(this);
		mWlan.setOnPreferenceChangeListener(this);
		mBluetooth.setOnPreferenceChangeListener(this);
		mGps.setOnPreferenceChangeListener(this);
		mSilentMode.setOnPreferenceChangeListener(this);
		mRingerVolume.setOnPreferenceChangeListener(this);
		mVibrateMode.setOnPreferenceChangeListener(this);
		mGSMPreference.setOnSoundChangedListener(this);
	}
	
	private void setPreferenceDefault(ListPreference preference, int value, String strs[], String values[], int size) {
		if (strs != null && values != null) {
			int val = -1;
			for (int idx=0; idx<size; idx++) {
				val = Integer.parseInt(values[idx]);
				if (val == value) {
					preference.setValueIndex(idx);
					preference.setSummary(strs[idx]);
					break;
				}
			}
		} else {
			preference.setSummary(ProfileConstants.BLANK_SUMMARY);
		}
	}

	private void updatePreference(Profile profile) {
		mName.setText(((CharSequence) profile.name));
		mAutoApplyEnablePref.setChecked(mProfile.ruleApplyEnabled);
		mRepeatPref.setDaysOfWeek(profile.daysOfWeek);
		
		String time = Profiles.formatTime(this, mProfile.hour,
				mProfile.minutes, mRepeatPref.getDaysOfWeek());
		mTimePref.setSummary(time);
		mWlan.setEnabled(profile.airplaneMode != 1);
		mBluetooth.setEnabled(profile.airplaneMode != 1);
		mGps.setEnabled(profile.airplaneMode != 1);
		mRingerVolume.setEnabled(profile.silent == 0);
		if (profile.silent != 0) {
			mRingerVolume.setSummary(R.string.ignore);
			mRingerVolume.mVolume = -1;
		} else {
			mRingerVolume.setSummary(Integer.toString(mRingerVolume.mVolume));
		}
		if (profile.GSMRingtoneType == -1 || profile.GSMRingtoneURIString == null) {
			mGSMPreference.setSummary(R.string.ignore);
			mGSMPreference.setValue(getString(SOUND_PREFERENCES[0]));
		} else {
			mGSMRingtoneType = profile.GSMRingtoneType;
			mGSMRingtone = Uri.parse(profile.GSMRingtoneURIString);
			mGSMPreference.setSummary(getString(SOUND_PREFERENCES[profile.GSMRingtoneType]) + ": " + Profiles.getMediaTitle(mGSMRingtone, this));
			mGSMPreference.setSound(getString(SOUND_PREFERENCES[mGSMRingtoneType]));
		}
		setPreferenceDefault(mAirplaneMode, profile.airplaneMode, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		setPreferenceDefault(mWlan, profile.wifi, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		setPreferenceDefault(mBluetooth, profile.bluetooth, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		setPreferenceDefault(mGps, profile.gps, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		setPreferenceDefault(mSilentMode, profile.silent, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		setPreferenceDefault(mVibrateMode, profile.vibrate, mVibrateModeEntries, mVibrateModeValues, mVibrateModeValueSize);
	}

	private void doSaveAction() {
		int wlan_state = -1;
		int gps_state = -1;
		int bluetooth_state = -1;
		int silent_on = -1;
		int vibrate_on = -1;
		int ringer_volume = -1;
		int airplane_mode = -1;
		int ringtone_type = -1;
		String ringtone = null;
		
		String name = this.mName.getText().toString();
		if ((name == null) || (name.length() == 0)) {
			showAlertDialog(this);
			return;
		}
		airplane_mode = Integer.parseInt(this.mAirplaneMode.getValue());
		if (this.mWlan.isEnabled() ) {
			wlan_state = Integer.parseInt(mWlan.getValue());
		} else {
			wlan_state = -1;
		}
		if (mBluetooth.isEnabled()) {
			bluetooth_state = Integer.parseInt(mBluetooth.getValue());
		} else {
			bluetooth_state = -1;
		}		
		if (mGps.isEnabled()) {
			gps_state = Integer.parseInt(mGps.getValue());
		} else {
			gps_state = -1;
		}
		if (mRingerVolume.isEnabled()) {
			ringer_volume = mRingerVolume.mVolume;
		} else {
			ringer_volume = -1;
		}
		silent_on = Integer.parseInt(this.mSilentMode.getValue());
		vibrate_on = Integer.parseInt(this.mVibrateMode.getValue());
		if (mGSMRingtoneType != -1 && mGSMRingtone != null) {
			ringtone_type = mGSMRingtoneType + 1;
			ringtone = mGSMRingtone.toString();
		}
		this.mProfileId = Profiles.setProfile(this, mProfileId, name,
				airplane_mode, wlan_state, gps_state, bluetooth_state,
				silent_on, vibrate_on, ringer_volume,
				this.mProfile.ruleApplyEnabled, this.mProfile.hour,
				this.mProfile.minutes, this.mRepeatPref.getDaysOfWeek(), ringtone_type, ringtone);

		if (this.mProfileId > 0) {
			if (this.mProfileId == ProfileUtil.getActivatedProfileId(this)) {
				ProfileUtil.startApplyProfile(this, this.mProfileId);
			}
		}
		finish();
	}

	private void updateSummary(Preference preference, int value) {
		if (preference == mVibrateMode) {
			ProfileUtil.setSummary(preference, value, mVibrateModeEntries, mVibrateModeValues, mVibrateModeValueSize);
			mProfile.vibrate = value;
		} else {
			ProfileUtil.setSummary(preference, value, mOnOffStrings, mOnOffValues, mOnOffEntrySize);
		}
		if (preference == mAirplaneMode) {
			mProfile.airplaneMode = value;
			mWlan.setEnabled(mProfile.airplaneMode == 0);
			mBluetooth.setEnabled(mProfile.airplaneMode == 0);
			mGps.setEnabled(mProfile.airplaneMode == 0);
		} 
		if (preference == mSilentMode) {
			mRingerVolume.setEnabled(value == 0);
			mProfile.silent = value;
		}
		if (preference == mWlan) {
			mProfile.wifi = value;
		} else {
			if (preference == mBluetooth) {
				mProfile.bluetooth = value;
			} else {
				if (preference == mGps)
					mProfile.gps = value;
			}
		}
	}

	@Override
	public void onBackPressed() {
		doRevertAction();
	}
	
	private void updateSound(String s) {
		if (s.equalsIgnoreCase("gsm_ringtone_settings")) {
			if (mGSMRingtoneType != -1) {
				mTempSound = getString(SOUND_PREFERENCES[mGSMRingtoneType + 1]);
				mGSMPreference.setSound(mTempSound);
				if (mGSMRingtone != null) {
					mGSMPreference.setSummary(mTempSound + ":" + Profiles.getMediaTitle(mGSMRingtone, this));
				}
			} else {
				mGSMPreference.setSummary(getString(R.string.ignore));
				mGSMPreference.setValueIndex(0);
			}
		}
	}
	
	public void onActivityResult(int i, int j, Intent intent) {
		if (this.mActivityResultListener != null) {
			this.mActivityResultListener.onActivityResult(i, j, intent);
		}
	}
	
	public void addOnActivityResultListener(
			AbstractSetAlarm.OnActivityResultListener listener) {
		this.mActivityResultListener = listener;
	}

	public void removeOnActivityResultListener() {
		this.mActivityResultListener = null;
	}

	@Override
	public void onSelectedChanged(Uri uri, int type, String key) {
		if (key.equalsIgnoreCase("gsm_ringtone_settings")) {
			if (type != ChooseSoundDialog.DIALOG_RINGTONE) {
				mGSMRingtone = uri;
				mGSMRingtoneType = type - 1;
				mGSMPreference.setAlert(uri);
			} else {
				mGSMRingtoneType = -1;
			}
		}
		updateSound(key);
	}

	@Override
	public void onSoundChanged(String value, int idx, String key) {
		if (idx != ChooseSoundDialog.DIALOG_RINGTONE) {
			if (key.equalsIgnoreCase("gsm_ringtone_settings")) {
				mSoundDlg = new ChooseSoundDialog(
						this, mGSMRingtone);
				mSoundDlg.setOnSoundSelectedListener(this, key);
			}
			mSoundDlg.showDialog(idx - 1);
		} else {
			onSelectedChanged(null, ChooseSoundDialog.DIALOG_RINGTONE, key);
		}
	}
}

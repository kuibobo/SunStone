package apollo.profilesetting.view;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class SoundPreference extends ListPreference {

	public abstract interface OnSoundChangedListener {
		public abstract void onSoundChanged(String value, int idx, String key);
	}

	public static final int DIALOG_MUSIC = 2;
	public static final int DIALOG_RINGTONE = 1;
	public static final int IGNORE_THIS_SETTING = 0;
	static final int SOUND_PREFERENCES[] = { R.string.ignore, R.string.sound_ringtones, R.string.sound_music };
	private Uri mAlert;
	private String mPreSound;
	private String mSound;
	private String mSoundPreferences[];
	private OnSoundChangedListener mSoundChangedListener;
	
	public SoundPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mSoundPreferences = new String[3];
		mSoundPreferences[IGNORE_THIS_SETTING] = context.getString(SOUND_PREFERENCES[IGNORE_THIS_SETTING]);
		mSoundPreferences[DIALOG_RINGTONE] = context.getString(SOUND_PREFERENCES[DIALOG_RINGTONE]);
		mSoundPreferences[DIALOG_MUSIC] = context.getString(SOUND_PREFERENCES[DIALOG_MUSIC]);
		setDialogIcon(R.drawable.ic_dialog_sound);
	}

	public Uri getAlert() {
		return mAlert;
	}

	public String getSoundText() {
		return mSound;
	}

	protected void onClick() {
		mPreSound = getValue();
		super.onClick();
	}

	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			String value = getValue();
			String key = getKey();
			int idx = findIndexOfValue(value);
			mSound = mPreSound;
			setValue(mSound);
			mSoundChangedListener.onSoundChanged(value, idx, key);
		}
	}

	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		setEntries(mSoundPreferences);
		setEntryValues(mSoundPreferences);
		super.onPrepareDialogBuilder(builder);
	}

	public void setAlert(Uri uri) {
		mAlert = uri;
	}

	public void setOnSoundChangedListener(OnSoundChangedListener listener) {
		mSoundChangedListener = listener;
	}

	public void setSound(String sound) {
		mSound = sound;
		setValue(mSound);
	}

}

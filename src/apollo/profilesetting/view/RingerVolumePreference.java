package apollo.profilesetting.view;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.SeekBar;
import apollo.preference.VolumePreferenceEx;

public class RingerVolumePreference extends VolumePreferenceEx {

	class SavedState extends BaseSavedState {
		VolumePreferenceEx.VolumeStore mVolumeStore;
		
		public final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
		
		public SavedState(Parcel dest) {
			super(dest);
			mVolumeStore = new VolumePreferenceEx.VolumeStore();
			mVolumeStore.volume = dest.readInt();
			mVolumeStore.originalVolume = dest.readInt();
		}
		
		VolumePreferenceEx.VolumeStore getVolumeStore() {
			if (mVolumeStore == null) {
				mVolumeStore = new VolumePreferenceEx.VolumeStore();
			}
			return mVolumeStore;
		}

		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(mVolumeStore.volume);
			dest.writeInt(mVolumeStore.originalVolume);
		}
	}

	private static final int SEEKBAR_TYPE = 2;
	private View.OnClickListener Checkbox_igorethissetting_Listener;
	private boolean isIgnored;
	private CheckBox mCheckBoxIngoreThisSetting;
	private VolumePreferenceEx.SeekBarVolumizer mSeekBarVolumizer;
	public int mVolume;
	private SeekBar seekBar;
	
	public RingerVolumePreference(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		Checkbox_igorethissetting_Listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCheckBoxIngoreThisSetting.isChecked()) {
					if (seekBar != null) {
						seekBar.setEnabled(false);
						isIgnored = true;
					}
				} else {
					if (seekBar != null) {
						seekBar.setEnabled(true);
						mSeekBarVolumizer.stopSample();
						isIgnored = false;
					}
				}
			}
		};
		AudioManager audio =  (AudioManager)context.getSystemService(Context.AUDIO_SERVICE); 
		mVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
		setStreamType(SEEKBAR_TYPE);
		setDialogLayoutResource(R.layout.preference_dialog_ringervolume);
		setDialogIcon(R.drawable.ic_settings_sound);
	}

	private void cleanup() {
		if (mSeekBarVolumizer != null) {
			Dialog dialog = getDialog();
			if (dialog != null && dialog.isShowing())
				mSeekBarVolumizer.revertVolume();
			mSeekBarVolumizer.stop();
			mSeekBarVolumizer = null;
		}
	}

	protected void onBindDialogView(View view) {
		Context context = getContext();

		seekBar = (SeekBar) view.findViewById(R.id.seekbar);
		mSeekBarVolumizer = new VolumePreferenceEx.SeekBarVolumizer(context, seekBar, SEEKBAR_TYPE);
		mSeekBarVolumizer.getSeekBar().setProgress(mVolume);
		mCheckBoxIngoreThisSetting = (CheckBox)view.findViewById(R.id.ignore_ringervolume_setting);
		mCheckBoxIngoreThisSetting.setOnClickListener(Checkbox_igorethissetting_Listener);
		if (mVolume == -1) {
			mCheckBoxIngoreThisSetting.setChecked(true);
			seekBar.setEnabled(false);
			isIgnored = true;
		}
	}

	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			mVolume = mSeekBarVolumizer.getSeekBar().getProgress();
			if (isIgnored) {
				mVolume = -1;
				isIgnored = false;
			}
			mSeekBarVolumizer.revertVolume();
			callChangeListener(mVolume);
		}
		cleanup();
	}

	protected void onSampleStarting(VolumePreferenceEx.SeekBarVolumizer seekbarvolumizer) {
		super.onSampleStarting(seekbarvolumizer);
		seekbarvolumizer.stopSample();
	}
}

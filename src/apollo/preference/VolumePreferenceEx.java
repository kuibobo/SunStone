package apollo.preference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.preference.Preference.BaseSavedState;
import android.preference.PreferenceManager.OnActivityStopListener;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import apollo.profilesetting.view.R;

public class VolumePreferenceEx extends SeekBarPreferenceEx implements
		PreferenceManager.OnActivityStopListener, View.OnKeyListener {

    private int mStreamType;
    private SeekBarVolumizer mSeekBarVolumizer;
    
	public VolumePreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		Class<?> clazz = null;
		int[] styleable = null;
		Field field = null;
		int stream_type = 0;
		
		try {
			clazz = Class.forName("android.R$styleable");
			field = clazz.getField("VolumePreference");  
			styleable = (int[])field.get(clazz);
			
			field = clazz.getField("VolumePreference_streamType"); 
			stream_type = field.getInt(clazz);
		} catch (Exception ex) {
			styleable = new int[]{16843273};
		}

		// TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VolumePreference, 0, 0);
		// mStreamType = a.getInt(R.styleable.VolumePreference_streamType, 0);
		// a.recycle();

		//int[] styleable = {16843273};
		
		
		TypedArray a = context.obtainStyledAttributes(attrs, styleable, 0, 0);
		mStreamType = a.getInt(stream_type, 0);
		a.recycle();
	}
	
    public void setStreamType(int streamType) {
        mStreamType = streamType;
    }
    
    private void callActivityStopListener(String name) {
    	Method method = null;
    	PreferenceManager manager = null;
    	Class<?>[] clazzs = null;
    	Object[] objs = null;
    	
    	clazzs = new Class<?>[1];
    	clazzs[0] = OnActivityStopListener.class;
    	
    	objs = new Object[1];
    	objs[0] = this;
    	
    	manager = this.getPreferenceManager();
    	try {
			method = PreferenceManager.class.getDeclaredMethod(name, clazzs);
			method.invoke(manager, objs);
		} catch (Exception ex) {
		}
    }
    
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);       
        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBarVolumizer = new SeekBarVolumizer(getContext(), seekBar, mStreamType);

        callActivityStopListener("registerOnActivityStopListener");

        // grab focus and key events so that pressing the volume buttons in the
        // dialog doesn't also show the normal volume adjust toast.
        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (mSeekBarVolumizer == null) return true;
        boolean isdown = (event.getAction() == KeyEvent.ACTION_DOWN);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (isdown) {
                    mSeekBarVolumizer.changeVolumeBy(-1);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (isdown) {
                    mSeekBarVolumizer.changeVolumeBy(1);
                }
                return true;
            default:
                return false;
        }
	}
	
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (!positiveResult && mSeekBarVolumizer != null) {
            mSeekBarVolumizer.revertVolume();
        }

        cleanup();
    }

	@Override
	public void onActivityStop() {
        cleanup();
	}

	private void cleanup() {
		callActivityStopListener("unregisterOnActivityStopListener");

	       if (mSeekBarVolumizer != null) {
	           Dialog dialog = getDialog();
	           if (dialog != null && dialog.isShowing()) {
	               View view = dialog.getWindow().getDecorView()
	                       .findViewById(R.id.seekbar);
	               if (view != null) view.setOnKeyListener(null);
	               // Stopped while dialog was showing, revert changes
	               mSeekBarVolumizer.revertVolume();
	           }
	           mSeekBarVolumizer.stop();
	           mSeekBarVolumizer = null;
	       }

	    }

	    protected void onSampleStarting(SeekBarVolumizer volumizer) {
	        if (mSeekBarVolumizer != null && volumizer != mSeekBarVolumizer) {
	            mSeekBarVolumizer.stopSample();
	        }
	    }

	    @Override
	    protected Parcelable onSaveInstanceState() {
	        final Parcelable superState = super.onSaveInstanceState();
	        if (isPersistent()) {
	            // No need to save instance state since it's persistent
	            return superState;
	        }

	        final SavedState myState = new SavedState(superState);
	        if (mSeekBarVolumizer != null) {
	            mSeekBarVolumizer.onSaveInstanceState(myState.getVolumeStore());
	        }
	        return myState;
	    }

	    @Override
	    protected void onRestoreInstanceState(Parcelable state) {
	        if (state == null || !state.getClass().equals(SavedState.class)) {
	            // Didn't save state for us in onSaveInstanceState
	            super.onRestoreInstanceState(state);
	            return;
	        }

	        SavedState myState = (SavedState) state;
	        super.onRestoreInstanceState(myState.getSuperState());
	        if (mSeekBarVolumizer != null) {
	            mSeekBarVolumizer.onRestoreInstanceState(myState.getVolumeStore());
	        }
	    }

	    public static class VolumeStore {
	        public int volume = -1;
	        public int originalVolume = -1;
	    }

	    private static class SavedState extends BaseSavedState {
	        VolumeStore mVolumeStore = new VolumeStore();

	        public SavedState(Parcel source) {
	            super(source);
	            mVolumeStore.volume = source.readInt();
	            mVolumeStore.originalVolume = source.readInt();
	        }

	        @Override
	        public void writeToParcel(Parcel dest, int flags) {
	            super.writeToParcel(dest, flags);
	            dest.writeInt(mVolumeStore.volume);
	            dest.writeInt(mVolumeStore.originalVolume);
	        }

	        VolumeStore getVolumeStore() {
	            return mVolumeStore;
	        }

	        public SavedState(Parcelable superState) {
	            super(superState);
	        }

	        public static final Parcelable.Creator<SavedState> CREATOR =
	                new Parcelable.Creator<SavedState>() {
	            public SavedState createFromParcel(Parcel in) {
	                return new SavedState(in);
	            }

	            public SavedState[] newArray(int size) {
	                return new SavedState[size];
	            }
	        };
	    }

	    /**
	     * Turns a {@link SeekBar} into a volume control.
	     */
	    public class SeekBarVolumizer implements OnSeekBarChangeListener, Runnable {

	        private Context mContext;
	        private Handler mHandler = new Handler();
	    
	        private AudioManager mAudioManager;
	        private int mStreamType;
	        private int mOriginalStreamVolume; 
	        private Ringtone mRingtone;
	    
	        private int mLastProgress = -1;
	        private SeekBar mSeekBar;
	        
	        private ContentObserver mVolumeObserver = new ContentObserver(mHandler) {
	            @Override
	            public void onChange(boolean selfChange) {
	                super.onChange(selfChange);
	                if (mSeekBar != null) {
	                    int volume = System.getInt(mContext.getContentResolver(),
	                            System.VOLUME_SETTINGS[mStreamType], -1);
	                    // Works around an atomicity problem with volume updates
	                    // TODO: Fix the actual issue, probably in AudioService
	                    if (volume >= 0) {
	                        mSeekBar.setProgress(volume);
	                    }
	                }
	            }
	        };

	        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType) {
	            mContext = context;
	            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	            mStreamType = streamType;
	            mSeekBar = seekBar;
	            
	            initSeekBar(seekBar);
	        }

	        private void initSeekBar(SeekBar seekBar) {
	            seekBar.setMax(mAudioManager.getStreamMaxVolume(mStreamType));
	            mOriginalStreamVolume = mAudioManager.getStreamVolume(mStreamType);
	            seekBar.setProgress(mOriginalStreamVolume);
	            seekBar.setOnSeekBarChangeListener(this);
	            
	            mContext.getContentResolver().registerContentObserver(
	                    System.getUriFor(System.VOLUME_SETTINGS[mStreamType]),
	                    false, mVolumeObserver);
	    
	            Uri defaultUri = null;
	            if (mStreamType == AudioManager.STREAM_RING) {
	                defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
	            } else if (mStreamType == AudioManager.STREAM_NOTIFICATION) {
	                defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
	            } else {
	                defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
	            }

	            mRingtone = RingtoneManager.getRingtone(mContext, defaultUri);
	            if (mRingtone != null) {
	                mRingtone.setStreamType(mStreamType);
	            }
	        }
	        
	        public void stop() {
	            stopSample();
	            mContext.getContentResolver().unregisterContentObserver(mVolumeObserver);
	            mSeekBar.setOnSeekBarChangeListener(null);
	        }
	        
	        public void revertVolume() {
	            mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume, 0);
	        }
	        
	        public void onProgressChanged(SeekBar seekBar, int progress,
	                boolean fromTouch) {
	            if (!fromTouch) {
	                return;
	            }
	    
	            postSetVolume(progress);
	        }

	        void postSetVolume(int progress) {
	            // Do the volume changing separately to give responsive UI
	            mLastProgress = progress;
	            mHandler.removeCallbacks(this);
	            mHandler.post(this);
	        }
	    
	        public void onStartTrackingTouch(SeekBar seekBar) {
	        }

	        public void onStopTrackingTouch(SeekBar seekBar) {
	            if (mRingtone != null && !mRingtone.isPlaying()) {
	                sample();
	            }
	        }
	        
	        public void run() {
	            mAudioManager.setStreamVolume(mStreamType, mLastProgress, 0);
	        }
	        
	        private void sample() {
	            onSampleStarting(this);
	            mRingtone.play();
	        }
	    
	        public void stopSample() {
	            if (mRingtone != null) {
	                mRingtone.stop();
	            }
	        }

	        public SeekBar getSeekBar() {
	            return mSeekBar;
	        }
	        
	        public void changeVolumeBy(int amount) {
	            mSeekBar.incrementProgressBy(amount);
	            if (mRingtone != null && !mRingtone.isPlaying()) {
	                sample();
	            }
	            postSetVolume(mSeekBar.getProgress());
	        }

	        public void onSaveInstanceState(VolumeStore volumeStore) {
	            if (mLastProgress >= 0) {
	                volumeStore.volume = mLastProgress;
	                volumeStore.originalVolume = mOriginalStreamVolume;
	            }
	        }

	        public void onRestoreInstanceState(VolumeStore volumeStore) {
	            if (volumeStore.volume != -1) {
	                mOriginalStreamVolume = volumeStore.originalVolume;
	                mLastProgress = volumeStore.volume;
	                postSetVolume(mLastProgress);
	            }
	        }
	    }
}

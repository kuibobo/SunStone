package apollo.profilesetting.view;

import java.text.DateFormatSymbols;
import java.util.Calendar;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import apollo.profilesetting.util.Profiles;

public class DigitalClock extends LinearLayout {
	class FormatChangeObserver extends ContentObserver {
		public FormatChangeObserver() {
	        super(new Handler());
		}

		public void onChange(boolean b) {
		      setDateFormat();
		      updateTime();
		}

	}

	class AmPm {
		private TextView mAmPm;
		private String mAmString;
		private String mPmString;

		AmPm(View parent) {
            mAmPm = (TextView) parent.findViewById(R.id.am_pm);

            String[] ampm = new DateFormatSymbols().getAmPmStrings();
            mAmString = ampm[0];
            mPmString = ampm[1];
		}

        void setShowAmPm(boolean show) {
            mAmPm.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        void setIsMorning(boolean isMorning) {
            mAmPm.setText(isMorning ? mAmString : mPmString);
        }
	}

	private static final String M12 = "h:mm";
	private AmPm mAmPm;
	private boolean mAttached;
	private boolean mLive;
	private Calendar mCalendar;
	private String mFormat;
	private TextView mTimeDisplay;
	private ContentObserver mFormatChangeObserver;
	private final Handler mHandler;
	private final BroadcastReceiver mIntentReceiver;

	public DigitalClock(Context context) {
		this(context, null);
	}

	public DigitalClock(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mHandler = new Handler();
		this.mIntentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (mLive&& intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
					mCalendar = Calendar.getInstance();
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						updateTime();
					}
				});
			}
		};
	}

	private void setDateFormat() {
//		boolean flag = Profiles.get24HourMode(getContext());
//		if (flag)
//			mFormat = "kk:mm";
//		else
//			mFormat = M12;
//		mAmPm.setShowAmPm(mFormat.equals("h:mm"));
		
        mFormat = Profiles.get24HourMode(getContext()) ? "kk:mm" : M12;
        mAmPm.setShowAmPm(mFormat == M12);
	}

	private void updateTime() {
        if (mLive) {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
        }

        CharSequence newTime = DateFormat.format(mFormat, mCalendar);
        mTimeDisplay.setText(newTime);
        mAmPm.setIsMorning(mCalendar.get(Calendar.AM_PM) == 0);
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (!mAttached) {
			mAttached = true;
			if (mLive) {
	            IntentFilter filter = new IntentFilter();
	            filter.addAction(Intent.ACTION_TIME_TICK);
	            filter.addAction(Intent.ACTION_TIME_CHANGED);
	            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
	            getContext().registerReceiver(mIntentReceiver, filter);
			}
	        mFormatChangeObserver = new FormatChangeObserver();
	        getContext().getContentResolver().registerContentObserver(
	                Settings.System.CONTENT_URI, true, mFormatChangeObserver);
			updateTime();
		}
	}

	protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (!mAttached) return;
        mAttached = false;

        if (mLive) {
            getContext().unregisterReceiver(mIntentReceiver);
        }
        getContext().getContentResolver().unregisterContentObserver(
                mFormatChangeObserver);
	}

	protected void onFinishInflate() {
        super.onFinishInflate();

        mTimeDisplay = (TextView) findViewById(R.id.timeDisplay);
        mAmPm = new AmPm(this);
        mCalendar = Calendar.getInstance();
        setDateFormat();
	}

    void setLive(boolean live) {
        mLive = live;
    }

    void setTypeface(Typeface tf) {
        mTimeDisplay.setTypeface(tf);
    }


	void updateTime(Calendar c) {
		mCalendar = c;
		updateTime();
	}

}

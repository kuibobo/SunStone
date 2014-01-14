package apollo.profilesetting.view;

import java.util.Calendar;

import apollo.profilesetting.view.R;


import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import apollo.profilesetting.model.Profile;

public class ProfileApplyRulePreference extends Preference {
	private int mHour;
	private boolean mIsRuleApplied;
	private int mMinute;
	private Profile.DaysOfWeek mRule;

	public ProfileApplyRulePreference(Context context) {
		super(context);
		init();
	}

	public ProfileApplyRulePreference(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		init();
	}

	public ProfileApplyRulePreference(Context context, AttributeSet attributeset, int i) {
		super(context, attributeset, i);
		init();
	}

	public ProfileApplyRulePreference(Context context, boolean applied,
			Profile.DaysOfWeek daysofweek, int hour, int minute) {
		super(context);
		init();
		mIsRuleApplied = applied;
		mRule = daysofweek;
		mHour = hour;
		mMinute = minute;
	}

	private void init() {
		setLayoutResource(R.layout.profile_auto_apply_preference_layout);
		setTitle(R.string.auto_apply_enable);
	}

	@Override
	public View getView(View view, ViewGroup viewgroup) {
		View parent_view = null;
		TextView textview = null;
		ProfileDigitalClock clock = null;
		boolean flag = false;
		
		String s = "   2131099745";
		parent_view = super.getView(view, viewgroup);
		clock = (ProfileDigitalClock) parent_view
				.findViewById(R.id.digitalClock);
		textview = (TextView) clock.findViewById(R.id.daysOfWeek);
		
		if (mIsRuleApplied && mRule.isRepeatSet()) {
			Calendar calendar = null;
			Context context = null;
			
			clock.setLive(flag);
			calendar = Calendar.getInstance();
			calendar.set(11, mHour);
			calendar.set(12, mMinute);
			clock.updateTime(calendar);
			
			context = getContext();
			String s1 = mRule.toString(context, flag);
			if (s1 != null && s1.length() != 0) {
				String s2 ="   " + s1;
				textview.setText(s2);
			} else {
				textview.setText(s);
			}
		} else {
			textview.setText(s);
		}
		return parent_view;
	}
}

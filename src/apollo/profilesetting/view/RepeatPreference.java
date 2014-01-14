package apollo.profilesetting.view;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import apollo.profilesetting.model.Profile;

public class RepeatPreference extends ListPreference {
	private Profile.DaysOfWeek mDaysOfWeek;
	private Profile.DaysOfWeek mNewDaysOfWeek;

	public RepeatPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.mDaysOfWeek = new Profile.DaysOfWeek(0);
		this.mNewDaysOfWeek = new Profile.DaysOfWeek(0);
		
        String[] weekdays = new DateFormatSymbols().getWeekdays();
        String[] values = new String[] {
            weekdays[Calendar.MONDAY],
            weekdays[Calendar.TUESDAY],
            weekdays[Calendar.WEDNESDAY],
            weekdays[Calendar.THURSDAY],
            weekdays[Calendar.FRIDAY],
            weekdays[Calendar.SATURDAY],
            weekdays[Calendar.SUNDAY],
        };
        setEntries(values);
        setEntryValues(values);
	}

	public Profile.DaysOfWeek getDaysOfWeek() {
		return this.mDaysOfWeek;
	}

	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			mDaysOfWeek.set(mNewDaysOfWeek);
			setSummary(mDaysOfWeek.toString(getContext(), true));
			callChangeListener(mDaysOfWeek);
		}
	}

	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		CharSequence acharsequence[] = getEntries();
		builder.setMultiChoiceItems(acharsequence, mDaysOfWeek.getBooleanArray(), new DialogInterface.OnMultiChoiceClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which,
					boolean isChecked) {
				mNewDaysOfWeek.set(which, isChecked);
			}
		});
	}

	public void setDaysOfWeek(Profile.DaysOfWeek daysofweek) {
		mDaysOfWeek.set(daysofweek);
		mNewDaysOfWeek.set(daysofweek);
		setSummary(daysofweek.toString(getContext(), true));
	}
}

package apollo.profilesetting.view;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import apollo.profilesetting.model.Profile;
import apollo.profilesetting.util.ProfileConstants;
import apollo.profilesetting.util.ProfileUtil;
import apollo.profilesetting.util.Profiles;

public class ProfilePreference extends Preference implements
		OnCheckedChangeListener, View.OnClickListener,
		View.OnLongClickListener, DialogInterface.OnClickListener {

	private static CompoundButton mCurrentChecked;
	private static String mSelectedKey;
	private int mCurrentKey;
	private int mHour;
	private boolean mIsRuleApplied;
	private int mMinute;
	private boolean mProtectFromCheckedChange;
	private int mRule;

	static {
		mCurrentChecked = null;
	}

	public ProfilePreference(Context context) {
		super(context);
		init();
	}

	public ProfilePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ProfilePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ProfilePreference(Context context, boolean flag, int rule, int hour, int minute) {
		super(context);
		init();
		mIsRuleApplied = flag;
		mRule = rule;
		mHour = hour;
		mMinute = minute;
	}

	public static void clearSelected() {
		mSelectedKey = null;
		mCurrentChecked = null;
	}

	public boolean isChecked() {
		return getKey().equals(mSelectedKey);
	}

	public void setChecked() {
		mSelectedKey = getKey();
	}

	public void onClick(View view) {
		if (view != null) {
			int id = view.getId();
			if (android.R.attr.theme == id && getContext() != null) {
				mCurrentKey = Integer.parseInt(getKey());
				if (mCurrentKey == 0) {
					Intent intent = new Intent();
					intent.setAction(ProfileConstants.INTENT_ACTION_EDIT);
					intent.putExtra(ProfileConstants.PROFILE_ID_EXTRA, 0);
					getContext().startActivity(intent);
				} else {
					viewProfile();
				}
			}
		}
	}

	public boolean onLongClick(View view) {
		this.mCurrentKey = Integer.parseInt(getKey());
		if (view != null) {
			int id = view.getId();
			if ((android.R.attr.theme == id) && (this.mCurrentKey != 0))
				createPopUpMenu();
		}
		return true;
	}

	private void viewProfile() {
		Intent intent = null;

		intent = new Intent();
		intent.putExtra(ProfileConstants.PROFILE_ID_EXTRA, mCurrentKey);
		intent.setAction(ProfileConstants.INTENT_ACTION_VIEW);
		getContext().startActivity(intent);
	}

	@Override
	public View getView(View view, ViewGroup viewgroup) {
		LinearLayout line_layout = null;
		RadioButton radiobutton = null;
		RelativeLayout rela_layout = null;
		ProfileDigitalClock clock = null;
		ImageView imgView = null;
		
		line_layout = (LinearLayout)super.getView(view, viewgroup);
		
		imgView = (ImageView)line_layout.findViewById(R.id.profileIcon);
		if (imgView != null) {
			imgView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_menu_backup));
		}
		
		radiobutton = (RadioButton)line_layout.findViewById(android.R.attr.label);
		if (radiobutton != null) {
			radiobutton.setOnCheckedChangeListener(this);
			String key = getKey();
			boolean selected = key.equals(mSelectedKey);
			if (selected) {
				mCurrentChecked = radiobutton;
				mSelectedKey = getKey();
			}
			mProtectFromCheckedChange = true;
			radiobutton.setChecked(selected);
			mProtectFromCheckedChange = false;
		}

		rela_layout = (RelativeLayout)line_layout.findViewById(android.R.attr.theme);
		if (rela_layout != null) {
			rela_layout.setOnClickListener(this);
			rela_layout.setOnLongClickListener(this);
		}
		clock = (ProfileDigitalClock) line_layout.findViewById(R.id.digitalClock);
		if (mIsRuleApplied && mRule != 0) {
			Calendar calendar = null;
			TextView textview = null;
			String week_str = null;
			
			clock.setLive(false);
			calendar = Calendar.getInstance();
			calendar.set(11, mHour);
			calendar.set(12, mMinute);
			clock.updateTime(calendar);
			textview = (TextView) clock.findViewById(R.id.daysOfWeek);
			Profile.DaysOfWeek daysofweek = new Profile.DaysOfWeek(mRule);
			week_str = daysofweek.toString(getContext(), false);
			if (week_str != null && week_str.length() != 0) {
				textview.setText("   " + week_str);
				textview.setVisibility(0);
			} else {
				textview.setVisibility(8);
			}
		} else {
			clock.setVisibility(4);
		}
		return line_layout;
	}

	private void init() {
		super.setLayoutResource(R.layout.profile_preference_layout);
		
	}

	private void deleteProfile() {
		AlertDialog.Builder dlg = null;
		final Intent intent = new Intent(ProfileConstants.INTENT_ACTION_DELETE);
		final Context context = getContext();

		dlg = new AlertDialog.Builder(context);
		dlg.setTitle(getContext().getResources().getString(R.string.delete_profile));
		dlg.setMessage(getContext().getResources().getString(R.string.delete_profile_confirm));
		dlg.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int activated_profile_key = ProfileUtil.getActivatedProfileId(context);

				if (activated_profile_key == mCurrentKey) {
					AlertDialog.Builder dlg = null;

					dlg = new AlertDialog.Builder(context);
					dlg.setTitle(context.getResources().getString(R.string.delete_activited_profile));
					dlg.setMessage(context.getResources().getString(R.string.delete_activited_profile_confirm));
					dlg.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									Profiles.deleteProfile(context, mCurrentKey);
									context.sendBroadcast(intent);
								}
							}).setNegativeButton(android.R.string.cancel, null).show();
					
				} else {
					Profiles.deleteProfile(context, mCurrentKey);
					context.sendBroadcast(intent);
				}
			}
		}).setNegativeButton(android.R.string.cancel, null).show();
	}

	private void createPopUpMenu() {
		AlertDialog.Builder dlg = null;

		dlg = new AlertDialog.Builder(getContext());
		dlg.setTitle(getTitle()).setItems(R.array.select_dialog_items, this).create().show();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (!mProtectFromCheckedChange)
			if (isChecked) {
				if (mCurrentChecked != null) {
			          mCurrentChecked.setChecked(false);
				}
				mCurrentChecked = buttonView;
				mSelectedKey = getKey();
				callChangeListener(mSelectedKey);
			} else {
				mCurrentChecked = null;
				mSelectedKey = null;
			}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Intent intent = null;

		intent = new Intent();
		intent.putExtra(ProfileConstants.PROFILE_ID_EXTRA, this.mCurrentKey);
		switch (which) {
		case 0:
			intent.setAction(ProfileConstants.INTENT_ACTION_VIEW);
			getContext().startActivity(intent);
			break;
		case 1:
			intent.setAction(ProfileConstants.INTENT_ACTION_EDIT);
			getContext().startActivity(intent);
			break;
		case 2:
			deleteProfile();
			break;
		case 3:
			intent.setAction(ProfileConstants.INTENT_ACTION_APPLY);
			getContext().startActivity(intent);
			break;
		}
	}
}

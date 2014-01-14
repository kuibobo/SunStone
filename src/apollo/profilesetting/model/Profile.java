package apollo.profilesetting.model;

import java.util.Calendar;

import apollo.profilesetting.view.R;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import apollo.profilesetting.util.ProfileConstants;

public final class Profile implements Parcelable {

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
		@Override
		public Profile createFromParcel(Parcel source) {
			return new Profile(source);
		}
		@Override
		public Profile[] newArray(int size) {
			return new Profile[size];
		}
    };
    
    public DaysOfWeek daysOfWeek;
    public String name;
    public boolean ruleApplyEnabled;
    public long nexttime = 0;
    public int airplaneMode = -1;
    public int bluetooth = -1;
    public int gps = -1;
    public int hour = 12;
    public int id = -1;
    public int minutes = 0;
    public int ringerVolume = -1;
    public int silent = -1;
    public int vibrate = -1;
    public int wifi = -1;
    public int GSMRingtoneType = -1;
    public String GSMRingtoneURIString;
    
	public Profile() {
	    this.ruleApplyEnabled = false;
	    this.hour = 12;
	    this.minutes = 0;
	    this.daysOfWeek = new DaysOfWeek(0x7F);
	}

	public Profile(Cursor cursor) {
		super();
		
		id = cursor.getInt(Profile.ProfileColumns.PROFILE_ID);
		name = cursor.getString(Profile.ProfileColumns.PROFILE_NAME);
		ruleApplyEnabled = cursor.getInt(Profile.ProfileColumns.PROFILE_APPLIED) == 1;
		hour = cursor.getInt(Profile.ProfileColumns.PROFILE_RULE_HOUR);
		minutes = cursor.getInt(Profile.ProfileColumns.PROFILE_RULE_MINITUE);
		daysOfWeek = new DaysOfWeek(cursor.getInt(Profile.ProfileColumns.PROFILE_RULE_REPEAT));
		airplaneMode = cursor.getInt(Profile.ProfileColumns.PROFILE_AIRPLANE_MODE);
		wifi = cursor.getInt(Profile.ProfileColumns.PROFILE_WLAN_ON);
		bluetooth = cursor.getInt(Profile.ProfileColumns.PROFILE_BLUETOOTH_ON);
		gps = cursor.getInt(Profile.ProfileColumns.PROFILE_GPS_ON);
		silent = cursor.getInt(Profile.ProfileColumns.PROFILE_SILENT_ON);
		ringerVolume = cursor.getInt(Profile.ProfileColumns.PROFILE_RINGER_VOLUME);
		vibrate = cursor.getInt(Profile.ProfileColumns.PROFILE_VIBRATE_ON);
		GSMRingtoneType = cursor.getInt(Profile.ProfileColumns.PROFILE_GSM_RINGTONE_TYPE);
		GSMRingtoneURIString = cursor.getString(Profile.ProfileColumns.PROFILE_GSM_RINGTONE_URISTRING);
	}

	public Profile(Parcel parcel) {
		id = parcel.readInt();
		name = parcel.readString();
		ruleApplyEnabled = parcel.readInt() == 1;
		hour = parcel.readInt();
		minutes = parcel.readInt();
		nexttime = parcel.readLong();
		daysOfWeek = new DaysOfWeek(parcel.readInt());
		airplaneMode = parcel.readInt();
		wifi = parcel.readInt();
		bluetooth = parcel.readInt();
		gps = parcel.readInt();
		silent = parcel.readInt();
		ringerVolume = parcel.readInt();
		vibrate = parcel.readInt();
		GSMRingtoneType = parcel.readInt();
		GSMRingtoneURIString = parcel.readString();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(id);
		parcel.writeString(name);
		parcel.writeInt(hour);
		parcel.writeInt(minutes);
		parcel.writeInt(daysOfWeek.getCoded());
		parcel.writeLong(nexttime);
		parcel.writeInt(airplaneMode);
		parcel.writeInt(wifi);
		parcel.writeInt(bluetooth);
		parcel.writeInt(gps);
		parcel.writeInt(silent);
		parcel.writeInt(ringerVolume);
		parcel.writeInt(vibrate);
		parcel.writeInt(ruleApplyEnabled ? 1 : 0);
		parcel.writeInt(GSMRingtoneType);
		parcel.writeString(GSMRingtoneURIString);
	}
	
	public boolean equals(Profile profile) {
		if (profile == null) {
			return false;
		}
		if ((this.airplaneMode != profile.airplaneMode) && (this.airplaneMode != -1 && profile.airplaneMode != -1)) {
			return false;
		} 
		if ((this.wifi != profile.wifi) && (this.wifi != -1 && profile.wifi != -1)) {
			return false;
		}
		if ((this.bluetooth != profile.bluetooth) && (this.bluetooth != -1 && profile.bluetooth != -1)) {
			return false;
		}
		if ((this.gps != profile.gps) && (this.gps != -1 && profile.gps != -1)) {
			return false;
		}
		if ((this.silent != profile.silent) && (this.silent != -1 && profile.silent != -1)) {
			return false;
		}
		if ((this.ringerVolume != profile.ringerVolume) && (this.ringerVolume != -1 && profile.ringerVolume != -1)) {
			return false;
		}
		if ((this.vibrate != profile.vibrate) && (this.vibrate != -1 && profile.vibrate != -1)) {
			return false;
		}
		return true;
	} 

	public static class DaysOfWeek {
		private int mDays;

		public DaysOfWeek(int days) {
			mDays = days;
		}

		private boolean isSet(int idx) {
			return (mDays  & (1 << idx)) != 0;
		}
		
		public void set(int idx, boolean flag) {
			if (flag) {
				mDays |= 1 << idx;
			} else {
				mDays &= ~(1 << idx);
			}
		}

		public boolean[] getBooleanArray() {
			boolean aflag[] = new boolean[7];
			for (int i = 0; i < 7; i++) {
				aflag[i] = isSet(i);
			}
			return aflag;
		}

		public int getCoded() {
			return mDays;
		}

		public int getNextProfile(Calendar calendar) {
			int day_of_week = 0;
			int cur_day_idx = 0;
			int next_day_idx = 0;
			int day_interval = 0;
			
			if (mDays == 0) {
				return -1;
			}
			day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
			cur_day_idx = (day_of_week + 5) % 7;
			for(int idx=0; idx<7; idx++) {
				next_day_idx  = (cur_day_idx + idx) % 7;
				if(isSet(next_day_idx) == false) {
					day_interval++;
				} else {
					break;
				}
			}
			return day_interval;
		}

		public boolean isRepeatSet() {
			return mDays == 0 ? false : true;
		}

		public void set(DaysOfWeek day) {
			this.mDays = day.mDays;
		}

		public String toString(Context context, boolean flag) {
			String str = null;
			String[] days_str = null;
			StringBuffer buf = null;
	        int val = 0;
	        
	        days_str = context.getResources().getStringArray(R.array.brief_days_of_week);
	        buf = new StringBuffer();
	        
	        if (mDays == 0) {
	        	str = context.getResources().getString(R.string.never);
	        } else if (mDays == 0x7F) {
	        	str = context.getResources().getString(R.string.every_day);
	        } else {
	        	buf.append(context.getResources().getString(R.string.week_prefix));
				for (int idx=0; idx<7; idx++) {
					val = mDays  & (1 << idx);
					if (val != 0) {
						buf.append(days_str[idx]).append(",");
					} 
				}
				int idx = buf.lastIndexOf(",");
				if (idx > 0) {
					buf.deleteCharAt(idx);
				}
				str = buf.toString();
	        }
	        return str;
		}		
	}

	public static class ProfileColumns implements BaseColumns {
		public static final String AIRPLANE_MODE = "airplane_mode";
		public static final String APPLIED = "rule_applied";
		public static final String AUTHORITY = ProfileConstants.PACKAGE_NAME;
		public static final String BLUETOOTH_ON = "bluetooth_state";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.apollo.profile";
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.apollo.profile";
		public static final Uri CONTENT_URI = Uri
				.parse("content://apollo.profilesetting.view/item");
		public static final String GPS_ON = "gps_state";
		public static final String ID = "_id";
		public static final int ITEM = 1;
		public static final int ITEM_ID = 2;
		public static final String NAME = "name";
		public static final int PROFILE_ID = 0;
		public static final int PROFILE_NAME = 1;
		public static final int PROFILE_APPLIED = 2;
		public static final int PROFILE_RULE_HOUR = 3;
		public static final int PROFILE_RULE_MINITUE = 4;
		public static final int PROFILE_RULE_REPEAT = 5;
		public static final int PROFILE_AIRPLANE_MODE = 6;
		public static final int PROFILE_WLAN_ON = 7;
		public static final int PROFILE_BLUETOOTH_ON = 8;
		public static final int PROFILE_GPS_ON = 9;
		public static final int PROFILE_SILENT_ON = 10;
		public static final int PROFILE_RINGER_VOLUME = 11;
		public static final int PROFILE_VIBRATE_ON = 12;
	    public static final int PROFILE_GSM_RINGTONE_TYPE = 13;
	    public static final int PROFILE_GSM_RINGTONE_URISTRING = 14;
	    
		public static final String RINGER_VOLUME = ProfileConstants.KEY_RINGER_VOLUME;
		public static final String RULE_REPEAT = "rule_repeat";
		public static final String RULE_TIME_HOUR = "rule_time_hour";
		public static final String RULE_TIME_MINUTE = "rule_time_minutes";
		public static final String SILENT_ON = "silent_on";
		public static final String VIBRATE_ON = "vibrate_on";
		public static final String WHERE_ENABLED = " rule_applied = 1 and rule_repeat <> 0";
		public static final String WLAN_ON = "wlan_state";
		public static final String[] PROFILE_QUERY_COLUMNS = {"_id","name","rule_applied","rule_time_hour","rule_time_minutes","rule_repeat",
																									"airplane_mode","wlan_state","bluetooth_state","gps_state","silent_on","ringer_volume","vibrate_on","gsm_ringtone_type","gsm_ringtone_uristring"};
	}
}

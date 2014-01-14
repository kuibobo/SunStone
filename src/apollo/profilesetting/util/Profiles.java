package apollo.profilesetting.util;

import java.util.Calendar;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.format.DateFormat;
import apollo.profilesetting.model.Profile;

import apollo.profilesetting.view.R;

public class Profiles {
 
	private static final String DM12 = "E h:mm aa";
	private static final String DM24 = "E k:mm";
	private static final String LOCALIZE_PREFIX = "localize_key_";
	private static final int LOCALIZE_PREFIX_LEN = LOCALIZE_PREFIX.length();
	private static final String M12 = "h:mm aa";
	static final String M24 = "kk:mm";
	public static final String PROFILE_ALERT_ACTION = "com.android.Profileclock.Profile_ALERT";
	public static int mActivatedProfileKey = -1;
	static HashMap<String, String> sGroupNameMap = null;
	
	public Profiles() {
	}
	public static Profile addProfile(Context context, Profile profile) {
		ContentValues values = createContentValues(profile);
		ContentResolver solver = context.getContentResolver();
		profile.id = (int) ContentUris.parseId(solver.insert(
				Profile.ProfileColumns.CONTENT_URI, values));
		return profile;
	}

	public static Profile calculateNextAutoProfile(Context context) {
		Profile profile = null;
		Profile auto_profile = null;
		Cursor cursor = null;
		Calendar calendar = null;
		
		long max_time = 0x7fffffffffffffffL;
		cursor = getFilteredProfilesCursor(context.getContentResolver());
		if (cursor != null && cursor.moveToFirst()) {
			do {
				profile = new Profile(cursor);
				calendar = calculateProfile(profile.hour, profile.minutes, profile.daysOfWeek);
				profile.nexttime = calendar.getTimeInMillis();
				if (profile.nexttime > 0) {
					if (max_time > profile.nexttime) {
						max_time = profile.nexttime;
						auto_profile = profile;
					}
				}
			} while (cursor.moveToNext());
		}
		if (cursor != null)
			cursor.close();
		return auto_profile;
	}

	static Calendar calculateProfile(int hour, int minutes, Profile.DaysOfWeek daysofweek) {
		Calendar calendar = null;
		int cur_hour = 0;
		int cur_minutes = 0;
		int day_interval = 0;
		
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		cur_hour = calendar.get(Calendar.HOUR_OF_DAY);
		cur_minutes = calendar.get(Calendar.MINUTE);
		if (hour < cur_hour || (hour == cur_hour && minutes <= cur_minutes)) {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		day_interval = daysofweek.getNextProfile(calendar);
		if (day_interval > 0)
			calendar.add(Calendar.DAY_OF_WEEK, day_interval);
		return calendar;
	}

	private static ContentValues createContentValues(Profile profile) {
		ContentValues values = new ContentValues(15);
		
		values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME], profile.name);
		values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_AIRPLANE_MODE], profile.airplaneMode);
		values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_WLAN_ON], profile.wifi);
		values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_BLUETOOTH_ON], profile.bluetooth);
		values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GPS_ON], profile.gps);
		values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_SILENT_ON], profile.silent);
		values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RINGER_VOLUME], profile.ringerVolume);
		values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_VIBRATE_ON], profile.vibrate);
		return values;

	}

	public static void deleteProfile(Context context, int id) {
		ContentResolver solver = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(Profile.ProfileColumns.CONTENT_URI, id);
		solver.delete(uri, "", null);
		setNextAutoProfile(context);
	}

	static void disableAutoProfile(Context context) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService("alarm");
		Intent intent = new Intent(Profiles.PROFILE_ALERT_ACTION);
		PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0,
				intent, 0x10000000);
		manager.cancel(pendingintent);
	}

	private static void enableAutoProfile(Context context, Profile profile, long triggerAtTime) {
		AlarmManager manager = null;
		Intent intent = null;
		PendingIntent pendingintent = null;
		
		manager =  (AlarmManager) context.getSystemService("alarm");
		intent = new Intent(Profiles.PROFILE_ALERT_ACTION);
		intent.putExtra("profileId", profile.id);
		pendingintent = PendingIntent.getBroadcast(context, 0, intent, 0x10000000);
		
		manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pendingintent);
	}

	private static String formatDayAndTime(Context context, Calendar calendar) {
		boolean flag = get24HourMode(context);
		String s1;
		String s2;
		if (flag) {
			s2 = Profiles.DM24;
		} else {
			s2 = Profiles.DM12;
		}
		if (calendar == null)
			s1 = "";
		else
			s1 = (String) DateFormat.format(s2, calendar);
		return s1;
	}

	public static String formatTime(Context context, int hour, int minutes,
			Profile.DaysOfWeek daysofweek) {
		Calendar calendar = calculateProfile(hour, minutes, daysofweek);
		return formatTime(context, calendar);
	}

	static String formatTime(Context context, Calendar calendar) {
		boolean mode_on = get24HourMode(context);
		String value;
		String inFormat;
		if (mode_on) {
			inFormat = Profiles.M24;
		} else {
			inFormat = Profiles.M12;
		}
		if (calendar == null)
			value = "";
		else
			value = (String) DateFormat.format(inFormat, calendar);
		return value;
	}

	public static boolean get24HourMode(Context context) {
		return DateFormat.is24HourFormat(context);
	}

	private static Cursor getFilteredProfilesCursor(ContentResolver resolver) {
		return resolver.query(Profile.ProfileColumns.CONTENT_URI,
				Profile.ProfileColumns.PROFILE_QUERY_COLUMNS,
				Profile.ProfileColumns.WHERE_ENABLED, null, null);
	}

	public static Profile getProfile(ContentResolver resolver, int id) {
		Uri uri = null;
		Cursor cursor = null;
		Profile profile = null;
		
		uri = ContentUris.withAppendedId(Profile.ProfileColumns.CONTENT_URI, id);
		cursor = resolver.query(uri, Profile.ProfileColumns.PROFILE_QUERY_COLUMNS, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				profile = new Profile(cursor);
			}
			cursor.close();
		}
		return profile;
	}

	public static Cursor getProfilesCursor(ContentResolver contentresolver) {
		return contentresolver.query(Profile.ProfileColumns.CONTENT_URI, null, null, null, null);
	}

	public static String localizeProfileName(String name, Context context) {
		if (name != null && name.startsWith(LOCALIZE_PREFIX)) {
			Resources res = null;
			String keys[] = null;
			String names[] = null;
			
			sGroupNameMap = new HashMap<String, String>();
			res = context.getResources();
			keys = res.getStringArray(R.array.profile_name_key);
			names= res.getStringArray(R.array.profile_name);
			String s1;
			if (keys.length == names.length) {
				for (int idx=0; idx<keys.length; idx++) {
					sGroupNameMap.put(keys[idx].substring(LOCALIZE_PREFIX_LEN), names[idx]);
				}
			}
			name = name.substring(LOCALIZE_PREFIX_LEN);
			s1 = (String) sGroupNameMap.get(name);
			if (s1 != null)
				name = s1;
		}
		return name;
	}

	public static void setNextAutoProfile(Context context) {
		Profile profile = calculateNextAutoProfile(context);
		if (profile != null) {
			enableAutoProfile(context, profile, profile.nexttime);
		} else {
			disableAutoProfile(context);
		}
	}
	
	public static int setProfile(Context context, int id, String name,
			int airplane_mode, int wlan_state, int gps_state,
			int bluetooth_state, int silent_on, int vibrate_on,
			int ringer_volume, boolean rule_applied, int rule_time_hour,
			int rule_time_minutes, Profile.DaysOfWeek daysofweek, int ringtone_type, String ringtone) {
        ContentValues values;
        ContentResolver solver;
        Uri uri = null;
        
        values = new ContentValues(14);        
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME], name);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_APPLIED], rule_applied);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_HOUR], rule_time_hour);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_MINITUE], rule_time_minutes);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_REPEAT], daysofweek.getCoded());
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_AIRPLANE_MODE], airplane_mode);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_WLAN_ON], wlan_state);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_BLUETOOTH_ON], bluetooth_state);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GPS_ON], gps_state);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_SILENT_ON], silent_on);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RINGER_VOLUME], ringer_volume);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_VIBRATE_ON], vibrate_on);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GSM_RINGTONE_TYPE], ringtone_type);
        values.put(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GSM_RINGTONE_URISTRING], ringtone);
        solver = context.getContentResolver();
        
		if (id == -1 || id == 0) {
			uri = solver.insert(Profile.ProfileColumns.CONTENT_URI, values);
			return (int)ContentUris.parseId(uri);//Integer.parseInt(uri.getFragment());;
		} else {
			int result = -1;
			uri = ContentUris.withAppendedId(Profile.ProfileColumns.CONTENT_URI, id);
			result = solver.update(uri, values, null, null);
			setNextAutoProfile(context);
			return result;
		}
	}
	
	public static String getMediaTitle(Uri uri, Context context) {
		if (context == null || uri == null) {
			return "";
		}
		String title = null;
		Cursor cursor;
		String authority;
		authority = uri.getAuthority();
		if ("settings".equals(authority)) {
			if (RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.getDefaultType(uri)) == null) {
				title = "";
			}
		}
		ContentResolver resolver = context.getContentResolver();
		String cols[] = new String[2];
		cols[0] = "_id";
		cols[1] = "title";
		cursor = resolver.query(uri, cols, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				title = cursor.getString(cursor.getColumnIndex("title"));
				if ("settings".equals(authority)) {
					Object aobj[] = new Object[1];
					aobj[0] = title;
					title = context.getString(R.string.ringtone_default_with_actual, aobj);
				}
			}
		}
		if (cursor != null)
			cursor.close();
		if (title == null) {
			title = context.getString(R.string.ringtone_unknown);
		}
		return title;
    }
}
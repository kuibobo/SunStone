package apollo.profilesetting.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;

public class ProfileUtil {

	public static int getActivatedProfileId(Context context) {
		SharedPreferences sharedata = null;
		String str = null;
		int profile_id = -1;
		
		sharedata = context.getSharedPreferences(ProfileConstants.PREFERENCES, 0);
		str = sharedata.getString(ProfileConstants.PREF_ACTIVATED_PROFILE_ID, "-1");
		profile_id = Integer.parseInt(str);
		return profile_id;
	}

	public static void setActivatedProfileId(Context text, int id) {
		String str = Integer.toString(id);
		setActivatedProfileId(text, str);
	}

	public static void setActivatedProfileId(Context context, String s) {
		int id = -1;
		try {
			id = Integer.parseInt(s);
		} catch (NumberFormatException numberformatexception) {
		}
		if (id > 0) {
			SharedPreferences.Editor editor = context.getSharedPreferences(
					ProfileConstants.PREFERENCES, 0).edit();
			editor.putString(ProfileConstants.PREF_ACTIVATED_PROFILE_ID, s);
			editor.commit();
		}
		Profiles.mActivatedProfileKey = id;
	}

	public static void startApplyProfile(Context context, int value) {
		Intent intent = new Intent(ProfileConstants.INTENT_ACTION_APPLY);
		intent.putExtra(ProfileConstants.PROFILE_ID_EXTRA, value);
		context.startActivity(intent);
	}

	public static void setSummary(Preference preference, int i, String as[],
			String as1[], int j) {
		if (as != null && as1 != null) {
			for (int k = 0; k < j; k++)
				if (Integer.parseInt(as1[k]) == i) {
					String s1 = as[k];
					preference.setSummary(s1);
				}

		} else {
			preference.setSummary(ProfileConstants.BLANK_SUMMARY);
		}
	}

}

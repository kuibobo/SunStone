package apollo.profilesetting.view;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import apollo.profilesetting.model.Profile;
import apollo.profilesetting.util.ProfileApplyUtil;
import apollo.profilesetting.util.ProfileConstants;
import apollo.profilesetting.util.ProfileUtil;
import apollo.profilesetting.util.Profiles;

public class ProfileReceiver extends BroadcastReceiver {

//	private void setActivatedProfileKey(Context context, int id) {
//		SharedPreferences.Editor editor = context
//				.getSharedPreferences(ProfileConstants.PREFERENCES, 0).edit();
//		String s = Integer.toString(id);
//		editor.putString(ProfileConstants.PREF_ACTIVATED_PROFILE_ID, s);
//		editor.commit();
//		Profiles.mActivatedProfileKey = id;
//	}

	private void showNotification(Context context, String s) {
		NotificationManager notificationmanager = (NotificationManager) context
				.getSystemService("notification");
		String title = context.getString(R.string.noti_auto_apply_title);
		String content = context.getString(R.string.noti_auto_apply_content) + ProfileConstants.BLANK_SUMMARY + s;
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		intent.setClassName(ProfileConstants.PACKAGE_NAME,
				"apollo.profilesetting.view.ProfileList");
		PendingIntent pendingintent = PendingIntent.getActivity(context, 0,
				intent, 0x8000000);
		Notification notification = new Notification(
				R.drawable.ic_status_profile_manager, content, 0);
		notification.setLatestEventInfo(context, title, content, pendingintent);
		notificationmanager.notify(1, notification);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = null;

		action = intent.getAction();
		if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
			Profiles.setNextAutoProfile(context);
			return;
		}
		
		action = intent.getAction();
		if (Profiles.PROFILE_ALERT_ACTION.equals(action)) {
			int profileId = intent.getIntExtra("profileId", -1);
			Profile profile = Profiles.getProfile(context.getContentResolver(),
					profileId);
			if (profile != null) {
				ProfileApplyUtil.applyProfile(context, profile);
				ProfileUtil.setActivatedProfileId(context, profile.id);
				Profiles.setNextAutoProfile(context);
				showNotification(context, profile.name);
			}
		}
	}
}

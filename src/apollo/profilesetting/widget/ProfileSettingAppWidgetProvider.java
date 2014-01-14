package apollo.profilesetting.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import apollo.profilesetting.model.Profile;
import apollo.profilesetting.util.ProfileApplyUtil;
import apollo.profilesetting.util.ProfileUtil;
import apollo.profilesetting.util.Profiles;
import apollo.profilesetting.view.ProfileList;
import apollo.profilesetting.view.R;

public class ProfileSettingAppWidgetProvider extends AppWidgetProvider {

	abstract class StateTracker {
		private Boolean mActualState = null;
		private boolean mDeferredStateChangeRequestNeeded = false;
		private boolean mInTransition = false;
		private Boolean mIntendedState = null;

		public abstract int getActualState(Context context);
		public abstract int getButtonId();
		public abstract int getButtonImageId(boolean flag);
		public abstract int getIndicatorId();
		public abstract void onActualStateChange(Context context, Intent intent);
		protected abstract void requestStateChange(Context context, boolean flag);

		public int getPosition() {
			return 1;
		}
		
		protected final void setCurrentState(Context context, int i) {
			
		}
	}
	
	class ProfileSettingStateTracker extends StateTracker {

		@Override
		public int getActualState(Context context) {
			int activated_profile_id = -1;
			int stat = 0;
			Profile activated_profile = null;
			
			activated_profile_id = ProfileUtil.getActivatedProfileId(context);
			activated_profile = Profiles.getProfile(context.getContentResolver(), activated_profile_id);
			if (activated_profile != null) {
				Profile sys_profile = ProfileApplyUtil.getCurrentSystemProfile(context);
				if (sys_profile.equals(activated_profile)) {
					stat = 1;
				}
			}
			return stat;
		}

		@Override
		public int getButtonId() {
			return 0;
		}

		@Override
		public int getButtonImageId(boolean flag) {
			return 0;
		}

		@Override
		public int getIndicatorId() {
			return 0;
		}

		@Override
		public void onActualStateChange(Context context, Intent intent) {
		      int i = getActualState(context);
		      setCurrentState(context, i);
		}

		@Override
		protected void requestStateChange(Context context, boolean flag) {
		}
		
		
	}
	
	private static final ComponentName THIS_APPWIDGET = new ComponentName("apollo.profilesetting.view", "apollo.profilesetting.widget.ProfileSettingAppWidgetProvider");
	
	private static RemoteViews buildUpdate(Context context, int i) {
        String s = context.getPackageName();
        RemoteViews views = null;
        PendingIntent intent = null;
        
        views = new RemoteViews(s, R.layout.profile_widget_layout);
        intent = getLaunchPendingIntent(context, i, 0);
        views.setOnClickPendingIntent(R.id.btn_wifi, intent);
        
        intent = getLaunchPendingIntent(context, i, 1);
        views.setOnClickPendingIntent(R.id.btn_bluetooth, intent);
        
        intent = getLaunchPendingIntent(context, i, 2);
        views.setOnClickPendingIntent(R.id.btn_gps, intent);
        
        intent = getLaunchPendingIntent(context, i, 3);
        views.setOnClickPendingIntent(R.id.btn_sync, intent);
        
        intent = getLaunchPendingIntent(context, i, 4);
        views.setOnClickPendingIntent(R.id.btn_brightness, intent);
        
        updateButtons(views, context);
        return views;
	}
	
	private static PendingIntent getLaunchPendingIntent(Context context, int i, int j) {
	    Intent intent = null;
	    Uri uri = null;
	    
	    uri = Uri.parse("custom:" + j);
	    
	    intent = new Intent();
	    intent.setClass(context, ProfileSettingAppWidgetProvider.class);
	    intent.addCategory("android.intent.category.ALTERNATIVE");
	    intent.setData(uri);
	    return PendingIntent.getBroadcast(context, 0, intent, 0);
	}
	
	private static void updateButtons(RemoteViews views, Context context) {
		views.setImageViewResource(R.id.img_bluetooth, R.drawable.ic_appwidget_settings_brightness_auto);
	}
	
	public static void updateWidget(Context context) {
        RemoteViews views = buildUpdate(context, -1);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(THIS_APPWIDGET, views);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = null;
		super.onReceive(context, intent);
		
		updateWidget(context);
		action = intent.getAction();

		//mCurrentTracerIndex = Integer.parseInt(intent.getData().getSchemeSpecificPart());
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews views = null;
		views = buildUpdate(context, -1);
		for (int idx = 0; idx < appWidgetIds.length; idx++) {
			appWidgetManager.updateAppWidget(appWidgetIds[idx], views);
		}
	}
}

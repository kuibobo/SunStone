package apollo.profilesetting.util;


import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import apollo.profilesetting.model.Profile;

public class ProfileApplyUtil {

	private static int applyAirplaneMode(Context context, Profile profile) {
		if (profile.airplaneMode != -1) {
			if (isAirplaneModeOn(context)) {
				if (profile.airplaneMode == ProfileConstants.VALUE_OFF) {
					setAirplaneModeOn(context, false);
				}
			} else {
				if (profile.airplaneMode == ProfileConstants.VALUE_ON) {
					setAirplaneModeOn(context, true);
				}
			}
		}
		return 0;
	}

	public static int applyProfile(Context context, Profile profile) {
		int setting;
		
		setting = ProfileConstants.VALUE_OFF;
		applyAirplaneMode(context, profile);
		setting = applyWirelessAndLocation(context, profile);
		setting = ProfileConstants.VALUE_OFF | setting;
		applySound(context, profile);
		return setting;
	}

	private static void applySound(Context context, Profile profile) {
		ContentResolver resolver = null;
		AudioManager audio = null;
		int vibrate_setting = -1;
		int ring_mode = -1;
		
		if (profile.GSMRingtoneType != -1) {
			Uri ringtoneUri = Uri.parse(profile.GSMRingtoneURIString);
			RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, ringtoneUri);
		}
		resolver = context.getContentResolver();
		audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		if (profile.silent == ProfileConstants.VALUE_ON) {
			audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		} else if (profile.silent == ProfileConstants.VALUE_OFF) {
			audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			audio.setStreamVolume(AudioManager.RINGER_MODE_NORMAL, profile.ringerVolume, 0);
		}
		
		switch(profile.vibrate) {
		case ProfileConstants.VIBRATE_ALWAYS_ON:
			vibrate_setting = AudioManager.VIBRATE_SETTING_ON;
			ring_mode = 1;
			break;
		case ProfileConstants.VIBRATE_NEVER:
			vibrate_setting = AudioManager.VIBRATE_SETTING_OFF;
			ring_mode = 0;
			break;
		case ProfileConstants.VIBRATE_ONLY_IN_SILENT:
			vibrate_setting = AudioManager.VIBRATE_SETTING_ONLY_SILENT;
			ring_mode = 1;
			break;
		case ProfileConstants.VIBRATE_UNLESS_SILENT:
			vibrate_setting = AudioManager.VIBRATE_SETTING_ON;
			ring_mode = 0;
			break;
		}
		Settings.System.putInt(resolver, "vibrate_in_silent", ring_mode);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, vibrate_setting);
	}

	private static int applyWirelessAndLocation(Context context, Profile profile) {
		int settings = 0;
		
		if (profile.airplaneMode != ProfileConstants.VALUE_ON) {
			if (profile.wifi != -1) {
				WifiManager manager =null;
				
				manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
				if (manager != null) {
					int wifi_state = getWifiState(manager);
					if (wifi_state == ProfileConstants.VALUE_ON && profile.wifi == ProfileConstants.VALUE_OFF)
						manager.setWifiEnabled(false);
					if (wifi_state == ProfileConstants.VALUE_OFF && profile.wifi == ProfileConstants.VALUE_ON)
						manager.setWifiEnabled(true);
				}
			}
			if (profile.bluetooth != -1) {
				BluetoothAdapter bluetoothadapter = BluetoothAdapter
						.getDefaultAdapter();
				if (bluetoothadapter != null) {
					int bt_state = getBluetoothState(bluetoothadapter);
					if (bt_state == ProfileConstants.VALUE_ON && profile.bluetooth == ProfileConstants.VALUE_OFF)
						bluetoothadapter.disable();
					if (bt_state == ProfileConstants.VALUE_OFF && profile.bluetooth == ProfileConstants.VALUE_ON)
						if (isAirplaneModeOn(context)) {
							settings |= 4;
						} else {
							bluetoothadapter.enable();
						}
				}
			}
			if (profile.gps != -1) {
				boolean flag = getGpsState(context);
				if (flag && profile.gps == ProfileConstants.VALUE_OFF) {
					setGpsState(context, false);
				}
				if (!flag && profile.gps == ProfileConstants.VALUE_ON)
					if (isAirplaneModeOn(context)) {
						settings |= 4;
					} else {
						setGpsState(context, true);
					}
			}
		}
		return settings;
	}

	private static int getBluetoothState(BluetoothAdapter adapter) {
		int bt_setting = -1;
		int setting = -1;

		if (adapter == null) {
			bt_setting = ProfileConstants.VALUE_OFF;
		} else {
			bt_setting = adapter.getState();
		}
		if (bt_setting == BluetoothAdapter.STATE_OFF) {
			setting = ProfileConstants.VALUE_OFF;
		} else {
			if (bt_setting == BluetoothAdapter.STATE_ON) {
				setting = ProfileConstants.VALUE_ON;
			} else {
				setting = -1;
			}
		}
		return setting;
	}

	private static boolean getGpsState(Context context) {
		return Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), android.location.LocationManager.GPS_PROVIDER);
	}

	public static Profile getCurrentSystemProfile(Context context) {
		Profile profile = null;
		AudioManager manager = null;
		boolean is_airplane_mode_on = false;
		int ring_mode = -1;
		
		profile = new Profile();
		is_airplane_mode_on = isAirplaneModeOn(context);
		if (is_airplane_mode_on) {
			profile.airplaneMode =  ProfileConstants.VALUE_ON;
		} else {
			profile.airplaneMode = ProfileConstants.VALUE_OFF;
		}
		profile.wifi = getWifiState((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
		profile.bluetooth = getBluetoothState(BluetoothAdapter.getDefaultAdapter());
		profile.gps = getGpsState(context) ? ProfileConstants.VALUE_ON : ProfileConstants.VALUE_OFF;
		manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		profile.ringerVolume = manager.getStreamVolume(2);
		
		ring_mode = manager.getRingerMode();
		if (profile.ringerVolume == 0) {
			profile.silent =  ProfileConstants.VALUE_ON;
		} else {
			profile.silent = ProfileConstants.VALUE_OFF;
		}
//		if (ring_mode == AudioManager.RINGER_MODE_NORMAL) {
//			profile.silent = ProfileConstants.VALUE_OFF;
//		} else {
//			if (is_airplane_mode_on) {
//				profile.silent =  ProfileConstants.VALUE_ON;
//			} else {
//				profile.silent = ProfileConstants.VALUE_OFF;
//			}
//		}
		
		profile.vibrate = getPhoneVibrateSettingValue(context, manager);
		return profile;
	}

	private static int getPhoneVibrateSettingValue(Context context, AudioManager manager) {
		ContentResolver resolver = null;
		int setting = -1;
		int vibrate_setting;
		
		resolver = context.getContentResolver();
		vibrate_setting = manager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);

		if (Settings.System.getInt(resolver, "vibrate_in_silent", 1) != AudioManager.RINGER_MODE_SILENT) {
			if (vibrate_setting == AudioManager.VIBRATE_TYPE_RINGER) {
				setting = ProfileConstants.VIBRATE_ONLY_IN_SILENT;
			}
			if (vibrate_setting == AudioManager.VIBRATE_TYPE_NOTIFICATION) {
				setting = ProfileConstants.VIBRATE_ALWAYS_ON;
			} else {
				setting = ProfileConstants.VIBRATE_ONLY_IN_SILENT;
			}
		} else {
			if (vibrate_setting == AudioManager.VIBRATE_SETTING_ONLY_SILENT)
				setting = ProfileConstants.VIBRATE_NEVER;
			if (vibrate_setting == AudioManager.VIBRATE_TYPE_NOTIFICATION)
				setting = ProfileConstants.VIBRATE_UNLESS_SILENT;
			else
				setting = ProfileConstants.VIBRATE_NEVER;
		}
		return setting;
	}

	private static int getWifiState(WifiManager manager) {
		int state = manager.getWifiState();
		int setting;
		if (state == WifiManager.WIFI_STATE_DISABLED) {
			setting = ProfileConstants.VALUE_OFF;
		} else {
			if (state == WifiManager.WIFI_STATE_ENABLED) {
				setting = ProfileConstants.VALUE_ON;
			} else {
				setting = -1;
			}
		}
		return setting;
	}

	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
	}

	private static void setAirplaneModeOn(Context context, boolean enabled) {
		Intent intent;

		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, enabled ? 1 : 0);
		intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", enabled);
		context.sendBroadcast(intent);
	}

	private static void setGpsState(Context context, boolean enabled) {
		// 没有权限执行该方法
		//Settings.Secure.setLocationProviderEnabled(context.getContentResolver(), "gps", enabled);
		
		Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        intent.addCategory("android.intent.category.ALTERNATIVE");
        intent.setData(Uri.parse("custom:3"));
        context.sendBroadcast(intent);
	}

}

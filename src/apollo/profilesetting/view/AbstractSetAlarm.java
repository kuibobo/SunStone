package apollo.profilesetting.view;

import android.content.Intent;
import android.preference.PreferenceActivity;

public class AbstractSetAlarm extends PreferenceActivity {
	public interface OnActivityResultListener {
		public abstract void onActivityResult(int i, int j, Intent intent);
	}

	public AbstractSetAlarm() {
		mActivityResultListener = null;
	}

	public void addOnActivityResultListener(
			OnActivityResultListener onactivityresultlistener) {
		mActivityResultListener = onactivityresultlistener;
	}

	public void onActivityResult(int i, int j, Intent intent) {
		if (mActivityResultListener != null)
			mActivityResultListener.onActivityResult(i, j, intent);
	}

	public void removeOnActivityResultListener() {
		mActivityResultListener = null;
	}

	private OnActivityResultListener mActivityResultListener;
}

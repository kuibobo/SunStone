package apollo.profilesetting.view;


import apollo.profilesetting.view.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import apollo.profilesetting.model.Profile;
import apollo.profilesetting.util.ProfileApplyUtil;
import apollo.profilesetting.util.ProfileConstants;
import apollo.profilesetting.util.ProfileUtil;
import apollo.profilesetting.util.Profiles;

public class ProfileApply extends Activity implements
		DialogInterface.OnDismissListener {

	class ProfileApplyHandler extends Handler {
		private Handler mCallbackHandler;

		public ProfileApplyHandler(Looper lopper, Handler handler) {
			super();
			this.mCallbackHandler = handler;
		}

		public void handleMessage(Message message) {
			switch (message.what) {
			case ProfileApply.DIALOG_ACTIVATE_PROFILE:
				Profile profile = (Profile) message.obj;
				Message msg = mWaitingUiHandler.obtainMessage(ProfileConstants.MSG_ACTIVATE_PROFILE_END);
				msg.arg1 = ProfileApplyUtil.applyProfile(ProfileApply.this, profile);
				this.mCallbackHandler.sendMessageDelayed(msg, 1000L);
				break;
			}
		}
	}

	class WaitingUiHandler extends Handler {
		public WaitingUiHandler() {
		}

		public void handleMessage(Message message) {
			switch (message.what) {
			case ProfileConstants.MSG_ACTIVATE_PROFILE_END:
				try {
					dismissDialog(ProfileApply.DIALOG_ACTIVATE_PROFILE);
					if (message.arg1 == 0) {
						mToast = Toast.makeText(ProfileApply.this, getResources().getString(R.string.apply_profile_completed), 1);
						mToast.show();
					} else if ((message.arg1 & 2) == 2) {
						mToast = Toast.makeText(ProfileApply.this, getResources().getString(R.string.please_exit_airplane_mode), 1);
						mToast.show();
					} else if ((message.arg1 & 1) == 1) {
						mToast = Toast.makeText(ProfileApply.this, getResources().getString(R.string.apply_profile_g_card_absent), 1);
						mToast.show();
					}
					
				} catch (IllegalArgumentException localIllegalArgumentException) {
				}
				break;
			}
		}
	}

	private static final int DIALOG_ACTIVATE_PROFILE = 1001;
	private static final int MSG_DISMISS_PROGRESS = 12;
	private ProfileApplyHandler mProfileApplyHandler;
	private ProgressDialog mProgressDialog;
	private Toast mToast;
	private WaitingUiHandler mWaitingUiHandler;

	public ProfileApply() {
	}

	private void applyProfile(int id) {
		Profile profile = null;
		
		showDialog(ProfileApply.DIALOG_ACTIVATE_PROFILE);
		ProfileUtil.setActivatedProfileId(this, id);
		profile = Profiles.getProfile(getContentResolver(), id);
		if (mWaitingUiHandler == null) {
			mWaitingUiHandler = new WaitingUiHandler();
		}
		if (mProfileApplyHandler == null) {
			HandlerThread thread = new HandlerThread("Aplly profile handler: Process Thread");
			thread.start();
			Looper looper = thread.getLooper();
			mProfileApplyHandler = new ProfileApplyHandler(looper, mWaitingUiHandler);
		}
		Message message = mProfileApplyHandler.obtainMessage(ProfileApply.DIALOG_ACTIVATE_PROFILE, profile);
		mProfileApplyHandler.sendMessage(message);
	}

	private void dismissProgress() {
		mWaitingUiHandler.sendMessage(mWaitingUiHandler.obtainMessage(ProfileApply.MSG_DISMISS_PROGRESS));
	}

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Intent intent = getIntent();
		int id = intent.getIntExtra(ProfileConstants.PROFILE_ID_EXTRA, -1);
		if (id == -1) {
			finish();
		}
		if (intent.getAction().equals(ProfileConstants.INTENT_ACTION_APPLY)) {
			applyProfile(id);
		}
	}

	protected Dialog onCreateDialog(int id) {
		if (id == ProfileApply.DIALOG_ACTIVATE_PROFILE) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(getResources().getString(R.string.applying_profile));
			mProgressDialog.setCancelable(false);
			mProgressDialog.setOnDismissListener(this);
		} 
		return mProgressDialog;
	}

	public void onDismiss(DialogInterface dialoginterface) {
		finish();
	}
}

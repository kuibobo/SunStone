package apollo.profilesetting.view;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.CheckedTextView;
import android.widget.AdapterView;
import apollo.profilesetting.model.Profile;
import apollo.profilesetting.util.ProfileConstants;
import apollo.profilesetting.util.ProfileUtil;
import apollo.profilesetting.util.Profiles;

public class ProfileMultiDelete extends Activity implements
		DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {
	
	class DeleteProfileProcessHandler extends Handler {
		private Handler mDeleteUiHandler;

		public DeleteProfileProcessHandler(Looper looper, Handler handler) {
			super(looper);
			mDeleteUiHandler = handler;
		}

		private boolean doDelete() {
			Message msg = null;
			int del_id;
			
			for (int idx = 0; idx < counttobedelete; idx++) {
				del_id = mTobedeleteIds[idx];
				Profiles.deleteProfile(ProfileMultiDelete.this, del_id);
			}

			msg = this.mDeleteUiHandler.obtainMessage(2);
			this.mDeleteUiHandler.sendMessage(msg);

			return true;
		}

		public void handleMessage(Message message) {
			switch(message.what) {
			case 0:
				break;
			case 1:
				doDelete();
				Message msg = this.mDeleteUiHandler.obtainMessage(3);
				mDeleteUiHandler.sendMessage(msg);
				break;
			}
		}
	}

	class WaitingUiHandler extends Handler {
		public void handleMessage(Message paramMessage) {
			switch (paramMessage.what) {
			case 2:
			case 3:
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}
				showDialog(ProfileConstants.MSG_ACTIVATE_PROFILE_START);
				mProgressDialog.dismiss();
			}
		}
	}
	
	private static final int DIALOG_DELETE_COMPLETE = 1001;
	private static final int DIALOG_DETETE_PROGRESS = 1002;
	private ArrayList<Profile> allprofile;
	private View.OnClickListener button_cancel_clicklistener;
	private View.OnClickListener button_ok_clicklistener;
	private CheckBox checkbox_deleteall;
	private View.OnClickListener checkbox_deteleall_clicklistener;
	private int countall;
	private int countselect;
	private int counttobedelete;
	private boolean islast;
	private ListView list;
	private String[] localize_profile_name;
	private Button mButtonCancel;
	private Button mButtonOK;
	private boolean mChecked[];
	private int mCurrentId;
	private String mCurrentName;
	private boolean mDeleteCurrentProfile;
	private int mId;
	private int mProfileIds[];
	private String mProfileNames[];
	private boolean mSelectAll;
	private int mTobedeleteIds[];
	private String mutidelete_activited_profile_confirm;
	private Dialog mInfoDialog;
	private ProgressDialog mProgressDialog;
	private WaitingUiHandler mWaitingUiHandler;
	private DeleteProfileProcessHandler mDeleteProfileProcessHandler;
	
	public ProfileMultiDelete() {
		allprofile = new ArrayList<Profile>();
		mChecked = null;
		mProfileIds = null;
		mTobedeleteIds = null;
		mProfileNames = null;
		countselect = 0;
		counttobedelete = 0;
		mCurrentName = "";
		mutidelete_activited_profile_confirm = "";
		checkbox_deteleall_clicklistener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkbox_deleteall.isChecked()) {
					for (int idx = 0; idx < countall; idx++) {
						list.setItemChecked(idx, true);
						mButtonOK.setEnabled(true);
						mChecked[idx] = true;
					}
					countselect = countall;
				} else {
					for (int idx = 0; idx < countall; idx++) {
						list.setItemChecked(idx, false);
						mButtonOK.setEnabled(false);
						mChecked[idx] = false;
					}
					countselect = 0;
				}
			}
		};
		
		button_ok_clicklistener = new View.OnClickListener() {
			//.3
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = null;
				final Intent intent = new Intent(ProfileConstants.INTENT_ACTION_DELETE);
				
				builder = new AlertDialog.Builder(ProfileMultiDelete.this);
				builder.setTitle(getResources().getString(R.string.mutidelete_profile));
				builder.setMessage(getResources().getString(R.string.mutidelete_profile_confirm));
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					//.3.1
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final int activated_profile_key = ProfileUtil.getActivatedProfileId(ProfileMultiDelete.this);
						
						for (int idx = 0; idx < countall; idx++) {
							mId = mProfileIds[idx];
							if (mChecked[idx] == true) {
								if (activated_profile_key == mId) {
									mCurrentId = mId;
									mDeleteCurrentProfile = true;
									mCurrentName = localize_profile_name[idx];
									mutidelete_activited_profile_confirm = String.format(
											getResources().getString(R.string.mutidelete_activited_profile_confirm),  
											mCurrentName);
								} else {
									mTobedeleteIds[counttobedelete++]= mId;
								}
							}
						}
						
						if (mDeleteCurrentProfile == true) {
							AlertDialog.Builder builder = null;
							
							builder = new AlertDialog.Builder(ProfileMultiDelete.this);
							builder.setTitle(R.string.mutidelete_activited_profile).setMessage(mutidelete_activited_profile_confirm);
							builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									mTobedeleteIds[counttobedelete++]= mCurrentId;
									
									showDialog(ProfileMultiDelete.DIALOG_DETETE_PROGRESS);
	                                initDeleteProcess();
	                                mDeleteProfileProcessHandler.sendEmptyMessage(1);
								}
							}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									showDialog(ProfileMultiDelete.DIALOG_DETETE_PROGRESS);
	                                initDeleteProcess();
	                                mDeleteProfileProcessHandler.sendEmptyMessage(1);
								}
							}).show();
						} else {
							showDialog(ProfileMultiDelete.DIALOG_DETETE_PROGRESS);
							initDeleteProcess();
							mDeleteProfileProcessHandler.sendEmptyMessage(1);
						}
					}
				}).setNegativeButton(android.R.string.cancel, null).show();
			}
		};
		button_cancel_clicklistener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		};
	}

	private void fillList() {
		String cols[] = null;
		String cols2[] = null;
		String col_val = null;
		Cursor cursor = null;
		int ai[] = null;
		int col = 0;
		int idx = 0;
		
		cols = new String[2];
		cols[0] = Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_ID];
		cols[1] = Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME];
		cursor = managedQuery(Profile.ProfileColumns.CONTENT_URI, cols, null, null, null);
		
		cols2 = new String[1];
		cols2[0] = Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME];
		
		ai = new int[1];
		ai[0] = R.id.checkedTextView;

		countall = cursor.getCount();
		if (countall == 0) {
			checkbox_deleteall.setEnabled(false);
		}
		mChecked = new boolean[countall]; 
		mProfileIds = new int[countall];
		mProfileNames = new String[countall];
		mTobedeleteIds = new int[countall];
		localize_profile_name = new String[countall];
		
		if (cursor != null && cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				col = cursor.getColumnIndex(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_ID]);
				col_val = cursor.getString(col);
				mProfileIds[idx] = Integer.parseInt(col_val);
				
				col = cursor.getColumnIndex(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME]);
				mProfileNames[idx] = cursor.getString(col);
                localize_profile_name[idx] = Profiles.localizeProfileName(mProfileNames[idx], this);
                
				idx++;
				cursor.moveToNext();
			}
		}
		
		list = (ListView) findViewById(R.id.listview);
		if (list != null) {
			ArrayAdapter adapter = new ArrayAdapter(this, R.layout.profile_multidelete_listitem, localize_profile_name);
			list.setItemsCanFocus(false);
			list.setChoiceMode(2);
			list.setAdapter(adapter);
		}
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {	
				if (((CheckedTextView) view).isChecked()) {
					mChecked[position] = false;
					countselect--;
					if (countselect == 0) {
						mButtonOK.setEnabled(false);
					}
					checkbox_deleteall.setChecked(false);
				} else {
					mChecked[position] = true;
					countselect ++;
					if (countselect == countall) {
						checkbox_deleteall.setChecked(true);
					}
					mButtonOK.setEnabled(true);
				}
			}
		});
	}

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.profile_multidelete);
		mButtonOK = (Button) findViewById(R.id.ok);
		mButtonCancel = (Button) findViewById(R.id.cancel);
		checkbox_deleteall = (CheckBox) findViewById(R.id.checkbox_deleteall);
		checkbox_deleteall.setOnClickListener(checkbox_deteleall_clicklistener);
		fillList();
		mButtonOK.setOnClickListener(button_ok_clicklistener);
		mButtonOK.setEnabled(false);
		mButtonCancel.setOnClickListener(button_cancel_clicklistener);
	}

	protected void onResume() {
		super.onResume();
		if (mChecked != null) {
			for (int idx = 0; idx < mChecked.length; idx++) {
				if (mChecked[idx] == true && list.getChildAt(idx) != null) {
					((CheckedTextView) list.getChildAt(idx)).setChecked(true);
				}
			}
		}
		if (mInfoDialog != null) {
			if (mInfoDialog.isShowing()) {
				mInfoDialog.hide();
				mInfoDialog.show();
			}
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		finish();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		finish();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dlg = null;

		if (id == ProfileMultiDelete.DIALOG_DELETE_COMPLETE) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			mInfoDialog = builder
					.setMessage(getResources().getString(R.string.delete_profile_completed))
					.setPositiveButton(0x104000a, null).create();
			mInfoDialog.setOnDismissListener(this);
			dlg = mInfoDialog;
		} else  if (id == ProfileMultiDelete.DIALOG_DETETE_PROGRESS) {
				mProgressDialog  = new ProgressDialog(this);
				mProgressDialog.setMessage(getResources().getString(R.string.deleting_profile));
				mProgressDialog.setCancelable(false);
				dlg = mProgressDialog;
		} else {
			dlg = null;
		}
		return dlg;
	}
	
	private void initDeleteProcess() {
		if (mWaitingUiHandler == null) {
			mWaitingUiHandler = new WaitingUiHandler();
		}
		if (mDeleteProfileProcessHandler == null) {
			HandlerThread handlerthread = new HandlerThread("delete profile handler: Process Thread");
			handlerthread.start();
			Looper looper = handlerthread.getLooper();
			mDeleteProfileProcessHandler = new DeleteProfileProcessHandler(
					looper, mWaitingUiHandler);
		}
	}
}

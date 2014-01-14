package apollo.profilesetting.view;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;

public class ChooseSoundDialog implements
		AbstractSetAlarm.OnActivityResultListener {

	public interface OnSoundSelectedListener {
		public abstract void onSelectedChanged(Uri uri, int type, String key);
	}
	
	public static final int DIALOG_MUSIC = 1;
	public static final int DIALOG_RINGTONE = 0;
	static final int SOUND_PREFERENCES[] = { R.string.sound_ringtones, R.string.sound_music };
	public static final int START_ALETTPICKER_REQUEST_CODE = 101;
	public static final int COLUMN_ID__INDEX = 0;
	public static final int COLUMN_TITLE_INDEX = 1;
	public static final int COLUMN_URI_INDEX = 2;
	private Uri mAlertUri;
	private Uri mTempUri;
	private Cursor mAudioCursor;
	private Dialog mDialog;
	private Drawable mIcon;
	private String mKey;
	private MediaPlayer mMediaPlayer;
	private ProfileEdit mProfileEdit;
	private OnSoundSelectedListener mSoundSelectedListener;
	private boolean mPlaying;
	private boolean mItemSelected;
	private int mSoundType;
	private int mTempMediaCursorPosition = -1;
	

	public ChooseSoundDialog(ProfileEdit profileedit, Uri uri) {
		mMediaPlayer = null;
		mAlertUri = uri;
		mProfileEdit = profileedit;
	}

	private void createDialog(final Cursor cursor) {
		AlertDialog.Builder builder = null;
		
		builder = new AlertDialog.Builder(mProfileEdit);
		builder.setIcon(mIcon);
		builder.setTitle(mProfileEdit.getString(SOUND_PREFERENCES[mSoundType]));
		if (cursor != null) {
			builder.setSingleChoiceItems(cursor, mTempMediaCursorPosition, "title", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
		            mItemSelected = true;
		            if(mPlaying && mMediaPlayer != null) {
						try {
							mPlaying = false;
							mMediaPlayer.reset();
						} catch (Exception exception) {
						}
		            }
					if (mMediaPlayer == null) {
						mMediaPlayer = new MediaPlayer();
						if (mMediaPlayer != null) {
							mMediaPlayer.setOnErrorListener(new OnErrorListener() {
								@Override
								public boolean onError(MediaPlayer mp, int what, int extra) {
									mp.stop();
									mp.release();
									return true;
								}
							});
						}
					}
					if (!cursor.moveToPosition(which)) {
						mTempUri = null;
					} else {
						mTempMediaCursorPosition = which;
						mTempUri = ContentUris.withAppendedId(Uri.parse(cursor.getString(COLUMN_URI_INDEX)), cursor.getLong(0));
					}
					if (mTempUri != null) {
						mPlaying = true;
						mMediaPlayer.setAudioStreamType(3);
						try {
							mMediaPlayer.setDataSource(mProfileEdit, mTempUri);
							mMediaPlayer.prepare();
						} catch (Exception ex) {
						}
						mMediaPlayer.start();
					}
	            }
			});
			builder.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog) {
		            if(mPlaying && mMediaPlayer != null) {
		            	try {
			            	mPlaying = false;
			            	mMediaPlayer.reset();
		            	} catch (Exception ex) {
		            	}
		            }					
				}
			});
			builder.setPositiveButton(android.R.string.ok, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mPlaying && mMediaPlayer != null) {
						try {
							mPlaying = false;
							mMediaPlayer.reset();
						} catch (Exception ex) {
						}
						if (mTempUri != null && mItemSelected == true) {
							mAlertUri = mTempUri;
						}
						if (cursor.requery() && mItemSelected == true) {
							mSoundSelectedListener.onSelectedChanged(mAlertUri, mSoundType + 1, mKey);
						}
					}
				}
			}).setNegativeButton(android.R.string.cancel, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
		            if(mPlaying && mMediaPlayer != null) {
						try {
							mPlaying = false;
							mMediaPlayer.reset();
						} catch (Exception ex) {
						}
		            }
				}
			});
		}

		mDialog = builder.create();
		if (mDialog != null) {
			mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				@Override
				public void onDismiss(DialogInterface dialog) {
		            if(mPlaying && mMediaPlayer != null) {
						try {
							mPlaying = false;
							mMediaPlayer.reset();
						} catch (Exception exception) {
						}
		            }
		            if(cursor != null) {
		                cursor.close();
		            }
				}}
			);
		}
	}

	private int getAlertPosition(Cursor cursor) {
		int pos = -1;
		int idx = 0;
		if (mAlertUri == null || cursor == null) {
			return -1;
		}
		String str = null;
		Uri uri = null;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			str = cursor.getString(COLUMN_URI_INDEX);
			uri = Uri.parse(str);
			uri = ContentUris.withAppendedId(uri, cursor.getLong(0));
			if (uri.equals(mAlertUri)) {
				pos = idx;
				break;
			}
			idx ++;
		}
		return pos;
	}

	private void setCursorForMusic() {
		String s = "\"";
		if (mAudioCursor == null || !mAudioCursor.requery()) {
			String cols[] = null;
			ArrayList<String> list = null;
			StringBuilder buf = null;
			
			cols = new String[3];
			cols[COLUMN_ID__INDEX] = "_id";
			cols[COLUMN_TITLE_INDEX] = "title";
			cols[COLUMN_URI_INDEX] = s + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + s;
			
			list = new ArrayList<String>();
			list.add("is_music");
			buf = new StringBuilder();
			for (int j = list.size() - 1; j >= 0; j--) {
				String s2 = (String) list.get(j);
				buf.append(s2).append("=1 or ");
			}

			if (list.size() > 0) {
				buf.setLength(buf.length() - 4);
			}
			mAudioCursor = mProfileEdit.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols, buf.toString(), null,
					"title_key");
		}
	}

	public void onActivityResult(int i, int j, Intent intent) {
		if (i == START_ALETTPICKER_REQUEST_CODE) {
			if (intent != null) {
				mAlertUri = (Uri) intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				mSoundSelectedListener.onSelectedChanged(mAlertUri, mSoundType + 1, mKey);
			}
			mProfileEdit.removeOnActivityResultListener();
		}
	}

	public void setOnSoundSelectedListener(
			OnSoundSelectedListener listener, String key) {
		mSoundSelectedListener = listener;
		mKey = key;
	}

	public void showDialog(int type) {
		mSoundType = type;
		mAudioCursor = null;
		
		switch (type) {
		case ChooseSoundDialog.DIALOG_RINGTONE:
			Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mAlertUri);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
			mProfileEdit.startActivityForResult(intent, START_ALETTPICKER_REQUEST_CODE);
			mProfileEdit.addOnActivityResultListener(this);
			break;
		case ChooseSoundDialog.DIALOG_MUSIC:
			setCursorForMusic();
			mIcon = mProfileEdit.getResources().getDrawable(R.drawable.ic_dialog_sound);
			mTempMediaCursorPosition = Integer.valueOf(getAlertPosition(mAudioCursor));
			if (mMediaPlayer == null) {
				mMediaPlayer = new MediaPlayer();
			}
			createDialog(mAudioCursor);
			mDialog.show();
			break;
		}
	}

	public void stopAlarmSound() {
		if (mPlaying) {
			MediaPlayer mediaplayer = mMediaPlayer;
			if (mediaplayer != null) {
				try {
					mPlaying = false;
					mMediaPlayer.reset();
				} catch (Exception exception) {
				}
			}
		}
		if (mAudioCursor != null) {
			mAudioCursor.close();
			mAudioCursor = null;
		}
	}

	public void stopMedia() {
		if (mPlaying) {
			MediaPlayer mediaplayer = mMediaPlayer;
			if (mediaplayer != null) {
				try {
					mPlaying = false;
					mMediaPlayer.reset();
				} catch (Exception exception) {
				}
			}
		}
		if (mAudioCursor != null)
			mAudioCursor.close();
	}
}

package apollo.profilesetting.view;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import apollo.profilesetting.model.Profile;
import apollo.profilesetting.util.ProfileConstants;

public class ProfileProvider extends ContentProvider {

	public class ProfileDatabaseHelper extends SQLiteOpenHelper {

		public ProfileDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		public int getDefaultStreamVolume() {
			return 7;
		}

		public int getStreamMaxVolume() {
			return ((AudioManager) getContext().getSystemService("audio"))
					.getStreamMaxVolume(2);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			StringBuffer buf1 = null;
			StringBuffer buf2 = null;
			String cmd_txt = null;

			buf1 = new StringBuffer();
			buf1.append("create table ");
			buf1.append(ProfileProvider.TABLE_NAME);
			buf1.append(" (");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_ID]);
			buf1.append(" integer primary key autoincrement,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME]);
			buf1.append(" text,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_APPLIED]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_HOUR]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_MINITUE]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_REPEAT]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_AIRPLANE_MODE]);
			buf1.append(" integer not null default -1,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_WLAN_ON]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_BLUETOOTH_ON]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GPS_ON]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_SILENT_ON]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RINGER_VOLUME]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_VIBRATE_ON]);
			buf1.append(" integer,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GSM_RINGTONE_TYPE]);
			buf1.append(" integer default -1,");
			buf1.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GSM_RINGTONE_URISTRING]);
			buf1.append(" text default null);");
		
			cmd_txt = buf1.toString();
			db.execSQL(cmd_txt);

			buf2 = new StringBuffer("insert into ");
			buf2.append(ProfileProvider.TABLE_NAME);
			buf2.append(" (");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_APPLIED]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_HOUR]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_MINITUE]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RULE_REPEAT]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_AIRPLANE_MODE]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_WLAN_ON]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_BLUETOOTH_ON]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GPS_ON]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_SILENT_ON]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_RINGER_VOLUME]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_VIBRATE_ON]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GSM_RINGTONE_TYPE]);
			buf2.append(",");
			buf2.append(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_GSM_RINGTONE_URISTRING]);
			buf2.append(") values");

			buf1 = new StringBuffer(buf2);
			buf1.append(" ('");
			buf1.append(getContext().getResources().getString(
					R.string.profile_default));
			buf1.append("',0,0,0,127,0,0,0,1,0,");
			buf1.append(getDefaultStreamVolume());
			buf1.append(",3,-1,null);");
			cmd_txt = buf1.toString();
			db.execSQL(cmd_txt);

			buf1 = new StringBuffer(buf2);
			buf1.append(" ('");
			buf1.append(getContext().getResources().getString(R.string.work));
			buf1.append("',0,9,30,31,0,-1,-1,-1,0,");
			buf1.append(getDefaultStreamVolume());
			buf1.append(",-1,-1,null);");
			cmd_txt = buf1.toString();
			db.execSQL(cmd_txt);

			buf1 = new StringBuffer(buf2);
			buf1.append(" ('");
			String s9 = getContext().getResources().getString(R.string.home);
			buf1.append(s9);
			buf1.append("',0,18,30,127,-1,-1,-1,-1,1,-1,1,-1,null);");
			cmd_txt = buf1.toString();
			db.execSQL(cmd_txt);

			buf1 = new StringBuffer(buf2);
			buf1.append(" ('");
			buf1.append(getContext().getResources().getString(
					R.string.save_power));
			buf1.append("',0,0,0,127,-1,0,0,0,-1,-1,-1,-1,null);");
			cmd_txt = buf1.toString();
			db.execSQL(cmd_txt);

			buf1 = new StringBuffer(buf2);
			buf1.append(" ('");
			String s13 = getContext().getResources().getString(R.string.map);
			buf1.append(s13);
			buf1.append("',0,0,0,127,-1,-1,-1,1,-1,-1,-1,-1,null);");
			cmd_txt = buf1.toString();
			db.execSQL(cmd_txt);

			buf1 = new StringBuffer(buf2);
			buf1.append(" ('");
			buf1.append(getContext().getResources().getString(R.string.outdoor));
			buf1.append("',0,0,0,127,");
			buf1.append("-1,-1,-1,1,");
			buf1.append("0,");
			buf1.append(getStreamMaxVolume()).append(", 1,-1,null);");
			cmd_txt = buf1.toString();
			db.execSQL(cmd_txt);

			buf1 = new StringBuffer(buf2);
			buf1.append(" ('");
			buf1.append(getContext().getResources().getString(R.string.meeting));
			buf1.append("',0,0,0,31,-1,-1,-1,-1,1,-1,1,-1,null);");
			cmd_txt = buf1.toString();
			db.execSQL(cmd_txt);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion < 1) {
				db.execSQL("DROP TABLE IF EXISTS profiles;");
				onCreate(db);
			} else if (oldVersion == 1) {
				db.execSQL("ALTER TABLE profiles ADD airplane_mode INTEGER NOT NULL DEFAULT -1");
				oldVersion++;
			}
		}
	}

	private static final String DATABASE_NAME = "profile.db";
	private static final String TABLE_NAME = "profiles";
	private static final UriMatcher uriMatcher = new UriMatcher(-1);
	private ProfileDatabaseHelper mDbHelper;

	static {
		uriMatcher.addURI(ProfileConstants.PACKAGE_NAME, "item", 1);
		uriMatcher.addURI(ProfileConstants.PACKAGE_NAME, "item/#", 2);
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		mDbHelper = new ProfileDatabaseHelper(context, ProfileProvider.DATABASE_NAME, null, 2);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = null;
		Cursor cursor = null;

		db = this.mDbHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case 1:
			cursor = db.query(ProfileProvider.TABLE_NAME, projection, selection, selectionArgs, null,
					null, sortOrder);
			ContentResolver resolver = getContext()
					.getContentResolver();
			cursor.setNotificationUri(resolver, uri);
			break;
		case 2:
			String id = (String) uri.getPathSegments().get(1);
			String clause = "_id=" + id;
			if (TextUtils.isEmpty(selection) == false) {
				clause = " AND (" + selection + ")";
			}
			cursor = db.query(ProfileProvider.TABLE_NAME, projection, clause, selectionArgs,
					null, null, sortOrder);
			break;
		default:
			throw new IllegalArgumentException("cannot query datas with the URI:" + uri);
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		String type = null;
		
		switch (uriMatcher.match(uri)) {
		case 1:
			type = "vnd.android.cursor.dir/vnd.apollo.profile";
			break;
		case 2:
			type = "vnd.android.cursor.item/vnd.apollo.profile";
			break;
		default:
			type = "Unknow URI" + uri;
		}
		return type;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = null;
		Uri newUri = null;
		long id = -1;
		
		if (uriMatcher.match(uri) != 1) {
			throw new IllegalArgumentException(
					"cannot insert data into the uri:" + uri);
		}
		
		if (!values
				.containsKey(Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME])) {
			values.put(
					Profile.ProfileColumns.PROFILE_QUERY_COLUMNS[Profile.ProfileColumns.PROFILE_NAME],
					"Temporary Profile");
		}

		db = mDbHelper.getWritableDatabase();
		id = db.insert(ProfileProvider.TABLE_NAME, null, values);
		if (id > 0) {
			newUri = ContentUris.withAppendedId(
					Profile.ProfileColumns.CONTENT_URI, id);
			getContext().getContentResolver().notifyChange(newUri, null);
		} else {
			throw new IllegalArgumentException("Failed to insert row into URI "
					+ uri);
		}
		return newUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int numChangedRows = -1;
		SQLiteDatabase db;

		db = mDbHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case 1:
			numChangedRows = db.delete(ProfileProvider.TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case 2:
			String clause = "_id=" + (String) uri.getPathSegments().get(1);
			if (TextUtils.isEmpty(selection) == false) {
				clause += " AND (" + selection + ")";
			}
			numChangedRows = db.delete(ProfileProvider.TABLE_NAME, clause, null);
			break;
		default:
			throw new IllegalArgumentException(
					"cannot delete datas with the URI:" + uri);
		}

		return numChangedRows;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = null;
		int numChangedRows = -1;

		db = mDbHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case 1:
			numChangedRows = db.update(ProfileProvider.TABLE_NAME, values, selection,
					selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case 2:
			String clause = "_id=" + (String) uri.getPathSegments().get(1);
			if (TextUtils.isEmpty(selection) == false) {
				clause += " AND (" + selection + ")";
			}
			numChangedRows = db.update(ProfileProvider.TABLE_NAME, values, clause,
					selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return numChangedRows;
	}

}

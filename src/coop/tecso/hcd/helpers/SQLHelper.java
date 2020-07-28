package coop.tecso.hcd.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import coop.tecso.hcd.persistence.DatabaseHelper;

public class SQLHelper {
	
	private static final String LOG_TAG = SQLHelper.class.getSimpleName();
	
	private SQLiteDatabase database;
	private Context context;
	
	public SQLHelper(Context context) {
		this.context = context;
	}

	/**
	 * Open connection whit readable database
	 */
	public void openDatabase(){
		database = new DatabaseHelper(context).getReadableDatabase();
	}

	public Cursor query(String table, String[] columns, String selection, 
			String[] selectionArgs, String groupBy, String having, String orderBy){
		return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	}

	public void closeDatabase(){
		try {
			database.close();
		} catch (Exception e) {
			Log.e(LOG_TAG, "closeDatabase: ***ERROR***", e);
		}
	}

}

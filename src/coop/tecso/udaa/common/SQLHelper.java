package coop.tecso.udaa.common;

import coop.tecso.udaa.persistence.DatabaseHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public final class SQLHelper {
	
	private static final String LOG_TAG = SQLHelper.class.getSimpleName();
	
	private SQLiteDatabase db;
	private Context context;
	
	SQLHelper(Context context) {
		this.context = context;
	}
	
	/**
	 * Open Custom Database from external
	 */
	public void openDatabase(String dbName){
		String path = context.getDir("db", Context.MODE_PRIVATE) + "/" + dbName;
        try {
        	db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
        	Log.e(LOG_TAG, "openDatabase: ***ERROR***", e);
        }
	}
	
	/**
	 * Open connection whit readable database
	 */
	void openDatabase(){
		db = new DatabaseHelper(context).getReadableDatabase();
	}

	Cursor query(String table, String[] columns, String selection,
				 String[] selectionArgs, String groupBy, String having, String orderBy){
		return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	}

	Cursor rawQuery(String sql, String... selectionArgs){
		return db.rawQuery(sql, selectionArgs);
	}
	
	
	void closeDatabase(){
		try {
			db.close();
		} catch (Exception e) {
			Log.e(LOG_TAG, "closeDatabase: ***ERROR***", e);
		}
	}
}
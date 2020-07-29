package coop.tecso.hcd.persistence;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import coop.tecso.hcd.R;

/**
 * 
 * @author tecso.coop
 *
 */
public final class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String LOG_TAG = DatabaseHelper.class.getName();

	private static final String DATABASE_NAME = "hcd.db";
	private static final int 	DATABASE_VERSION = 95;

	private final Context context;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.db_config); 
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		Log.i(LOG_TAG, "Creating database...");
		try {
			for (Class<?> clazz: DatabaseConfigUtil.PERSISTENT_CLASSES) {
				TableUtils.createTable(connectionSource, clazz);    
			}   			
			Log.i(LOG_TAG, "Se ha creado la base de datos");
			//
			initialImport(db);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Can't create database", e);
			throw new RuntimeException(e);
		}  
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. 
	 * This allows you to adjust the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		Log.i(LOG_TAG, String.format("onUpgrade, oldVersion=[%s], newVersion=[%s]", oldVersion, newVersion));
		int initVersion = oldVersion;
		try {
			// Loop round until newest version has been reached and add the appropriate migration
			boolean shouldAlterAtCerrada = false;
			oldVersion++;
			while (oldVersion <= newVersion) {				
				switch (oldVersion) {
					case 58: {
						// Add migration for version 58
						UpgradeHelper.addUpgrade(oldVersion);
						break;
						}
					case 64: {
						// Add migration for version 64
						UpgradeHelper.addUpgrade(oldVersion);
						break;
						}
					case 69: {
						// Add migration for version 69
						if(this.requireThumbnailColumn(db))
							UpgradeHelper.addUpgrade(oldVersion);
						break;
						}
					case 70: {
						// Add migration for version 70
						UpgradeHelper.addUpgrade(oldVersion);
						break;
						}
					case 71: {
						// Add migration for version 71
						UpgradeHelper.addUpgrade(oldVersion);
						break;
						}
					case 72: {
						// Add migration for version 72
						UpgradeHelper.addUpgrade(oldVersion);
						break;
						}
					case 73: {
						// Add migration for version 73
						UpgradeHelper.addUpgrade(oldVersion);
						break;
						}
					case 77: {
						// Add migration for version 77
						UpgradeHelper.addUpgrade(oldVersion);
						break;
						}
					case 80:
					case 81: {
						shouldAlterAtCerrada = true;
						break;
					}
					case 92: {
						// Add migration for version 77
						UpgradeHelper.addUpgrade(oldVersion);
						break;
					}
					case 93: {
						if(!this.existsColumn(db, "extraFile")) {
							// Add migration for version 93
							UpgradeHelper.addUpgrade(oldVersion);
						}
						break;
					}
				}
				oldVersion++;
			}
			// Loop to create tables for persistent classes
			for (Class<?> clazz: DatabaseConfigUtil.PERSISTENT_CLASSES) {
				if(UpgradeHelper.canUpgrade(clazz)){
					// Drop Table
					TableUtils.dropTable(connectionSource, clazz, true);  
					// Create Table
					TableUtils.createTable(connectionSource, clazz);
				}
			}
			for (Class<?> clazz: DatabaseConfigUtil.IMMUTABLE_CLASSES) {
				TableUtils.createTableIfNotExists(connectionSource, clazz);
			}		
			
			// Get all the available updates
			List<String> updates = UpgradeHelper.availableUpdates(this.context.getResources());
			Log.d(LOG_TAG, String.format("Found a total of %s update statements", updates.size()));
			for (String statement : updates) {
				db.beginTransaction();
				try {
					Log.d(LOG_TAG, String.format("Executing statement: %s", statement));
					db.execSQL(statement);
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
			
			if(shouldAlterAtCerrada) {
				db.beginTransaction();
				try {
					Log.d(LOG_TAG, String.format("Executing statement: %s", "migration-AlterAtCerrada.sql"));
					final String sqlStatements = UpgradeHelper.loadAssetFile(this.context.getResources(), "migration-AlterAtCerrada.sql");

					final String[] splitSql = sqlStatements.split("\\r?\\n");
					for (final String sql : splitSql) {
						if (UpgradeHelper.isNotComment(sql))	updates.add(sql);
					}

					for (String statement : updates) {
						db.execSQL(statement);
					}
					db.setTransactionSuccessful();
				} 
				catch(Exception ex) {
				}
				finally {
					db.endTransaction();
				}
			}
			//
			initialImport(db);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Can't migrate databases, bootstrap database, data will be lost", e);
			onCreate(db, connectionSource);
		}
	}

	/**
	 * Close the database connections.
	 */
	@Override
	public void close() {
		super.close();
	}

	/**
	 * 
	 * @param db
	 */
	private void initialImport(SQLiteDatabase db){
		// Get all the available inserts
		List<String> inserts = UpgradeHelper.availableInserts(this.context.getResources());
		Log.d(LOG_TAG, String.format("Found a total of %s insert statements", inserts.size()));
		for (String statement : inserts) {
			db.beginTransaction();
			try {
				Log.d(LOG_TAG, String.format("Executing statement: %s", statement));
				db.execSQL(statement);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}
	
	protected boolean requireThumbnailColumn(SQLiteDatabase db) {	
		Cursor cursor = db.rawQuery("PRAGMA table_info('hcd_atencionValor')", null);
		try {
		    int nameIdx = cursor.getColumnIndexOrThrow("name");
		    while (cursor.moveToNext()) {
		        String name = cursor.getString(nameIdx);
		        if ("thumbnail".equals(name)) {
		        	return false;
		        }
		    }
		} finally {
		    cursor.close();
		}
		return true;
	}

	protected boolean existsColumn(SQLiteDatabase db, String columnName) {

		Cursor cursor = db.rawQuery("PRAGMA table_info('hcd_atencionValor')", null);
		try {
			int nameIdx = cursor.getColumnIndexOrThrow("name");
			while (cursor.moveToNext()) {
				String name = cursor.getString(nameIdx);
				if (columnName.toLowerCase().equals(name.toLowerCase())) {
					return true;
				}
			}
		} finally {
			cursor.close();
		}
		return false;
	}
}
package coop.tecso.udaa.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.List;

import coop.tecso.udaa.R;
import coop.tecso.udaa.utils.Constants;

public final class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String LOG_TAG = DatabaseHelper.class.getName();	
	
	private static final String DATABASE_NAME = "udaa.db";
	private static final int	DATABASE_VERSION = 346;

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

			this.initialImportDomicilio(db);
			this.initialImportPadron(db);
			
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
		try {
			boolean initDomicilio = false;
			boolean initPadron = false;
			
			// Loop round until newest version has been reached and add the appropriate migration
			while (oldVersion <= newVersion) {
				switch (oldVersion) {
				case 58: {
					// Add migration for version 58
					UpgradeHelper.addUpgrade(oldVersion);
					break;
					}
				case 330: {
					try {
						for (Class<?> clazz: DatabaseConfigUtil.IMMUTABLE_CLASSES) {
							TableUtils.createTable(connectionSource, clazz);
						} 
						initDomicilio = true;
						initPadron = true;
					}
					catch (Exception ignore) {}
					break;
					}
				case 331: 
				case 335:
				case 338:{
					for (Class<?> clazz: DatabaseConfigUtil.IMMUTABLE_CLASSES) {
						TableUtils.clearTable(connectionSource, clazz);
					} 
					initDomicilio = true;
					initPadron = true;
					break;
					}
				case 340: {
					// Add migration for version 340
					UpgradeHelper.addUpgrade(oldVersion);
					break;
					}
				case 341: {
					// Add migration for version 341
					UpgradeHelper.addUpgrade(oldVersion);
					break;
					}
				case 342: {
					// Add migration for version 342
					UpgradeHelper.addUpgrade(oldVersion);
					break;
					}
				case 343: {
					Log.i("TESTOSO", "343");
					// Add migration for version 343
					UpgradeHelper.addUpgrade(oldVersion);
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
			//
			initialImport(db);

			if(initDomicilio) {
				this.initialImportDomicilio(db);
			}
			if(initPadron) {
				this.initialImportPadron(db);
			}
			
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
	
	private void doImport(SQLiteDatabase db, final String fileName){
		// Get all the available inserts for a specific file
		List<String> inserts = UpgradeHelper.availableInserts(this.context.getResources(), fileName);
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
	
	private void initDBs(String shouldInitDBs) {
		SharedPreferences myPrefs = context.getSharedPreferences("settings", context.MODE_PRIVATE);
		SharedPreferences.Editor e = myPrefs.edit();
	    e.putString("initDBs", shouldInitDBs);
	    e.commit();
	}
	
	private void initialImportDomicilio(SQLiteDatabase db) {
		if(!shouldInitiateDB()) return;
		
		// Inicializar Barrios
		doImport(db, "inserts/insert_barrios_001.sql");

		// Inicializar Calles
		 doImport(db, "inserts/insert_calles_001.sql");
		 doImport(db, "inserts/insert_calles_002.sql");
		 doImport(db, "inserts/insert_calles_003.sql");

		 // Inicializar Zonas
		 doImport(db, "inserts/insert_zonas_001.sql");
		 doImport(db, "inserts/insert_zonas_002.sql");
		 doImport(db, "inserts/insert_zonas_003.sql");
		 doImport(db, "inserts/insert_zonas_004.sql");
		 doImport(db, "inserts/insert_zonas_005.sql");
		 doImport(db, "inserts/insert_zonas_006.sql");
		 doImport(db, "inserts/insert_zonas_007.sql");
		 doImport(db, "inserts/insert_zonas_008.sql");
		 doImport(db, "inserts/insert_zonas_009.sql");
		 doImport(db, "inserts/insert_zonas_010.sql");

		 //Inicializar sucursales
		 doImport(db, "inserts/insert_sucursal_001.sql");

		 //Inicializar provincias
		 doImport(db, "inserts/insert_provincias_001.sql");

		 //Inicializar ciudades
		 doImport(db, "inserts/insert_ciudades_001.sql");

	}
	
	private void initialImportPadron(SQLiteDatabase db) {
		if (!shouldInitiateDB()) {
            return;
        }
		
		 //Inicializar tipodocumento
		 doImport(db, "inserts/insert_tiposdocumento_001.sql");
	}
	
	private Boolean shouldInitiateDB() {
		SharedPreferences myPrefs = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        String initDBs = myPrefs.getString("initDBs", null);
        return initDBs == null || TextUtils.isEmpty(initDBs) || initDBs.equals(Constants.INIT_DBS);
    }

}
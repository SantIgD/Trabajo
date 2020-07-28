package coop.tecso.hcd.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.util.Crypto;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class TxtExporter {

	// MARK: - Data

	private SQLiteDatabase db;

	private TxtBuilder txtBuilder;

	// MARK: - Init

	public TxtExporter(SQLiteDatabase db) {
		this.db = db;
	}

	// MARK: - Interface

	public void export(String dbName, String exportFileNamePrefix) throws IOException {
		this.txtBuilder = new TxtBuilder();
		this.txtBuilder.start(dbName);

		// get tablas
		String sql = "select * from sqlite_master";
		Cursor cursor = this.db.rawQuery(sql, new String[0]);

		if (cursor.moveToFirst()) {
			do {
				String tableName = cursor.getString(cursor.getColumnIndex("name"));
				if (shouldExportTable(tableName)) {
					this.exportTable(tableName);
				}
			} while (cursor.moveToNext());
		}
		cursor.close();

		Crypto crypto = new Crypto(); 
		String txtString = this.txtBuilder.end();
		txtString = crypto.encryptBase64(txtString);
		this.writeToFile(txtString, exportFileNamePrefix + ".txt");
	}

	private boolean shouldExportTable(String tableName) {
		return  !tableName.equals("android_metadata") &&
				!tableName.equals("sqlite_sequence") &&
				!tableName.startsWith("uidx");
	}

	private void exportTable(final String tableName) {
		this.txtBuilder.openTable(tableName);
		String sql = "select * from " + tableName;
		Cursor cursor = this.db.rawQuery(sql, new String[0]);
	
		List<String> byteArrayCols = getByteArrayColumn(tableName);

		if (cursor.moveToFirst()) {
			int columnCount = cursor.getColumnCount();
			do {
				this.txtBuilder.openRow();
				this.exportRow(cursor, byteArrayCols, columnCount);
				this.txtBuilder.closeRow();
			} while (cursor.moveToNext());
		}
		cursor.close();
		this.txtBuilder.closeTable();
	}

	private void exportRow(Cursor cursor, List<String> byteArrayCols, int columnCount) {
		for (int i = 0; i < columnCount; i++) {
			if (byteArrayCols.contains(cursor.getColumnName(i))) {
				if (cursor.getBlob(i) != null) {
					this.txtBuilder.addColumn(cursor.getColumnName(i),"IMAGEN");
				}
				else {
					this.txtBuilder.addColumn(cursor.getColumnName(i), "");
				}
			}
			else {
				this.txtBuilder.addColumn(cursor.getColumnName(i), cursor.getString(i));
			}
		}
	}

	private List<String> getByteArrayColumn (String tableName) {
		String sql = "PRAGMA table_info(" + tableName + ")";

		List<String> byteArrayCols = new ArrayList<>();
		try (Cursor cursor = db.rawQuery(sql, null)) {
			int nameIdx = cursor.getColumnIndexOrThrow("name");
			int typeIdx = cursor.getColumnIndexOrThrow("type");
			while (cursor.moveToNext()) {
				String type = cursor.getString(typeIdx);
				if ("BLOB".equals(type)) {
					byteArrayCols.add(cursor.getString(nameIdx));
				}
			}
		}
		return byteArrayCols;
	}

	private void writeToFile(String txtString, String exportFileName) throws IOException {
	    File destinationDirectory = this.getDbExportDirectory();
	    
	    if (!destinationDirectory.exists()) {
			destinationDirectory.mkdirs();
		}
		
	    File file = new File(destinationDirectory, exportFileName);
		file.createNewFile();

		ByteBuffer buffer = ByteBuffer.wrap(txtString.getBytes());
		try (FileChannel channel = new FileOutputStream(file).getChannel()) {
			channel.write(buffer);
		}
	}

	private File getDbExportDirectory() {
		String exportDirectory = ParamHelper.getString(ParamHelper.EXPORT_DB_PATH,"ExportHC");

		File root = Environment.getExternalStorageDirectory();
		String path = root.getAbsolutePath()+ "/" + exportDirectory + "/";

		return new File(path);
	}

	static class TxtBuilder {
		private static final String OPEN_TXT_STANZA = "";
		private static final String DB_OPEN = "Base=";
		private static final String DB_CLOSE = "";
		private static final String TABLE_OPEN = "Tabla=";
		private static final String TABLE_CLOSE = "";
		private static final String ROW_OPEN = "";
		private static final String ENTER = "\n";
		private static final String ROW_CLOSE = ENTER;
		private static final String COL_OPEN = "\"";
		private static final String COL_CLOSE = "\",";

		private final StringBuilder stringBuilder;

		TxtBuilder() {
			this.stringBuilder = new StringBuilder();
		}

		void start(String dbName) {
			this.stringBuilder.append(OPEN_TXT_STANZA);
			this.stringBuilder.append(DB_OPEN);
			this.stringBuilder.append(dbName);
			this.stringBuilder.append(ENTER);
		}

		String end() {
			this.stringBuilder.append(DB_CLOSE);
			return this.stringBuilder.toString();
		}

		void openTable(String tableName) {
			this.stringBuilder.append(TABLE_OPEN);
			this.stringBuilder.append(tableName);
			this.stringBuilder.append(ENTER);
		}

		void closeTable() {
			this.stringBuilder.append(TABLE_CLOSE);
		}

		void openRow() {
			this.stringBuilder.append(ROW_OPEN);
		}

		void closeRow() {
			this.stringBuilder.append(ROW_CLOSE);
		}

		void addColumn(final String name, final String val) {
			this.stringBuilder.append(COL_OPEN);
			this.stringBuilder.append(val);
			this.stringBuilder.append(COL_CLOSE);
		}

	}

}

package coop.tecso.udaa.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import coop.tecso.udaa.domain.util.Crypto;
import coop.tecso.udaa.utils.ParamHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 *
 * @author ivan.schaab@tecso.coop
 *
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public final class TxtExporter {

	private SQLiteDatabase db;

	private TxtBuilder txtBuilder;
	public TxtExporter(SQLiteDatabase db) {
		this.db = db;
	}

	public void export(String dbName, String exportFileNamePrefix) throws IOException {
		this.txtBuilder = new TxtBuilder();
		this.txtBuilder.start(dbName);

		// get tablas
		String sql = "select * from sqlite_master";
		Cursor cursor = this.db.rawQuery(sql, new String[0]);
		if (cursor.moveToFirst()) {
			do {
				String tableName = cursor.getString(cursor.getColumnIndex("name"));

				// saltar metadata, sequence, and uidx (unique indexes)
				// agregar aqui las tablas que no se desean copiar
				if (!tableName.equals("android_metadata")
						&& !tableName.equals("sqlite_sequence")
						&& !tableName.startsWith("uidx")) {
					this.exportTable(tableName);
				}
			} while (cursor.moveToNext());
		}
		Crypto crypto = new Crypto();
		String txtString = this.txtBuilder.end();
		txtString=crypto.encryptBase64(txtString);
		this.writeToFile(txtString, exportFileNamePrefix + ".txt");
		cursor.close();
	}

	private void exportTable(final String tableName) {
		this.txtBuilder.openTable(tableName);
		String sql = "select * from " + tableName;
		if ("afi_zona".equals(tableName) || "afi_calle".equals(tableName) ) {
			sql += " LIMIT 100 " ;
			this.txtBuilder.appendText("Limite 100 registros para esta tabla.");
		}
		Cursor cursor = this.db.rawQuery(sql, new String[0]);

		List<String> byteArrayCols = getByteArrayColumn(tableName);

		if (cursor.moveToFirst()) {
			int cols = cursor.getColumnCount();
			do {
				this.txtBuilder.openRow();
				for (int i = 0; i < cols; i++) {
					if(byteArrayCols.contains(cursor.getColumnName(i))){
						if(cursor.getBlob(i) != null){
							this.txtBuilder.addColumn("IMAGEN");
						}
						else {
							this.txtBuilder.addColumn("");
						}
					}
					else {
						this.txtBuilder.addColumn(cursor.getString(i));
					}
				}
				this.txtBuilder.closeRow();
			} while (cursor.moveToNext());
		}
		cursor.close();
		this.txtBuilder.closeTable();
	}

	private List<String> getByteArrayColumn (String tableName) {
		List<String> byteArrayCols = new ArrayList<>();
		try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null)) {
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
		File root = Environment.getExternalStorageDirectory();
		String exportDBPath = ParamHelper.getString(ParamHelper.EXPORT_DB_PATH,"ExportUDAA");
		File dir = new File(root.getAbsolutePath()+ "/" + exportDBPath + "/");

		if (!dir.exists()) {
			dir.mkdirs();
		}

		File file = new File(dir, exportFileName);
		file.createNewFile();

		ByteBuffer buff = ByteBuffer.wrap(txtString.getBytes());
		try (FileChannel channel = new FileOutputStream(file).getChannel()) {
			channel.write(buff);
		}
	}

	@SuppressWarnings("SameParameterValue")
	class TxtBuilder {
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

		private final StringBuilder builder;

		TxtBuilder() {
			this.builder = new StringBuilder();
		}

		void appendText(String texto) {
			this.builder.append(texto);
			this.builder.append(ENTER);
		}

		void start(String dbName) {
			this.builder.append(OPEN_TXT_STANZA);
			this.builder.append(DB_OPEN);
			this.builder.append(dbName);
			this.builder.append(ENTER);
		}

		String end() {
			this.builder.append(DB_CLOSE);
			return this.builder.toString();
		}

		void openTable(String tableName) {
			this.builder.append(TABLE_OPEN);
			this.builder.append(tableName);
			this.builder.append(ENTER);
		}

		void closeTable() {
			this.builder.append(TABLE_CLOSE);
		}

		void openRow() {
			this.builder.append(ROW_OPEN);
		}

		void closeRow() {
			this.builder.append(ROW_CLOSE);
		}

		void addColumn(final String val) {
			this.builder.append(COL_OPEN);
			this.builder.append(val);
			this.builder.append(COL_CLOSE);
		}
	}
}

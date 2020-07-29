package coop.tecso.hcd.persistence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

/**
 * Used as a aid in the migration process for loading 
 * required SQL files as specified by a given version.
 * 
 * @author tecso.coop
 * 
 */
public final class UpgradeHelper {

	private static final String LOG_TAG = UpgradeHelper.class.getSimpleName();

	protected static final Set<Integer> VERSION;
	static {
		VERSION = new LinkedHashSet<Integer>();
	}

	/**
	 * Add the given version to the list of available updates
	 */
	public static final void addUpgrade(final int version) {
		Log.d(LOG_TAG, String.format("Adding %s to upgrade path", version));
		VERSION.add(version);
	}

	/**
	 * Get all available SQL Statements
	 * 
	 * @param resources the {@link Resources} from the given {@link Context} which maybe using the helper class
	 * @return A list of SQL statements which have been included in the
	 */
	public static List<String> availableInserts(Resources resources) {
		final List<String> inserts = new ArrayList<String>();
		
		// DDL files must be kept in assets/inserts/inserts.sql
		final String fileName = "inserts/inserts.sql";

		final String sqlStatements = loadAssetFile(resources, fileName);
		final String[] splitSql = sqlStatements.split("\\r?\\n");
		for (final String sql : splitSql) {
			if (isNotComment(sql))	inserts.add(sql);
		}
		return inserts;
	}

	/**
	 * Get all available SQL Statements
	 * 
	 * @param resources the {@link Resources} from the given {@link Context} which maybe using the helper class
	 * @return A list of SQL statements which have been included in the
	 */
	public static List<String> availableUpdates(Resources resources) {
		final List<String> updates = new ArrayList<String>();

		for (final Integer version : VERSION) {
			// Migration files must be kept in assets/updates/migration-X.sql
			final String fileName = String.format("updates/migration-%s.sql", version);

			Log.d(LOG_TAG, String.format(
					"Adding db version [%s] to update list, loading file [%s]", version, fileName));

			final String sqlStatements = loadAssetFile(resources, fileName);

			final String[] splitSql = sqlStatements.split("\\r?\\n");
			for (final String sql : splitSql) {
				if (isNotComment(sql))	updates.add(sql);
			}
		}
		return updates;
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public static boolean canUpgrade(Class<?> clazz){
		Log.d(LOG_TAG, String.format("canUpgrade %s entity ?", clazz.getSimpleName()));
		for (Class<?> t: DatabaseConfigUtil.IMMUTABLE_CLASSES) {
			if(t == clazz) return false;
		}
		return true;
	}

	/**
	 * Load the given asset file, throws wrapped {@link RuntimeException} if not found
	 * 
	 * @param fileName of the file to load, including asset directory path and sub path if required
	 * @param resources the {@link Resources}, usually from a {@link Context}
	 * @return the fully loaded file as a {@link String}
	 */
	public static String loadAssetFile(Resources resources, String fileName) {
		try {
			final InputStream is = resources.getAssets().open(fileName);
			final byte[] buffer = new byte[is.available()];
			is.read(buffer);
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(buffer);
			os.close();
			is.close();
			return os.toString();
		} catch (final IOException e) {
			Log.e(LOG_TAG, "IOException: ", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * A comment must consist of either '--' or '#'
	 * 
	 * @return true if not found to be an SQL Comment
	 */
	public static boolean isNotComment(final String sql) {
		return !(TextUtils.isEmpty(sql) || sql.startsWith("--") || sql.startsWith("#"));
	}
}
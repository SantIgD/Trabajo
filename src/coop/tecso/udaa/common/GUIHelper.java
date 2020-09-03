package coop.tecso.udaa.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public final class GUIHelper {

	public static ProgressDialog getIndetProgressDialog(Context context, CharSequence title, CharSequence message) {
		try {
			return ProgressDialog.show(context, title, message, true, true);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static void showRecoverableError(Context context, CharSequence error) {
		Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
	}
	
	public static void showError(Context context, CharSequence error, final int color) {
		Toast toast = Toast.makeText(context, error, Toast.LENGTH_LONG);
		TextView textView = toast.getView().findViewById(android.R.id.message);
		textView.setTextColor(color);
		toast.show();
	}	

	public static void showError(Context context, CharSequence error) {
		Toast.makeText(context, error, Toast.LENGTH_LONG).show();
	}
	public static boolean canHandleIntent(Context context, Intent intent){
	    PackageManager packageManager = context.getPackageManager();
	    List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	    return activities.size() > 0;
	}

}
package coop.tecso.hcd.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.List;

import coop.tecso.hcd.R;

public final class GUIHelper {

	public static void showMessage(Context ctx, CharSequence error) {
		Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
	}
	
	public static void showError(Context ctx, CharSequence error) {
		Toast.makeText(ctx, error, Toast.LENGTH_LONG).show();
	}	

	public static void showErrorLossSession(final Activity context) {
		showErrorDialog(context, "Error al iniciar sesión con UDAA.");
	}

	public static void showErrorDialog(final Activity context, String error) {
		final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle("Error de Sesión");
		alertDialog.setMessage(error);
		alertDialog.setCancelable(false);
		alertDialog.setIcon(R.drawable.ic_error_default);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.accept), (dialog, id) -> {
            alertDialog.dismiss();
            context.finish();
        });
		alertDialog.show();
	}

	public static boolean canHandleIntent(Context context, Intent intent){
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return activities.size() > 0;
	}

	private static Float scale;

	public static int dpToPixel(int dp, Context context) {
		if (scale == null) {
			scale = context.getResources().getDisplayMetrics().density;
		}
		return (int) ((float) dp * scale);
	}

	public static void dismissDialog(Dialog dialog) {
		if (dialog == null || !dialog.isShowing()) {
			return;
		}

		try {
			dialog.dismiss();
		} catch (Exception ignore) {}
	}

	public interface VoidFunction {
		void function();
	}

	public static AlertDialog createAlertDialog(Context context, int title, int mensaje, VoidFunction yesFunction, VoidFunction noFunction){
		String titleString = context.getString(title);
		String mensajeString = context.getString(mensaje);

		return createAlertDialog(context, titleString, mensajeString, yesFunction, noFunction);
	}

	public static AlertDialog createAlertDialog(Context context, String title, String mensaje, VoidFunction yesFunction, VoidFunction noFunction){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(mensaje);
		builder.setPositiveButton(R.string.yes,  (dialog, id) -> {
			if(yesFunction != null) {
					yesFunction.function();
				}
		});

		builder.setNegativeButton(R.string.no,  (dialog, id) -> {
			if(noFunction != null) {
					noFunction.function();
			}
		});
		return builder.create();
	}

}
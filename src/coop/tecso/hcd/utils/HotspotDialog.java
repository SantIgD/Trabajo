package coop.tecso.hcd.utils;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import coop.tecso.hcd.R;

public class HotspotDialog {

    public static void showDialog(Context context, boolean activate) {
        String message = activate ? "Active el anclaje WiFi" : "Desactive el anclaje WiFi";
        showDialog(context, message);
    }

    public static void showDialog(Context context, int messageStringID) {
        showDialog(context, context.getString(messageStringID));
    }

    private static void showDialog(Context sourceActivity, String message){
        final Dialog dialog = new Dialog(sourceActivity);
        dialog.setTitle(R.string.atencion);
        dialog.setContentView(R.layout.dialog_hotspot);
        dialog.setCancelable(false);

        Uri videoUri = getVideoUri(sourceActivity);

        final VideoView videoView = dialog.findViewById(R.id.videoView);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(mediaPlayer -> mediaPlayer.setLooping(true));

        Button acceptButton = dialog.findViewById(R.id.btnAceptar);
        acceptButton.setOnClickListener(v -> {
            dialog.dismiss();
            videoView.stopPlayback();
        });

        TextView textView = dialog.findViewById(R.id.messageTextView);
        textView.setText(message);

        videoView.start();
        dialog.show();

        growWidth(dialog);
    }

    private static Uri getVideoUri(Context context) {
        String uri = "android.resource://" + context.getPackageName() + "/" + R.raw.hotspot;
        return Uri.parse(uri);
    }

    private static void growWidth(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        DisplayMetrics metrics = dialog.getContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}

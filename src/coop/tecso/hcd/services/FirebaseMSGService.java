package coop.tecso.hcd.services;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import coop.tecso.hcd.utils.Constants;

public class FirebaseMSGService extends FirebaseMessagingService {

	private static final String TAG = "FCM Service";

	 @Override
	 public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        String type = remoteMessage.getData().get("type");

        if (type != null && type.equals(Constants.NEW_ATTENTION)) {
            Intent intent = new Intent(Constants.NEW_ATTENTION);
            sendBroadcast(intent);
        }
	 }
}

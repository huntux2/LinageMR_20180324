package mr.linage.com.service;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "MyFMService";
    private String socket_client_ip = "";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG,"data:"+data);
        socket_client_ip = data.get("ip");
        Log.d(TAG,"socket_client_ip:"+ socket_client_ip);
        Intent mIntent=  new Intent(getApplicationContext(), MyService.class);
        mIntent.putExtra("socket_client_ip", socket_client_ip);
        startService(mIntent);
//        sendNotification(notification, data);
    }

//    /**
//     * Create and show a custom notification containing the received FCM message.
//     *
//     * @param notification FCM notification payload received.
//     * @param data FCM data payload received.
//     */
//    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
//        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
//                .setContentTitle(notification.getTitle())
//                .setContentText(notification.getBody())
//                .setAutoCancel(true)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setContentIntent(pendingIntent)
//                .setContentInfo(notification.getTitle())
//                .setLargeIcon(icon)
//                .setColor(Color.RED)
//                .setLights(Color.RED, 1000, 300)
//                .setDefaults(Notification.DEFAULT_VIBRATE)
//                .setSmallIcon(R.mipmap.ic_launcher);
//
//        try {
//            String picture_url = data.get("picture_url");
//            if (picture_url != null && !"".equals(picture_url)) {
//                URL url = new URL(picture_url);
//                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                notificationBuilder.setStyle(
//                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.getBody())
//                );
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // Notification Channel is required for Android O and above
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    "channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT
//            );
//            channel.setDescription("channel description");
//            channel.setShowBadge(true);
//            channel.canShowBadge();
//            channel.enableLights(true);
//            channel.setLightColor(Color.RED);
//            channel.enableVibration(true);
//            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        notificationManager.notify(0, notificationBuilder.build());
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
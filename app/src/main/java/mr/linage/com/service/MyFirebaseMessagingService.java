package mr.linage.com.service;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Map;

import mr.linage.com.soket.TCPClient;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "MyFMService";

    private String socket_client_ip = "";
    private int socket_server_port = 9999;
    TCPClient tc = null;

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
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        File videoFile = new File(Environment.getExternalStorageDirectory() + "/screenrecord-sample.mp4");
//        if(videoFile.exists()) {
//            Log.d(TAG,"search3:files.setDataSource");
//            retriever.setDataSource(videoFile.toString());
//            Log.d(TAG,"search3:files.getFrameAtTime");
//            Bitmap bitmap = retriever.getFrameAtTime();
//            Log.d(TAG,"search3:bitmap:"+bitmap);
//        }
//        search();
        if(!"".equals(socket_client_ip)) {
            tc = new TCPClient(socket_client_ip, socket_server_port) {
                @Override
                public void run() {
                    super.run();
                    // runOnUiThread를 추가하고 그 안에 UI작업을 한다.
                    search();
                }
                @Override
                public void sendDing(String msg) {
                    super.sendDing(msg);
                    stop_search = true;
                }
            };
            tc.start();
        }
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

    public void search() {
        Log.d(TAG,"search1");
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            File videoFile = new File(Environment.getExternalStorageDirectory() + "/screenrecord-sample.mp4");
            Log.d(TAG,"search2");
            if(videoFile.exists()) {
                Log.d(TAG,"search3:videoFile:"+videoFile.toString());
                while (true) {
                    try {
                        Log.d(TAG,"search3:files.setDataSource");
                        retriever.setDataSource(this, Uri.parse(videoFile.toString()));
                        Log.d(TAG,"search3:files.getFrameAtTime");
                        bitmap = retriever.getFrameAtTime();
                    } catch (Exception ss) {
                        Log.d(TAG,"setDataSource:"+ss.toString());
                    }
                    Log.d(TAG,"search4:bitmap:"+bitmap);
                    if(bitmap!=null) {
                        break;
                    }
                }
                Log.d(TAG,"search4:bitmap:"+bitmap);
                if(bitmap!=null) {
                    Log.d(TAG,"search5");
                    final Bitmap copyBitmap = bitmap.copy(bitmap.getConfig(),true);
                    Log.d(TAG,"search6");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(copyBitmap!=null)
                                bitmap(copyBitmap);
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(bitmap!=null) {
                bitmap.recycle();
                bitmap = null;
            }
            if(retriever!=null) {
                retriever.release();
                retriever = null;
            }
        }
    }

    public void bitmap(Bitmap bitmap) {
        Log.d(TAG,"bitmap1");
        /**
         * 로직 실행 확인 : 49,121,206
         * 위치 : 1125, 320
         * 액션 : 없음
         */
        {
            int x = 130;
            int y = 175;
            pixelSearch(bitmap, x, y,"");
            if(stop_search) {
                return;
            }
        }
        /**
         * 세이프티 존 확인 : 49,121,206
         * 위치 : 1125, 320
         * 액션 : 없음
         */
        {
            int x = 1125;
            int y = 320;
            pixelSearch(bitmap, x, y,"");
            if(stop_search) {
                return;
            }
        }
        /**
         * HP 빨강 확인 : 154,23,19
         * 위치 : 102, 253
         * 액션 : 4번째 클릭
         */
        {
            int x = 102;
            int y = 253;
            pixelSearch(bitmap, x, y,"app_log_1");
            if(stop_search) {
                return;
            }
        }
        /**
         * HP 빨강 확인 : 154,23,19
         * 위치 : 162, 253
         * 액션 : 2번째 클릭
         */
        {
            int x = 162;
            int y = 253;
            pixelSearch(bitmap, x, y,"app_log_2");
            if(stop_search) {
                return;
            }
        }
//        /**
//         * MP 파랑 확인 : 16,73,115
//         * 위치 : 76, 266
//         * 액션 : 1번째 클릭
//         */
//        {
//            int x = 130;
//            int y = 266;
//            pixelSearch(bitmap, x, y,"app_log_3");
//        }
        tc.quit();
    }

    boolean stop_search = false;
    public void pixelSearch(Bitmap bitmap,int x, int y, String msg) {
        Log.d(TAG,"pixelSearch1 x:"+x+","+" y:"+y+","+" msg:"+msg);
        /**
         * 캐릭명(x, y)
         */
        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
        int A = Color.alpha(rgb); //alpha값 추출
        int R = Color.red(rgb); //red값 추출
        int G = Color.green(rgb); //green값 추출
        int B = Color.blue(rgb); //blue값 추출
        Log.d(TAG,"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B);
        stop_search = ((R>240&&R<255)&&(G>240&&G<255)&&(B>240&&B<255));//파티 탭(49,121,206)
        /**
         * 실행 여부 확인
         */
        if(!stop_search) {
            return;
        }
        stop_search = ((R>20&&R<90)&&(G>90&&G<150)&&(B>160&&B<240));//세이프티 존(49,121,206)
        /**
         * 안전확인
         */
        if(stop_search) {
            Log.d(TAG,"세이프티 존"+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B);
        } else {
            if(!"".equals(msg)) {
                boolean flag = (260>R&&R>130&&G<100&&B<100);//캐릭명 빨강(154,23,19)
                if(flag) {
                    Log.d(TAG,"빨강색"+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B+"msg :"+msg);
                } else {
                    Log.d(TAG,"빨강색 아님"+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B+"msg :"+msg);
                    tc.sendDing(msg);
                }
            }
        }
    }
}
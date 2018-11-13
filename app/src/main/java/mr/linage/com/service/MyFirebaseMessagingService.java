package mr.linage.com.service;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "MyFMService";

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
        ip = data.get("ip");
        Log.d(TAG,"ip:"+ip);
        if(!"".equals(ip)) {
            Log.d(TAG,"client:"+client);
            if (client == null) {
                SoketStart();
            } else {
                search();
            }
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
                        File files = new File(videoFile.toString());
                        Log.d(TAG,"search3:files.exists():"+files.exists());
                        if(files.exists()==true) {
                            Log.d(TAG,"search3:files.setDataSource");
                            retriever.setDataSource(videoFile.toString());
                            Log.d(TAG,"search3:files.getFrameAtTime");
                            bitmap = retriever.getFrameAtTime(0,MediaMetadataRetriever.OPTION_CLOSEST);
                        }
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
        SoketClose();
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
                    sendDing(msg);
                }
            }
        }
    }
    /**
     * 소켓 변수
     */
    private String ip = "";
    private Socket socket;
    private TCPClient client;
    private BufferedWriter networkWriter;
    public class TCPClient extends Thread {
        SocketAddress socketAddress;
        private final int connection_timeout = 2000;
        public TCPClient(String ip, int port) throws RuntimeException {
            Log.d(TAG,"TCPClient:"+ip);
            Log.d(TAG,"TCPClient:"+port);
            socketAddress = new InetSocketAddress(ip, port);
        }
        @Override
        public void run() {
            Log.d(TAG,"client run");
            try {
                socket = new Socket();
                socket.setSoTimeout(connection_timeout);
                socket.setSoLinger(true, 0);
                socket.connect(socketAddress, connection_timeout);
                networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Log.d(TAG,"setThread"+" "+"정상적으로 서버에 접속하였습니다.");
                search();
            } catch (Exception e) {
                Log.d(TAG,"setThread"+" "+"소켓을 생성하지 못했습니다.");
                quit();
            }
        }
        public void quit() {
            try {
                if (networkWriter != null) {
                    networkWriter.close();
                    networkWriter = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                    Log.d(TAG,"setThread"+" "+"접속을 중단합니다.");
                }
                if(client != null) {
                    client = null;
                }
            } catch (IOException e) {
                Log.d(TAG, "에러 발생", e);
            }
        }
    }
    public void sendDing(String msg) {
        Log.d(TAG,"sendDing1");
        try {
            if(networkWriter!=null) {
                Log.d(TAG,"sendDing2");
                networkWriter.write(msg);
                networkWriter.newLine();
                networkWriter.flush();
                Log.d(TAG,"sendDing3");
            }
        } catch (Exception e) {
            Log.d(TAG, "에러 발생", e);
            SoketClose();
        }
        stop_search = true;
    }
    private void SoketStart() {
        if(!"".equals(ip)) {
            try {
                Log.d(TAG,"client:"+client);
                if(client==null) {
                    client = new TCPClient(ip, 9999);
                    client.start();
                }
            } catch (RuntimeException e) {
                Log.d(TAG, "에러 발생", e);
                SoketClose();
            }
        }
    }
    private void SoketClose() {
        if (client != null) {
            client.quit();
            client = null;
        }
    }
}
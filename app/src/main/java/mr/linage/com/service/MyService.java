package mr.linage.com.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;

import mr.linage.com.R;
import mr.linage.com.soket.TCPClient;

/**
 * Created by YDH on 2018-11-17.
 * adb shell am startservice -n mr.linage.com/mr.linage.com.service.MyService --es 'socket_client_ip' '192.168.0.4'
 */

public class MyService extends Service {

    private final String TAG = MyService.class.toString();

    private String socket_client_ip = "";
    private int socket_server_port = 9999;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG,"onBind()");
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate()");
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG,"onStartCommand intent 1");
        try {
            socket_client_ip = intent.getStringExtra("socket_client_ip");
//            callServer("app_on_destory");
            search();
        } catch (Exception e) {
            e.printStackTrace();
            callServer("app_on_destory");
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()");
        super.onDestroy();
    }

    public void search() throws Exception {
        Log.d(TAG,"search1");
        Bitmap bitmap = null;
        try {
            Log.d(TAG,"search2");
            File file = new File(Environment.getExternalStorageDirectory() + "/screencap-sample.png");
            Log.d(TAG,"search3:file.toString:"+file.toString());
            bitmap = BitmapFactory.decodeFile(file.toString());

            //** 회전 갤럭시3
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Log.d(TAG,"search3:width:"+width);
            Log.d(TAG,"search3:height:"+height);
            if(width<height) {
                Matrix matrix = new Matrix();
                matrix.postRotate(-90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                Log.d(TAG,"search3:width:"+width);
                Log.d(TAG,"search3:height:"+height);
            }
            //** 회전 갤럭시3

            Log.d(TAG,"search4:bitmap:"+bitmap);
            bitmapRead(bitmap);
        } finally {
            if(bitmap!=null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
    }

    public void bitmapRead(Bitmap bitmap) throws Exception {
        Log.d(TAG,"bitmapRead");
        String msg = "app_on_destory";
        boolean party_tab = false;
        boolean safety_zone = false;
        boolean hp_red = false;
        /**
         * 테스트 컬러 확인용
         * 로직 실행 확인 :
         * 위치 : 952, 512
         * 액션 : 없음
         */
        {
            int x = 952;
            int y = 512;
//            pixelSearch(bitmap, x, y, "컬러테스트");
        }
        /**
         * 파티 탭
         * 로직 실행 확인 : 49,121,206
         * 위치 : 130, 175
         * 액션 : 없음
         */
        {
            int x = 130;
            int y = 175;
            party_tab = pixelSearch(bitmap, x, y, "party_tab");
        }
        Log.d(TAG,"bitmapRead party_tab:"+party_tab);
        if(party_tab) {
            /**
             * 세이프티 존 확인 : 49,121,206
             * 위치 : 1125, 320
             * 액션 : 없음
             */
            {
                int x = 1134;
                int y = 318;
                safety_zone = pixelSearch(bitmap, x, y, "safety_zone");
            }
            Log.d(TAG,"bitmapRead safety_zone:"+safety_zone);
            /**
             * 귀환 체크
             */
            if(!safety_zone) {
                /**
                 * 파티 확인용
                 * HP 빨강 확인 : 154,23,19
                 * 위치 : 75, 255
                 * 액션 : 없음
                 */
                {
                    int x = 75;
                    int y = 255;
                    hp_red = pixelSearch(bitmap, x, y, "hp_red");
                    Log.d(TAG,"bitmapRead 파티 확인용 75, 255 hp_red:"+hp_red);
                }
                if(hp_red) {
                    /**
                     * 귀환 확인 용
                     * HP 빨강 확인 : 154,23,19
                     * 위치 : 102, 253
                     * 액션 : L1 실행
                     */
                    {
                        int x = 102;
                        int y = 253;
                        hp_red = pixelSearch(bitmap, x, y, "hp_red");
                        Log.d(TAG,"bitmapRead 귀환 확인용 102, 253 hp_red:"+hp_red);
                        if(!hp_red) {
                            msg = "app_log_1";
                            setNoti(0,bitmap);
                        }
                    }
                }
            }
            /**
             * 귀환이 아닐경우
             * 힐 체크
             */
            if("app_on_destory".equals(msg)) {
                /**
                 * 힐 확인용
                 * HP 빨강 확인 : 154,23,19
                 * 위치 : 75, 255
                 * 액션 : 없음
                 */
                {
                    int x = 75;
                    int y = 255;
                    hp_red = pixelSearch(bitmap, x, y, "hp_red");
                    Log.d(TAG,"bitmapRead 파티 확인용 75, 255 hp_red:"+hp_red);
                }
                if(hp_red) {
                    /**
                     * 데스힐 확인용
                     * HP 빨강 확인 : 154,23,19
                     * 위치 : 75, 255
                     * 액션 : 없음
                     */
                    {
                        int x = 245;
                        int y = 275;
                        hp_red = pixelSearch(bitmap, x, y, "hp_death");
                        Log.d(TAG,"bitmapRead 파티 확인용 75, 255 hp_red:"+hp_red);
                        if(hp_red) {
//                            msg = "app_log_3";
                        }
                    }
                    if(!hp_red) {
                        /**
                         * 힐 확인용
                         * HP 빨강 확인 : 154,23,19
                         * 위치 : 162, 253
                         * 액션 : L2 실행
                         */
                        {
                            int x = 162;
                            int y = 253;
                            hp_red = pixelSearch(bitmap, x, y, "hp_red");
                            Log.d(TAG,"bitmapRead 힐 확인용 162, 253 hp_red:"+hp_red);
                            if(!hp_red) {
                                msg = "app_log_2";
                            }
                        }
                    }
                }
            }
        }
        callServer(msg);
        if(bitmap!=null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

//    public void bitmapRead(Bitmap bitmap) throws Exception {
//        Log.d(TAG,"bitmapRead1");
//        boolean callServerFlag = false;
//        String msg = "";
//        if(!stop_search) {
//            /**
//             * 로직 실행 확인 : 49,121,206
//             * 위치 : 1125, 320
//             * 액션 : 없음
//             */
//            {
//                int x = 130;
//                int y = 175;
//                msg = pixelSearch(bitmap, x, y, "party_tab");
//            }
//        }
//        if(!stop_search) {
//            /**
//             * 세이프티 존 확인 : 49,121,206
//             * 위치 : 1125, 320
//             * 액션 : 없음
//             */
//            {
//                int x = 1125;
//                int y = 320;
//                msg = pixelSearch(bitmap, x, y, "safety_zone");
//            }
//            if(!stop_search) {
//                /**
//                 * HP 빨강 확인 : 154,23,19
//                 * 위치 : 102, 253
//                 * 액션 : 4번째 클릭
//                 */
//                {
//                    int x = 102;
//                    int y = 253;
//                    msg = pixelSearch(bitmap, x, y, "app_log_1");
//                }
//            }
//        }
//        if(!stop_search) {
//            /**
//             * HP 빨강 확인 : 154,23,19
//             * 위치 : 75, 255
//             * 액션 : 없음
//             */
//            {
//                int x = 75;
//                int y = 255;
//                msg = pixelSearch(bitmap, x, y, "app_log_2");
//            }
//            if(!stop_search) {
//                /**
//                 * HP 빨강 확인 : 154,23,19
//                 * 위치 : 162, 253
//                 * 액션 : 2번째 클릭
//                 */
//                {
//                    int x = 162;
//                    int y = 253;
//                    msg = pixelSearch(bitmap, x, y, "app_log_2");
//                }
//            }
//        }
//        if(!stop_search) {
////        /**
////         * MP 파랑 확인 : 16,73,115
////         * 위치 : 76, 266
////         * 액션 : 1번째 클릭
////         */
////        {
////            int x = 130;
////            int y = 266;
////            msg = pixelSearch(bitmap, x, y,"app_log_3");
////        }
//        }
//        callServer(msg);
//        if(bitmap!=null) {
//            bitmap.recycle();
//            bitmap = null;
//        }
//    }

    public boolean pixelSearch(Bitmap bitmap,int x, int y, String msg) throws Exception {
        boolean retrunValue = false;
        Log.d(TAG,"pixelSearch x:"+x+","+" y:"+y+","+" msg:"+msg);
        /**
         * 캐릭명(x, y)
         */
        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
        int A = Color.alpha(rgb); //alpha값 추출
        int R = Color.red(rgb); //red값 추출
        int G = Color.green(rgb); //green값 추출
        int B = Color.blue(rgb); //blue값 추출
        Log.d(TAG,"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B+" msg:"+msg);
        if("party_tab".equals(msg)) {
            retrunValue = (R>=230&&R<=255)&&(G>=230&&G<=255)&&(B>=230&&B<=255);//파티 탭(49,121,206)
        }
        if("safety_zone".equals(msg)) {
            retrunValue = ((R>=20&&R<=90)&&(G>=90&&G<=150)&&(B>=160&&B<=255));//세이프티 존(49,121,206)
        }
        if("hp_red".equals(msg)) {
            retrunValue = (260>R&&R>130&&G<100&&B<100);//HP 빨강(154,23,19)
        }
        if("hp_death".equals(msg)) {
            retrunValue = (260>R&&R>180&&G<180&&B<180);//HP 빨강(216,99,85)
        }
        return retrunValue;
    }

    private void callServer(final String msg) {
        Log.d(TAG,"callServer msg:"+msg);
        try {
            TCPClient tc = new TCPClient(socket_client_ip, socket_server_port) {
                @Override
                public void run() {
                    super.run();
                    if(!RESULT_OK) {
                        quit();
                        stopSelf();
                    } else {
                        sendDing(msg);
                    }
                }
                @Override
                public void sendDing(String msg) {
                    super.sendDing(msg);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    quit();
                    stopSelf();
                }
            };
            tc.start();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"callServer call end ?????????");
            stopSelf();
        }
    }

    private void setNoti(int id, Bitmap bitmap) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.noti_test);
        RemoteViews contentBigView = new RemoteViews(getPackageName(), R.layout.noti_test_big);
        CharSequence tickerText = "Shortcuts";
        long when = System.currentTimeMillis();
        @SuppressWarnings("deprecation")
        Notification notification = new Notification(R.drawable.ic_launcher_background, tickerText, when);
//        notification.defaults |= Notification.DEFAULT_SOUND;
//        notification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
//        mBuilder.setContentTitle("제목");
//        mBuilder.setContentText("테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스");
//        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스"));
//        mBuilder.addAction(R.drawable.ic_launcher_background, "재실행", null);
//        mBuilder.addAction(R.drawable.ic_launcher_background, "취소", null);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setSmallIcon(R.drawable.ic_launcher_background);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
//            notificationChannel.setDescription("channel description");
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.GREEN);
//            notificationChannel.enableVibration(true);
//            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
//            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);
            mBuilder = new NotificationCompat.Builder(this, notificationChannel.getId());
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setSmallIcon(R.drawable.ic_launcher_background);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBuilder.setCustomContentView(contentView);
            if (contentBigView != null) {

                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                bigText.bigText("빅테스트 내용");
                bigText.setBigContentTitle("빅테스트 제목");
                bigText.setSummaryText(getResources().getString(R.string.app_name));
                mBuilder.setStyle(bigText);
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                mBuilder.setShowWhen(true);

                mBuilder.setCustomBigContentView(contentBigView);
            }
            notification = mBuilder.build();
        } else {
//            notification.contentView = contentView;
//            if (contentBigView != null) {
//                notification.bigContentView = contentBigView;
//            }
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle(mBuilder); //상단의 빌더를 인자로 받음..
            bigPictureStyle.bigPicture(bitmap) //상단의 비트맵을 넣어준다.
            .setBigContentTitle("타이틀" ) //열렸을때의 타이틀
            .setSummaryText("이미지"); //열렸을때의 Description
            notification = mBuilder.build();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        mNotificationManager.notify(id, notification);

//        NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//        mNotificationManager.notify(0, mBuilder.build());
    }

}

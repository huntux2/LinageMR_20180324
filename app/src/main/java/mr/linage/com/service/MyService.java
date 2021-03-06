package mr.linage.com.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import mr.linage.com.R;
import mr.linage.com.linagemr.ColorCheckActivity;
import mr.linage.com.soket.TCPClient;
import mr.linage.com.utils.StringUtil;

/**
 * Created by YDH on 2018-11-17.
 * adb shell am startservice -n mr.linage.com/mr.linage.com.service.MyService --es 'socket_server_ip' '192.168.0.4'
 */

public class MyService extends Service {

    private final String TAG = MyService.class.toString();

    int id = 111;
    private String socket_server_ip = "";
    private int socket_server_port = 0;
    private List<String> data_list = null;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            startForeground(id, getStartForegroundNoti());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG,"onStartCommand intent 1");
        try {
            socket_server_ip = intent.getStringExtra("socket_server_ip");
            socket_server_port = Integer.parseInt(StringUtil.nvl(intent.getStringExtra("socket_server_port"),"9999"));
            String s_data_list = intent.getStringExtra("data_list");
            if(s_data_list!=null) {
                data_list = Arrays.asList(s_data_list.split(", "));;
            }
            Log.d(TAG,"socket_server_ip:"+socket_server_ip);
            Log.d(TAG,"socket_server_port:"+socket_server_port);
            Log.d(TAG,"s_data_list:"+s_data_list);
            Log.d(TAG,"data_list:"+data_list);

            HashMap map = new HashMap();

            String type = intent.getStringExtra("type");
            Log.d(TAG,"type:"+type);
            map.put("type", type);

            int cc_x = Integer.parseInt(StringUtil.nvl(intent.getStringExtra("cc_x"),"0"));
            int cc_y = Integer.parseInt(StringUtil.nvl(intent.getStringExtra("cc_y"),"0"));
            Log.d(TAG,"cc_x:"+cc_x);
            Log.d(TAG,"cc_y:"+cc_y);
            map.put("cc_x", cc_x);
            map.put("cc_y", cc_y);

//            callServer("app_on_destory");
            search(map);
        } catch (Exception e) {
            e.printStackTrace();
            callServer("app_on_destory");
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG,"removeNotification");
//            stopForeground(true);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void search(HashMap map) throws Exception {
        Log.d(TAG,"search1");
        Bitmap bitmap = null;
        try {
            Log.d(TAG,"search2");
            File file = new File(Environment.getExternalStorageDirectory() + "/screencap-sample.png");
            Log.d(TAG,"search3:file.toString:"+file.toString());
            bitmap = BitmapFactory.decodeFile(file.toString());
            Log.d(TAG,"search4:bitmap:"+bitmap);
            if("cc".equals(map.get("type"))) {
                String msg = pixelSearch(bitmap, Integer.parseInt(map.get("cc_x").toString()), Integer.parseInt(map.get("cc_y").toString()));
                callServer(msg);
            } else {
                bitmapRead(bitmap);
            }
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
        {
            String CX = "";
            String CY = "";
            String R = "";
            String G = "";
            String B = "";
            String C = "";
            String X = "";
            String Y = "";
            String RGB_D = "";
            for (int i=0;i<data_list.size();i++) {
                String data = data_list.get(i);
                Log.d(TAG,"data_list data:"+data);
                Log.d(TAG,"data_list start i:"+i);
                if(data.split("[|]").length==9) {
                    CX = data.split("[|]")[0].replace("\\","");
                    CY = data.split("[|]")[1].replace("\\","");
                    R = data.split("[|]")[2].replace("\\","");
                    G = data.split("[|]")[3].replace("\\","");
                    B = data.split("[|]")[4].replace("\\","");
                    C = data.split("[|]")[5].replace("\\","");
                    X = data.split("[|]")[6];
                    Y = data.split("[|]")[7];
                    RGB_D = data.split("[|]")[8].trim();
                    Log.d(TAG,"data_list CX:"+CX);
                    Log.d(TAG,"data_list CY:"+CY);
                    Log.d(TAG,"data_list R:"+R);
                    Log.d(TAG,"data_list G:"+G);
                    Log.d(TAG,"data_list B:"+B);
                    Log.d(TAG,"data_list C:"+C);
                    Log.d(TAG,"data_list RGB_D:"+RGB_D);
                    Log.d(TAG,"data_list X:"+X);
                    Log.d(TAG,"data_list Y:"+Y);
                    int cnt = 0;
                    boolean execute = true;
                    for (int j=0;j<CX.split(",").length;j++) {
                        int x = Integer.parseInt(CX.split(",")[cnt].trim());
                        int y = Integer.parseInt(CY.split(",")[cnt].trim());
                        int r = Integer.parseInt(R.split(",")[cnt].trim());
                        int g = Integer.parseInt(G.split(",")[cnt].trim());
                        int b = Integer.parseInt(B.split(",")[cnt].trim());
                        String c = C.split(",")[cnt].trim();
                        int rgb_d = Integer.parseInt(RGB_D.trim());
                        if(!pixelSearch(bitmap, x, y, r, g, b, c, rgb_d)) {
                            execute = false;
                            break;
                        }
                        cnt++;
                    }
                    if(execute&&i==0) {
                        setNoti(0,bitmap);
                    }
                    if(execute){
                        msg = X.trim()+"|"+Y.trim();
                        break;
                    }
                }
                if(!"app_on_destory".equals(msg)) {
                    break;
                }
                Log.d(TAG,"data_list end");
                Log.d(TAG,"data_list >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }
        }
        callServer(msg);
        if(bitmap!=null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public void bitmapRead(Bitmap bitmap, HashMap<String, Object> map) throws Exception {
        Log.d(TAG,"bitmapRead");
        String msg = "app_on_destory";
        boolean action = false;
        boolean safety_zone = false;
        boolean hp_red = false;
        /**
         * 동작조건 필수!! (파티 탭)
         * 로직 실행 확인 : 49,121,206
         * 위치 : 130, 175
         * 액션 : 없음
         */
        {
            int x = Integer.parseInt(map.get("action_x").toString());
            int y = Integer.parseInt(map.get("action_y").toString());
            int r = Integer.parseInt(map.get("action_r").toString());
            int g = Integer.parseInt(map.get("action_g").toString());
            int b = Integer.parseInt(map.get("action_b").toString());
            action = pixelSearch(bitmap, x, y, r, g, b);
        }
        Log.d(TAG,"bitmapRead action:"+action);
        if(action) {
            /**
             * 세이프티 존 확인 : 49,121,206
             * 위치 : 1134, 318
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
//        Log.d(TAG,"bitmapRead");
//        String msg = "app_on_destory";
//        boolean party_tab = false;
//        boolean safety_zone = false;
//        boolean hp_red = false;
//        /**
//         * 테스트 컬러 확인용
//         * 로직 실행 확인 :
//         * 위치 : 952, 512
//         * 액션 : 없음
//         */
//        {
//            int x = 952;
//            int y = 512;
////            pixelSearch(bitmap, x, y, "컬러테스트");
//        }
//        /**
//         * 파티 탭
//         * 로직 실행 확인 : 49,121,206
//         * 위치 : 130, 175
//         * 액션 : 없음
//         */
//        {
//            int x = 130;
//            int y = 175;
//            int r = 49;
//            int g = 121;
//            int b = 206;
//            party_tab = pixelSearch(bitmap, x, y, r, g, b,"party_tab");
//        }
//        Log.d(TAG,"bitmapRead party_tab:"+party_tab);
//        if(party_tab) {
//            /**
//             * 세이프티 존 확인 : 49,121,206
//             * 위치 : 1134, 318
//             * 액션 : 없음
//             */
//            {
//                int x = 1134;
//                int y = 318;
//                safety_zone = pixelSearch(bitmap, x, y, "safety_zone");
//            }
//            Log.d(TAG,"bitmapRead safety_zone:"+safety_zone);
//            /**
//             * 귀환 체크
//             */
//            if(!safety_zone) {
//                /**
//                 * 파티 확인용
//                 * HP 빨강 확인 : 154,23,19
//                 * 위치 : 75, 255
//                 * 액션 : 없음
//                 */
//                {
//                    int x = 75;
//                    int y = 255;
//                    hp_red = pixelSearch(bitmap, x, y, "hp_red");
//                    Log.d(TAG,"bitmapRead 파티 확인용 75, 255 hp_red:"+hp_red);
//                }
//                if(hp_red) {
//                    /**
//                     * 귀환 확인 용
//                     * HP 빨강 확인 : 154,23,19
//                     * 위치 : 102, 253
//                     * 액션 : L1 실행
//                     */
//                    {
//                        int x = 102;
//                        int y = 253;
//                        hp_red = pixelSearch(bitmap, x, y, "hp_red");
//                        Log.d(TAG,"bitmapRead 귀환 확인용 102, 253 hp_red:"+hp_red);
//                        if(!hp_red) {
//                            msg = "app_log_1";
//                            setNoti(0,bitmap);
//                        }
//                    }
//                }
//            }
//            /**
//             * 귀환이 아닐경우
//             * 힐 체크
//             */
//            if("app_on_destory".equals(msg)) {
//                /**
//                 * 힐 확인용
//                 * HP 빨강 확인 : 154,23,19
//                 * 위치 : 75, 255
//                 * 액션 : 없음
//                 */
//                {
//                    int x = 75;
//                    int y = 255;
//                    hp_red = pixelSearch(bitmap, x, y, "hp_red");
//                    Log.d(TAG,"bitmapRead 파티 확인용 75, 255 hp_red:"+hp_red);
//                }
//                if(hp_red) {
//                    /**
//                     * 데스힐 확인용
//                     * HP 빨강 확인 : 154,23,19
//                     * 위치 : 75, 255
//                     * 액션 : 없음
//                     */
//                    {
//                        int x = 245;
//                        int y = 275;
//                        hp_red = pixelSearch(bitmap, x, y, "hp_death");
//                        Log.d(TAG,"bitmapRead 파티 확인용 75, 255 hp_red:"+hp_red);
//                        if(hp_red) {
////                            msg = "app_log_3";
//                        }
//                    }
//                    if(!hp_red) {
//                        /**
//                         * 힐 확인용
//                         * HP 빨강 확인 : 154,23,19
//                         * 위치 : 162, 253
//                         * 액션 : L2 실행
//                         */
//                        {
//                            int x = 162;
//                            int y = 253;
//                            hp_red = pixelSearch(bitmap, x, y, "hp_red");
//                            Log.d(TAG,"bitmapRead 힐 확인용 162, 253 hp_red:"+hp_red);
//                            if(!hp_red) {
//                                msg = "app_log_2";
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        callServer(msg);
//        if(bitmap!=null) {
//            bitmap.recycle();
//            bitmap = null;
//        }
//    }

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

    public String pixelSearch(Bitmap bitmap,int x, int y) throws Exception {
        String retrunValue = "";
        Log.d(TAG,"pixelSearch x:"+x+","+" y:"+y);
        /**
         * 캐릭명(x, y)
         */
        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
        int A = Color.alpha(rgb); //alpha값 추출
        int R = Color.red(rgb); //red값 추출
        int G = Color.green(rgb); //green값 추출
        int B = Color.blue(rgb); //blue값 추출
        Log.d(TAG,"A:"+A+" "+"R:"+R+" "+"G:"+G+" "+"B:"+B);
        retrunValue = "A:"+A+",R:"+R+",G:"+G+",B:"+B;
        return retrunValue;
    }

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
            retrunValue = (260>R&&R>180&&G<180&&B<180);//데스힐 스킬(216,99,85)
        }
        return retrunValue;
    }

    public boolean pixelSearch(Bitmap bitmap,int x, int y,int r, int g,int b,String c,int rgb_d) throws Exception {
        boolean retrunValue = false;
        Log.d(TAG,"pixelSearch x:"+x+","+" y:"+y);
        /**
         * 캐릭명(x, y)
         */
        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
        int A = Color.alpha(rgb); //alpha값 추출
        int R = Color.red(rgb); //red값 추출
        int G = Color.green(rgb); //green값 추출
        int B = Color.blue(rgb); //blue값 추출
        int range = rgb_d;
        Log.d(TAG,"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B);
        retrunValue = (R>=(r-range)&&R<=(r+range))&&(G>=(g-range)&&G<=(g+range))&&(B>=(b-range)&&B<=(b+range));
        if("1".equals(c)) {
            retrunValue = !retrunValue;
        }
        Log.d(TAG,"retrunValue :"+retrunValue);
        return retrunValue;
    }

    public boolean pixelSearch(Bitmap bitmap,int x, int y,int r, int g,int b) throws Exception {
        boolean retrunValue = false;
        Log.d(TAG,"pixelSearch x:"+x+","+" y:"+y);
        /**
         * 캐릭명(x, y)
         */
        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
        int A = Color.alpha(rgb); //alpha값 추출
        int R = Color.red(rgb); //red값 추출
        int G = Color.green(rgb); //green값 추출
        int B = Color.blue(rgb); //blue값 추출
        int range = 30;
        Log.d(TAG,"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B);
        retrunValue = (R>=(r-range)&&R<=(r+range))&&(G>=(g-range)&&G<=(g+range))&&(B>=(b-range)&&B<=(b+range));
        Log.d(TAG,"retrunValue :"+retrunValue);
        return retrunValue;
    }

    private void callServer(final String msg) {
        Log.d(TAG,"callServer msg:"+msg);
        try {
            TCPClient tc = new TCPClient(socket_server_ip, socket_server_port) {
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
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setSmallIcon(android.R.drawable.btn_star);
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle(mBuilder);
        bigPictureStyle.bigPicture(bitmap);
        //7버전
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBuilder.setStyle(bigPictureStyle);
        }
        //8버전
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel";
            String channelName = "Channel Name";
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);
            mBuilder = new NotificationCompat.Builder(this, notificationChannel.getId());
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setSmallIcon(android.R.drawable.btn_star);
            mBuilder.setStyle(bigPictureStyle);
        }
        Notification notification = mBuilder.build();
        mNotificationManager.notify(id, notification);
    }

    public Notification getStartForegroundNoti() {
        Intent notificationIntent = new Intent(this, ColorCheckActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.noti_test);
        RemoteViews contentBigView = new RemoteViews(getPackageName(), R.layout.noti_test_big);
        CharSequence tickerText = "Shortcuts";
        long when = System.currentTimeMillis();
        @SuppressWarnings("deprecation")
        Notification notification = new Notification(R.drawable.ic_launcher_background, tickerText, when);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setSmallIcon(R.drawable.ic_launcher_background);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
            mBuilder = new NotificationCompat.Builder(this, notificationChannel.getId());
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setSmallIcon(R.drawable.ic_launcher_background);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBuilder.setCustomContentView(contentView);
//            mBuilder.setContentIntent(pendingIntent);
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
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle(mBuilder); //상단의 빌더를 인자로 받음..
            bigPictureStyle.setBigContentTitle("타이틀" ).setSummaryText("이미지");
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
        }
        return notification;
    }

}

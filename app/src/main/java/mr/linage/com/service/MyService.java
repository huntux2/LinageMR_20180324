package mr.linage.com.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

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
        Log.d(TAG,"bitmapRead1");
        String msg = "app_on_destory";
        boolean party_tab = false;
        boolean safety_zone = false;
        boolean hp_red = false;
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
        if(party_tab) {
            /**
             * 세이프티 존 확인 : 49,121,206
             * 위치 : 1125, 320
             * 액션 : 없음
             */
            {
                int x = 1125;
                int y = 320;
                safety_zone = pixelSearch(bitmap, x, y, "safety_zone");
            }
            if(!safety_zone) {
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
                        if(!hp_red) {
                            msg = "app_log_1";
                        }
                    }
                }
            }
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
                }
                if(hp_red) {
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
                        if(!hp_red) {
                            msg = "app_log_2";
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
        if("party_tab".equals(msg)) {
            retrunValue = (R>=230&&R<=255)&&(G>=230&&G<=255)&&(B>=230&&B<=255);//파티 탭(49,121,206)
        }
        if("safety_zone".equals(msg)) {
            retrunValue = ((R>=20&&R<=90)&&(G>=90&&G<=150)&&(B>=160&&B<=255));//세이프티 존(49,121,206)
        }
        if("hp_red".equals(msg)) {
            retrunValue = (260>R&&R>130&&G<100&&B<100);//HP 빨강(154,23,19)
        }
        return retrunValue;
    }

    private void callServer(final String msg) {
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

}

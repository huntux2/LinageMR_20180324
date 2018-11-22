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
        boolean callServerFlag = false;
        String msg = "";
        if(!stop_search) {
            /**
             * 로직 실행 확인 : 49,121,206
             * 위치 : 1125, 320
             * 액션 : 없음
             */
            {
                int x = 130;
                int y = 175;
                msg = pixelSearch(bitmap, x, y, "party_tab");
            }
        }
        if(!stop_search) {
            /**
             * 세이프티 존 확인 : 49,121,206
             * 위치 : 1125, 320
             * 액션 : 없음
             */
            {
                int x = 1125;
                int y = 320;
                msg = pixelSearch(bitmap, x, y, "safety_zone");
            }
        }
        if(!stop_search) {
            /**
             * HP 빨강 확인 : 154,23,19
             * 위치 : 102, 253
             * 액션 : 4번째 클릭
             */
            {
                int x = 102;
                int y = 253;
                msg = pixelSearch(bitmap, x, y, "app_log_1");
            }
        }
        if(!stop_search) {
            /**
             * HP 빨강 확인 : 154,23,19
             * 위치 : 162, 253
             * 액션 : 2번째 클릭
             */
            {
                int x = 162;
                int y = 253;
                msg = pixelSearch(bitmap, x, y, "app_log_2");
            }
        }
        if(!stop_search) {
//        /**
//         * MP 파랑 확인 : 16,73,115
//         * 위치 : 76, 266
//         * 액션 : 1번째 클릭
//         */
//        {
//            int x = 130;
//            int y = 266;
//            msg = pixelSearch(bitmap, x, y,"app_log_3");
//        }
        }
        callServer(msg);
        if(bitmap!=null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    boolean stop_search = false;
    public String pixelSearch(Bitmap bitmap,int x, int y, String msg) throws Exception {
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
            stop_search = !((R>=230&&R<=255)&&(G>=230&&G<=255)&&(B>=230&&B<=255));//파티 탭(49,121,206)
            /**
             * 실행 여부 확인
             */
            if(stop_search) {
                Log.d(TAG,"실행 안함"+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B);
                msg = "app_on_destory";
            }
        }
        if("safety_zone".equals(msg)) {
            stop_search = ((R>=20&&R<=90)&&(G>=90&&G<=150)&&(B>=160&&B<=240));//세이프티 존(49,121,206)
            /**
             * 안전확인
             */
            if(stop_search) {
                Log.d(TAG,"세이프티 존"+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B);
                msg = "app_on_destory";
            }
        }
        if("app_log_1".equals(msg)&&!stop_search) {
            boolean flag = (260>R&&R>130&&G<100&&B<100);//캐릭명 빨강(154,23,19)
            if(flag) {
                Log.d(TAG,"빨강색"+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B+"msg :"+msg);
                msg = "app_on_destory";
            } else {
                Log.d(TAG,"빨강색 아님"+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B+"msg :"+msg);
                stop_search = true;
            }
        }
        if("app_log_2".equals(msg)&&!stop_search) {
            boolean flag = (260>R&&R>130&&G<100&&B<100);//캐릭명 빨강(154,23,19)
            if(flag) {
                Log.d(TAG,"빨강색"+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B+"msg :"+msg);
                msg = "app_on_destory";
            } else {
                Log.d(TAG,"빨강색 아님"+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B+"msg :"+msg);
                stop_search = true;
            }
        }
        return msg;
    }

    private void callServer(final String msg) {
        try {
            TCPClient tc = new TCPClient(socket_client_ip, socket_server_port) {
                @Override
                public void run() {
                    super.run();
                    stop_search = true;
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
                        Thread.sleep(2000);
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
        }
    }

}

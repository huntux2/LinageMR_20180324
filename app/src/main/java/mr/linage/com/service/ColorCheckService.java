package mr.linage.com.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

import mr.linage.com.soket.TCPClient;

/**
 * Created by YDH on 2018-11-17.
 * adb -s 172.30.1.9 shell am startservice -n mr.linage.com/mr.linage.com.service.ColorCheckService --es 'server_ip' '192.168.0.4'
 */

public class ColorCheckService extends Service {

    private final String TAG = ColorCheckService.class.toString();

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

        String server_ip = (intent.getStringExtra("server_ip")==null)?"":intent.getStringExtra("server_ip");
        String client_ip = (intent.getStringExtra("client_ip")==null)?"":intent.getStringExtra("client_ip");

        String color_range = (intent.getStringExtra("color_range")==null)?"":intent.getStringExtra("color_range");

        String position_x = (intent.getStringExtra("position_x")==null)?"":intent.getStringExtra("position_x");
        String position_y = (intent.getStringExtra("position_y")==null)?"":intent.getStringExtra("position_y");

        String color_r = (intent.getStringExtra("color_r")==null)?"":intent.getStringExtra("color_r");
        String color_g = (intent.getStringExtra("color_g")==null)?"":intent.getStringExtra("color_g");
        String color_b = (intent.getStringExtra("color_b")==null)?"":intent.getStringExtra("color_b");

        Log.d(TAG,"server_ip:"+server_ip);
        Log.d(TAG,"client_ip:"+client_ip);
        Log.d(TAG,"position_x:"+position_x);
        Log.d(TAG,"position_y:"+position_y);
        Log.d(TAG,"color_r:"+color_r);
        Log.d(TAG,"color_g:"+color_g);
        Log.d(TAG,"color_b:"+color_b);

        if(!"".equals(server_ip)) {
            if(splitLength(position_x,"")==splitLength(position_y,"")
            &&splitLength(position_x,"")==splitLength(color_r,"")
            &&splitLength(position_x,"")==splitLength(color_g,"")
            &&splitLength(position_x,"")==splitLength(color_b,"")) {
                String msg = "";
                for (int i = 0; i < splitLength(position_x, ""); i++) {
                    try {
                        if(!"".equals(msg)) {
                            msg += "|";
                        }
                        msg += imageSearch(server_ip, client_ip, color_range, splitValue(position_x,"",i), splitValue(position_y,"",i), splitValue(color_r,"",i), splitValue(color_g,"",i), splitValue(color_b,"",i))+"";
                    } catch (Exception e) {
                        callServer(server_ip,"app_on_destory");
                    }
                }
                Log.d(TAG,"msg:"+msg);
//                        callServer(server_ip, msg);
            }
        }
        return START_NOT_STICKY;
    }

    public int splitLength(String msg, String split) {
        String split_string = "[=]";
        if("".equals(split)) {
            split = split_string;
        }
        return msg.split(split).length;
    }

    public String splitValue(String msg, String split,int num) {
        String split_string = "[=]";
        if("".equals(split)) {
            split = split_string;
        }
        if((msg.split(split).length-1)>=num) {
            return msg.split(split)[num];
        }
        return "";
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()");
        super.onDestroy();
    }

    public String imageSearch(String server_ip, String client_ip, String color_range, String position_x, String position_y, String color_r, String color_g, String color_b) throws Exception {
        String returnValue = "";
        Bitmap bitmap = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/screencap-sample.png");
            bitmap = BitmapFactory.decodeFile(file.toString());
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if(width<height) {
                Matrix matrix = new Matrix();
                matrix.postRotate(-90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            }
            returnValue = pixelSearch(bitmap, Integer.parseInt(color_range.equals("")?"0":color_range), Integer.parseInt(position_x.equals("")?"0":position_x), Integer.parseInt(position_y.equals("")?"0":position_y), Integer.parseInt(color_r.equals("")?"0":color_r), Integer.parseInt(color_g.equals("")?"0":color_g), Integer.parseInt(color_b.equals("")?"0":color_b));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public String pixelSearch(Bitmap bitmap, int range, int x, int y, int r, int g, int b) throws Exception {
        String retrunValue = "";
        /**
         * 캐릭명(x, y)
         */
        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
//        int A = Color.alpha(rgb); //alpha값 추출
        int R = Color.red(rgb); //red값 추출
        int G = Color.green(rgb); //green값 추출
        int B = Color.blue(rgb); //blue값 추출

        int r_range_a = ((r-range)<0?0:(r-range));
        int r_range_b = ((r+range)<0?0:(r+range));
        int g_range_a = ((g-range)<0?0:(g-range));
        int g_range_b = ((g+range)<0?0:(g+range));
        int b_range_a = ((b-range)<0?0:(b-range));
        int b_range_b = ((b+range)<0?0:(b+range));

        Log.d(TAG,"R:"+R+" "+"G:"+G+" "+"B:"+B);

        Log.d(TAG,"r_range_a:"+r_range_a);
        Log.d(TAG,"r_range_b:"+r_range_b);
        Log.d(TAG,"g_range_a:"+g_range_a);
        Log.d(TAG,"g_range_b:"+g_range_b);
        Log.d(TAG,"b_range_a:"+b_range_a);
        Log.d(TAG,"b_range_b:"+b_range_b);

        retrunValue = ((R>=r_range_a&&R<=r_range_b)&&(G>=g_range_a&&G<=g_range_b)&&(B>=b_range_a&&B<=b_range_b))+"";

        if("false".equals(retrunValue)) {
            retrunValue = R+":"+G+":"+B;
        }

        return retrunValue;
    }

    private void callServer(String server_ip, final String msg) {
        Log.d(TAG,"callServer msg:"+msg);
        try {
            TCPClient tc = new TCPClient(server_ip, socket_server_port) {
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
            stopSelf();
        }
    }

}

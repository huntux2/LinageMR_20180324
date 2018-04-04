package mr.linage.com.linagemr;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import mr.linage.com.utils.AndroidUtils;
import mr.linage.com.vo.ArgbVo;

public class Main4Activity extends Activity implements View.OnClickListener {

    private String TAG = getClass().getSimpleName().trim();

    private File videoFile;
    private Uri videoFileUri;

    private MediaMetadataRetriever retriever;
    private ArrayList<Bitmap> bitmapArrayList;
    private MediaPlayer mediaPlayer;
    private Bitmap bitmap;
    private Thread thread;

    int cnt = 0;

    TimerTask tt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
//        findViewById(R.id.button_b).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                test();
//            }
//        });
        tt = new TimerTask() {
            @Override
            public void run() {
                try {
                    test();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Timer t = new Timer();
        t.schedule(tt,0, 500);
    }

    public void test() {
        try {
            //adb -s ce12160cfb8a5f3504 shell screenrecord --time-limit 1 --verbose /sdcard/screenrecord-sample0.mp4
            videoFile = new File(Environment.getExternalStorageDirectory() + "/screenrecord-sample.mp4");
            videoFileUri = Uri.parse(videoFile.toString());
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoFile.toString());
            mediaPlayer = MediaPlayer.create(getBaseContext(), videoFileUri);
            bitmap = retriever.getFrameAtTime(-1);
            if(bitmap!=null)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int x = AndroidUtils.DPFromPixel(54,getApplicationContext());
                    int y = AndroidUtils.DPFromPixel(180,getApplicationContext());
                    int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
                    int A_ = Color.alpha(rgb); //alpha값 추출
                    int R_ = Color.red(rgb); //red값 추출
                    int G_ = Color.green(rgb); //green값 추출
                    int B_ = Color.blue(rgb); //blue값 추출
                    Log.d(TAG, "pixelSearch"+" "+" "+"x :"+x+" "+"y :"+y+" "+" "+"A :"+A_+" "+"R :"+R_+" "+"G :"+G_+" "+"B :"+B_);
                    findViewById(R.id.button_b).setBackgroundColor(Color.argb(A_, R_, G_, B_));
                    ((ImageView)findViewById(R.id.image_iv)).setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(retriever!=null) {
                retriever.release();
            }
        }
        cnt++;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"onClick");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
    }

}

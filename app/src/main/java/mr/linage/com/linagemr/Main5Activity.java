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
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.util.Timer;
import java.util.TimerTask;

import mr.linage.com.utils.AndroidUtils;
import mr.linage.com.vo.ArgbVo;

public class Main5Activity extends Activity implements View.OnClickListener {

    private String TAG = getClass().getSimpleName().trim();

    /**
     * 서비스 변수
     */
    private boolean mIsBound;
    private Messenger mServiceMessenger = null;
    private int p_x = 57;
    private int p_y = 175;
    private int rank = 1;
    private boolean sending = true;
    private int mode_num = 1;

    /**
     * 캡쳐 변수
     */
    private File videoFile;
    private MediaMetadataRetriever retriever;
    private Bitmap bitmap;
    private TimerTask tt;

    /**
     * 소켓 변수
     */
    private Socket socket;
    private TCPClient client;
    private BufferedWriter networkWriter;

    /**
     * 기타
     */
    private long id = 0;
    private int flag_1 = 0;
    private int flag_2 = 0;
    private int flag_3 = 0;
    private int flag_4 = 0;
    private boolean flag_stop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }
        }
        findViewById(R.id.start).setOnClickListener(this);          //시작버튼
        findViewById(R.id.end).setOnClickListener(this);            //중시버튼
        findViewById(R.id.soket).setOnClickListener(this);          //연결
        findViewById(R.id.soket_end).setOnClickListener(this);      //종료
        findViewById(R.id.soket_send).setOnClickListener(this);     //보내기
        SoketStart();
        tt = new TimerTask() {
            @Override
            public void run() {
                try {
                    search();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Timer t = new Timer();
        t.schedule(tt,0, 500);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"onClick");
        int view = v.getId();
        if(view == R.id.start) {
            bindService(new Intent(this, MainService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        } else if(view == R.id.end) {
            if(mIsBound) {
                unbindService(mConnection);
            }
        } else if(view == R.id.soket) {
            SoketClose();
            SoketStart();
        } else if(view == R.id.soket_end) {
            SoketClose();
        } else if(view == R.id.soket_send) {
            sendDing("Hello World!");
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("test","onServiceConnected");
            mServiceMessenger = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null, MainService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceMessenger = null;
            mIsBound = false;
        }
    };

    /** Service 로 부터 message를 받음 */
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i("test","act : what "+msg.what);
            switch (msg.what) {
                case MainService.MSG_SEND_TO_ACTIVITY:
                    int x = msg.getData().getInt("x");
                    int y = msg.getData().getInt("y");
                    int _rank = msg.getData().getInt("rank");
                    boolean _sending = (boolean)msg.getData().getSerializable("sending");
                    int _mode_num = msg.getData().getInt("mode_num");
                    Log.i("test","act : x :"+x);
                    Log.i("test","act : y :"+y);
                    Log.i("test","act : rank :"+_rank);
                    Log.i("test","act : sending :"+_sending);
                    Log.i("test","act : mode_num :"+_mode_num);
                    p_x = x;
                    p_y = y;
                    rank = _rank;
                    sending = _sending;
                    mode_num = _mode_num;
                    break;
            }
            return false;
        }
    }));

    /** Service 로 메시지를 보냄 */
    private void sendMessageToService(ArgbVo argbVo, int number) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, MainService.MSG_SEND_TO_SERVICE, argbVo);
                    msg.arg1 = number;
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void bitmap(Bitmap bitmap) {
        Log.d(TAG,"onImageAvailable bitmap 생성");
        {
            if(mode_num>=1) {
                int x = AndroidUtils.DPFromPixel(57,getApplicationContext());
                int y = AndroidUtils.DPFromPixel(175,getApplicationContext());
                if(rank==1) {
                    x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                    y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                }
                pixelSearch(bitmap, x, y,"app_log_1");
            }
        }

        {
            if(mode_num>=2) {
                int x = AndroidUtils.DPFromPixel(57,getApplicationContext());
                int y = AndroidUtils.DPFromPixel(222,getApplicationContext());
                if(rank==2) {
                    x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                    y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                }
                pixelSearch(bitmap, x, y,"app_log_2");
            }
        }

        {
            if(mode_num>=3) {
                int x = AndroidUtils.DPFromPixel(57,getApplicationContext());
                int y = AndroidUtils.DPFromPixel(270,getApplicationContext());
                if(rank==3) {
                    x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                    y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                }
                pixelSearch(bitmap, x, y,"app_log_3");
            }
        }

        {
            if(mode_num>=4) {
                //54, 314 : 114, 329
                int x = AndroidUtils.DPFromPixel(117,getApplicationContext());
                int y = AndroidUtils.DPFromPixel(332,getApplicationContext());
                if(rank==4) {
                    x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                    y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                }
                pixelSearch(bitmap, x, y,"app_log_4");
            }
        }
        if(bitmap!=null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public void pixelSearch(Bitmap bitmap,int x, int y, String file_name) {
        /**
         * 캐릭명(x, y)
         */
        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
        int A = Color.alpha(rgb); //alpha값 추출
        int R = Color.red(rgb); //red값 추출
        int G = Color.green(rgb); //green값 추출
        int B = Color.blue(rgb); //blue값 추출
        if(rank==Integer.parseInt(file_name.split("_")[2])) {
            ArgbVo argbVo = new ArgbVo();
            argbVo.setA(A);
            argbVo.setR(R);
            argbVo.setG(G);
            argbVo.setB(B);
            sendMessageToService(argbVo, rank);
        }
        boolean flag = false;
        if("app_log_4".equals(file_name)) {
            flag = !(260>R&&R>150&&G<100&&B<100);//캐릭 에너지 빨강 아닐때(154,23,19)
        } else {
            flag = (260>R&&R>150&&G<100&&B<100);//캐릭명 빨강(154,23,19)
        }
        /**
         * 캐릭명 빨강(154,23,19)
         */
        if(flag) {
            Log.d(TAG,"pixelSearch"+" "+"캐릭명"+" "+"빨강"+" "+file_name+" "+"x :"+x+" "+"y :"+y+" "+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B);
            if("app_log_2".equals(file_name)) {
                if(flag_2==0) {
                    Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환~");
                    setLog(file_name);
                } else {
                    Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환초기화대기중~");
                }
                flag_2++;
            } else if("app_log_3".equals(file_name)) {
                if(flag_3==0) {
                    Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환~");
                    setLog(file_name);
                } else {
                    Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환초기화대기중~");
                }
                flag_3++;
            } else {
                /**
                 * 마을세이프티(780,238)
                 */
                int rgb_ = bitmap.getPixel(AndroidUtils.DPFromPixel(780,getApplicationContext()), AndroidUtils.DPFromPixel(238,getApplicationContext())); //원하는 좌표값 입력
                int A_ = Color.alpha(rgb_); //alpha값 추출
                int R_ = Color.red(rgb_); //red값 추출
                int G_ = Color.green(rgb_); //green값 추출
                int B_ = Color.blue(rgb_); //blue값 추출
                /**
                 * 마을세이프티 파랑(30,138,230)
                 */
                if((R_<35&&R_>20)&&(G_<145&&G_>130)&&(B_<240&&B_>225)) {
                    Log.d(TAG, "pixelSearch"+" "+"마을세이프티"+" "+"맞음"+" "+file_name+"x :"+x+" "+"y :"+y+" "+" "+"A :"+A+" "+"R :"+R_+" "+"G :"+G_+" "+"B :"+B_+"===>>> 귀환초기화대기중~");
                } else {
                    Log.d(TAG, "pixelSearch"+" "+"마을세이프티"+" "+"아님"+" "+file_name+"x :"+x+" "+"y :"+y+" "+" "+"A :"+A+" "+"R :"+R_+" "+"G :"+G_+" "+"B :"+B_+"===>>> 귀환~");
                    setLog(file_name);
                }
            }
        } else {
            Log.d(TAG, "pixelSearch"+" "+"캐릭명"+" "+"흰색"+" "+file_name+"x :"+x+" "+"y :"+y+" "+" "+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B+"===>>> 귀환준비~");
            if("app_log_1".equals(file_name)) {
                flag_1 = 0;
            } else if("app_log_2".equals(file_name)) {
                flag_2 = 0;
            } else if("app_log_3".equals(file_name)) {
                flag_3 = 0;
            } else if("app_log_4".equals(file_name)) {
                flag_4 = 0;
            }
        }
    }

    public void setLog(final String file_name) {
        id++;
        Log.d("LinageMR", file_name+" "+"adb shell input tap 750 650::id::" + id+" app_log");
//        AndroidUtils.writeFile("LinageMR adb shell input tap 750 650::id::" + id, file_name);
        sendDing(file_name);
    }

    private void SoketStart() {
        final String ip = ((EditText)findViewById(R.id.soket_ip)).getText().toString();
        if(!"".equals(ip)) {
            try {
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

    public class TCPClient extends Thread {
        SocketAddress socketAddress;
        private final int connection_timeout = 2000;
        public TCPClient(String ip, int port) throws RuntimeException {
            socketAddress = new InetSocketAddress(ip, port);
        }
        @Override
        public void run() {
            try {
                socket = new Socket();
                socket.setSoTimeout(connection_timeout);
                socket.setSoLinger(true, 0);
                socket.connect(socketAddress, connection_timeout);
                networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String ip = ((EditText)findViewById(R.id.soket_ip)).getText().toString();
                Log.d(TAG,"setThread"+" "+"정상적으로 서버에 접속하였습니다.");
                setUiText("정상적으로 서버에 접속하였습니다.");
            } catch (Exception e) {
                Log.d(TAG,"setThread"+" "+"소켓을 생성하지 못했습니다.");
                setUiText("소켓을 생성하지 못했습니다.");
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
                    setUiText("접속을 중단합니다.");
                }
            } catch (IOException e) {
                Log.d(TAG, "에러 발생", e);
                setUiText("에러 발생");
            }
        }
    }

    public void sendDing(String msg) {
        if(sending) {
            try {
                if(networkWriter!=null) {
                    networkWriter.write(msg);
                    networkWriter.newLine();
                    networkWriter.flush();
                }
            } catch (Exception e) {
                Log.d(TAG, "에러 발생", e);
                SoketClose();
            }
        }
    }

    public void setUiText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.text_tv)).setText(text);
            }
        });
    }

    public void search() {
        try {
            //adb -s ce12160cfb8a5f3504 shell screenrecord --time-limit 1 --verbose /sdcard/screenrecord-sample.mp4
            videoFile = new File(Environment.getExternalStorageDirectory() + "/screenrecord-sample.mp4");
            if(videoFile.exists()) {
                retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoFile.toString());
                bitmap = retriever.getFrameAtTime(-1);
                final Bitmap copyBitmap = bitmap.copy(bitmap.getConfig(),true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(copyBitmap!=null)
                            bitmap(copyBitmap);
                    }
                }).start();
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
            }
        }
    }

}

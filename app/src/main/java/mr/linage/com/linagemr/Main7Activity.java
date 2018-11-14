package mr.linage.com.linagemr;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.TimerTask;

import mr.linage.com.R;
import mr.linage.com.common.Config;
import mr.linage.com.utils.AndroidUtils;
import mr.linage.com.vo.ArgbVo;

public class Main7Activity extends Activity implements View.OnClickListener {

    private String TAG = getClass().getSimpleName().trim();

    /**
     * 서비스 변수
     */
    private boolean mIsBound;
    private Messenger mServiceMessenger = null;
    private int p_x = 0;
    private int p_y = 0;
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
    private BufferedReader networkReader;

    /**
     * 기타
     */
    private long id = 0;
    private int flag_1 = 0;
    private int flag_2 = 0;
    private int flag_3 = 0;
    private int flag_4 = 0;
    private boolean flag_stop = false;

    public void setConfig() {
//        /**
//         * 에뮬레이터
//         * y 64 증가
//         */
//        Config.x1 = 76;
//        Config.y1 = 231;
//
//        Config.x2 = 76;
//        Config.y2 = 295;
//
//        Config.x3 = 76;
//        Config.y3 = 359;
//
//        Config.x4 = 71;
//        Config.y4 = 378;

        /**
         * 갤럭시s3
         */
        Config.x1 = 57;
        Config.y1 = 175;

        Config.x2 = 57;
        Config.y2 = 222;

        Config.x3 = 57;
        Config.y3 = 270;

        Config.x4 = 57;
        Config.y4 = 318;

        p_x = Config.x1;
        p_y = Config.y1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setConfig();
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            } else {
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    findViewById(R.id.start).setOnClickListener(this);          //시작버튼
                    findViewById(R.id.end).setOnClickListener(this);            //중시버튼
                    findViewById(R.id.soket).setOnClickListener(this);          //연결
                    findViewById(R.id.soket_end).setOnClickListener(this);      //종료
                    findViewById(R.id.soket_send).setOnClickListener(this);     //보내기
                    SoketStart();
                }
            }
        } else {
            findViewById(R.id.start).setOnClickListener(this);          //시작버튼
            findViewById(R.id.end).setOnClickListener(this);            //중시버튼
            findViewById(R.id.soket).setOnClickListener(this);          //연결
            findViewById(R.id.soket_end).setOnClickListener(this);      //종료
            findViewById(R.id.soket_send).setOnClickListener(this);     //보내기
            SoketStart();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= 23) {
            if(requestCode==1) {
                if (!Settings.canDrawOverlays(this)) {
                    Log.d(TAG, "ACTION_MANAGE_OVERLAY_PERMISSION 권한 종료");
                    finish();
                } else {
                    Log.d(TAG, "ACTION_MANAGE_OVERLAY_PERMISSION 권한 획득");
                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= 23) {
            if(requestCode==1) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "READ_EXTERNAL_STORAGE 권한 종료");
                    finish();
                } else {
                    Log.d(TAG, "READ_EXTERNAL_STORAGE 권한 획득");
                    findViewById(R.id.start).setOnClickListener(this);          //시작버튼
                    findViewById(R.id.end).setOnClickListener(this);            //중시버튼
                    findViewById(R.id.soket).setOnClickListener(this);          //연결
                    findViewById(R.id.soket_end).setOnClickListener(this);      //종료
                    findViewById(R.id.soket_send).setOnClickListener(this);     //보내기
                    SoketStart();
                }
            }
        }
    }

    public void searchStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if(client==null) {
                            Log.d(TAG, "search 쓰레드 종료");
                            break;
                        }
                        Log.d(TAG, "search 쓰레드 중~");
                        Thread.sleep(500);
                        search();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void searchStartOne() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(client!=null) {
                    search();
                }
            }
        }).start();
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
                int x = AndroidUtils.DPFromPixel(Config.x1,getApplicationContext());
                int y = AndroidUtils.DPFromPixel(Config.y1,getApplicationContext());
                if(rank==1) {
                    x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                    y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                }
                pixelSearch(bitmap, x, y,"app_log_1");
            }
        }

        {
            if(mode_num>=2) {
                int x = AndroidUtils.DPFromPixel(Config.x2,getApplicationContext());
                int y = AndroidUtils.DPFromPixel(Config.y2,getApplicationContext());
                if(rank==2) {
                    x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                    y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                }
                pixelSearch(bitmap, x, y,"app_log_2");
            }
        }

        {
            if(mode_num>=3) {
                int x = AndroidUtils.DPFromPixel(Config.x3,getApplicationContext());
                int y = AndroidUtils.DPFromPixel(Config.y3,getApplicationContext());
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
                int x = AndroidUtils.DPFromPixel(Config.x4,getApplicationContext());
                int y = AndroidUtils.DPFromPixel(Config.y4,getApplicationContext());
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
        if("app_log_1".equals(file_name)) {
            if(!Config.flag_search_app_1) {
                flag = (260>R&&R>130&&G<100&&B<100);//캐릭명 빨강(154,23,19)
            } else {
                flag = !(260>R&&R>130&&G<100&&B<100);//캐릭 에너지 빨강 아닐때(154,23,19)
            }
        } else if("app_log_2".equals(file_name)) {
            if(!Config.flag_search_app_2) {
                flag = (260>R&&R>130&&G<100&&B<100);//캐릭명 빨강(154,23,19)
            } else {
                flag = !(260>R&&R>130&&G<100&&B<100);//캐릭 에너지 빨강 아닐때(154,23,19)
            }
        } else if("app_log_3".equals(file_name)) {
            if(!Config.flag_search_app_3) {
                flag = (260>R&&R>130&&G<100&&B<100);//캐릭명 빨강(154,23,19)
            } else {
                flag = !(260>R&&R>130&&G<100&&B<100);//캐릭 에너지 빨강 아닐때(154,23,19)
            }
        } else if("app_log_4".equals(file_name)) {
            if(!Config.flag_search_app_4) {
                flag = (260>R&&R>130&&G<100&&B<100);//캐릭명 빨강(154,23,19)
            } else {
                flag = !(260>R&&R>130&&G<100&&B<100);//캐릭 에너지 빨강 아닐때(154,23,19)
            }
        } else {
//            flag = (260>R&&R>130&&G<100&&B<100);//캐릭명 빨강(154,23,19)
        }
        /**
         * 캐릭명 빨강(154,23,19)
         */
        if(flag) {
            Log.d(TAG,"pixelSearch"+" "+"캐릭명"+" "+"빨강"+" "+file_name+" "+"x :"+x+" "+"y :"+y+" "+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B);
            if("app_log_1".equals(file_name)) {
                if(Config.flag_search_app_1) {
                    setLog(file_name);
                } else {
                    if(flag_1==0) {
                        Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환~");
                        setLog(file_name);
                    } else {
                        Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환초기화대기중~");
                    }
                    flag_1++;
                }
            } else if("app_log_2".equals(file_name)) {
                if(Config.flag_search_app_2) {
                    setLog(file_name);
                } else {
                    if(flag_2==0) {
                        Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환~");
                        setLog(file_name);
                    } else {
                        Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환초기화대기중~");
                    }
                    flag_2++;
                }
            } else if("app_log_3".equals(file_name)) {
                if(Config.flag_search_app_3) {
                    setLog(file_name);
                } else {
                    if(flag_3==0) {
                        Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환~");
                        setLog(file_name);
                    } else {
                        Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환초기화대기중~");
                    }
                    flag_3++;
                }
            } else if("app_log_4".equals(file_name)) {
                if(Config.flag_search_app_4) {
                    setLog(file_name);
                } else {
                    if(flag_4==0) {
                        Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환~");
                        setLog(file_name);
                    } else {
                        Log.d(TAG, "pixelSearch"+" "+" "+file_name+"===>>> 귀환초기화대기중~");
                    }
                    flag_4++;
                }
            } else {
//                setLog(file_name);
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
        private final int connection_timeout = 10000;
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
                networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                if (networkReader != null) {
                    networkReader.close();
                    networkReader = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                    Log.d(TAG,"setThread"+" "+"접속을 중단합니다.");
                    setUiText("접속을 중단합니다.");
                }
                if(client != null) {
                    client = null;
                }
            } catch (IOException e) {
                Log.d(TAG, "에러 발생", e);
                setUiText("에러 발생");
            }
        }
    }

    public void sendDing(final String msg) {
        Log.d(TAG,"sendDing"+" "+"서버로부터 받은 데이터 sendDing");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(sending) {
                    try {
                        if(networkWriter!=null) {
                            networkWriter.write(msg);
                            networkWriter.newLine();
                            networkWriter.flush();
                        }
                        if(networkReader!=null) {
                            String receiveData = "";
                            try {
                                receiveData = networkReader.readLine();        // 서버로부터 데이터 한줄 읽음
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "서버로부터 받은 데이터 : " + receiveData);
                            if(!"".equals(receiveData)) {
                                searchStartOne();
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "에러 발생", e);
                        SoketClose();
                    }
                }
            }
        }).start();
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
            bitmap = AndroidUtils.loadBackgroundBitmap(this, Environment.getExternalStorageDirectory() + "/screen.png");
            if(bitmap!=null)
                bitmap(bitmap);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
            sendDing("Hello World!");
        }
//        boolean reStartFlag = false;
//        try {
//            //http://www.dreamy.pe.kr/zbxe/CodeClip/163979
//            //https://android.googlesource.com/platform/frameworks/av/+/kitkat-release/cmds/screenrecord/
//            ///dev/graphics/fb0
//            //adb -s ce12160cfb8a5f3504 shell screenrecord --time-limit 1 --verbose /sdcard/screenrecord-sample.mp4 && adb -s 192.168.0.9 pull /sdcard/screenrecord-sample0.mp4
//            videoFile = new File(Environment.getExternalStorageDirectory() + "/screenrecord-sample.mp4");
//            if(videoFile.exists()) {
//                retriever = new MediaMetadataRetriever();
//                retriever.setDataSource(videoFile.toString());
//                bitmap = retriever.getFrameAtTime(-1);
//                if(bitmap!=null) {
//                    final Bitmap copyBitmap = bitmap.copy(bitmap.getConfig(),true);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(copyBitmap!=null)
//                                bitmap(copyBitmap);
//                        }
//                    }).start();
//                } else {
////                    reStartFlag = true;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if(bitmap!=null) {
//                bitmap.recycle();
//                bitmap = null;
//            }
//            if(retriever!=null) {
//                retriever.release();
//                retriever = null;
//            }
//        }
//        if(reStartFlag) {
//            search();
//        }
    }

}

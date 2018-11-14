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
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import mr.linage.com.R;
import mr.linage.com.utils.AndroidUtils;
import mr.linage.com.vo.ArgbVo;

public class Main2Activity extends Activity implements View.OnClickListener {

    private String TAG = getClass().getSimpleName().trim();

    /**
     * 서비스 변수
     */
    private boolean mIsBound;
    private Messenger mServiceMessenger = null;
    private int p_x = 54;
    private int p_y = 180;
    private int rank = 1;

    /**
     * 녹화 변수
     */
    private int REQUEST_CODE = 0;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection sMediaProjection;
    private int mDensity;
    private Display mDisplay;
    private int mWidth;
    private int mHeight;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private Handler mHandler;
    private OrientationChangeCallback mOrientationChangeCallback;
    private int mRotation;
    private boolean flag_stop ;

    /**
     * 소켓 변수
     */
    private Socket socket;
    private TCPClient client;
    private Handler mMainHandler;
    private HandlerThread thread;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private BufferedWriter networkWriter;
    private BufferedReader networkReader;
    private final int MSG_CONNECT = 0;
    private final int MSG_CLIENT_STOP = 1;
    private final int MSG_SERVER_STOP = 2;
    private final int MSG_START = 3;
    private final int MSG_STOP = 4;
    private final int MSG_ERROR = 5;
    private final int MSG_TEST = 6;
    private final int MSG_TEST2 = 7;

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
        if(mProjectionManager==null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startProjection();
            }
        }
        setThread();
        SoketStart();
        findViewById(R.id.start).setOnClickListener(this);          //시작버튼
        findViewById(R.id.end).setOnClickListener(this);            //중시버튼
        findViewById(R.id.soket).setOnClickListener(this);          //연결
        findViewById(R.id.soket_send).setOnClickListener(this);     //보내기
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
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
            setThread();
            SoketStart();
        } else if(view == R.id.soket_send) {
            Message msg = mServiceHandler.obtainMessage();
            msg.what = MSG_START;
            msg.obj = "Hello World!";
            mServiceHandler.sendMessage(msg);
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
                    Log.i("test","act : x :"+x);
                    Log.i("test","act : y :"+y);
                    Log.i("test","act : rank :"+_rank);
                    p_x = x;
                    p_y = y;
                    rank = _rank;
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

    private void startProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                finish();
                return;
            }
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

            Message toMain = mMainHandler.obtainMessage();
            toMain.what = MSG_TEST;
            mMainHandler.sendMessage(toMain);

//            if (sMediaProjection != null) {
//                TimerTask adTast = new TimerTask() {
//                    public void run() {
//                        Message toMain = mMainHandler.obtainMessage();
//                        toMain.what = MSG_TEST;
//                        mMainHandler.sendMessage(toMain);
//                    }
//                };
//                Timer timer = new Timer();
//                timer.schedule(adTast, 0, 5000); // 0초후 첫실행, 3초마다 계속실행
////                createVirtualDisplay();
//            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createVirtualDisplay() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDensity = metrics.densityDpi;
        mDisplay = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay("VirtualDisplay name", mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null /* Callbacks */, mHandler /* Handler */);
        HandlerThread thread = new HandlerThread("CameraPicture");
        thread.start();
        final Handler backgroudHandler = new Handler(thread.getLooper());
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), backgroudHandler/*backgroudHandler*/);
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                    if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                    sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {
        public OrientationChangeCallback(Context context) {
            super(context);
        }
        @Override
        public void onOrientationChanged(int orientation) {
            synchronized (this) {
                final int rotation = mDisplay.getRotation();
                if (rotation != mRotation) {
                    mRotation = rotation;
                    try {
//                        Message toMain = mMainHandler.obtainMessage();
//                        toMain.what = MSG_TEST;
//                        mMainHandler.sendMessage(toMain);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Log.d(TAG,"onImageAvailable image 시작");
            Image image = null;
            Bitmap bitmap = null;
            try {
                image = mImageReader.acquireLatestImage();
                Log.d(TAG,"onImageAvailable image 생성");
                if (image != null) {
                    if(flag_stop) {
                        new Handler().postDelayed(new Runnable() {// 1 초 후에 실행
                            @Override public void run() { // 실행할 동작 코딩
                                flag_stop = false;
                            }
                        }, 1000);
                    } else {
                        flag_stop = true;
                        final Image.Plane[] planes = image.getPlanes();
                        final Buffer buffer = planes[0].getBuffer().rewind();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * mWidth;
                        bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);

                        Log.d(TAG,"onImageAvailable bitmap 생성");
                        {
                            int x = AndroidUtils.DPFromPixel(54,getApplicationContext());
                            int y = AndroidUtils.DPFromPixel(180,getApplicationContext());
                            if(rank==1) {
                                x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                                y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                            }
                            pixelSearch(bitmap, x, y,"app_log_1");
                        }

                        {
                            int x = AndroidUtils.DPFromPixel(64,getApplicationContext());
                            int y = AndroidUtils.DPFromPixel(231,getApplicationContext());
                            if(rank==2) {
                                x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                                y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                            }
                            pixelSearch(bitmap, x, y,"app_log_2");
                        }

                        {
                            int x = AndroidUtils.DPFromPixel(54,getApplicationContext());
                            int y = AndroidUtils.DPFromPixel(270,getApplicationContext());
                            if(rank==3) {
                                x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                                y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                            }
                            pixelSearch(bitmap, x, y,"app_log_3");
                        }

                        {
                            int x = AndroidUtils.DPFromPixel(54,getApplicationContext());
                            int y = AndroidUtils.DPFromPixel(314,getApplicationContext());
                            if(rank==4) {
                                x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                                y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                            }
                            pixelSearch(bitmap, x, y,"app_log_4");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bitmap != null) {
                    bitmap.recycle();
                    Log.d(TAG,"onImageAvailable bitmap 제거");
                }
                if (image != null) {
                    image.close();
                    Log.d(TAG,"onImageAvailable image 제거");
                }
            }
            Log.d(TAG,"onImageAvailable image 종료");
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
        /**
         * 캐릭명 빨강(154,23,19)
         */
        if(260>R&&R>150&&G<100&&B<100) {
            Log.d(TAG,"pixelSearch"+" "+"캐릭명"+" "+"빨강"+" "+file_name+" "+"x :"+x+" "+"y :"+y+" "+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B);
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
                Message msg = mServiceHandler.obtainMessage();
                msg.what = MSG_START;
                msg.obj = file_name;
                mServiceHandler.sendMessage(msg);
            }
        } else {
            Log.d(TAG, "pixelSearch"+" "+"캐릭명"+" "+"흰색"+" "+file_name+"x :"+x+" "+"y :"+y+" "+" "+"A :"+A+" "+"R :"+R+" "+"G :"+G+" "+"B :"+B+"===>>> 귀환준비~");
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    Message toMain = mMainHandler.obtainMessage();
                    try {
                        networkWriter.write((String) msg.obj);
                        networkWriter.newLine();
                        networkWriter.flush();
                        toMain.what = MSG_START;
                    } catch (Exception e) {
                        toMain.what = MSG_ERROR;
                        Log.d(TAG, "에러 발생", e);
                    }
                    toMain.obj = msg.obj;
                    mMainHandler.sendMessage(toMain);
                    break;
                case MSG_STOP:
                case MSG_CLIENT_STOP:
                case MSG_SERVER_STOP:
                    client.quit();
                    client = null;
                    break;
            }
        }
    }

    public class TCPClient extends Thread {
        String line;
        Boolean loop;
        SocketAddress socketAddress;
        private final int connection_timeout = 3000;
        public TCPClient(String ip, int port) throws RuntimeException {
            socketAddress = new InetSocketAddress(ip, port);
        }
        @Override
        public void run() {
            try {
                socket = new Socket();
                socket.setSoTimeout(connection_timeout);
                socket.setSoLinger(true, connection_timeout);
                socket.connect(socketAddress, connection_timeout);
                networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                InputStreamReader i = new InputStreamReader(socket.getInputStream());
                networkReader = new BufferedReader(i);
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_CONNECT;
                mMainHandler.sendMessage(toMain);
                loop = true;
            } catch (Exception e) {
                loop = false;
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_ERROR;
                toMain.obj = "소켓을 생성하지 못했습니다.";
                mMainHandler.sendMessage(toMain);
            }
            while (loop) {
                try {
                    line = networkReader.readLine();
                    if (line == null)
                        break;
                    Runnable showUpdate = new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, line);
                        }
                    };
                    mMainHandler.post(showUpdate);
                } catch (InterruptedIOException e) {
                } catch (IOException e) {
                    loop = false;
                    e.printStackTrace();
                    break;
                }
            }
            try  {
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
                }
                client = null;
                if (loop) {
                    loop = false;
                    Message toMain = mMainHandler.obtainMessage();
                    toMain.what = MSG_SERVER_STOP;
                    toMain.obj = "네트워크가 끊어졌습니다.";
                    mMainHandler.sendMessage(toMain);
                }
            } catch(IOException e ) {
                Log.d(TAG, "에러 발생", e);
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_ERROR;
                toMain.obj = "소켓을 닫지 못했습니다..";
                mMainHandler.sendMessage(toMain);
            }
        }

        public void quit() {
            loop = false;
            try {
                if (socket != null) {
                    socket.close();
                    socket = null;
                    Message toMain = mMainHandler.obtainMessage();
                    toMain.what = MSG_CLIENT_STOP;
                    toMain.obj = "접속을 중단합니다.";
                    mMainHandler.sendMessage(toMain);
                }
            } catch (IOException e) {
                Log.d(TAG, "에러 발생", e);
            }
        }
    }

    private void SoketStart() {
        final String ip = ((EditText)findViewById(R.id.soket_ip)).getText().toString();
        if(!"".equals(ip)) {
            try {
                client = new TCPClient(ip, 9999);
                client.start();
            } catch (RuntimeException e) {
                Log.d(TAG, "에러 발생", e);
            }
        }
    }

    private void SoketClose() {
        if (client != null) {
            Message msg = mServiceHandler.obtainMessage();
            msg.what = MSG_STOP;
            mServiceHandler.sendMessage(msg);
        }
        if(thread!=null) {
            thread.quit();
            thread = null;
        }
    }

    private void setThread() {
        thread = new HandlerThread("HandlerThread");
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String m;
                switch (msg.what) {
                    case MSG_CONNECT:
                        String ip = ((EditText)findViewById(R.id.soket_ip)).getText().toString();
                        Log.d(TAG,"ip:"+ip);
                        m = "정상적으로 서버에 접속하였습니다.";
                        Log.d(TAG,"setThread"+" "+m);
                        ((TextView)findViewById(R.id.text_tv)).setText(m);
                        break;
                    case MSG_CLIENT_STOP:
                        Log.d(TAG, (String) msg.obj);
                        m = "클라이언트가 접속을 종료하였습니다.";
                        Log.d(TAG,"setThread"+" "+m);
                        ((TextView)findViewById(R.id.text_tv)).setText(m);
                        break;
                    case MSG_SERVER_STOP:
                        Log.d(TAG, (String) msg.obj);
                        m = "서버가 접속을 종료하였습니다.";
                        Log.d(TAG,"setThread"+" "+m);
                        ((TextView)findViewById(R.id.text_tv)).setText(m);
                        break;
                    case MSG_START:
                        Log.d(TAG, (String) msg.obj);
                        m = "메세지 전송 완료!";
                        Log.d(TAG,"setThread"+" "+m);
                        ((TextView)findViewById(R.id.text_tv)).setText(m);
                        break;
                    case MSG_TEST:
                        Log.d(TAG,"MSG_TEST"+" "+"시작");
                        flag_stop = false;
                        createVirtualDisplay();
                        mOrientationChangeCallback = new OrientationChangeCallback(Main2Activity.this);
                        if (mOrientationChangeCallback.canDetectOrientation()) {
                            mOrientationChangeCallback.enable();
                        }
                        sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
                        new Handler().postDelayed(new Runnable() {// 1 초 후에 실행
                            @Override
                            public void run() { // 실행할 동작 코딩
                                Message toMain = mMainHandler.obtainMessage();
                                toMain.what = MSG_TEST2;
                                mMainHandler.sendMessage(toMain);
                            }
                        }, 10000);
                        break;
                    case MSG_TEST2:
                        Log.d(TAG,"MSG_TEST2"+" "+"스톱");
                        if (mVirtualDisplay != null) mVirtualDisplay.release();
                        if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                        new Handler().postDelayed(new Runnable() {// 1 초 후에 실행
                            @Override
                            public void run() { // 실행할 동작 코딩
                                Message toMain = mMainHandler.obtainMessage();
                                toMain.what = MSG_TEST;
                                mMainHandler.sendMessage(toMain);
                            }
                        }, 2500);
                        break;
                    default:
                        Log.d(TAG, (String) msg.obj);
                        m = "에러 발생!";
                        Log.d(TAG,"setThread"+" "+m);
                        ((TextView)findViewById(R.id.text_tv)).setText(m);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }
}

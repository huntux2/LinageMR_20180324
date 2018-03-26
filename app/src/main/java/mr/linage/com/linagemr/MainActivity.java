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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import mr.linage.com.utils.AndroidUtils;
import mr.linage.com.vo.ArgbVo;

public class MainActivity extends Activity implements View.OnClickListener {

    private String TAG = getClass().getSimpleName().trim();

    private int REQUEST_CODE = 0;

    private MediaProjectionManager mProjectionManager;
    private MediaProjection sMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;
    private Display mDisplay;
    private int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private int p_x = 54;
    private int p_y = 180;
    private int rank = 1;

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
        Boolean loop;
        SocketAddress socketAddress;
        String line;
        private final int connection_timeout = 3000;

        public TCPClient(String ip, int port) throws RuntimeException {
            socketAddress = new InetSocketAddress(ip, port);
        }

        @Override
        public void run() {
            try {
                socket = new Socket();

                socket.setSoTimeout(connection_timeout);
                //read 메서드가 connection_timeout 시간동안 응답을 기다린다.
                socket.setSoLinger(true, connection_timeout);
                //서버와의 정상 종료를 위해서 connection_timeout 시간동안 close 호출 후 기다린다.
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
                    //readLine()은 블록모드로 작동하기 때문에 별도의 스레드에서 실행한다.
                    if (line == null) //서버에서 FIN 패킷을 보내면 null을 반환한다.
                        break;
                    Runnable showUpdate = new Runnable() {
                        @Override
                        public void run() {
//                            text.setText(line);
                            Log.d(TAG, line);
                        }
                    };

                    mMainHandler.post(showUpdate);
                    //Runnable 객체를 메인 핸들러로 전달해 UI를 변경한다.
                } catch (InterruptedIOException e) {
                } catch (IOException e) {
                    loop = false;
//                    Log.d(TAG, "에러 발생", e);
                    e.printStackTrace();
                    break;
                }
            }

            try  {  //소켓을 close 하면 null로 설정해서 가비지 컬렉션이 되도록 한다.
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
                if (loop) { //클라이언트에서 연결을 끊지 않고 서버에서 FIN 패킷을 받았을 경우
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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
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
                // 시스템의 Projection service를 획득합니다.
                mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

                // 실제 권한을 사용자에게 통보하고 권한을 요구하게 됩니다.
                startProjection();
            }
        }

        findViewById(R.id.start).setOnClickListener(this);		//시작버튼
        findViewById(R.id.end).setOnClickListener(this);			//중시버튼
        findViewById(R.id.soket).setOnClickListener(this);			//연결
        findViewById(R.id.soket_send).setOnClickListener(this);			//보내기

        SoketStart();

        /*
        여기서 mHandler가 만들어지게 됩니다.
        새로운 스레드 하나 만들고, 핸들러 만들어서 prepare()로 메세지큐가 준비되면, 핸들러 만들고
        loop로 이제 무한정 기다리게 된다네요. 해당 스레드를 사용할때는 성능상 문제가 없도록
        구현해야 된다고 합니다. 강제종료를 시키지 않으면, 메모리를 계속 차지하고 있으니까요.
        그런데 아직도 핸들러 개념이 어렵습니다. 위에서 말했던 것과 다르게 엄...
        스레드가 충돌 나는 부분이라기도 애매한게 여기서 새로운 스레드를 만든건데...아...
        모르겠습니다 그냥 패스. 책 보면서 추가 공부 해야되겠습니다.
        */
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }

    private void startProjection() {
        //사용자 허가 요청!
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        //projection을 종료 합니다. stop()! mediaprojection callback의
        //onstop method가 호출 되겠네요.
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

            // 사용자가 권한을 허용해주었는지에 대한 처리
            if (resultCode != RESULT_OK) {
                // 사용자가 권한을 허용해주지 않았습니다.
                finish();
                return;
            }

            // 사용자가 권한을 허용해주었기에 mediaProjection을 사용할 수 있는 권한이 생기게 됩니다.
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            if (sMediaProjection != null) {

                TimerTask adTast = new TimerTask() {

                    public void run() {

                        /*
                        virtualdisplay를 release 해주고 imagereader의 이벤트도 빼버리고 그런데
                        imagereader 이벤트 빼고나서 null로 주어야되지않나? 알아서 가비지컬렉터가
                        메모리 해제해주는지 모르겠네요. newInstance로 새롭게 객체 만들텐데
                        쨋든 그러고나서 createVirtualDisplay로 virtualdisplay 새로 생성
                        */
                        if (mVirtualDisplay != null) mVirtualDisplay.release();
                        if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                        createVirtualDisplay();

                    }

                };

                Timer timer = new Timer();
                timer.schedule(adTast, 0, 5000); // 0초후 첫실행, 3초마다 계속실행

                /*
                여건 orientation callback 등록 부분.
                감지 할 수 있ㅇ면, enable()로 이제 감지하게끔 해주는가 봅니다.
                */
                mOrientationChangeCallback = new OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }
                sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    boolean flag_stop = false;

    /*
    ImageReader에서 Image를 처리할 class 입니다.
    OnImageAvailableListener를 상속받아 구현 되었으며, onImageAvailable(ImageReader) 메소드가
    오버라이딩 되었고, 해당 메소드 내에서 이미지를 꺼내 처리하믄 되나 봅니다.
    */
    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;

            try {
                //가장 최신 이미지를 가져 옵니다. image 객체로
                image = mImageReader.acquireLatestImage();
                if (image != null) {
                    if(flag_stop) {
                        new Handler().postDelayed(new Runnable() {// 1 초 후에 실행
                            @Override public void run() { // 실행할 동작 코딩
                                flag_stop = false;
                            }
                        }, 250);
                    }
                    flag_stop = true;
                    /*
                    여기서 getPlanes()로 가져와서 0배열만 쓰는 이유를 모르겠습니다.
                    혹시 다차원 이미지도 지원되기때문에 그런건 아닐까 하는 생각을 조심스럽게 해봅니다.
                    그래서 2차원 평면만 가져온건 아닌지...는 모르겠고 그냥 모르겠습니다.
                    쨋든 이미지 버퍼정보와.... 픽셀하고 행??...보폭이라 표현한건 단위인건가??
                    rowPadding 패딩값을 알기위해 행단위 - 픽셀*너비?? 이게 밑에서
                    너비부분만 사용할 때 사용되는데 이유를 모르겠습니다. 왜 이런 수식을 쓰는지
                    (mWidth + (rowStride - pixelStride * mWidth) / pixelStride)
                    인건데... 아 모르겠다. 안드로이드 공부가 너무 부족한 듯 합니다.
                    이 api 공부 이후로는 안드로이드 기본이나 공부를 더 해야겠어요.
                    */
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;

                    //쨋든 createBitmap으로 bitmap파일 만들고 위의 이미지 buffer로 이미지를 가져옵니다.
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    {
//                        int rgb = bitmap.getPixel(AndroidUtils.DPFromPixel(p_x,getApplicationContext()), AndroidUtils.DPFromPixel(p_y,getApplicationContext())); //원하는 좌표값 입력
//                        int A = Color.alpha(rgb); //alpha값 추출
//                        int R = Color.red(rgb); //red값 추출
//                        int G = Color.green(rgb); //green값 추출
//                        int B = Color.blue(rgb); //blue값 추출
//
//                        ArgbVo argbVo = new ArgbVo();
//                        argbVo.setA(A);
//                        argbVo.setR(R);
//                        argbVo.setG(G);
//                        argbVo.setB(B);
//                        Log.i("rgb","p_x:"+p_x+" "+"p_y:"+p_y+" "+"A : "+argbVo.getA()+" "+"R : "+argbVo.getR()+" "+"G : "+argbVo.getG()+" "+"B : "+argbVo.getB());
//                        sendMessageToService(argbVo);
                    }

                    {
                        int x = AndroidUtils.DPFromPixel(54,getApplicationContext());
                        int y = AndroidUtils.DPFromPixel(180,getApplicationContext());
                        if(rank==1) {
                            x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                            y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                        }
                        /**
                         * 1번째 캐릭명(54,180)
                         */
                        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
                        int A = Color.alpha(rgb); //alpha값 추출
                        int R = Color.red(rgb); //red값 추출
                        int G = Color.green(rgb); //green값 추출
                        int B = Color.blue(rgb); //blue값 추출

                        Log.i("rgb","app_log_1"+" "+"p_x:"+x+" "+"p_y:"+y+" "+"A : "+A+" "+"R : "+R+" "+"G : "+G+" "+"B : "+B);

                        if(rank==1) {
                            ArgbVo argbVo = new ArgbVo();
                            argbVo.setA(A);
                            argbVo.setR(R);
                            argbVo.setG(G);
                            argbVo.setB(B);
                            sendMessageToService(argbVo, rank);
                        }

                        log(bitmap, rgb, A, R, G, B, "app_log_1", 1);
                    }

                    {
                        int x = AndroidUtils.DPFromPixel(64,getApplicationContext());
                        int y = AndroidUtils.DPFromPixel(231,getApplicationContext());
                        if(rank==2) {
                            x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                            y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                        }
                        /**
                         * 2번째 캐릭명(54,225)
                         */
                        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
                        int A = Color.alpha(rgb); //alpha값 추출
                        int R = Color.red(rgb); //red값 추출
                        int G = Color.green(rgb); //green값 추출
                        int B = Color.blue(rgb); //blue값 추출

                        Log.i("rgb","app_log_2"+" "+"p_x:"+x+" "+"p_y:"+y+" "+"A : "+A+" "+"R : "+R+" "+"G : "+G+" "+"B : "+B);

                        if(rank==2) {
                            ArgbVo argbVo = new ArgbVo();
                            argbVo.setA(A);
                            argbVo.setR(R);
                            argbVo.setG(G);
                            argbVo.setB(B);
                            sendMessageToService(argbVo, rank);
                        }

                        log(bitmap, rgb, A, R, G, B, "app_log_2", 2);
                    }

                    {
                        int x = AndroidUtils.DPFromPixel(54,getApplicationContext());
                        int y = AndroidUtils.DPFromPixel(270,getApplicationContext());
                        if(rank==3) {
                            x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                            y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                        }
                        /**
                         * 3번째 캐릭명(54,270)
                         */
                        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
                        int A = Color.alpha(rgb); //alpha값 추출
                        int R = Color.red(rgb); //red값 추출
                        int G = Color.green(rgb); //green값 추출
                        int B = Color.blue(rgb); //blue값 추출

                        Log.i("rgb","app_log_3"+" "+"p_x:"+x+" "+"p_y:"+y+" "+"A : "+A+" "+"R : "+R+" "+"G : "+G+" "+"B : "+B);

                        if(rank==3) {
                            ArgbVo argbVo = new ArgbVo();
                            argbVo.setA(A);
                            argbVo.setR(R);
                            argbVo.setG(G);
                            argbVo.setB(B);
                            sendMessageToService(argbVo, rank);
                        }

                        log(bitmap, rgb, A, R, G, B, "app_log_3", 3);
                    }

                    {
                        int x = AndroidUtils.DPFromPixel(54,getApplicationContext());
                        int y = AndroidUtils.DPFromPixel(314,getApplicationContext());
                        if(rank==4) {
                            x = AndroidUtils.DPFromPixel(p_x,getApplicationContext());
                            y = AndroidUtils.DPFromPixel(p_y,getApplicationContext());
                        }
                        /**
                         * 4번째 캐릭명(54,314)
                         */
                        int rgb = bitmap.getPixel(x, y); //원하는 좌표값 입력
                        int A = Color.alpha(rgb); //alpha값 추출
                        int R = Color.red(rgb); //red값 추출
                        int G = Color.green(rgb); //green값 추출
                        int B = Color.blue(rgb); //blue값 추출

                        Log.i("rgb","app_log_4"+" "+"p_x:"+x+" "+"p_y:"+y+" "+"A : "+A+" "+"R : "+R+" "+"G : "+G+" "+"B : "+B);

                        if(rank==4) {
                            ArgbVo argbVo = new ArgbVo();
                            argbVo.setA(A);
                            argbVo.setR(R);
                            argbVo.setG(G);
                            argbVo.setB(B);
                            sendMessageToService(argbVo, rank);
                        }

                        log(bitmap, rgb, A, R, G, B, "app_log_4", 4);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    }

    public void log(Bitmap bitmap,int rgb, int A, int R, int B, int G, String file_name, int rank) {
        /**
         * 캐릭명 빨강(233,154,23,19)
         */
        if(260>R&&R>150&&G<100&&B<100) {
            if(rank==1) {
                /**
                 * 마을세이프티(780,238)
                 */
                int rgb_ = bitmap.getPixel(AndroidUtils.DPFromPixel(780,getApplicationContext()), AndroidUtils.DPFromPixel(238,getApplicationContext())); //원하는 좌표값 입력
                int A_ = Color.alpha(rgb_); //alpha값 추출
                int R_ = Color.red(rgb_); //red값 추출
                int G_ = Color.green(rgb_); //green값 추출
                int B_ = Color.blue(rgb_); //blue값 추출
                Log.i("rgb","app_log_4"+" "+"R : "+R_+" "+"G : "+G_+" "+"B : "+B_);
                if((R_<35&&R_>20)&&(G_<145&&G_>130)&&(B_<240&&B_>225)) {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
                } else {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
                    setLog(file_name);
                }
//                if(flag_1==0) {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
//                    setLog(file_name);
//                } else {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
//                }
//                flag_1++;
            } else if(rank==2) {
                /**
                 * 마을세이프티(780,238)
                 */
                int rgb_ = bitmap.getPixel(AndroidUtils.DPFromPixel(780,getApplicationContext()), AndroidUtils.DPFromPixel(238,getApplicationContext())); //원하는 좌표값 입력
                int A_ = Color.alpha(rgb_); //alpha값 추출
                int R_ = Color.red(rgb_); //red값 추출
                int G_ = Color.green(rgb_); //green값 추출
                int B_ = Color.blue(rgb_); //blue값 추출
                Log.i("rgb","app_log_4"+" "+"R : "+R_+" "+"G : "+G_+" "+"B : "+B_);
                if((R_<35&&R_>20)&&(G_<145&&G_>130)&&(B_<240&&B_>225)) {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
                } else {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
                    setLog(file_name);
                }
//                if(flag_2==0) {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
//                    setLog(file_name);
//                } else {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
//                }
//                flag_2++;
            } else if(rank==3) {
                /**
                 * 마을세이프티(780,238)
                 */
                int rgb_ = bitmap.getPixel(AndroidUtils.DPFromPixel(780,getApplicationContext()), AndroidUtils.DPFromPixel(238,getApplicationContext())); //원하는 좌표값 입력
                int A_ = Color.alpha(rgb_); //alpha값 추출
                int R_ = Color.red(rgb_); //red값 추출
                int G_ = Color.green(rgb_); //green값 추출
                int B_ = Color.blue(rgb_); //blue값 추출
                Log.i("rgb","app_log_4"+" "+"R : "+R_+" "+"G : "+G_+" "+"B : "+B_);
                if((R_<35&&R_>20)&&(G_<145&&G_>130)&&(B_<240&&B_>225)) {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
                } else {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
                    setLog(file_name);
                }
//                if(flag_3==0) {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
//                    setLog(file_name);
//                } else {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
//                }
//                flag_3++;
            } else if(rank==4) {
                /**
                 * 마을세이프티(780,238)
                 */
                int rgb_ = bitmap.getPixel(AndroidUtils.DPFromPixel(780,getApplicationContext()), AndroidUtils.DPFromPixel(238,getApplicationContext())); //원하는 좌표값 입력
                int A_ = Color.alpha(rgb_); //alpha값 추출
                int R_ = Color.red(rgb_); //red값 추출
                int G_ = Color.green(rgb_); //green값 추출
                int B_ = Color.blue(rgb_); //blue값 추출
                Log.i("rgb","app_log_4"+" "+"R : "+R_+" "+"G : "+G_+" "+"B : "+B_);
                if((R_<35&&R_>20)&&(G_<145&&G_>130)&&(B_<240&&B_>225)) {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
                } else {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
                    setLog(file_name);
                }
//                if(flag_4==0) {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
//                    setLog(file_name);
//                } else {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
//                }
//                flag_4++;
            }
        } else {
            Log.d("LinageMR", file_name+" "+"===>>> 귀환준비~");
            if(rank==1) {
                flag_1 = 0;
            } else if(rank==2) {
                flag_2 = 0;
            } else if(rank==3) {
                flag_3 = 0;
            } else if(rank==4) {
                flag_4 = 0;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private PrintWriter out;

    public void setSocket(String ip, int port) throws Exception {
        try {
            socket = new Socket(ip, port);
            networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void closeSoket() {
        try{
            if(networkWriter!=null) {
                networkWriter.close();
                networkWriter = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(socket!=null) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long flag_1 = 0;
    private long flag_2 = 0;
    private long flag_3 = 0;
    private long flag_4 = 0;
    private long id = 0;

    public void setLog(final String file_name) {
        try {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    id++;
                    Log.d("LinageMR", file_name+" "+"adb shell input tap 750 650::id::" + id+" app_log");
                    AndroidUtils.writeFile("LinageMR adb shell input tap 750 650::id::" + id, file_name);
                }
            }.start();
            Message msg = mServiceHandler.obtainMessage();
            msg.what = MSG_START;
            msg.obj = file_name;
            mServiceHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // Do something with overlay permission
                finish();
            }
        }
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onClick(View v) {
        int view = v.getId();
        if(view == R.id.start) {
//            startService(new Intent(this, MainService.class));    //서비스 시작
            bindService(new Intent(this, MainService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        } else if(view == R.id.end) {
            if(mIsBound)
            unbindService(mConnection);    //서비스 종료
        } else if(view == R.id.soket) {
            Log.i("test","networkWriter"+networkWriter);
            SoketStart();
        } else if(view == R.id.soket_send) {
            Log.i("test","networkWriter"+networkWriter);
            Message msg = mServiceHandler.obtainMessage();
            msg.what = MSG_START;
            msg.obj = "Hello World!";
            mServiceHandler.sendMessage(msg);
        }
    }

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

    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
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

    private void SoketStart() {
        final String ip = ((EditText)findViewById(R.id.soket_ip)).getText().toString();
        if(!"".equals(ip)) {
            SoketClose();
            /**
             * 소켓 통신 세팅
             */
            try {
                client = new TCPClient(ip, 9999);
                client.start();
            } catch (RuntimeException e) {
                Log.d(TAG, "에러 발생", e);
            }
            thread = new HandlerThread("HandlerThread");
            thread.start();
            // 루퍼를 만든다.
            mServiceLooper = thread.getLooper();
            mServiceHandler = new ServiceHandler(mServiceLooper);
            mMainHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    String m;
                    switch (msg.what) {
                        case MSG_CONNECT:
                            Log.d(TAG,"ip:"+ip);
                            m = "정상적으로 서버에 접속하였습니다.";
                            Log.d(TAG,m);
                            ((TextView)findViewById(R.id.text_tv)).setText(m);
                            break;
                        case MSG_CLIENT_STOP:
                            Log.d(TAG, (String) msg.obj);
                            m = "클라이언트가 접속을 종료하였습니다.";
                            Log.d(TAG,m);
                            ((TextView)findViewById(R.id.text_tv)).setText(m);
                            break;
                        case MSG_SERVER_STOP:
                            Log.d(TAG, (String) msg.obj);
                            m = "서버가 접속을 종료하였습니다.";
                            Log.d(TAG,m);
                            ((TextView)findViewById(R.id.text_tv)).setText(m);
                            break;
                        case MSG_START:
                            Log.d(TAG, (String) msg.obj);
                            m = "메세지 전송 완료!";
                            Log.d(TAG,m);
                            ((TextView)findViewById(R.id.text_tv)).setText(m);
                            break;
                        default:
                            Log.d(TAG, (String) msg.obj);
                            m = "에러 발생!";
                            Log.d(TAG,m);
                            ((TextView)findViewById(R.id.text_tv)).setText(m);
                            break;
                    }
                    super.handleMessage(msg);
                }
            };
        }
    }

    private void SoketClose() {
        if (client != null) {  //소켓과 스레드를 모두 종료시킨다.
            Message msg = mServiceHandler.obtainMessage();
            msg.what = MSG_STOP;
            mServiceHandler.sendMessage(msg);
        }
        if(thread!=null) {
            thread.quit();
            thread = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoketClose();
    }
    private Handler mHandler;

    private OrientationChangeCallback mOrientationChangeCallback;

    private class OrientationChangeCallback extends OrientationEventListener {
        //생성자가 필히 요구 됩니다.
        public OrientationChangeCallback(Context context) {
            super(context);
        }
        @Override
        public void onOrientationChanged(int orientation) {
            synchronized (this) {
                //화면 전환으로 인해서 virtualdisplay를 새로만드는 과정이니 동기화 시켜주고
                final int rotation = mDisplay.getRotation();

                //rotation값이다르다면
                if (rotation != mRotation) {
                    mRotation = rotation;
                    try {
                        /*
                        virtualdisplay를 release 해주고 imagereader의 이벤트도 빼버리고 그런데
                        imagereader 이벤트 빼고나서 null로 주어야되지않나? 알아서 가비지컬렉터가
                        메모리 해제해주는지 모르겠네요. newInstance로 새롭게 객체 만들텐데
                        쨋든 그러고나서 createVirtualDisplay로 virtualdisplay 새로 생성
                        */
                        if (mVirtualDisplay != null) mVirtualDisplay.release();
                        if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                        createVirtualDisplay();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //가상 디스플레이를 만듭니다.
    private void createVirtualDisplay() {
        /*
        현재 디스플레이의 density dpi 가져 옵니다.
        */
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDensity = metrics.densityDpi;
        mDisplay = getWindowManager().getDefaultDisplay();

        //가로,세로 고려 사이즈는 다시 설정하고
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);

        // MediaProjection에 대한 Event 정보를 받으려면 아래와 같이 적용하시면 됩니다.
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
}

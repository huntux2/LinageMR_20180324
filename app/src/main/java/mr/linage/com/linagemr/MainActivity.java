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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import mr.linage.com.utils.AndroidUtils;
import mr.linage.com.vo.ArgbVo;

public class MainActivity extends Activity implements View.OnClickListener {
    int REQUEST_CODE = 0;

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
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            }
        }

        findViewById(R.id.start).setOnClickListener(this);		//시작버튼
        findViewById(R.id.end).setOnClickListener(this);			//중시버튼
        findViewById(R.id.soket).setOnClickListener(this);			//중시버튼
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
                mVirtualDisplay = sMediaProjection.createVirtualDisplay("VirtualDisplay name", mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null /* Callbacks */, null /* Handler */);

                HandlerThread thread = new HandlerThread("CameraPicture");
                thread.start();
                final Handler backgroudHandler = new Handler(thread.getLooper());
                mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), backgroudHandler);
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
                        }, 100);
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
                    } catch (IOException ioe) {
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
            /**
             * 마을세이프티(780,238)
             */
//            rgb = bitmap.getPixel(AndroidUtils.DPFromPixel(780,getApplicationContext()), AndroidUtils.DPFromPixel(238,getApplicationContext())); //원하는 좌표값 입력
//            A = Color.alpha(rgb); //alpha값 추출
//            R = Color.red(rgb); //red값 추출
//            G = Color.green(rgb); //green값 추출
//            B = Color.blue(rgb); //blue값 추출
            if(rank==1) {
                if(flag_1==0) {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
                    setLog(file_name);
                } else {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
                }
                flag_1++;
            } else if(rank==2) {
                if(flag_2==0) {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
                    setLog(file_name);
                } else {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
                }
                flag_2++;
            } else if(rank==3) {
                if(flag_3==0) {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
                    setLog(file_name);
                } else {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
                }
                flag_3++;
            } else if(rank==4) {
//                if(R==31&&G==138&&B==236) {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
//                } else {
//                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
//                    setLog(file_name);
//                }
                if(flag_4==0) {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환~");
                    setLog(file_name);
                } else {
                    Log.d("LinageMR", file_name+" "+"===>>> 귀환초기화대기중~");
                }
                flag_4++;
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

    private String ip = "172.30.1.6"; // IP
    private int port = 9999; // PORT번호

    private Socket socket;

    private BufferedWriter networkWriter;

    public void setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void startSoket(final String file_name) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String ip = ((EditText)findViewById(R.id.soket_ip)).getText().toString();
                    if(!"".equals(ip)) {
                        setSocket(ip, port);
                        PrintWriter out = new PrintWriter(networkWriter, true);
                        String return_msg = file_name;
                        out.println(return_msg);
                        closeSoket();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }

    public void closeSoket() {
        try {
            if(socket!=null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void soket() {
        //자동 close
        try(Socket client = new Socket()){
            //클라이언트 초기화
            InetSocketAddress ipep = new InetSocketAddress("172.30.1.6", 9999);
            //접속
            client.connect(ipep);

            //send,reciever 스트림 받아오기
            //자동 close
            try(OutputStream sender = client.getOutputStream();
                InputStream receiver = client.getInputStream();){
                //서버로부터 데이터 받기
                //11byte
                byte[] data = new byte[11];
                receiver.read(data,0,11);

                //수신메시지 출력
                String message = new String(data);
                String out = String.format("recieve - %s", message);
                System.out.println(out);

                //서버로 데이터 보내기
                //2byte
                message = "OK";
                data = message.getBytes();
                sender.write(data, 0, data.length);
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private long flag_1 = 0;
    private long flag_2 = 0;
    private long flag_3 = 0;
    private long flag_4 = 0;
    private long id = 0;

    public void setLog(String file_name) {
        id++;
        Log.d("LinageMR", file_name+" "+"adb shell input tap 750 650::id::" + id+" app_log");
        AndroidUtils.writeFile("LinageMR adb shell input tap 750 650::id::" + id, file_name);

        startSoket(file_name);
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
            startSoket("file_name");
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
                } catch (RemoteException e) {
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
            }
            catch (RemoteException e) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceMessenger = null;
            mIsBound = false;
        }
    };

}

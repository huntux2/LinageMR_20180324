package mr.linage.com.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by YDH on 2018-03-20.
 */

public class AndroidUtils {

    private static final float DEFAULT_HDIP_DENSITY_SCALE = 1.5f;

    /**
     * 픽셀단위를 현재 디스플레이 화면에 비례한 크기로 반환합니다.
     *
     * @param pixel 픽셀
     * @return 변환된 값 (DP)
     */
    public static int DPFromPixel(int pixel, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pixel / DEFAULT_HDIP_DENSITY_SCALE * scale);
    }

    /**
     * 현재 디스플레이 화면에 비례한 DP단위를 픽셀 크기로 반환합니다.
     *
     * @param DP 픽셀
     * @return 변환된 값 (pixel)
     */
    public static int PixelFromDP(int DP, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (DP / scale * DEFAULT_HDIP_DENSITY_SCALE);
    }

    /**
     * 서비스 실행 확인
     * @param context
     * @return
     */
    public static boolean isServiceRunningCheck(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("mr.linage.com.linagemr.service.MainService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * SDCARD에 로그 파일 생성
     * @param msg
     * @return
     */
    public static boolean writeFile(String msg) {
        boolean flag = false;
        String filename = "app_log.txt";
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(sdPath);
        file.mkdirs();
        sdPath += "/" + filename;
        file = new File(sdPath);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(msg);
            bw.flush();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("AndroidUtils", "writeFile::" + msg);
        return flag;
    }

    /**
     * SDCARD에 로그 파일 생성
     * @param msg
     * @return
     */
    public static boolean writeFile(String msg, String name) {
        boolean flag = false;
        String filename = name+".txt";
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(sdPath);
        file.mkdirs();
        sdPath += "/" + filename;
        file = new File(sdPath);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(msg);
            bw.flush();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("AndroidUtils", "writeFile::" + msg);
        return flag;
    }

    /**
     * 지정한 패스의 파일을 읽어서 Bitmap을 리턴 (화면사이즈에 최다한 맞춰서 리스케일한다.)
     *
     * @param context
     * application context
     * @param imgFilePath
     * bitmap file path
     * @return Bitmap
     * @throws IOException
     */
    public static Bitmap loadBackgroundBitmap(Context context, String imgFilePath) throws Exception, OutOfMemoryError {
        File f = new File(imgFilePath);
        if (!f.exists()) {
            throw new FileNotFoundException("background-image file not found : " + imgFilePath);
        }
        // 폰의 화면 사이즈를 구한다.
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();
        // 읽어들일 이미지의 사이즈를 구한다.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgFilePath, options);
        // 화면 사이즈에 가장 근접하는 이미지의 리스케일 사이즈를 구한다.
        // 리스케일의 사이즈는 짝수로 지정한다. (이미지 손실을 최소화하기 위함.)
        float widthScale = options.outWidth / displayWidth;
        float heightScale = options.outHeight / displayHeight;
        float scale = widthScale > heightScale ? widthScale : heightScale;

        if(scale >= 8) {
            options.inSampleSize = 8;
        } else if(scale >= 6) {
            options.inSampleSize = 6;
        } else if(scale >= 4) {
            options.inSampleSize = 4;
        } else if(scale >= 2) {
            options.inSampleSize = 2;
        } else {
//            2018. 4. 8. ukzzang :: 안드로이드 이미지 파일 Bitmap으로 읽기 (화면 사이즈에 맞게 리스케일)
//            http://ukzzang.tistory.com/62 2/2
            options.inSampleSize = 1;
        }
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imgFilePath, options);
    }

}

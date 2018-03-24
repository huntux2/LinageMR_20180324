package mr.linage.com.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
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

}

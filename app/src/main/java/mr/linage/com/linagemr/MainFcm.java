package mr.linage.com.linagemr;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import mr.linage.com.R;

public class MainFcm extends Activity {

    private static final String TAG = "MainFcm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
//        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkVerify(0);
        } else {
            startApp();
        }
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        Log.d(TAG, "onRestart");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkVerify(1);
        } else {
            startApp();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        switch (requestCode) {
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        // Do something with overlay permission
//                        checkVerify(0);
                    } else {
                        // Show dialog which persuades that we need permission
                        checkVerify(0);
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == 1) {
            Log.d(TAG, "requestCode");
            if (grantResults.length > 0) {
                Log.d(TAG, "grantResults");
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        // 하나라도 거부한다면.
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                            .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(intent);
                            }
                        }).setCancelable(false).show();
                        return;
                    }
                }
                startApp();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify(int start_code) {
        Log.d(TAG, "checkVerify");
        if (!Settings.canDrawOverlays(this)) {
            if (start_code == 0) {
                new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                        .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 1);
                    }
                }).setCancelable(false).show();
            }
        } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // ...
            }
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            startApp();
        }
    }

    private void startApp () {
        Log.d(TAG, "startApp");
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        File videoFile = new File(Environment.getExternalStorageDirectory() + "/screenrecord-sample.mp4");
//        if(videoFile.exists()) {
//            Log.d(TAG,"search3:files.setDataSource");
//            retriever.setDataSource(videoFile.toString());
//            Log.d(TAG,"search3:files.getFrameAtTime");
//            Bitmap bitmap = retriever.getFrameAtTime(1000);
//            Log.d(TAG,"search3:bitmap:"+bitmap);
//        }
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        if (!"".equals(refreshedToken)) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("token", refreshedToken);
                    FirebaseDatabase.getInstance().getReference("users").child("01084092025").updateChildren(childUpdates);
                    Log.d(TAG, "FirebaseDatabase token update: " + refreshedToken);
                }
            });
        }
        setNoti(0);
        setNoti(1);
//        finish();
    }

    private void setNoti(int id) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.noti_test);
        RemoteViews contentBigView = new RemoteViews(getPackageName(), R.layout.noti_test_big);

        CharSequence tickerText = "Shortcuts";
        long when = System.currentTimeMillis();
        @SuppressWarnings("deprecation")
        Notification notification = new Notification(R.drawable.ic_launcher_background, tickerText, when);
//        notification.defaults |= Notification.DEFAULT_SOUND;
//        notification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
//        mBuilder.setContentTitle("제목");
//        mBuilder.setContentText("테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스");
//        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스 테스트 노티 커스텀 테스트세트스"));
//        mBuilder.addAction(R.drawable.ic_launcher_background, "재실행", null);
//        mBuilder.addAction(R.drawable.ic_launcher_background, "취소", null);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setSmallIcon(R.drawable.ic_launcher_background);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
//            notificationChannel.setDescription("channel description");
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.GREEN);
//            notificationChannel.enableVibration(true);
//            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
//            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);
            mBuilder = new NotificationCompat.Builder(this, notificationChannel.getId());
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setSmallIcon(R.drawable.ic_launcher_background);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBuilder.setCustomContentView(contentView);
            if (contentBigView != null) {

                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                bigText.bigText("빅테스트 내용");
                bigText.setBigContentTitle("빅테스트 제목");
                bigText.setSummaryText(getResources().getString(R.string.app_name));
                mBuilder.setStyle(bigText);
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                mBuilder.setShowWhen(true);

                mBuilder.setCustomBigContentView(contentBigView);
            }
            notification = mBuilder.build();
        } else {
            notification.contentView = contentView;
            if (contentBigView != null) {
                notification.bigContentView = contentBigView;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        mNotificationManager.notify(id, notification);

//        NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//        mNotificationManager.notify(0, mBuilder.build());
    }
}
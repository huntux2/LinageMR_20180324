package mr.linage.com.linagemr;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

public class ColorCheckActivity extends Activity {

    private static final String TAG = "ColorCheckActivity";

    private Intent service_intent = null;
    int permission = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkVerify();
        } else {
            startApp();
        }
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        Log.d(TAG, "onRestart");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkVerify();
        } else {
            startApp();
        }
        permission = 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode:"+requestCode);
        switch (requestCode) {
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d(TAG, "onActivityResult 1");
                    if (!Settings.canDrawOverlays(this)) {
                        Log.d(TAG, "onActivityResult 2");
                        // Do something with overlay permission
//                        checkVerify();
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
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, 1);
                                permission = 1;
                            }
                        }).setCancelable(false).show();
                    } else {
                        Log.d(TAG, "onActivityResult 3");
                        // Show dialog which persuades that we need permission
                        checkVerify();
                    }
                }
                break;
            case 111:
                Log.d(TAG, "onActivityResult 3");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    boolean isWhiteListing = pm.isIgnoringBatteryOptimizations(getPackageName());
                    if(!isWhiteListing) {
                        finish();
                    } else {
                        checkVerify();
                    }
                }
                break;
        }
        Log.d(TAG, "onActivityResult 4");
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
                checkVerify();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isWhiteListing = pm.isIgnoringBatteryOptimizations(getPackageName());
        Log.d(TAG, "checkVerify isWhiteListing:"+isWhiteListing);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // ...
            }
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else if(!isWhiteListing) {
            AlertDialog.Builder setdialog = new AlertDialog.Builder(ColorCheckActivity.this);
            setdialog.setTitle("권한이 필요합니다.")
                    .setMessage("어플을 사용하기 위해서는 해당 어플을 \"배터리 사용량 최적화\" 목록에서 제외하는 권한이 필요합니다. 계속하시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent  = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:"+ getPackageName()));
                            startActivityForResult (intent,111);
                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create()
                    .show();
        } else if (!Settings.canDrawOverlays(this)) {
            if (permission == 0) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
                permission = 1;
            }
        } else {
            startApp();
        }
    }

    private void startApp() {
//        try {unbindService(mConnection);} catch (Exception e) {};
//        if() {
//
//        }
//        bindService(, mConnection, Context.BIND_AUTO_CREATE);
        Toast.makeText(this,"YDH 편한 세상 연동",Toast.LENGTH_SHORT).show();
        finish();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("test","onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("test","onServiceDisconnected");
        }
    };
}
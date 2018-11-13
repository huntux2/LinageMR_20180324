package mr.linage.com.linagemr;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

public class MainFcm extends Activity {

    private static final String TAG = "MainFcm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_main_fcm);*/
        FirebaseInstanceId.getInstance().getToken();
        if (FirebaseInstanceId.getInstance().getToken() != null) {
            Log.d(TAG, "token = " + FirebaseInstanceId.getInstance().getToken());
        }
        finish();
    }
}

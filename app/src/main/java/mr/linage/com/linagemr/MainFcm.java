package mr.linage.com.linagemr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;

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
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        File videoFile = new File(Environment.getExternalStorageDirectory() + "/screenrecord-sample.mp4");
        if(videoFile.exists()) {
            Log.d(TAG,"search3:files.setDataSource");
            retriever.setDataSource(this, Uri.parse(videoFile.toString()));
            Log.d(TAG,"search3:files.getFrameAtTime");
            Bitmap bitmap = retriever.getFrameAtTime();
            Log.d(TAG,"search3:bitmap:"+bitmap);
        }
        finish();
    }
}

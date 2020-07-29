package coop.tecso.hcd.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

public class MainActivity extends Activity {

    private static final String[] permissions = new String[]{
        Manifest.permission.WRITE_SETTINGS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CALL_PHONE
    };

    // MARK: - Life Cycle

    @Override
    protected void onResume() {
        super.onResume();

        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 1; i < grantResults.length; i++) {
            int status = grantResults[i];
            if (status != PackageManager.PERMISSION_GRANTED) {
                finish();
                return;
            }
        }

        this.goMainActivity();
    }

    // MARK: - Internal

    private void goMainActivity() {
        Intent intent = new Intent(this, MainHCActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        this.finish();
    }

}

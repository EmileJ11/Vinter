package be.kuleuven.vinter.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.concurrent.TimeUnit;

import be.kuleuven.vinter.R;

public class ScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Use volume up key for flash");
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setCaptureActivity(Capture.class);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult.getContents() != null) {
            String urlQR = intentResult.getContents().toString();

            if (urlQR.startsWith("https://") || urlQR.startsWith("http://")) {
                Uri uri = Uri.parse(urlQR);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
            else{
                Toast.makeText(ScannerActivity.this, "Invalid Url", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent = new Intent(this, ScannerActivity.class);
                startActivity(intent);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "You didn't scan anything", Toast.LENGTH_SHORT).show();
        }
    }
}
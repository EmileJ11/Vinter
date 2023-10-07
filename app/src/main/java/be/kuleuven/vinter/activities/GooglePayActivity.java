package be.kuleuven.vinter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.databinding.ActivityGooglePayBinding;

public class GooglePayActivity extends AppCompatActivity {

    private ActivityGooglePayBinding binding;
    private static final String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.walletnfcrel";
    private int GOOGLE_PAY_REQUEST_CODE = 123;
    private String amount, name ="Emile Schockaert", upiId ="emile.schockaert@gmail.com", transactionNote ="pay test", status;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGooglePayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.googlePayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = binding.amountEdit.getText().toString();
                if (!amount.isEmpty()) {
                    uri = getUpiPaymentUri(name, upiId, transactionNote, amount);
                    payWithGooglePay();
                }
                else {
                    binding.amountEdit.setError("Amount is required");
                    binding.amountEdit.requestFocus();
                }

            }
        });

    }
     /* On bought do this:
     Toast.makeText(BuyActivity.this, "Congratulations, you bought a shoe! See in myOrders", Toast.LENGTH_SHORT).show();
    MainActivity.rowItems.remove(0);

    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    startActivity(intent);
    */

    private void payWithGooglePay() {
        if (isAppInstalled(GooglePayActivity.this, GOOGLE_PAY_PACKAGE_NAME)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
            startActivityForResult(intent,GOOGLE_PAY_REQUEST_CODE);
        }
        else {
            Toast.makeText(this, "Please first install Google Pay", Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            status = data.getStringExtra("Status").toLowerCase();
        }
        if ((RESULT_OK == resultCode) && (status.equals("success"))) {
            Toast.makeText(this, "Transaction successful!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Transaction failed!", Toast.LENGTH_SHORT).show();

        }
    }

    private static boolean isAppInstalled(Context ct, String packageName) {
//        try {
//            ct.getPackageManager().getPackageInfo(packageName, 0);
//            return true;
//        }
//        catch (PackageManager.NameNotFoundException e) {
//            return false;
//        }
        PackageManager pm = ct.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Tag",packageName + " not installed");
            return false;
        }

    }

    private static Uri getUpiPaymentUri(String name, String upiId, String transactionNote, String amount) {
        return new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", transactionNote)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

    }
}
package be.kuleuven.vinter.activities;

import androidx.annotation.RequiresApi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import be.kuleuven.vinter.R;

public class BuyActivity extends CardChangedChecker {

    private EditText myStreet, myNumber, myCity, myCode;
    private Button buyButton;
    private TextView myPrice, myTotalPrice;
    private ImageView buyShoeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myPrice = findViewById(R.id.price);
        myPrice.setText("€" +  pc.getPrice());
        myTotalPrice = findViewById(R.id.total);
        myTotalPrice.setText("€" + (pc.getPrice() + 5));
        buyShoeImage = findViewById(R.id.buyShoeImage);
        buyShoeImage.setImageBitmap(pc.getSpecImage());

        myStreet = findViewById(R.id.street);
        myNumber = findViewById(R.id.streetNumber);
        myCity = findViewById(R.id.city);
        myCode = findViewById(R.id.postalCode);
        buyButton = findViewById(R.id.buy);

        buyButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(myStreet.getText().toString())
                        || TextUtils.isEmpty(myNumber.getText().toString())
                        ||TextUtils.isEmpty(myCity.getText().toString())
                        ||TextUtils.isEmpty(myCode.getText().toString()))
                {
                    Toast.makeText(BuyActivity.this, "No empty fields allowed!", Toast.LENGTH_SHORT).show();
                }
                else {
                    getDateTimeOfLatestChange();
                }
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_buy;
    }

    @Override
    public void noChangeInPC() {
        //Intent intent = new Intent(BuyActivity.this, GooglePayActivity.class);
        buyProductInDB();
    }

    @Override
    protected void pcIsReloaded() {}

    @Override
    protected void pcImagesReloaded() {
        finish();
        Intent intent = new Intent(BuyActivity.this, InfoActivity.class);
        intent.putExtra("ProductCard", pc);
        startActivity(intent);
    }

    protected String getRequestURL(String profileid){
        StringBuilder sb = new StringBuilder(profileid);
        sb.append("/" + pc.getProductId());
        sb.append("/" + myStreet.getText());
        sb.append("/" + myNumber.getText());
        sb.append("/" + myCity.getText());
        sb.append("/" + myCode.getText());
        System.out.println(sb);
        return sb.toString();
    }

    private void buyProductInDB(){
        SharedPreferences prfs = getSharedPreferences("LoginValue", Context.MODE_PRIVATE);
        String profileid = prfs.getString("id", "");

        String requestURL = "https://studev.groept.be/api/a21pt311/buyProduct/" + getRequestURL(profileid);
        System.out.println(requestURL);
        StringRequest postOrderRequest = new StringRequest (Request.Method.GET, requestURL,  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(BuyActivity.this, "Congratulations, you bought a shoe! See in myOrders", Toast.LENGTH_SHORT).show();

                if (MainActivity.rowItems.get(0).equals(pc)) // because a shoe can be bought from the shortlistActivity,
                    // so then it shouldn't be deleted from the list op pc's in the mainActivity
                    // see equals() method in ProductCard; only compares the productId's
                {
                    MainActivity.rowItems.remove(0);
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(BuyActivity.this, "Order request failed", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(postOrderRequest);
    }

    public void goBack(View view) {
        finish();
    }
}
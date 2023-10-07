package be.kuleuven.vinter.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import be.kuleuven.vinter.activities.MainActivity;
import be.kuleuven.vinter.models.ProductCard;

public abstract class CardChangedChecker extends AppCompatActivity {
    protected ProductCard pc;
    protected RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        requestQueue = Volley.newRequestQueue(this);

        pc = (ProductCard) getIntent().getExtras().getParcelable("ProductCard");
    }

    protected abstract int getLayoutResourceId();


    protected void getDateTimeOfLatestChange() {
        String GET_REVIEWS_URL = "https://studev.groept.be/api/a21pt311/getDateTimeOfLatestChange/" + pc.getProductId();
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_REVIEWS_URL, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(0);
                            if (o.getString("productId").equals("null")) // Otherwise it's already bought by someone else after the products were loaded in
                            {
                                String dateTime = o.getString("dateTimeOfChange");
                                if (!dateTime.equals("null")) {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    compareDateTimes(LocalDateTime.parse(dateTime, formatter));
                                } else {
                                    noChangeInPC();
                                }
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Product aleady bought by someone else.", Toast.LENGTH_SHORT).show();
                                if (MainActivity.rowItems.get(0).equals(pc)) // because a shoe can be bought from the shortlistActivity,
                                // so then it shouldn't be deleted from the list op pc's in the mainActivity
                                // see equals() method in ProductCard; only compares the productId's
                                {
                                    MainActivity.rowItems.remove(0);
                                }

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                //finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );
        requestQueue.add(retrieveImageRequest);
    }

    public abstract void noChangeInPC();

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void compareDateTimes(LocalDateTime dateTimeOfLatestChange){
        LocalDateTime dateTimeOfLoadingIn = pc.getDateTimeOfLoadingIn();
        boolean shouldRefresh = dateTimeOfLoadingIn.isBefore(dateTimeOfLatestChange);
        if (shouldRefresh)
        {
            reloadInfoActivity();
        }
        else{
            noChangeInPC();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void reloadInfoActivity()
    {
        pc.setDateTimeOfLoadingIn(LocalDateTime.now());

        getChangedDataOfProduct();
        Toast.makeText(getApplicationContext(), "Data has changed, Reloaded!", Toast.LENGTH_SHORT).show();
    }

    public void getChangedDataOfProduct() {
        final String GET_PRODUCTS_URL = "https://studev.groept.be/api/a21pt311/getDataOfProduct/"+ pc.getProductId();
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_PRODUCTS_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(0);
                            pc = new ProductCard(o);
                            pcIsReloaded();
                            getPicturesForProduct();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );

        requestQueue.add(retrieveImageRequest);
    }

    protected abstract void pcIsReloaded();

    private void getPicturesForProduct() {
        pc.clearImages();
        final String GET_IMAGE_URL = "https://studev.groept.be/api/a21pt311/getImages/" + pc.getProductId();
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_IMAGE_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //Check if the DB actually contains an image
                            for (int i=0; i < response.length(); i++) {
                                JSONObject o = response.getJSONObject(i);

                                //converting base64 string to image
                                String b64String = o.getString("image");
                                byte[] imageBytes = Base64.decode(b64String, Base64.DEFAULT);
                                Bitmap bitmap2 = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                                //Link the bitmap to the ImageView, so it's visible on screen
                                pc.addImage(bitmap2);

                            }
                            pcImagesReloaded();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Unable to communicate with server", Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(retrieveImageRequest);
    }

    protected abstract void pcImagesReloaded();
}

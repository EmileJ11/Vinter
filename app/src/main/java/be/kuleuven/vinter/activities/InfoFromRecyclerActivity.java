package be.kuleuven.vinter.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.models.ProductCard;

public class InfoFromRecyclerActivity extends InfoActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOtherPicturesFromProduct(pc);

        disableBuyButton();
    }

    protected void disableBuyButton(){ // bcs it depends if we arrive from shortlist(buy enabled) or the others (buy disabled)
        // disable buy button
        if (!getIntent().getExtras().getBoolean("buyEnabled")) // returns false if not assigned in the other adapters
        {
            button.setVisibility(View.GONE);
        }
    }

    private void getOtherPicturesFromProduct(ProductCard pc) {
        final String GET_IMAGE_URL = "https://studev.groept.be/api/a21pt311/getOtherImages/" + pc.getProductId()+"/"+pc.getProductId();
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_IMAGE_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i=0; i < response.length(); i++) {
                                JSONObject o = response.getJSONObject(i);

                                //converting base64 string to image
                                String b64String = o.getString("image");
                                byte[] imageBytes = Base64.decode(b64String, Base64.DEFAULT);
                                Bitmap bitmap2 = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                                //Link the bitmap to the ImageView, so it's visible on screen
                                pc.addImage(bitmap2);
                                imageAdapter.notifyDataSetChanged();
                            }
                            prepareDots(pc.getImageIndex(), imageAdapter.getCount());

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
}

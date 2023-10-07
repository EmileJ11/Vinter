package be.kuleuven.vinter.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.activities.ShortlistActivity;
import be.kuleuven.vinter.models.MyAdapter;
import be.kuleuven.vinter.models.ProductCard;

public abstract class BasePCListActivityManager extends AppCompatActivity {

    protected RecyclerView recyclerView;
    protected MyAdapter myAdapter;

    protected RequestQueue requestQueue;

    protected ArrayList<ProductCard> productCards;

    protected TextView emptyNotifier; // TextView to show if adapter is empty

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        productCards = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
    }

    protected abstract int getLayoutResourceId();


    protected void getListedProducts(String GET_PRODUCTS_URL) {
        JsonArrayRequest retrieveProductsRequest = new JsonArrayRequest(Request.Method.GET, GET_PRODUCTS_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        extracted(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );

        requestQueue.add(retrieveProductsRequest);
    }

    protected void extracted(JSONArray response) {
        for (int i = 0; i<response.length(); ++i) {
            JSONObject o = null;
            try {
                o = response.getJSONObject(i);
                ProductCard pc = new ProductCard(o);
                productCards.add(pc);
                myAdapter.notifyItemInserted(i);
                getFirstPictureForProduct(pc, myAdapter, i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setEmptyNotifier(productCards.isEmpty(), emptyNotifier);
    }

    protected boolean setEmptyNotifier(boolean isListEmpty, TextView specEmptyNotifier){
        if (isListEmpty)
        {
            specEmptyNotifier.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    protected void getFirstPictureForProduct(ProductCard pc, MyAdapter specificAdapter, int position) {
        final String GET_IMAGE_URL = "https://studev.groept.be/api/a21pt311/getOnlyFirstImage/" + pc.getProductId();
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_IMAGE_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //Check if the DB actually contains an image
                            JSONObject o = response.getJSONObject(0); // Only the FIRST Image loaded

                            //converting base64 string to image
                            String b64String = o.getString("image");
                            byte[] imageBytes = Base64.decode(b64String, Base64.DEFAULT);
                            Bitmap bitmap2 = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                            //Link the bitmap to the ImageView, so it's visible on screen
                            pc.addImage(bitmap2);
                            specificAdapter.notifyItemChanged(position);

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

/*    protected void notifiyDSChanged(MyAdapter specificAdapter, int position){
        specificAdapter.notifyItemChanged(position);
    }*/
}

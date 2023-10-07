package be.kuleuven.vinter.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.models.ProductCard;

public class ChangeProductActivity extends AddActivity{
    private ProductCard pc;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bottomNavigationView.setVisibility(View.GONE);
        pc = (ProductCard) getIntent().getExtras().getParcelable("ProductCard");
        getOtherPicturesFromProduct(pc);
        fillForm(pc);
        myButton.setText("Change Product");
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProduct(pc.getProductId());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void fillForm(ProductCard pc){
        myBrand.setText(pc.getBrand());
        myModel.setText(pc.getName());
        mySize.setText(Integer.toString(pc.getSize()));
        myPrice.setText(Integer.toString(pc.getPrice()));
        myColor.setSelection(getIndex(myColor, pc.getColor()));
        myGender.setSelection(getIndex(myGender, pc.getGender()));
        myCategory.setSelection(getIndex(myCategory, pc.getCategory()));

        imageArray = pc.getImages();
        pictureIndex = imageArray.size();
        setImages();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }

    private void changeProduct(int productid){
        // update product
        // Delete images for product
        // insert images for product

        String requestURLToUpdateProduct = "https://studev.groept.be/api/a21pt311/updateProduct/" + getRequestURL(Integer.toString(productid));
        JsonArrayRequest getURLrequest = new JsonArrayRequest(Request.Method.GET, requestURLToUpdateProduct, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        deletePictures(productid);
                        informChangedProduct(productid);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ChangeProductActivity.this, "Unable to communicate with server", Toast.LENGTH_LONG).show();
                    }
                }
        );
        requestQueue.add(getURLrequest);
    }

    private void informChangedProduct(int productid){ // Set the datetime to now
        String requestURLToUpdateProduct = "https://studev.groept.be/api/a21pt311/changeProduct/" + productid;
        JsonArrayRequest getURLrequest = new JsonArrayRequest(Request.Method.GET, requestURLToUpdateProduct, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ChangeProductActivity.this, "Unable to communicate with server", Toast.LENGTH_LONG).show();
                    }
                }
        );
        requestQueue.add(getURLrequest);
    }


    private void deletePictures(int productid){
        String requestURLToUpdateProduct = "https://studev.groept.be/api/a21pt311/deleteImagesOfProduct/" + productid;
        JsonArrayRequest getURLrequest = new JsonArrayRequest(Request.Method.GET, requestURLToUpdateProduct, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i=0; i<imageArray.size(); i++){
                            Bitmap bm= imageArray.get(i);
                            postPictures(bm, Integer.toString(productid), i == (imageArray.size() - 1));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ChangeProductActivity.this, "Unable to communicate with server", Toast.LENGTH_LONG).show();
                    }
                }
        );
        requestQueue.add(getURLrequest);
    }

    @Override
    protected void resetPage(){
        Intent intent = new Intent(ChangeProductActivity.this, MyShoesActivity.class); //go back to myshoes (refreshed)
        startActivity(intent);
    }

    private void getOtherPicturesFromProduct(ProductCard pc) {
        final String GET_IMAGE_URL = "https://studev.groept.be/api/a21pt311/getOtherImages/" + pc.getProductId() +"/"+ + pc.getProductId();
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
                                //pc.addImage(bitmap2);
                                imageArray.add(bitmap2);
                                setImages();
                                pictureIndex ++;
                            }

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

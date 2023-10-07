package be.kuleuven.vinter.activities;

import static java.lang.Integer.parseInt;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.kuleuven.vinter.R;

public class AddActivity extends AppCompatActivity {

    protected RequestQueue requestQueue;
    private static final String POST_Product_URL = "https://studev.groept.be/api/a21pt311/insertProduct/";
    private static final String GET_ProductId_URL = "https://studev.groept.be/api/a21pt311/getNewestProductFromPerson/";
    private static final String POST_Image_URL = "https://studev.groept.be/api/a21pt311/insertImage";

    protected ImageView myImageView;
    protected ImageView myImageView2;
    protected ImageView myImageView3;
    protected ImageView myImageView4;
    protected ImageView myImageView5;
    protected ImageView myImageView6;
    protected EditText myBrand;
    protected EditText myModel;
    protected Spinner myColor;
    protected Spinner myGender;
    protected Spinner myCategory;
    protected EditText mySize;
    protected Button myButton;
    protected EditText myPrice;

    protected BottomNavigationView bottomNavigationView;

    protected List<Bitmap> imageArray;
    protected ImageView[] imageViewArray;
    protected int pictureIndex;

    private ActivityResultLauncher activityResultLauncher;
    private boolean isCamera;
    public static final int MY_CAMERA_REQUEST_CODE = 100;

    protected ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        checkCameraPermission();

        myImageView = findViewById(R.id.shoePicture);
        myImageView2 = findViewById(R.id.shoePicture2);
        myImageView3 = findViewById(R.id.shoePicture3);
        myImageView4 = findViewById(R.id.shoePicture4);
        myImageView5 = findViewById(R.id.shoePicture5);
        myImageView6 = findViewById(R.id.shoePicture6);

        myBrand = findViewById(R.id.brand);
        myModel = findViewById(R.id.model);
        myColor = findViewById(R.id.color);
        mySize = findViewById(R.id.size);
        myPrice = findViewById(R.id.price);
        myButton = findViewById(R.id.submit);
        myGender = findViewById(R.id.gender);
        myCategory = findViewById(R.id.category);

        progressDialog = new ProgressDialog(this);

        //bottomNavView
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.addNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.homeNav:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    ///bottomNavigationView.setSelectedItemId(R.id.homeNav);
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.shortlistNav:
                    startActivity(new Intent(getApplicationContext(), ShortlistActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.addNav:
                    return true;
                case R.id.messageNav:
                    startActivity(new Intent(getApplicationContext(), MyShoesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.profileNav:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
        //bottomNavView

        myButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(myBrand.getText().toString())
                        || TextUtils.isEmpty(myModel.getText().toString())
                        || TextUtils.isEmpty(mySize.getText().toString())
                        || TextUtils.isEmpty(myPrice.getText().toString())
                        || TextUtils.equals(myCategory.getSelectedItem().toString(), "Choose category")
                        || TextUtils.equals(myColor.getSelectedItem().toString(), "Choose color")
                        || TextUtils.equals(myGender.getSelectedItem().toString(), "Choose gender")
                        || imageArray.size()==0
                ){
                    Toast.makeText(AddActivity.this, "Fill all info in, pls!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (parseInt(myPrice.getText().toString()) > 500
                            || parseInt(mySize.getText().toString()) > 50) {
                        Toast.makeText(AddActivity.this, "Price max 500 and size max 50", Toast.LENGTH_SHORT).show();
                    }
                    else {// Submit

                        // db stuff
                        onBtnPostClicked();
                    }
                    // Verander misschien dat we van de ingegeven data een ProductCard object maken en de getURL method daarin zetten
                    }
                }
            });

        requestQueue = Volley.newRequestQueue(this);
        imageArray = new ArrayList<>();
        imageViewArray = new ImageView[]{myImageView, myImageView2, myImageView3, myImageView4, myImageView5, myImageView6};


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> { // new ActivityResultCallback<ActivityResult>() @Override public void onActivityResult(ActivityResult result)
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap;
                    if (isCamera) {
                        imageBitmap = (Bitmap) extras.get("data");
                        imageBitmap = getResizedBitmap( imageBitmap, 400 );
                    }
                    else {
                        Uri filepath = result.getData().getData();
                        try {
                            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filepath);
                            imageBitmap = getResizedBitmap( imageBitmap, 400 );
                        }
                        catch (IOException IOe){
                            return;
                        }
                    }
                    imageArray.add(pictureIndex, imageBitmap);
                    setImages();
                    pictureIndex ++;
                }
            });
    }

    protected String getRequestURL(String profileid){
        String brand = myBrand.getText().toString();//.replaceAll(" ", "_");
        String model = myModel.getText().toString();//.replaceAll(" ", "_");

        StringBuilder sb = new StringBuilder(brand);
        sb.append("/" + model);
        sb.append("/" + mySize.getText());
        sb.append("/" + myPrice.getText());
        sb.append("/" + myGender.getSelectedItem());
        sb.append("/" + myColor.getSelectedItem());
        sb.append("/" + myCategory.getSelectedItem());
        sb.append("/" + profileid);

        return sb.toString();
    }


    private void onBtnPostClicked()
    {
        //Start an animating progress widget
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.show();

        SharedPreferences prfs = getSharedPreferences("LoginValue", Context.MODE_PRIVATE);
        String profileid = prfs.getString("id", "");

        String requestURL = POST_Product_URL + getRequestURL(profileid);
        System.out.println(requestURL);
        StringRequest postProductRequest = new StringRequest (Request.Method.GET, requestURL,  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("inresponse: " + requestURL);
                getIdOfProduct(profileid);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(AddActivity.this, "Product post request failed", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(postProductRequest);
    }

    private void getIdOfProduct(String profileid){
        String requestURLForIdProduct = GET_ProductId_URL + profileid + "/" + profileid;
        System.out.println(requestURLForIdProduct);
        JsonArrayRequest getURLrequest = new JsonArrayRequest(Request.Method.GET, requestURLForIdProduct, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject o = response.getJSONObject(0);
                            String id = o.getString("idProduct");
                            for (int i=0; i<imageArray.size(); i++){
                                Bitmap bm= imageArray.get(i);
                                postPictures(bm, id, i == (imageArray.size() - 1));
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(AddActivity.this, "Unable to communicate with server", Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(getURLrequest);
    }

    /**
     * Submits a new image to the database
     */
    protected void postPictures(Bitmap bitmap, String id, boolean isFinal){
        //convert image to base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        //Execute the Volley call. Note that we are not appending the image string to the URL, that happens further below
        StringRequest postImageRequest = new StringRequest (Request.Method.POST, POST_Image_URL,  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Turn the progress widget off
                if (isFinal){
                progressDialog.dismiss();
                    Toast.makeText(AddActivity.this, "Shoe submitted", Toast.LENGTH_SHORT).show();
                    resetPage();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(AddActivity.this, "Image post request failed", Toast.LENGTH_LONG).show();
            }
        }) { //NOTE THIS PART: here we are passing the parameter to the webservice, NOT in the URL!
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("image", imageString); // Vraag of dit met meerdere images tergelijk kan !!!!!!
                params.put("id", id);
                return params;
            }
        };

        requestQueue.add(postImageRequest);
    }

    protected void resetPage(){
        myBrand.setText("");
        myModel.setText("");
        myPrice.setText("");
        myCategory.setSelection(0);
        myColor.setSelection(0);
        myGender.setSelection(0);
        mySize.setText("");
        imageArray.clear();
        setImages();
    }

    public void takePicture(View view) {

        int i;
        switch (view.getId()) {
            case R.id.shoePicture:
                i = 0;
                break;
            case R.id.shoePicture2:
                i = 1;
                break;
            case R.id.shoePicture3:
                i = 2;
                break;
            case R.id.shoePicture4:
                i = 3;
                break;
            case R.id.shoePicture5:
                i = 4;
                break;
            case R.id.shoePicture6:
                i = 5;
                break;
            default:
                i = 0;
        }

        if (i>=imageArray.size()) {
            String[] colors = {"Camera", "Gallery"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Content via");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isCamera = (which == 0);
                    if (checkCameraPermission()) {
                        getPicture();
                    }
                }
            });
            builder.show();
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Delete image")
                    .setMessage("Are you sure you want to delete this image?")

                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            imageArray.remove(i);
                            setImages();
                            pictureIndex --;
                        }
                    })

                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }



    private void getPicture(){
            if (isCamera){
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activityResultLauncher.launch(takePictureIntent);}

            else{
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(pickPhoto);
            }
    }

    private boolean checkCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            return false;
        }
        else{
            return true;
        }
    }


    protected void setImages(){
        for(int i=0; i<imageViewArray.length; i++){
            if (i<imageArray.size())
            {
                imageViewArray[i].setImageBitmap(imageArray.get(i));
            }
            else {
                imageViewArray[i].setImageResource(R.drawable.camera);
            }
        }
    }

    /**
     * Helper method to create a rescaled bitmap. You enter a desired width, and the height is scaled uniformly
     */
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scale = ((float) newWidth) / width;

        // We create a matrix to transform the image
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create the new bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

}
package be.kuleuven.vinter.activities;

import static be.kuleuven.vinter.activities.AddActivity.MY_CAMERA_REQUEST_CODE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
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
import java.util.HashMap;
import java.util.Map;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.models.PCProcessing;

public class ProfileActivity extends AppCompatActivity {

    private Button myFeedback;
    private ImageView myProfile;
    private ProgressDialog progressDialog;
    private ActivityResultLauncher activityResultLauncher;
    private boolean isCamera;
    private RequestQueue requestQueue;

    private SharedPreferences loginValues;

    private static Bitmap profilePictureBitmap; // static because it shoudn't be called everytime

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        checkCameraPermission();

        loginValues = getSharedPreferences("LoginValue", Context.MODE_PRIVATE);

        requestQueue = Volley.newRequestQueue(this);

        myProfile = findViewById(R.id.profileImage);

        if (profilePictureBitmap != null)
            {myProfile.setImageBitmap(profilePictureBitmap);}
        else { //if the static value is null aka not yet assigned it should call for the picture
            getProfilePicture();
        }

        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkCameraPermission()) {
                    getOptionsAlert();
                }
                //dispatchTakePictureIntent();
            }
        });

        //bottomNavView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.profileNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.homeNav:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.shortlistNav:
                    startActivity(new Intent(getApplicationContext(),ShortlistActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.addNav:
                    startActivity(new Intent(getApplicationContext(), AddActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.messageNav:
                    startActivity(new Intent(getApplicationContext(), MyShoesActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.profileNav:
                    return true;
            }
            return false;
        });
        //bottomNavView

        Intent rateApp = new Intent(Intent.ACTION_VIEW);

        myFeedback = findViewById(R.id.feedback);
        myFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rateApp.setData(Uri.parse("Paste google play app link here!!!"));
                //startActivity(rateApp);
            }
        });

        //database fixen

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> { // new ActivityResultCallback<ActivityResult>() @Override public void onActivityResult(ActivityResult result)
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap;
                        if (isCamera) {
                            imageBitmap = (Bitmap) extras.get("data");
                            profilePictureBitmap = getResizedBitmap( imageBitmap, 400 );
                        }
                        else {
                            Uri filepath = result.getData().getData();
                            try {
                                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filepath);
                                profilePictureBitmap = getResizedBitmap( imageBitmap, 400 );
                            }
                            catch (IOException IOe){
                                return;
                            }
                        }
                        myProfile.setImageBitmap(profilePictureBitmap);
                        deleteImageFromDB(profilePictureBitmap); // delete the previous one from the DB
                    }
                });

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

    private void getOptionsAlert(){
        String[] options;
        if (profilePictureBitmap != null)
        {
            options = new String[]{"Camera", "Gallery", "Delete"};
        }
        else {
            options = new String[]{"Camera", "Gallery"};
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Content via");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 2){
                    deleteImage();
                }
                else{
                isCamera = (which == 0);
                getPicture();
                }
            }
        });
        builder.show();
    }

    private void getProfilePicture(){
        String GET_REVIEWS_URL = "https://studev.groept.be/api/a21pt311/getProfileImage/" + loginValues.getString("id", "");
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_REVIEWS_URL, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(0);
                            //converting base64 string to image
                            String b64String = o.getString("profilePicture");
                            if (!b64String.equals("null"))
                            {
                                byte[] imageBytes = Base64.decode(b64String, Base64.DEFAULT);
                                profilePictureBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                myProfile.setImageBitmap(profilePictureBitmap);
                            }
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


    private void deleteImage(){
        new AlertDialog.Builder(this)
                .setTitle("Delete profile picture")
                .setMessage("Are you sure you want to delete your profile picture?")

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteImageFromDB(null);
                        profilePictureBitmap = null;
                        myProfile.setImageResource(R.mipmap.ic_launcher);
                    }
                })

                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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

    private void deleteImageFromDB(Bitmap bitmap){
        String GET_REVIEWS_URL = "https://studev.groept.be/api/a21pt311/DeleteProfilePicture/" + loginValues.getString("id", "");
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_REVIEWS_URL, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray response) {
                        if (bitmap != null)
                        {postPictures(bitmap);}
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


    /**
     * Submits a new image to the database
     */
    protected void postPictures(Bitmap bitmap){
        //convert image to base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        //Execute the Volley call. Note that we are not appending the image string to the URL, that happens further below
        StringRequest postImageRequest = new StringRequest (Request.Method.POST, "https://studev.groept.be/api/a21pt311/insertProfileImage",  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Turn the progress widget off
                Toast.makeText(ProfileActivity.this, "Post request executed", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProfileActivity.this, "Image post request failed", Toast.LENGTH_LONG).show();
            }
        }) { //NOTE THIS PART: here we are passing the parameter to the webservice, NOT in the URL!
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("image", imageString);
                params.put("id", loginValues.getString("id", ""));
                return params;
            }
        };

        requestQueue.add(postImageRequest);
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


    public void goToProfileData(View view) {
        Intent intent = new Intent(this, ProfileDataActivity.class);
        intent.putExtra("profileId", Integer.parseInt(loginValues.getString("id", "")));
        intent.putExtra("profileUserName", loginValues.getString("UserName", ""));

        if (profilePictureBitmap != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            profilePictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();
            intent.putExtra("profilePicture", bytes);
        }

        startActivity(intent);
    }

    public void goToShortlist(View view) {
        finish();
        Intent intent = new Intent(this, ShortlistActivity.class);
        startActivity(intent);
    }

    public void goToMyOrders(View view) {
        Intent intent = new Intent(this, MyOrdersActivity.class);
        startActivity(intent);
    }

    public void goToMyShoes(View view) {
        Intent intent = new Intent(this, MyShoesActivity.class);
        startActivity(intent);
    }

    public void goToLogin(View view) { // log out
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        MainActivity.rowItems.clear(); // clear the static fields for the next user
        PCProcessing.productCards.clear();
        profilePictureBitmap = null;
        MainActivity.minSize = "0";
        MainActivity.maxSize = "50";
        MainActivity.minPrice = "0";
        MainActivity.maxPrice = "500";
        MainActivity.prefBrand = null;
        MainActivity.prefGender = null;
        MainActivity.prefColor = null;
        MainActivity.prefCategory = null;
        startActivity(intent);
    }


}
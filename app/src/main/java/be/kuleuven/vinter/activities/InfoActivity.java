package be.kuleuven.vinter.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.models.MyImageAdapter;

public class InfoActivity extends CardChangedChecker{
    private TextView mySellerName;
    private ImageView myProfilePic;
    private TextView myAantalRatings;
    private RatingBar rating;

    private TextView brand;
    private TextView model;
    private TextView size;
    private TextView price;
    private TextView gender;
    private TextView color;
    private TextView category;

    protected Button button;

    protected MyImageAdapter imageAdapter;
    private ViewPager viewPager;

    private LinearLayout dotsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        brand = findViewById(R.id.brand);
        model = findViewById(R.id.model);
        size = findViewById(R.id.size);
        price = findViewById(R.id.price);
        gender = findViewById(R.id.gender);
        color = findViewById(R.id.color);
        category = findViewById(R.id.category);
        button = findViewById(R.id.buyButton);


        rating = findViewById(R.id.ratingBarInfo);
        myProfilePic = findViewById(R.id.sellerProfilePic);
        mySellerName = findViewById(R.id.sellerName);
        myAantalRatings = findViewById(R.id.aantalRatings);

        getInfo(); // get the seller info + reviews
        rating.setEnabled(false);

        viewPager = findViewById(R.id.shoePictures);
        imageAdapter = new MyImageAdapter(this, pc.getImages());
        viewPager.setAdapter(imageAdapter);
        viewPager.setCurrentItem(pc.getImageIndex()); // Open with the image which was in front in mainActivity

        dotsLayout = findViewById(R.id.SliderDots);
        prepareDots(pc.getImageIndex(), imageAdapter.getCount());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                prepareDots(position, imageAdapter.getCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        fillInDate();

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_info;
    }

    private void fillInDate(){
        model.setText(pc.getName());
        price.setText("€" + Integer.toString(pc.getPrice()));

        brand.setText(pc.getBrand());
        size.setText("Size: " + Integer.toString(pc.getSize()));
        gender.setText(pc.getGender());
        color.setText(pc.getColor());
        category.setText(pc.getCategory());
    }

    protected void prepareDots(int currentSlidePosition, int amountOfImages) {
        if (dotsLayout.getChildCount() > 0)
        {
            dotsLayout.removeAllViews(); // the previous ones should be deleted
        }

        ImageView dots[] = new ImageView[amountOfImages];

        for (int i=0; i<amountOfImages; i++)
        {
            dots[i] = new ImageView(this);
            if(i==currentSlidePosition)
            {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dot));
            }
            else{
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_dot));
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(4,0,4,0);
            dotsLayout.addView(dots[i], layoutParams);
        }

    }

    public void getInfo() // Data for the box with profilepicture, name and reviewdata
    {
        final String GET_INFO_URL = "https://studev.groept.be/api/a21pt311/getInfoData/"+ pc.getSellerID();
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_INFO_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(0);
                            mySellerName.setText(o.getString("username"));

                            if (!o.getString("profilePicture").equals("null")) //otherwise keep the android logo
                            {
                                String b64String = o.getString("profilePicture");
                                byte[] imageBytes = Base64.decode(b64String, Base64.DEFAULT);
                                myProfilePic.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
                            }
                            if (!(o.getString("mostars").equals("null"))) //otherwise keep the empty stars
                            {
                                rating.setRating(Float.parseFloat(o.getString("mostars")));
                            }
                            String amountOfRatings = o.getString("aostars");
                            if (amountOfRatings.equals("1"))
                            {
                                myAantalRatings.setText(new StringBuilder(o.getString("aostars")).append(" review"));
                            }
                            else
                            myAantalRatings.setText(new StringBuilder(o.getString("aostars")).append(" reviews"));
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


    public void goBack(View view) {
        finish();
    }

    public void goToBuy(View view) {
        getDateTimeOfLatestChange();
    }

    private void startBuyActivity()
    {
        Intent intent = new Intent(this, BuyActivity.class);
        intent.putExtra("ProductCard", pc);
        startActivity(intent);
    }

    public void goToSellerInfo(View view) {
        Intent intent = new Intent(this, ProfileDataActivity.class);
        intent.putExtra("profileId", pc.getSellerID());
        intent.putExtra("profileUserName", mySellerName.getText());

        startActivity(intent);
    }

    @Override
    public void noChangeInPC() {
        startBuyActivity();
    }

    @Override
    public void pcIsReloaded() {
        fillInDate();
    }

    @Override
    public void pcImagesReloaded() {
        imageAdapter = new MyImageAdapter(getApplicationContext(), pc.getImages());
        viewPager.setAdapter(imageAdapter);
        prepareDots(pc.getImageIndex(), imageAdapter.getCount());
    }
}
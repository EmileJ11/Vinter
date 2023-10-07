package be.kuleuven.vinter.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.models.ProductCard;

public class ReviewActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private ProductCard pc;

    private Button reviewBut;
    private RatingBar ratingBar;
    private ImageView myImageView;
    private EditText textReview;
    private TextView myDatetimeReviewed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        requestQueue = Volley.newRequestQueue(this);

        reviewBut = findViewById(R.id.submitReview);
        ratingBar = findViewById(R.id.ratingBar);
        myImageView = findViewById(R.id.shoeImage);
        textReview  = findViewById(R.id.textReview);
        myDatetimeReviewed = findViewById(R.id.datetimeReview);

        pc = (ProductCard) getIntent().getExtras().getParcelable("ProductCard");
        try {
            myImageView.setImageBitmap(pc.getSpecImage());
        }
        catch (Exception e) {
            //image.setImageResource(R.mipmap.ic_launcher);
        }

        myDatetimeReviewed.setVisibility(View.GONE);

        checkForReview();

        reviewBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratingBar.getRating() != 0) { // Text can be empty
                    String stars = String.valueOf(ratingBar.getRating());
                    String revtext = textReview.getText().toString();
                    postReview(stars, revtext);
                }
                else{
                    Toast.makeText(ReviewActivity.this, "Rating cannot be zero!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkForReview()
    {
        final String GET_Review_URL = "https://studev.groept.be/api/a21pt311/getReviewForProduct/" + pc.getProductId();
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_Review_URL, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(0);
                            ratingBar.setRating(Float.parseFloat(o.getString("stars")));
                            String text = o.getString("reviewText");
                            if (text.equals(""))
                            {
                                textReview.setText(" ");
                            }
                            else {textReview.setText(text);}
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            myDatetimeReviewed.setText(getTimeDifference(LocalDateTime.parse(o.getString("reviewDateTime"), formatter)));

                            reviewBut.setVisibility(View.GONE);
                            myDatetimeReviewed.setVisibility(View.VISIBLE);
                            ratingBar.setEnabled(false);
                            textReview.setEnabled(false);

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTimeDifference(LocalDateTime dateTime){
        LocalDateTime now = LocalDateTime.now();
        long timeDifference = ChronoUnit.SECONDS.between(dateTime, now);
        if (timeDifference < 59){
            // show in seconds
            return timeDifference + " seconds ago";
        }
        else if (timeDifference < 3599) {
            // show in minutes
            return (timeDifference/60) + " minutes ago";
        }
        else if (timeDifference < 86399) {
            // show in hours
            return timeDifference/3600 + " hours ago";
        }
        else //if (timeDifference < 2592000)
        {
            // show in days
            return timeDifference/86400 + " days ago";
        }
    }


    private void postReview(String stars, String revtext){
        final String POST_Review_URL = "https://studev.groept.be/api/a21pt311/insertReview/";
        StringRequest postImageRequest = new StringRequest (Request.Method.POST, POST_Review_URL,  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(ReviewActivity.this, "Review posted", Toast.LENGTH_LONG).show();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ReviewActivity.this, "Review post request failed", Toast.LENGTH_LONG).show();
            }
        }) { //NOTE THIS PART: here we are passing the parameter to the webservice, NOT in the URL!
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("stars", stars);
                params.put("revtext", revtext);
                params.put("productid", Integer.toString(pc.getProductId()));
                return params;
            }
        };

        requestQueue.add(postImageRequest);
    }

    public void goBack(View view) {
        finish();
    }
}
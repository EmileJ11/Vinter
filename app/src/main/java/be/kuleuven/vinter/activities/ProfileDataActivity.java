package be.kuleuven.vinter.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import be.kuleuven.vinter.models.MyReviewAdapter;
import be.kuleuven.vinter.models.ReviewData;

public class ProfileDataActivity extends AppCompatActivity {
    private RequestQueue requestQueue;

    private RecyclerView recyclerView;
    private MyReviewAdapter myReviewAdapter;
    private ArrayList<ReviewData> reviews;

    private TextView username;
    private TextView myEmptyNotifierReviews;

    private int profileId;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_data);
        requestQueue = Volley.newRequestQueue(this);

        reviews = new ArrayList<>();

        username = findViewById(R.id.username);
        myEmptyNotifierReviews = findViewById(R.id.emptyNotifierReview);

        profileId = getIntent().getExtras().getInt("profileId");
        username.setText("Reviews for " + getIntent().getExtras().getString("profileUserName"));

        getReviews();

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));



        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(dividerItemDecoration);
        myReviewAdapter = new MyReviewAdapter(this, reviews);
        recyclerView.setAdapter(myReviewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    protected void getReviews() {
        String GET_REVIEWS_URL = "https://studev.groept.be/api/a21pt311/getReviewsForProfile/" + profileId;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_REVIEWS_URL, null,
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
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

        requestQueue.add(retrieveImageRequest);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void extracted(JSONArray response) {
        for (int i = 0; i<response.length(); ++i) {
            JSONObject o = null;
            try {
                o = response.getJSONObject(i);
                ReviewData review = new ReviewData(o);
                reviews.add(review);
                //myReviewAdapter.notifyDataSetChanged();
                myReviewAdapter.notifyItemInserted(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setEmptyNotifier(reviews.isEmpty(), myEmptyNotifierReviews);
    }

    protected void setEmptyNotifier(boolean isListEmpty, TextView specEmptyNotifier){
        if (isListEmpty)
        {
            specEmptyNotifier.setVisibility(View.VISIBLE);
        }
    }

    public void goBack(View view) {
        finish();
    }
}
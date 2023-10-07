package be.kuleuven.vinter.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ReviewData {
    private String username;
    private Bitmap profilePicture;
    private float stars;
    private String reviewText;
    private LocalDateTime reviewDateTime;


    public ReviewData(String username, Bitmap profilePicture, int stars, String reviewText, LocalDateTime reviewDateTime) {
        this.username = username;
        this.profilePicture = profilePicture;
        this.stars = stars;
        this.reviewText = reviewText;
        this.reviewDateTime = reviewDateTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ReviewData(JSONObject o) {
        try {
            this.username = o.getString("username");

            if (!o.getString("profilePicture").equals("null")) //otherwise keep the android logo
            {
                String b64String = o.getString("profilePicture");
                byte[] imageBytes = Base64.decode(b64String, Base64.DEFAULT);
                this.profilePicture = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }

            this.stars = (float) o.getDouble("stars");
            this.reviewText =  o.getString("reviewText");//.replaceAll("_", " ");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.reviewDateTime = LocalDateTime.parse(o.getString("reviewDateTime"), formatter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public float getStars() {
        return stars;
    }

    public String getReviewText() {
        return reviewText;
    }

    public LocalDateTime getReviewDateTime() {
        return reviewDateTime;
    }

    @Override
    public String toString() {
        return "ReviewData{" +
                "username='" + username + '\'' +
                ", stars=" + stars +
                ", reviewText='" + reviewText + '\'' +
                ", reviewDateTime=" + reviewDateTime +
                '}';
    }
}

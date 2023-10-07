package be.kuleuven.vinter.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import be.kuleuven.vinter.R;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.MyReviewViewHolder> {

    private ArrayList<ReviewData> reviews;
    private Context context;
    private LocalDateTime now;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public MyReviewAdapter(Context ct, ArrayList<ReviewData> reviews){
        this.context = ct;
        this.reviews = reviews;
        this.now = LocalDateTime.now();
    }

    @NonNull
    @Override
    public MyReviewAdapter.MyReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row_review, parent, false);
        return new MyReviewAdapter.MyReviewViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MyReviewAdapter.MyReviewViewHolder holder, int position) {
        ReviewData review = reviews.get(holder.getAdapterPosition());
        holder.revUsername.setText(review.getUsername());
        holder.revDateTime.setText(getTimeDifference(review.getReviewDateTime()));
        holder.revText.setText(review.getReviewText());
        holder.rating.setRating(review.getStars());
        holder.rating.setEnabled(false);
        Bitmap pp = review.getProfilePicture();
        if (!(pp == null))
        {
            holder.myImage.setImageBitmap(pp);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTimeDifference(LocalDateTime dateTime){
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

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class MyReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView revUsername, revDateTime, revText;
        private ImageView myImage;
        private RatingBar rating;
        protected ConstraintLayout mainLayout;

        public MyReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            revUsername = itemView.findViewById(R.id.revUsername);
            revDateTime = itemView.findViewById(R.id.revDateTime);
            myImage = itemView.findViewById(R.id.myImage);
            rating = itemView.findViewById(R.id.ratingBar2);
            revText = itemView.findViewById(R.id.revText);

            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}

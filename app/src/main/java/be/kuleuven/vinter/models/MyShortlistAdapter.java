package be.kuleuven.vinter.models;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

import be.kuleuven.vinter.activities.InfoFromRecyclerActivity;

public class MyShortlistAdapter extends MyAdapter{

    public MyShortlistAdapter(Context ct, ArrayList<ProductCard> pcs){
        super(ct, pcs);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InfoFromRecyclerActivity.class);
                intent.putExtra("ProductCard", productCars.get(holder.getAdapterPosition())); // doe mis ook met holder.getAdapterPosition()
                intent.putExtra("buyEnabled", true);
                context.startActivity(intent);
            }
        });
    }


    @Override
    protected void getButtonValue(MyViewHolder holder){
        holder.button.setText("Remove");
        holder.button.setTextColor(Color.RED);
    }



}

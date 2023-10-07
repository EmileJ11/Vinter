package be.kuleuven.vinter.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.activities.InfoActivity;
import be.kuleuven.vinter.activities.InfoFromRecyclerActivity;
import be.kuleuven.vinter.activities.ReviewActivity;

public class MyOrdersAdapter extends MyAdapter{

    public MyOrdersAdapter(Context ct, ArrayList<ProductCard> pcs){
        super(ct, pcs);
    }

/*    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row_orders, parent, false);
        return new MyViewHolder(view);
    }*/


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        //ProductCard pc = this.pc;

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // call for the other pictures of the product (bcs only first loaded)!!!
                Intent intent = new Intent(context, InfoFromRecyclerActivity.class);
                intent.putExtra("ProductCard", productCars.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ReviewActivity.class);
                intent.putExtra("ProductCard", productCars.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    protected void getRegisteredSignValue(MyViewHolder holder){
        // keep registeredSign visible
    }

    @Override
    protected void getButtonValue(MyViewHolder holder){
        holder.button.setText("Add Review");
    }

}
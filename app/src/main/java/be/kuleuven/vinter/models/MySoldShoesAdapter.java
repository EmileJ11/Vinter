package be.kuleuven.vinter.models;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import be.kuleuven.vinter.activities.ChangeProductActivity;
import be.kuleuven.vinter.activities.InfoActivity;
import be.kuleuven.vinter.activities.InfoFromRecyclerActivity;

public class MySoldShoesAdapter extends MyAdapter{

    public MySoldShoesAdapter(Context ct, ArrayList<ProductCard> pcs){
        super(ct, pcs);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        //ProductCard pc = this.pc;

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InfoFromRecyclerActivity.class);
                intent.putExtra("ProductCard", productCars.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    protected void getRegisteredSignValue(MyViewHolder holder){
        // keep registeredSign visible (and green)
    }

    @Override
    protected void getButtonValue(MyViewHolder holder){
        holder.button.setText("Claim money");
    }

}
package be.kuleuven.vinter.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import be.kuleuven.vinter.R;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    protected ArrayList<ProductCard> productCars;
    protected Context context;
    protected ProductCard pc;

    public MyAdapter(Context ct, ArrayList<ProductCard> pcs){
        context = ct;
        productCars = pcs;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row_orders, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        pc = productCars.get(position);
        holder.myText1.setText(pc.getName());
        holder.myText2.setText("€" + String.valueOf(pc.getPrice()));
        try {
            holder.myImage.setImageBitmap(pc.getSpecImage());
        }
        catch (IndexOutOfBoundsException e) {
        }
        getRegisteredSignValue(holder);
        getButtonValue(holder);
    }

    protected void getRegisteredSignValue(MyViewHolder holder){
        holder.registeredSign.setVisibility(View.GONE);
    }

    protected void getButtonValue(MyViewHolder holder){
        holder.button.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return productCars.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView myText1, myText2;
        private ImageView myImage;
        public ImageView registeredSign;
        public Button button;
        protected ConstraintLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myText1 = itemView.findViewById(R.id.title);
            myText2 = itemView.findViewById(R.id.revText);
            myImage = itemView.findViewById(R.id.myImage);
            registeredSign = itemView.findViewById(R.id.registeredSign);
            button = itemView.findViewById(R.id.button3);

            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}

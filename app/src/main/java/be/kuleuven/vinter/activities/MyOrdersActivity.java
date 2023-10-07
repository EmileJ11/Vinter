package be.kuleuven.vinter.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.models.MyAdapter;
import be.kuleuven.vinter.models.MyOrdersAdapter;
import be.kuleuven.vinter.models.ProductCard;

public class MyOrdersActivity extends BasePCListActivityManager {
    private ArrayList<ProductCard> registeredProductCards;
    private RecyclerView recyclerViewRegistered;
    private MyAdapter myAdapterRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_my_orders);

        SharedPreferences prfs = getSharedPreferences("LoginValue", Context.MODE_PRIVATE);
        String productid = prfs.getString("id", "");

        emptyNotifier = findViewById(R.id.emptyNotifierOrders);

        getListedProducts("https://studev.groept.be/api/a21pt311/getOrderedProducts/"+ productid);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));


        // Non-Registered
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(dividerItemDecoration);
        myAdapter = new MyOrdersAdapter(this, productCards){
            @Override
            protected void getRegisteredSignValue(MyViewHolder holder){
                holder.registeredSign.setColorFilter(ContextCompat.getColor(context, R.color.duskYellow));
            }

            @Override
            protected void getButtonValue(MyViewHolder holder){
                holder.button.setVisibility(View.GONE);
            }
        };
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Registered
        recyclerViewRegistered = findViewById(R.id.recyclerView2);
        recyclerViewRegistered.addItemDecoration(dividerItemDecoration);
        registeredProductCards = new ArrayList<>();
        myAdapterRegistered = new MyOrdersAdapter(this, registeredProductCards);
        recyclerViewRegistered.setAdapter(myAdapterRegistered);
        recyclerViewRegistered.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_my_orders;
    }

    @Override
    protected void extracted(JSONArray response){
        for (int i = 0; i<response.length(); ++i) {
            JSONObject o = null;
            try {
                o = response.getJSONObject(i);
                ProductCard pc = new ProductCard(o);
                int position;
                if (o.getString("orderId").equals("null")) // not yet registered
                {
                    productCards.add(pc); // Non-Registered
                    //myAdapter.notifyDataSetChanged(); // show productcard data (name and price)
                    position = myAdapter.getItemCount()-1;
                    myAdapter.notifyItemInserted(position);
                    getFirstPictureForProduct(pc, myAdapter, position);
                }
                else {
                    registeredProductCards.add(pc);
                    //myAdapterRegistered.notifyDataSetChanged(); // show productcard data (name and price)
                    position = myAdapterRegistered.getItemCount()-1;
                    myAdapterRegistered.notifyItemInserted(position);
                    getFirstPictureForProduct(pc, myAdapterRegistered, position);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setEmptyNotifier((productCards.isEmpty() && registeredProductCards.isEmpty()), emptyNotifier);
    }

    public void goBack(View view) {
        finish();
    }

}

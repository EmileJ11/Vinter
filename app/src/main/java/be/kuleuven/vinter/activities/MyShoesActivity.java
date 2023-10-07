package be.kuleuven.vinter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.models.MyAdapter;
import be.kuleuven.vinter.models.ProductCard;
import be.kuleuven.vinter.models.MySellingShoesAdapter;
import be.kuleuven.vinter.models.MySoldShoesAdapter;

public class MyShoesActivity extends BasePCListActivityManager {

    private RecyclerView soldRecyclerView;
    private MyAdapter soldAdapter;
    private ArrayList<ProductCard> soldProductCards;

    private RecyclerView soldAndRegisteredRecyclerView;
    private MyAdapter soldAndRegisteredAdapter;
    private ArrayList<ProductCard> soldAndRegisteredProductCards;


    private Button myAll, mySelling, mySold;
    private MaterialButtonToggleGroup myGroup;
    private TextView myTextSell, myTextSold;

    private TextView emptyNotifierSoldShoes;

    private boolean sellingShoesEmpty;
    private boolean soldShoesEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_shortlist);

        myAll = findViewById(R.id.allButton);
        mySelling = findViewById(R.id.sellingButton);
        mySold = findViewById(R.id.soldButton);
        myGroup = findViewById(R.id.toggle);
        myTextSell = findViewById(R.id.selling);
        myTextSold = findViewById(R.id.sold);


        SharedPreferences prfs = getSharedPreferences("LoginValue", Context.MODE_PRIVATE);
        String profileid = prfs.getString("id", "");

        emptyNotifier = findViewById(R.id.emptyNotifierSellingShoes);
        emptyNotifierSoldShoes = findViewById(R.id.emptyNotifierSoldShoes);

        getListedProducts("https://studev.groept.be/api/a21pt311/getMyProducts/"+ profileid);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));

        // selling products
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(dividerItemDecoration);
        myAdapter = new MySellingShoesAdapter(this, productCards);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // sold products; NON-REGISTERED
        soldRecyclerView = findViewById(R.id.recyclerView2);
        soldRecyclerView.addItemDecoration(dividerItemDecoration);
        soldProductCards = new ArrayList<>();

        soldAdapter = new MySoldShoesAdapter(this, soldProductCards){
            @Override
            public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, QRActivity.class);
                        intent.putExtra("productId", soldProductCards.get(holder.getAdapterPosition()).getProductId());
                        context.startActivity(intent);
                    }
                });
            }
            @Override
            protected void getRegisteredSignValue(MyViewHolder holder){
                holder.registeredSign.setColorFilter(ContextCompat.getColor(context, R.color.duskYellow));
            }

            @Override
            protected void getButtonValue(MyViewHolder holder){
                holder.button.setText("Get Label");
            }
        };
        soldRecyclerView.setAdapter(soldAdapter);
        soldRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // sold products; Registered

        soldAndRegisteredRecyclerView = findViewById(R.id.recyclerView3);
        soldAndRegisteredRecyclerView.addItemDecoration(dividerItemDecoration);
        soldAndRegisteredProductCards = new ArrayList<>();

        soldAndRegisteredAdapter = new MySoldShoesAdapter(this, soldAndRegisteredProductCards){
            @Override
            public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        claimMoneyCall(productCars.get(holder.getAdapterPosition()).getProductId());
                    }
                });
            }
        };
        soldAndRegisteredRecyclerView.setAdapter(soldAndRegisteredAdapter);
        soldAndRegisteredRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        myAll.setBackgroundColor(getColor(R.color.purple_500));

        myGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {

                    switch (checkedId) {
                        case R.id.allButton:
                            mySold.setBackgroundColor(getColor(R.color.purple_200));
                            mySelling.setBackgroundColor(getColor(R.color.purple_200));
                            myAll.setBackgroundColor(getColor(R.color.purple_500));

                            recyclerView.setVisibility(View.VISIBLE);
                            soldRecyclerView.setVisibility(View.VISIBLE);
                            soldAndRegisteredRecyclerView.setVisibility(View.VISIBLE);

                            myTextSell.setVisibility(View.VISIBLE);
                            myTextSold.setVisibility(View.VISIBLE);
                            if (sellingShoesEmpty)
                            {
                                emptyNotifier.setVisibility(View.VISIBLE);
                            }
                            if (soldShoesEmpty)
                            {
                                emptyNotifierSoldShoes.setVisibility(View.VISIBLE);
                            }
                            break;

                        case R.id.sellingButton:
                            mySelling.setBackgroundColor(getColor(R.color.purple_500));
                            mySold.setBackgroundColor(getColor(R.color.purple_200));
                            myAll.setBackgroundColor(getColor(R.color.purple_200));

                            recyclerView.setVisibility(View.VISIBLE);
                            myTextSell.setVisibility(View.VISIBLE);
                            soldRecyclerView.setVisibility(View.GONE);
                            soldAndRegisteredRecyclerView.setVisibility(View.GONE);

                            myTextSold.setVisibility(View.GONE);

                            if (sellingShoesEmpty)
                            {
                                emptyNotifier.setVisibility(View.VISIBLE);
                            }
                            emptyNotifierSoldShoes.setVisibility(View.GONE);
                            break;

                        case R.id.soldButton:
                            myAll.setBackgroundColor(getColor(R.color.purple_200));
                            mySelling.setBackgroundColor(getColor(R.color.purple_200));
                            mySold.setBackgroundColor(getColor(R.color.purple_500));

                            soldRecyclerView.setVisibility(View.VISIBLE);
                            soldAndRegisteredRecyclerView.setVisibility(View.VISIBLE);
                            myTextSold.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);

                            myTextSell.setVisibility(View.GONE);

                            if (soldShoesEmpty)
                            {
                                emptyNotifierSoldShoes.setVisibility(View.VISIBLE);
                            }
                            emptyNotifier.setVisibility(View.GONE);
                            break;
                    }
                }
            }
        });

        //bottomNavView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.messageNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.homeNav:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.shortlistNav:
                    startActivity(new Intent(getApplicationContext(), ShortlistActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.addNav:
                    startActivity(new Intent(getApplicationContext(), AddActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.messageNav:
                    return true;
                case R.id.profileNav:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0,0);
                    return true;

            }
            return false;
        });
    }

    @Override
    protected void extracted(JSONArray response){
        for (int i = 0; i<response.length(); ++i) {
            JSONObject o = null;
            try {
                o = response.getJSONObject(i);
                ProductCard pc = new ProductCard(o);
                int position;
                if (o.getString("soldTo").equals("null")) // not yet sold
                    // verander met db verandering
                {
                    productCards.add(pc);
                    //myAdapter.notifyDataSetChanged();
                    position = myAdapter.getItemCount()-1;
                    myAdapter.notifyItemInserted(position);
                    getFirstPictureForProduct(pc, myAdapter, position);
                }
                else if (o.getString("orderId").equals("null")){
                    soldProductCards.add(pc);
                    //soldAdapter.notifyDataSetChanged();
                    position = soldAdapter.getItemCount()-1;
                    soldAdapter.notifyItemInserted(position);
                    getFirstPictureForProduct(pc, soldAdapter, position);
                }
                else {
                    soldAndRegisteredProductCards.add(pc);
                    //soldAndRegisteredAdapter.notifyDataSetChanged();
                    position = soldAndRegisteredAdapter.getItemCount()-1;
                    soldAndRegisteredAdapter.notifyItemInserted(position);
                    getFirstPictureForProduct(pc, soldAndRegisteredAdapter, position);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sellingShoesEmpty = setEmptyNotifier(productCards.isEmpty(), emptyNotifier);
        soldShoesEmpty = setEmptyNotifier((soldProductCards.isEmpty() && soldAndRegisteredProductCards.isEmpty()), emptyNotifierSoldShoes);
    }

/*    @Override
    protected void notifiyDSChanged(ProductCard pc){ // when images are loaded
        soldAndRegisteredAdapter.notifyDataSetChanged(); // FIX DIT NOG // TODO
        soldAdapter.notifyDataSetChanged();
        //soldAdapter.notifyItemChanged(soldProductCards.indexOf(pc));
        myAdapter.notifyDataSetChanged();
    }*/

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_shoes;
    }

    private void claimMoneyCall(int productId) {
        final String CLAIM_MONEY_URL = "https://studev.groept.be/api/a21pt311/claimMoney/"+ productId;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, CLAIM_MONEY_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(0);
                            String price = o.getString("price");
                            double money = (Integer.parseInt(price)*0.95);
                            Toast.makeText(MyShoesActivity.this, "You received " + money + " euros ! (5% tax)", Toast.LENGTH_LONG).show();

                            updateMoneyClaim(productId);
                        }
                        catch (JSONException e) {
                            Toast.makeText(MyShoesActivity.this, "Already claimed the money!", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MyShoesActivity.this, "Unable to communicate with server", Toast.LENGTH_LONG).show();
                    }
                }
        );
        requestQueue.add(retrieveImageRequest);
    }

    private void updateMoneyClaim(int productId) {
        final String CLAIM_MONEY_URL = "https://studev.groept.be/api/a21pt311/updateClaimMoney/"+ productId;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, CLAIM_MONEY_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {}
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MyShoesActivity.this, "Unable to communicate with server", Toast.LENGTH_LONG).show();
                    }
                }
        );
        requestQueue.add(retrieveImageRequest);
    }
}
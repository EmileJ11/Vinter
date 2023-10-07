package be.kuleuven.vinter.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

import be.kuleuven.vinter.R;
import be.kuleuven.vinter.models.MyShortlistAdapter;

public class ShortlistActivity extends BasePCListActivityManager{

    private String profileid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_shortlist);

        SharedPreferences prfs = getSharedPreferences("LoginValue", Context.MODE_PRIVATE);
        profileid = prfs.getString("id", "");

        emptyNotifier = findViewById(R.id.emptyNotifierShortlist);

        getListedProducts("https://studev.groept.be/api/a21pt311/getShortlistedProducts/"+ profileid);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false); //for smooth scrolling
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        myAdapter = new MyShortlistAdapter(this, productCards)
        {
            @Override
            public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete product from favourites")
                                .setMessage("Are you sure you want to delete this product from your favourites?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //db call
                                        deleteProductFromShortlist(
                                                productCars.get(holder.getAdapterPosition()).getProductId(),
                                                holder.getAdapterPosition());
                                        productCars.remove(holder.getAdapterPosition());
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
            }
        };
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //bottomNavView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.shortlistNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.homeNav:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.shortlistNav:
                    return true;
                case R.id.addNav:
                    startActivity(new Intent(getApplicationContext(), AddActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.messageNav:
                    startActivity(new Intent(getApplicationContext(), MyShoesActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.profileNav:
                    startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });
        //bottomNavView
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_shortlist;
    }

    //@Override
    public void deleteProductFromShortlist(int pcid, int position) {
        final String DEL_PROD_URL = "https://studev.groept.be/api/a21pt311/deleteProductFromShortlist/" + profileid + "/" + pcid;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, DEL_PROD_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        setEmptyNotifier(productCards.isEmpty(), emptyNotifier); // Should display no favourites if it's empty
                        myAdapter.notifyItemRemoved(position);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Unable to communicate with server", Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(retrieveImageRequest);
    }
}
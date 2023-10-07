package be.kuleuven.vinter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import be.kuleuven.vinter.FilterFragment;
import be.kuleuven.vinter.models.PCProcessing;
import be.kuleuven.vinter.models.ProductsLoadedNotifier;
import be.kuleuven.vinter.models.CardArrayAdapter;
import be.kuleuven.vinter.models.ProductCard;
import be.kuleuven.vinter.R;


public class MainActivity extends AppCompatActivity implements ProductsLoadedNotifier {

    private CardArrayAdapter arrayAdapter;

    public static List<ProductCard> rowItems = new ArrayList<>(); // static to remember when going to another activity

    private ArrayDeque<ProductCard> deletedPCs;

    private ImageButton getPreviousCardButton;

    private SwipeFlingAdapterView flingContainer;

    private RequestQueue requestQueue;

    private PCProcessing pcProcessing;

    private boolean isFirst = true;
    private ProductCard firstpc;

    private String profileid;

    private boolean noProductsToBeLoaded = false;

    private boolean productsListEmpty = false;

    private ImageButton filterButton, getInfoCardButton;
    private Boolean filterFragmentShown = false;

    private TextView noProductsText;
    private ImageButton reloadGetProductsButton;

    //preferences from filter, moet bij filterfragmetent staan
    public static String minSize = "0";
    public static String maxSize = "50";
    public static String minPrice = "0";
    public static String maxPrice = "500";
    public static String prefBrand;
    public static String prefGender;
    public static String prefColor;
    public static String prefCategory;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filterButton = findViewById(R.id.filterBut);
        getInfoCardButton = findViewById(R.id.info);
        getPreviousCardButton = findViewById(R.id.back);
        getPreviousCardButton.setEnabled(false);

        noProductsText = findViewById(R.id.noProduct);
        reloadGetProductsButton = findViewById(R.id.reload);
        noProductsText.setVisibility(View.GONE);
        reloadGetProductsButton.setVisibility(View.GONE);
        reloadGetProductsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pcProcessing.getFilteredProducts(minSize, maxSize, minPrice, maxPrice,
                        prefBrand, prefGender, prefColor, prefCategory);
                isFirst = true;
            }
        });

        //bottomNavView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.homeNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.homeNav:
                    return true;
                case R.id.addNav:
                    startActivity(new Intent(getApplicationContext(), AddActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.shortlistNav:
                    startActivity(new Intent(getApplicationContext(), ShortlistActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.profileNav:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.messageNav:
                    startActivity(new Intent(getApplicationContext(), MyShoesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
        //bottomNavView


        requestQueue = Volley.newRequestQueue(this);


        SharedPreferences loginValues = getSharedPreferences("LoginValue", Context.MODE_PRIVATE);
        profileid = loginValues.getString("id", "");


        pcProcessing = new PCProcessing(getApplicationContext(), profileid);
        pcProcessing.setNotifier(this);


        if (!rowItems.isEmpty()){
            isFirst = false;
        }

        else if (!PCProcessing.productCards.isEmpty()){
            notifyProductsLoaded();
        }

        else{ // So Initial onCreate() or when the static Lists or both empty
            pcProcessing.getProducts();
        }


        this.deletedPCs = new ArrayDeque<>();

        this.arrayAdapter = new CardArrayAdapter(this, R.layout.item, rowItems);
        this.flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);


        this.flingContainer.setAdapter(arrayAdapter);
        this.flingContainer.setMinStackInAdapter(2);
        this.flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                ProductCard pc = rowItems.remove(0);
                deletedPCs.push(pc);
                Log.d("LIST", "removed object!" + pc);
                getPreviousCardButton.setEnabled(true);
                if (!(rowItems.isEmpty() && productsListEmpty)) // notifyDataSetChanged will trigger onAdapterAboutToEmpty (when adapterCount <= MIN_ADAPTER_STACK),
                    // so we delay this call till the (dis)liked response is arrived if a new query for getting products is called
                {
                    arrayAdapter.notifyDataSetChanged(); // see code (dis)liked()
                }
            }

            @Override
            public void onLeftCardExit(Object dataObject) { // get's calld right AFTER removeFirstObjectInAdapter() (if swiped left), see FlingCardListener
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                ProductCard pc = (ProductCard) dataObject;// lorentzos' API doesn't have specific functionality for getting the previous,
                //this 'dataObject' is the correct one for normal use, but doesn't get updated when going back to a previous
                pc = deletedPCs.peek();
                if (pc.getIsLiked() == null) {
                    dislikeOnDB(pc.getProductId());
                }
                else if (pc.getIsLiked()){
                    // update
                    deleteProductFromShortlist(pc.getProductId());
                    dislikeOnDB(pc.getProductId());
                }
                pc.dislike();
            }

            @Override
            public void onRightCardExit(Object dataObject) { // get's calld right AFTER removeFirstObjectInAdapter() (if swiped right), see FlingCardListener
                ProductCard pc = (ProductCard) dataObject; // lorentzos' API doesn't have specific functionality for getting the previous,
                //this 'dataObject' is the correct one for normal use, but doesn't get updated when going back to a previous
                // see FlingCardListener
                pc = deletedPCs.peek();
                if (pc.getIsLiked() == null) {
                    likeOnDB(pc.getProductId());
                }
                else if (!pc.getIsLiked()){
                    // update
                    deleteProductFromDislikedList(pc.getProductId());
                    likeOnDB(pc.getProductId());
                }
                pc.like();
            }


            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                if (productsListEmpty) // if the list of products in PCProcessing is empty a new query should be called
                {
                    if (rowItems.isEmpty()){
                        pcProcessing.getFilteredProducts(minSize, maxSize, minPrice, maxPrice,
                                prefBrand, prefGender, prefColor, prefCategory);
                        isFirst = true;
                        Log.d("LIST", "getFilteredProducts() called in adapterAboutToBeEmptyNotified");
                    }
                }
                else {
                    if (!isFirst) { // Shouldn't call addProductItems() when starting up
                        addProductItems();
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
                Log.d("LIST", "adapterAboutToBeEmptyNotified");
                Log.d("LIST", rowItems.toString());
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                rowItems.get(0).incrementImageNb();
                View view = flingContainer.getSelectedView();
                ImageView image = (ImageView) view.findViewById(R.id.image);
                image.setImageBitmap(rowItems.get(0).getSpecImage());
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment();
            }
        });

    }

    private void replaceFragment() {

        FilterFragment filterFragment = new FilterFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (!filterFragmentShown) {
            reloadGetProductsButton.setVisibility(View.GONE);
            fragmentTransaction.replace(R.id.frameLayout, filterFragment);
            fragmentTransaction.commit();
            getInfoCardButton.setVisibility(View.GONE);
            getPreviousCardButton.setVisibility(View.GONE);
            filterFragmentShown = true;
        }
        else {

            Fragment fragment = fragmentManager.findFragmentById(R.id.frameLayout);
            if (fragment != null) {
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
                getInfoCardButton.setVisibility(View.VISIBLE);
                getPreviousCardButton.setVisibility(View.VISIBLE);
                filterFragmentShown = false;
                filter();
            }
        }
    }

    private void filter() {
        if (prefBrand != null && prefBrand.equals("")) {
            prefBrand = null;
        }
        if (prefGender != null &&  prefGender.equals("Choose gender")) {
            prefGender = null;
        }
        if (prefColor != null &&  prefColor.equals("Choose color")) {
            prefColor = null;
        }
        if (prefCategory != null &&  prefCategory.equals("Choose category")) {
            prefCategory = null;
        }
        rowItems.clear(); // We don't want the unfiltered products
        isFirst = true;
        productsListEmpty = false;  // for onAdapterAboutToEmpty so it doesn't call getProducts() again
        pcProcessing.getFilteredProducts(minSize, maxSize, minPrice, maxPrice,
                prefBrand, prefGender, prefColor, prefCategory);
    }

    private void likeOnDB(int productid){
        final String LIKE_PRODUCTS_URL = "https://studev.groept.be/api/a21pt311/insertIntoShortlist/"+ profileid + "/" + productid;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, LIKE_PRODUCTS_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (rowItems.isEmpty() && productsListEmpty) // For the final product the query (insertIntoShortlist)
                            // should be called before a new getProducts() (db query) is called
                            // so the final one doesn't get called again from the new getProducts query
                        {
                            arrayAdapter.notifyDataSetChanged();
                        }
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


    private void dislikeOnDB(int productid){
        final String DISLIKE_PRODUCTS_URL = "https://studev.groept.be/api/a21pt311/insertIntoDislikedList/"+ profileid + "/" + productid;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, DISLIKE_PRODUCTS_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (rowItems.isEmpty() && productsListEmpty) // For the final product the query (insertIntoDislikedList) should be called before a new getProducts() is called
                            // so the final one doesn't get called again from the new getProducts query
                        {
                            arrayAdapter.notifyDataSetChanged();
                        }
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

    public void deleteProductFromShortlist(int productid) {
        final String DEL_PROD_URL = "https://studev.groept.be/api/a21pt311/deleteProductFromShortlist/" + profileid + "/" + productid;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, DEL_PROD_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {}
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

    public void deleteProductFromDislikedList(int productid) {
        final String DEL_PROD_URL = "https://studev.groept.be/api/a21pt311/deleteProductFromDislikedList/" + profileid + "/" + productid;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, DEL_PROD_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {}
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



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override // Overridden from ProductsLoadedNotifier
    public void notifyProductsLoaded() {
        if (noProductsToBeLoaded) {
            noProductsText.setVisibility(View.GONE);
            reloadGetProductsButton.setVisibility(View.GONE);
            noProductsToBeLoaded = false;
        }
        addProductItems();
    }

    @Override // Overridden from ProductsLoadedNotifier
    public void notifyNoProductsToBeLoaded() {
        noProductsText.setVisibility(View.VISIBLE);
        reloadGetProductsButton.setVisibility(View.VISIBLE);
        noProductsToBeLoaded = true;
    }

    @Override // Overridden from ProductsLoadedNotifier
    public void notifyProductsListEmpty() {
        productsListEmpty = true;
    }

    @Override // Overridden from ProductsLoadedNotifier
    public void notifyProductsListNotEmptyAnymore() {
        productsListEmpty = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addProductItems(){

        List<ProductCard> addedPCs = pcProcessing.getFirstXProducts(3);
        addedPCs.stream()
                .forEach(pc -> getPicturesForProduct(pc));
        rowItems.addAll(addedPCs);
        if (isFirst)
        {firstpc = rowItems.get(0);}
    }


    private void getPicturesForProduct(ProductCard pc) {
        final String GET_IMAGE_URL = "https://studev.groept.be/api/a21pt311/getImages/" + pc.getProductId();
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_IMAGE_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //Check if the DB actually contains an image
                            for (int i=0; i < response.length(); i++) {
                                JSONObject o = response.getJSONObject(i);

                                //converting base64 string to image
                                String b64String = o.getString("image");
                                byte[] imageBytes = Base64.decode(b64String, Base64.DEFAULT);
                                Bitmap bitmap2 = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                                //Link the bitmap to the ImageView, so it's visible on screen
                                pc.addImage(bitmap2);
                            }

                            arrayAdapter.notifyDataSetChanged();

                            if (pc.equals(firstpc) && isFirst){ // necessary to set the image of the first image in the view
                                View view = flingContainer.getSelectedView();
                                try{
                                    isFirst = false;
                                    ImageView image = (ImageView) view.findViewById(R.id.image);
                                    image.setImageBitmap(pc.getSpecImage());
                                }
                                catch (Exception e){
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Unable to communicate with server", Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(retrieveImageRequest);
    }


    public void goToPrevious(View view) {
        if (!deletedPCs.isEmpty())
        {
            ProductCard deletedPC = deletedPCs.pop();
            rowItems.add(0, deletedPC);

            arrayAdapter.notifyDataSetChanged(); // doet niet wat het zou moeten doen

            if (flingContainer.getSelectedView() == null)
            {
                this.arrayAdapter = new CardArrayAdapter(this, R.layout.item, rowItems);
                this.flingContainer.setAdapter(arrayAdapter);
            }
            else {
                arrayAdapter.getView(0, flingContainer.getSelectedView(), (ViewGroup) flingContainer.getSelectedView());
            }

            if (deletedPCs.isEmpty())
            {
                getPreviousCardButton.setEnabled(false);
            }
        }
    }

    public void goToInfo(View view) {
        try {
            ProductCard pc = rowItems.get(0);
            Intent intent = new Intent(MainActivity.this, InfoActivity.class);
            intent.putExtra("ProductCard", pc);
            startActivity(intent);
        }
        catch (IndexOutOfBoundsException e)
        {}
    }
}

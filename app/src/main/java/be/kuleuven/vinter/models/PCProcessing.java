package be.kuleuven.vinter.models;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import be.kuleuven.vinter.activities.MainActivity;

public class PCProcessing {
    public static List<ProductCard> productCards = new ArrayList<>(); // Static so getProducts() shouldn't get called everytime mainActivity onCreate() is called
    private RequestQueue requestQueue;
    private Context context;
    private String profileid;
    private ProductsLoadedNotifier viewNotifier;
    private boolean productCardsListIsEmpty = false;


    public PCProcessing(Context context, String profileid) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        this.profileid = profileid;
    }

    public final void setNotifier(ProductsLoadedNotifier notifier){
        this.viewNotifier = notifier;
    }

    public void getProducts() {
        final String GET_PRODUCTS_URL = "https://studev.groept.be/api/a21pt311/getProductNew/"+ profileid;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_PRODUCTS_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        extracted(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Unable to communicate with server", Toast.LENGTH_LONG).show();
                        viewNotifier.notifyNoProductsToBeLoaded();
                    }
                }
        );

        requestQueue.add(retrieveImageRequest);
    }
    private String isSpecific(String column) // see DB query, if there's a preference for e.g. brand, (O or brand=prefbrand), no preference: (1 or brand=null)
    {
        if (column == null){
            return "1";
        }
        return "0";
    }

    public void getFilteredProducts(String minSize, String maxSize, String minPrice, String maxPrice,
                                    String prefBrand, String prefGender, String prefColor, String prefCategory)
    {
        final String GET_PRODUCTS_URL = "https://studev.groept.be/api/a21pt311/getFilteredProducts/"
                + profileid + "/" + profileid + "/" + profileid + "/" +
                minSize + "/" + maxSize + "/" + minPrice + "/" + maxPrice + "/" +
                isSpecific(prefBrand)  + "/" + prefBrand  + "/" +
                isSpecific(prefGender) + "/" + prefGender + "/" +
                isSpecific(prefColor)  + "/" + prefColor  + "/" +
                isSpecific(prefCategory) + "/" + prefCategory;
        productCards.clear(); // Delete all the non-filtered products still left in the list
        productCardsListIsEmpty = true;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_PRODUCTS_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        extracted(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Unable to communicate with server", Toast.LENGTH_LONG).show();
                        viewNotifier.notifyNoProductsToBeLoaded();
                    }
                }
        );

        requestQueue.add(retrieveImageRequest);
    }

    private void extracted(JSONArray response) {
        if (response.length() == 0){
            viewNotifier.notifyNoProductsToBeLoaded();
        }
        else {
            JSONObject o = null;
            for (int i = 0; i<response.length(); ++i) {
                try {
                    o = response.getJSONObject(i);
                    productCards.add(new ProductCard(o));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            viewNotifier.notifyProductsLoaded();

            if (productCardsListIsEmpty){
                viewNotifier.notifyProductsListNotEmptyAnymore();
                productCardsListIsEmpty = false;
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<ProductCard> getFirstXProducts(int amount){
        List<ProductCard> elemFromProductCardsList =
                productCards
                .stream()
                .limit(amount)
                .collect(Collectors.toList());
        for (ProductCard pc : elemFromProductCardsList) // ook nog verwijderen van lijst
        {
            productCards.remove(pc);
        }
        if (productCards.isEmpty())
        {
            productCardsListIsEmpty = true;
            viewNotifier.notifyProductsListEmpty();
        }
        return elemFromProductCardsList;
    }
}

package be.kuleuven.vinter.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.kuleuven.vinter.R;

public class LoginActivity extends AppCompatActivity {

    private EditText myUsername;
    private EditText myPassword;
    private Button myLogin;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myPassword = findViewById(R.id.password);
        myUsername = findViewById(R.id.username);
        myLogin = findViewById(R.id.loginButton);

        requestQueue = Volley.newRequestQueue(this);

        myLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(myPassword.getText().toString()) || TextUtils.isEmpty(myUsername.getText().toString())){
                    Toast.makeText(LoginActivity.this, "No empty fields allowed!", Toast.LENGTH_SHORT).show();
                }
                else if (myPassword.getText().toString().equals("mail") &&
                        (myUsername.getText().toString().equals("Bpost"))) {
                    finish();
                    Intent intent = new Intent(LoginActivity.this, ScannerActivity.class);
                    startActivity(intent);
                }

                else {
                    //check met database als alles overeenstemt en dan doorsturen naar main
                    checkCredentials(myUsername.getText().toString(), myPassword
                            .getText().toString());

                }
            }
        }
        );
    }

    private void checkCredentials(String userName, String password) {
        final String GET_ID = "https://studev.groept.be/api/a21pt311/checkLogin/" + userName + "/" + password;
        JsonArrayRequest retrieveImageRequest = new JsonArrayRequest(Request.Method.GET, GET_ID, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject o = null;
                            try
                            {
                                o = response.getJSONObject(0);
                                String id = o.getString("idprofile");
                                login(id, userName, password);
                            }
                            catch (JSONException e) //username and password don't match
                            {
                                Toast.makeText(LoginActivity.this, "LOGIN FAILED", Toast.LENGTH_SHORT).show();
                            }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "COULDN'T CONNECT TO THE DATABASE", Toast.LENGTH_SHORT).show();
                        myUsername.setText("");
                        myPassword.setText("");
                    }
                }
        );

        requestQueue.add(retrieveImageRequest);
    }

    private void login(String id, String userName, String password){
        SharedPreferences sharedPreferences = getSharedPreferences("LoginValue",MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("id", id);
        myEdit.putString("UserName", userName);
        myEdit.putString("Password", password);
        myEdit.commit();

        goToMain();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Fetching the stored data
        // from the SharedPreference
        SharedPreferences sh = getSharedPreferences("LoginValue", MODE_PRIVATE);

        String us = sh.getString("UserName", "");
        String p = sh.getString("Password", "");

        // Setting the fetched data
        // in the EditTexts
        myUsername.setText(us);
        myPassword.setText(p);
    }


    public void goToMain() {
        finish();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }


}
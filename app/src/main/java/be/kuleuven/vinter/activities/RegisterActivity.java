package be.kuleuven.vinter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import be.kuleuven.vinter.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText myUsername;
    private EditText myPassword;
    private EditText myEmail;
    private String myBirthdateDB;
    private TextView myBirthdate;
    private Button makeAccount;

    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        makeAccount = findViewById(R.id.makeAccount);
        myPassword = findViewById(R.id.password);
        myEmail = findViewById(R.id.email);
        myBirthdate = findViewById(R.id.birthdate);
        myUsername = findViewById(R.id.username);

        Calendar calender = Calendar.getInstance();
        final int year = calender.get(Calendar.YEAR);
        final int month = calender.get(Calendar.MONTH);
        final int day = calender.get(Calendar.DAY_OF_MONTH);

        myBirthdate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month+1;
                        String monthS = "";
                        String dayS = "";
                        String yearS = Integer.toString(year);
                        if (month<10){
                            monthS = "0" + month;
                        }
                        else{
                            monthS = Integer.toString(month);
                        }
                        if (day<10){
                            dayS = "0" + day;
                        }
                        else{
                            dayS = Integer.toString(day);
                        }
                        String dateDB = yearS + monthS + dayS;
                        myBirthdateDB = dateDB;
                        String date = day+"/"+month+"/"+year;
                        myBirthdate.setText(date);
                    }
                },year, month, day);
                datePickerDialog.show();
            }
        });

        makeAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(myPassword.getText().toString())
                        || TextUtils.isEmpty(myUsername.getText().toString())
                        || TextUtils.isEmpty(myEmail.getText().toString())
                        //|| TextUtils.isEmpty(myBirthdate.getText().toString())
                    ){
                    Toast.makeText(RegisterActivity.this, "Credentials incomplete", Toast.LENGTH_SHORT).show();
                }
                else {
                    //een nieuwe persoon maken in database
                    disableEditing();
                    checkUsername(myUsername.getText().toString());
                }
            }
        });

        requestQueue = Volley.newRequestQueue(this);
    }

    private void disableEditing(){
        myUsername.setFocusable(false);
        myPassword.setFocusable(false);
        myEmail.setFocusable(false);
        myBirthdate.setFocusable(false);
    }

    private void enableEditing(){
        myUsername.setFocusableInTouchMode(true);
        myPassword.setFocusableInTouchMode(true);
        myEmail.setFocusableInTouchMode(true);
        myBirthdate.setFocusableInTouchMode(true);
    }

    private void checkUsername(String username) { // Check if username not already taken
        final String GET_USERNAME_URL = "https://studev.groept.be/api/a21pt311/getPersonIdFromUsername/"+ username;
        JsonArrayRequest checkUsernameRequest = new JsonArrayRequest(Request.Method.GET, GET_USERNAME_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0)
                        {
                            insertNewPerson(username);
                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
                            enableEditing();
                            myUsername.setText("");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        enableEditing();
                    }
                }
        );

        requestQueue.add(checkUsernameRequest);
    }

    private void insertNewPerson(String username) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.show();

        StringBuilder sb = new StringBuilder("https://studev.groept.be/api/a21pt311/insertNewPerson");
        sb.append("/" + username);
        sb.append("/" + myPassword.getText().toString());
        sb.append("/" + myEmail.getText().toString());
        sb.append("/" + myBirthdateDB);

        final String insertNewPerson_URL = sb.toString();
        JsonArrayRequest insertNewPersonRequest = new JsonArrayRequest(Request.Method.GET, insertNewPerson_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        getPersonIdFromUsername(username);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Failed to register account", Toast.LENGTH_SHORT).show();
                        enableEditing();
                    }
                }
        );

        requestQueue.add(insertNewPersonRequest);
    }

    private void getPersonIdFromUsername(String username) { // for login
        final String GET_ID_URL = "https://studev.groept.be/api/a21pt311/getPersonIdFromUsername/"+ username;
        JsonArrayRequest insertNewPersonRequest = new JsonArrayRequest(Request.Method.GET, GET_ID_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(0);
                            progressDialog.dismiss();
                            String id = o.getString("idprofile");
                            login(id, username, myPassword.getText().toString());
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            enableEditing();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        enableEditing();
                    }
                }
        );

        requestQueue.add(insertNewPersonRequest);
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


    private void goToMain() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goToLogin(View view) {
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void goBack(View view) {
        finish();
    }
}
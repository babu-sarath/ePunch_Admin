package com.scb.epunchv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.scb.epunchv2.UtilsService.SharedPrefClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddEmployee extends AppCompatActivity {
    String nameStr,email2Str,pwd2Str,positionStr;
    EditText name,email2,pwd2,position;
    ProgressDialog progressDialog;
    SharedPrefClass sharedPrefClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee);

        name=findViewById(R.id.name);
        email2=findViewById(R.id.email2);
        pwd2=findViewById(R.id.pwd2);
        position=findViewById(R.id.position);
        sharedPrefClass=new SharedPrefClass(getApplicationContext());
        progressDialog=new ProgressDialog(AddEmployee.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Creating new employee");
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs=getSharedPreferences("user_prefs_epunch", Activity.MODE_PRIVATE);
        if(!prefs.contains("token")){
            startActivity(new Intent(AddEmployee.this, MainActivity.class));
            finish();
        }
    }

    public void create(View view) {
        progressDialog.show();

        nameStr=name.getText().toString();
        email2Str=email2.getText().toString();
        pwd2Str=pwd2.getText().toString();
        positionStr=position.getText().toString();

        HashMap<String, String> data=new HashMap<>();
        data.put("name",nameStr);
        data.put("email",email2Str);
        data.put("password",pwd2Str);
        data.put("position",positionStr);

        String apiKey="https://epunchapp.herokuapp.com/auth/create";

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),response.getString("msg"),Toast.LENGTH_LONG).show();
                            name.getText().clear();
                            email2.getText().clear();
                            pwd2.getText().clear();
                            position.getText().clear();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response=error.networkResponse;
                if(error instanceof ServerError && response!=null){
                    try{
                        String res=new String(response.data, HttpHeaderParser.parseCharset(response.headers, "UTF-8"));
                        JSONObject object=new JSONObject(res);
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),object.getString("msg"),Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        Log.d("SERVER ERROR ",e.getMessage());
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorized",sharedPrefClass.getValue_string("token"));
                return headers;
            }
        };

        //retry the stuff
        RetryPolicy retryPolicy=new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);

        //adding the request
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);
    }

    public void goBack(View view) {
        startActivity(new Intent(AddEmployee.this,Home.class));
        finish();
    }
}
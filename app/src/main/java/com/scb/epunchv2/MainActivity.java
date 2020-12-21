package com.scb.epunchv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class MainActivity extends AppCompatActivity {
    EditText email,pwd;
    String emailStr,pwdStr;
    SharedPrefClass sharedPrefClass;
    ProgressDialog progressDialog;
    RetryPolicy retryPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email=findViewById(R.id.email);
        pwd=findViewById(R.id.pwd);
        retryPolicy=new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        sharedPrefClass=new SharedPrefClass(this);
        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs=getSharedPreferences("user_prefs_epunch", Activity.MODE_PRIVATE);
        if(prefs.contains("token")){
            //go to dash
            startActivity(new Intent(MainActivity.this, Home.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    public void login(View view) {
        emailStr=email.getText().toString();
        pwdStr=pwd.getText().toString();
        progressDialog.show();
        HashMap<String, String> data=new HashMap<>();
        data.put("email",emailStr);
        data.put("password",pwdStr);

        String apiKey="https://epunchapp.herokuapp.com/auth/login";

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")){
                                progressDialog.dismiss();
                                sharedPrefClass.setValue_string("token",response.getString("token"));
                                JSONObject jsonObject=response.getJSONObject("user");
                                sharedPrefClass.setValue_string("username",jsonObject.getString("name"));
                                sharedPrefClass.setValue_string("email",jsonObject.getString("email"));
                                startActivity(new Intent(MainActivity.this, Home.class));
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                NetworkResponse response=error.networkResponse;
                if(error instanceof ServerError && response!=null){
                    try{
                        String res=new String(response.data, HttpHeaderParser.parseCharset(response.headers, "UTF-8"));
                        JSONObject object=new JSONObject(res);
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
                return headers;
            }
        };

        //retry the stuff
        jsonObjectRequest.setRetryPolicy(retryPolicy);

        //adding the request
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public void forgotPassword(View view) {
        final View layout=getLayoutInflater().inflate(R.layout.forgot_password,null);
        EditText email=layout.findViewById(R.id.email);
        Button send=layout.findViewById(R.id.send);
        Button cancel=layout.findViewById(R.id.cancel);
        ProgressBar progressBar=layout.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setView(layout);
        AlertDialog alertDialog=builder.create();
        alertDialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr=email.getText().toString();
                if(TextUtils.isEmpty(emailStr)){
                    email.setError("Enter your email");
                }
                else {
                    sendPasswordReset(emailStr,email,cancel,send,alertDialog,progressBar);
                }
            }
        });
    }

    private void sendPasswordReset(String emailStr, EditText email, Button cancel, Button send, AlertDialog alertDialog, ProgressBar progressBar) {
        email.setEnabled(false);
        cancel.setEnabled(false);
        send.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        String apiKey="https://epunchapp.herokuapp.com/reset/forgot/"+emailStr;
        Log.d("VOLLEY","Inside main func");
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, apiKey, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VOLLEY",response.toString());
                        try {
                            if(response.getBoolean("success")){
                                Toast.makeText(getApplicationContext(),response.getString("msg"),Toast.LENGTH_LONG).show();
                                alertDialog.dismiss();
                            }
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
                        Toast.makeText(getApplicationContext(),object.getString("msg"),Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        email.setEnabled(true);
                        cancel.setEnabled(true);
                        send.setEnabled(true);
                    }catch (Exception e){
                        Log.d("SERVER ERROR ",e.getMessage());
                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorized", sharedPrefClass.getValue_string("token"));
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(retryPolicy);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        requestQueue.add(jsonObjectRequest);
    }
}
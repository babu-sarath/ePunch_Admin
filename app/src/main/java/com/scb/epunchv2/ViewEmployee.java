package com.scb.epunchv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Measure;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
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
import com.scb.epunchv2.Adapters.UsersAdaptor;
import com.scb.epunchv2.UtilsService.SharedPrefClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewEmployee extends AppCompatActivity {
    SharedPrefClass sharedPrefClass;
    List<String> id=new ArrayList<>();
    List<String> name=new ArrayList<>();
    List<String> email=new ArrayList<>();
    List<String> positionList =new ArrayList<>();
    List<String> grace =new ArrayList<>();
    RetryPolicy retryPolicy;
    ListView listView;
    ProgressDialog progressDialog;
    EditText search;
    TextView empty_list_item;
    UsersAdaptor usersAdaptor;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employee);
        listView=findViewById(R.id.listView);
        search=findViewById(R.id.search);
        empty_list_item=findViewById(R.id.empty_list_item);
        sharedPrefClass=new SharedPrefClass(getApplicationContext());
//        retryPolicy=new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        retryPolicy=new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        progressDialog=new ProgressDialog(ViewEmployee.this,R.style.CustomAlertDialog);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Retrieving data");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDialog(position);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ViewEmployee.this.arrayAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setEmptyView(findViewById(R.id.empty_list_item));
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs=getSharedPreferences("user_prefs_epunch", Activity.MODE_PRIVATE);
        if(!prefs.contains("token")){
            startActivity(new Intent(ViewEmployee.this, MainActivity.class));
            finish();
        }else {
            getInfo();
        }

    }

    private void getInfo() {
        progressDialog.show();
        Log.d("VOLLEY","called function");
        String apiKey="https://epunchapp.herokuapp.com/view/listAll";
        id.clear();name.clear();email.clear();grace.clear();
        positionList.clear();
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, apiKey, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")){
                                JSONArray jsonArray=response.getJSONArray("data");
                                Log.d("VOLLEY","success on if");
                                for(int i=0;i<jsonArray.length();i++){
                                    Log.d("VOLLEY","inside loop");
                                    JSONObject jsonObject= jsonArray.getJSONObject(i);
                                    id.add(jsonObject.getString("_id"));
                                    name.add(jsonObject.getString("name"));
                                    email.add(jsonObject.getString("email"));
                                    positionList.add(jsonObject.getString("position"));
                                    grace.add(jsonObject.getString("grace"));
                                }
//                                usersAdaptor=new UsersAdaptor(id,name, positionList,getApplicationContext());
                                arrayAdapter= new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,name);
                                listView.setAdapter(arrayAdapter);
                                Log.d("VOLLEY","success on if");
                                progressDialog.dismiss();
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
                        progressDialog.dismiss();
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
        jsonObjectRequest.setRetryPolicy(retryPolicy);

        //adding the request
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);
    }

    private void showEditDialog(int position) {

        final View dialogView=getLayoutInflater().inflate(R.layout.dialog_user_edit,null);
        EditText nameEt=(EditText) dialogView.findViewById(R.id.name);
        EditText emailEt=(EditText) dialogView.findViewById(R.id.email);
        EditText positionEt=(EditText) dialogView.findViewById(R.id.position);
        TextView graceTv=(TextView)dialogView.findViewById(R.id.grace);

        nameEt.setText(name.get(position));
        emailEt.setText(email.get(position));
        positionEt.setText(positionList.get(position));
        graceTv.setText(String.format("Grace remaining this month is %s", grace.get(position)));

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do the delete
                        String apiKey,uid;
                        uid=id.get(position);
                        sendVolleyDeleteRequest(uid);
                    }
                })
                .setNegativeButton("Save Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName,newEmail,newPosition,apiKey,uid;

                        newName=nameEt.getText().toString();
                        newEmail=emailEt.getText().toString();
                        newPosition=positionEt.getText().toString();
                        if(newName.equals(name.get(position)) && newEmail.equals(email.get(position)) && newPosition.equals(positionList.get(position))){
                            Toast.makeText(getApplicationContext(),"No changes made",Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }else if(TextUtils.isEmpty(newName) || TextUtils.isEmpty(newEmail) || TextUtils.isEmpty(newPosition)){
                            //setting error
                            Toast.makeText(getApplicationContext(),"Do not leave any fields empty",Toast.LENGTH_LONG).show();
                        }else {
                            //do the update
                            uid=id.get(position);
                            HashMap<String,String> data=new HashMap<>();
                            data.put("name",newName);
                            data.put("email",newEmail);
                            data.put("position",newPosition);
                            sendVolleyUpdateRequest(data,uid);
                        }
                    }
                })
                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(dialogView)
                .setMessage("Use this to edit the existing user")
                .setTitle("Edit Panel");
        AlertDialog alertDialog=builder.create();
//        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    private void sendVolleyDeleteRequest(String uid) {
        progressDialog.show();
        String apiKey="https://epunchapp.herokuapp.com/auth/delete/"+uid;
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.DELETE, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")){
                        getInfo();
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),response.getString("msg"),Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorized",sharedPrefClass.getValue_string("token"));
                return headers;
            }
        };
        //retry the stuff
       jsonObjectRequest.setRetryPolicy(retryPolicy);

        //adding the request
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);
    }

    private void sendVolleyUpdateRequest(HashMap<String, String> data, String uid) {
        progressDialog.show();
        String apiKey="https://epunchapp.herokuapp.com/auth/update/"+uid;
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.PUT, apiKey, new JSONObject(data), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")){
                        getInfo();
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),response.getString("msg"),Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorized",sharedPrefClass.getValue_string("token"));
                return headers;
            }
        };

        //retry the stuff
        jsonObjectRequest.setRetryPolicy(retryPolicy);

        //adding the request
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);
    }

    public void goBack(View view) {
        startActivity(new Intent(ViewEmployee.this,Home.class));
        finish();
    }
}
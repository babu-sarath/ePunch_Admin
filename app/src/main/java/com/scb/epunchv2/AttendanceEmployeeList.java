package com.scb.epunchv2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import com.scb.epunchv2.UtilsService.SharedPrefClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceEmployeeList extends AppCompatActivity {
    SharedPrefClass sharedPrefClass;
    List<String> id=new ArrayList<>();
    List<String> name=new ArrayList<>();
    List<String> email=new ArrayList<>();
    int socketTime= 3000;
    RetryPolicy retryPolicy;
    ListView listView;
    ProgressDialog progressDialog;
    ArrayAdapter<String> arrayAdapter;
    TextView empty_list_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_employee_list);

        listView=findViewById(R.id.listView);
        empty_list_item=findViewById(R.id.empty_list_item);
        sharedPrefClass=new SharedPrefClass(getApplicationContext());
//        retryPolicy=new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        retryPolicy=new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        progressDialog=new ProgressDialog(AttendanceEmployeeList.this,R.style.CustomAlertDialog);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Retrieving data");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long ID) {
                Intent intent=new Intent(AttendanceEmployeeList.this,AttendanceEmployee.class);
                intent.putExtra("email",email.get(position));
                intent.putExtra("name",name.get(position));
                startActivity(intent);
            }
        });
        listView.setEmptyView(findViewById(R.id.empty_list_item));
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs=getSharedPreferences("user_prefs_epunch", Activity.MODE_PRIVATE);
        if(!prefs.contains("token")){
            startActivity(new Intent(AttendanceEmployeeList.this, MainActivity.class));
            finish();
        }else {
            getInfo();
        }

    }

    public void goBack(View view) {
        startActivity(new Intent(AttendanceEmployeeList.this,Home.class));
        finish();
    }

    private void getInfo() {
        progressDialog.show();
        Log.d("VOLLEY","called function");
        String apiKey="https://epunchapp.herokuapp.com/view/listAll";
        id.clear();name.clear();
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
                                }
//                                UsersAdaptor usersAdaptor=new UsersAdaptor(id,name, positionList,getApplicationContext());
                                arrayAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,name);
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
            //Caching the json Data
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 5 * 60 * 1000; // in 5 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 60 * 60 * 1000; // in 1 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), cacheEntry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

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

}
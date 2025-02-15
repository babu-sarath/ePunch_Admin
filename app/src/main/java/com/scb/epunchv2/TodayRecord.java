package com.scb.epunchv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodayRecord extends AppCompatActivity {
    SharedPrefClass sharedPrefClass;
    List<String> id=new ArrayList<>();
    List<String> name=new ArrayList<>();
    List<String> in_time=new ArrayList<>();
    List<String> out_time=new ArrayList<>();
    List<String> type=new ArrayList<>();
    int socketTime= 3000;
    RetryPolicy retryPolicy;
    ListView listView;
    ProgressDialog progressDialog;
    ArrayAdapter<String> arrayAdapter;
    TextView empty_list_item,dayTv,dateTv,timeTv,notPunchedIn;
    Handler handler = new Handler();
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_record);
        listView=findViewById(R.id.listView);
        empty_list_item=findViewById(R.id.empty_list_item);
        notPunchedIn=findViewById(R.id.notPunchedIn);
        dayTv=findViewById(R.id.dayTv);
        dateTv=findViewById(R.id.dateTv);
        timeTv=findViewById(R.id.timeTv);
        sharedPrefClass=new SharedPrefClass(getApplicationContext());
//        retryPolicy=new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        retryPolicy=new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        progressDialog=new ProgressDialog(TodayRecord.this,R.style.CustomAlertDialog);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Retrieving data");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createRecordDialog(name.get(position),in_time.get(position),out_time.get(position),type.get(position));
            }
        });
        listView.setEmptyView(findViewById(R.id.empty_list_item));
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs=getSharedPreferences("user_prefs_epunch", Activity.MODE_PRIVATE);
        if(!prefs.contains("token")){
            startActivity(new Intent(TodayRecord.this, MainActivity.class));
            finish();
        }else {
            getTime();
            getInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void getTime() {
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm aa");
        Calendar calendar = Calendar.getInstance();
        String[] days = new String[] { "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" };
        dayTv.setText(days[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        dateTv.setText(dateFormatter.format(date));
        timeTv.setText(timeFormatter.format(date));

        final int delay = 10 * 1000; // 1000 milliseconds == 1 second
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("THREAD","RAN");
                Date dateForTime = new Date();
                timeTv.setText(timeFormatter.format(dateForTime));
                handler.postDelayed(this, delay);
            }
        };
        handler.postDelayed(runnable,delay);
    }

    private void getInfo() {
        progressDialog.show();
        String apiKey="https://epunchapp.herokuapp.com/punches/today/"+sharedPrefClass.getValue_string("email");
        id.clear();name.clear();in_time.clear();out_time.clear();
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, apiKey, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")){
                                JSONArray jsonArray=response.getJSONArray("enteredData");
                                notPunchedIn.setText(response.getString("notEntered"));
                                for(int i=0;i<jsonArray.length();i++){
                                    JSONObject jsonObject= jsonArray.getJSONObject(i);
                                    id.add(jsonObject.getString("_id"));
                                    name.add(jsonObject.getString("username"));
                                    in_time.add(jsonObject.getString("in_time"));
                                    out_time.add(jsonObject.getString("out_time"));
                                    type.add(jsonObject.getString("entry"));
                                }
//                                UsersAdaptor usersAdaptor=new UsersAdaptor(id,name, positionList,getApplicationContext());
                                arrayAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,name);
                                listView.setAdapter(arrayAdapter);
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

    private void createRecordDialog(String Name, String inTime, String outTime, String Type) {
        Integer[] iconVal={R.drawable.ic_present,R.drawable.ic_late,R.drawable.ic_half,R.drawable.ic_absent};
        final View dialogView=getLayoutInflater().inflate(R.layout.today_dialog,null);
        TextView nameTv=(TextView) dialogView.findViewById(R.id.name);
        TextView typeTv=(TextView) dialogView.findViewById(R.id.type);
        TextView checkinTv=(TextView) dialogView.findViewById(R.id.checkin);
        TextView checkoutTv=(TextView) dialogView.findViewById(R.id.checkout);
        ImageView icon=(ImageView)dialogView.findViewById(R.id.icon);

        nameTv.setText(Name);
        checkinTv.setText(inTime);
        checkoutTv.setText(outTime);
        typeTv.setText(Type);
        switch(Type){
            case "ONTIME":icon.setImageResource(iconVal[0]);
                break;
            case "LATE":icon.setImageResource(iconVal[1]);
                break;
            case "HALF-DAY":icon.setImageResource(iconVal[2]);
                break;
            case "ABSENT":icon.setImageResource(iconVal[3]);
                break;
        }

        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.CustomAlertDialog);
        builder.setCancelable(true)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(dialogView);
        AlertDialog alertDialog=builder.create();
//        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    public void goBack(View view) {
        startActivity(new Intent(TodayRecord.this,Home.class));
        finish();
    }

}
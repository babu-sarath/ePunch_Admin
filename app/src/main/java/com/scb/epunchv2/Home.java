package com.scb.epunchv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scb.epunchv2.Adapters.AdminHomeAdapter;
import com.scb.epunchv2.UtilsService.SharedPrefClass;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Home extends AppCompatActivity {
    TextView greeting,name;
    ListView listView;
    SharedPrefClass sharedPrefClass;
    String[] title ={"Add Employee","View Employee","Today Record","Attendance Report"};
    String[] description ={"Create a new employee for  application","View list of all registered employees","Check the employees presence or punch in/out","View the list of all the previous attendance logs"};
    Integer[] imgs={R.drawable.ic_employee,R.drawable.ic_list,R.drawable.ic_today,R.drawable.ic_history};
    AdminHomeAdapter adminHomeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPrefClass=new SharedPrefClass(this);
        greeting=findViewById(R.id.greeting);
        name=findViewById(R.id.name);
        listView=findViewById(R.id.listView);
        loadList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: startActivity(new Intent(Home.this,AddEmployee.class));
                        break;
                    case 1: startActivity(new Intent(Home.this,ViewEmployee.class));
                        break;
                    case 2: startActivity(new Intent(Home.this,TodayRecord.class));
                        break;
                    case 3: startActivity(new Intent(Home.this,AttendanceEmployee.class));
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs=getSharedPreferences("user_prefs_epunch", Activity.MODE_PRIVATE);
        if(!prefs.contains("token")){
            startActivity(new Intent(Home.this, MainActivity.class));
            finish();
        }else {
            wish();
            name.setText(sharedPrefClass.getValue_string("username"));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    private void loadList() {
        adminHomeAdapter=new AdminHomeAdapter(this,title,description,imgs);
        listView.setAdapter(adminHomeAdapter);
    }

    private void wish() {
//        DateFormat dateFormat=new SimpleDateFormat("dd MMMM yyyy");
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12)
            greeting.setText("Good Morning,");
        else if(timeOfDay >= 12 && timeOfDay < 16)
            greeting.setText("Good Afternoon,");
        else if(timeOfDay >= 16 && timeOfDay < 21)
            greeting.setText("Good Evening,");
        else if(timeOfDay >= 21 && timeOfDay < 24)
            greeting.setText("Good Night,");

//        dateTv.setText(String.format("Today is %s", dateFormat.format(date)));
    }

    public void settings(View view) {
        startActivity(new Intent(this, Settings.class));
    }
}
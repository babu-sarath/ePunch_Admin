package com.scb.epunchv2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scb.epunchv2.R;

import java.util.ArrayList;
import java.util.List;

public class UsersAdaptor extends BaseAdapter {

    List<String> id=new ArrayList<>();
    List<String> name=new ArrayList<>();
    List<String> positionList =new ArrayList<>();
    LayoutInflater inflater;
    private Context context;

    public UsersAdaptor(List<String> id, List<String> name, List<String> position, Context context) {
        this.id = id;
        this.name = name;
        this.positionList = position;
        this.context = context;
        inflater=(LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return id.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView=inflater.inflate(R.layout.list_users,null);
        TextView idTv=convertView.findViewById(R.id.id);
        TextView nameTv=convertView.findViewById(R.id.name);
        TextView positionTv=convertView.findViewById(R.id.position);
        idTv.setText(id.get(position));
        nameTv.setText(name.get(position));
        positionTv.setText(positionList.get(position));
        return convertView;
    }
}

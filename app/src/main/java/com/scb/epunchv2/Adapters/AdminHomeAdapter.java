package com.scb.epunchv2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.scb.epunchv2.R;

public class AdminHomeAdapter extends BaseAdapter {
    private Context context;
    private String[] title,description;
    Integer[] image;
    LayoutInflater inflater;

    public AdminHomeAdapter(Context context, String[] title, String[] description, Integer[] image) {
        this.context = context;
        this.title = title;
        this.description = description;
        this.image = image;
        inflater=(LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return title.length;
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
        convertView=inflater.inflate(R.layout.list_admin_home,null);
        TextView titleTv=convertView.findViewById(R.id.title);
        TextView descriptionTv=convertView.findViewById(R.id.description);
        ImageView imageIv=convertView.findViewById(R.id.image);
        titleTv.setText(title[position]);
        descriptionTv.setText(description[position]);
        imageIv.setImageResource(image[position]);
        return convertView;
    }
}

package com.example.user.cs496_002;


import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomViewAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    private List<String> dataset, filtered;

    public CustomViewAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.filtered = objects;
        this.dataset = new ArrayList<>();
        dataset.addAll(filtered);
    }


    @Override
    public int getCount() {
        return filtered.size();
    }

    @Override
    public Object getItem(int i) {
        return filtered.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        TextView text_name;
        ImageView img_person;

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.listview_item, viewGroup, false);
        }

        text_name = (TextView) convertView.findViewById(R.id.list_item_name);
        img_person = (ImageView) convertView.findViewById(R.id.list_item_image);

        text_name.setText((String) getItem(i));
        img_person.setImageResource(R.mipmap.icon_person);

        return convertView;

    }

    public void filter(String str) {
        filtered.clear();
        if(str.length() == 0) {
            filtered.addAll(dataset);
        } else {
            for(String item: dataset) {
                if(item.toLowerCase().contains(str)) {
                    filtered.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

}
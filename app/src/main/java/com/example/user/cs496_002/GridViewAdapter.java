package com.example.user.cs496_002;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter<Bitmap> {

    private Context mContext;
    private int layoutResourceId;
    private ArrayList<Bitmap> data;

    public GridViewAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Bitmap> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.layoutResourceId = resource;
        this.data = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.img);

        imageView.setImageBitmap(getItem(position));

        return convertView;

    }
}

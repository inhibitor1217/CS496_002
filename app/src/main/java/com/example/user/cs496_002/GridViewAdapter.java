package com.example.user.cs496_002;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int layoutResourceId;
    private ArrayList<String> data;

    private ImageView imageView;

    public GridViewAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
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

        imageView = (ImageView) convertView.findViewById(R.id.img);

        File file = new File(Environment.getExternalStorageDirectory().toString() + "/CS496_caches/" + getItem(position));

        // cache hit
        if(file.exists()) {
            imageView.setImageURI(Uri.fromFile(file));
        } else {
            new GetSingelItemTask(getItem(position)).execute();
        }

        return convertView;

    }

    private void saveImageBitmap(Bitmap bitmap, String id) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/CS496_caches");
        myDir.mkdirs();

        File file = new File(myDir, id);

        if(file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class GetSingelItemTask extends AsyncTask {

        private String id;

        public GetSingelItemTask(String id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            String jsonResponse = "";

            try {

                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://13.125.74.66:8082/api/images/" + id;
                URI url = new URI(urlString);

                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                JSONObject obj = new JSONObject(jsonResponse);

                String obj_id           = obj.getString("_id");
                String parsedString     = obj.getString("parsedString");

                byte[] decodedString = Base64.decode(parsedString, Base64.DEFAULT);

                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                saveImageBitmap(bitmap, obj_id);

                return true;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Object o) {
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/CS496_caches/" + id);
            imageView.setImageURI(Uri.fromFile(file));
        }
    }

}

package com.example.user.cs496_002;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TabFragment2 extends Fragment {

    private GridView gridView;

    private FloatingActionButton folderButton;

    private GridViewAdapter adapter;

    private ArrayList<String> idset;

    private final int REQ_CODE_SELECT_IMAGE = 51;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View resultView = inflater.inflate(R.layout.tab_fragment_2, container, false);

        idset = new ArrayList<>();

        gridView = (GridView) resultView.findViewById(R.id.gridView);

        adapter = new GridViewAdapter(getActivity(), R.layout.grid_item, idset);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getActivity(), GridPopupActivity.class);

                intent.putExtra("id", (String) adapterView.getAdapter().getItem(i));

                startActivity(intent);

            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {

                AlertDialog.Builder del_btn = new AlertDialog.Builder(getActivity());
                del_btn.setMessage("이미지를 삭제하시겠습니까?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteTask(idset.get(position)).execute();
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                del_btn.show();

                return true;

            }
        });

        folderButton = (FloatingActionButton) resultView.findViewById(R.id.button_folder);

        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.READ_EXTERNAL_STORAGE_ALLOWED) {

                    // collect image from local device
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);

                    // (continued to onActivityResult)

                } else {
                    Toast.makeText(getActivity(), "서버 접속을 위해 [설정]>[애플리케이션 관리]에서 저장소 접속 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

        });

        synchronizeWithServer();

        return resultView;

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQ_CODE_SELECT_IMAGE) {
            if(resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                    new EncodeBitmapAndPostTask(bitmap).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    private void saveImageBitmap(Bitmap bitmap, String id) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/CS496_caches");
        myDir.mkdirs();

        File file = new File(myDir, id);

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

    private void synchronizeWithServer() {

        new GetTask().execute();

    }

    private class EncodeBitmapAndPostTask extends AsyncTask {

        private Bitmap bitmap;

        public EncodeBitmapAndPostTask(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(getActivity(), "업로드 중...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOS);

            String encodedString = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);

            String jsonResponse = "";

            try {

                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://13.125.74.66:8082/api/images/";
                URI url = new URI(urlString);

                HttpPost httpPost = new HttpPost(url);

                // include parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("parsedString", encodedString));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPost.setEntity(ent);

                HttpResponse response = httpClient.execute(httpPost);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                Log.e("jsonResponse", jsonResponse);

                // parse response to verify success
                JSONObject obj = new JSONObject(jsonResponse);
                return obj.getInt("result");

            } catch (IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "서버 DB에 접속할 수 없습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return 0;

        }

        @Override
        protected void onPostExecute(Object o) {
            int code = (int) o;
            if(code == 1) {
                Toast.makeText(getActivity(), "업로드 완료", Toast.LENGTH_SHORT).show();
                synchronizeWithServer();
            } else {
                Toast.makeText(getActivity(), "업로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class GetTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {

            String jsonResponse = "";
            idset = new ArrayList<>();

            try {

                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://13.125.74.66:8082/api/images/";
                URI url = new URI(urlString);

                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                // parse JSON String into HashMap
                JSONArray arr = new JSONArray(jsonResponse);
                for(int i = 0; i < arr.length(); i++) {

                    String obj_id           = arr.getJSONObject(i).getString("_id");
                    String parsedString     = arr.getJSONObject(i).getString("parsedString");

                    byte[] decodedString = Base64.decode(parsedString, Base64.DEFAULT);

                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    saveImageBitmap(bitmap, obj_id);

                    idset.add(obj_id);

                }
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
            boolean success = (boolean) o;
            if(success) {
                adapter = new GridViewAdapter(getActivity(), R.layout.grid_item, idset);
                gridView.setAdapter(adapter);
                // Toast.makeText(getActivity(), "갱신 완료", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(getActivity(), "서버 DB에 접속할 수 없습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
        }

    }

    private class DeleteTask extends AsyncTask {

        private String id;
        private final int SUCCESS = 1;
        private final int DATABASE_FAILURE = 2;
        private final int EXCEPTION_OCCURED = 3;

        public DeleteTask(String id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            String jsonResponse = "";

            try {

                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://13.125.74.66:8082/api/images/" + id;
                URI url = new URI(urlString);

                HttpDelete httpDelete = new HttpDelete(url);
                HttpResponse response = httpClient.execute(httpDelete);

                if(response.getEntity() == null) return SUCCESS;
                else return DATABASE_FAILURE;

            } catch (IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "서버 DB에 접속할 수 없습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            return EXCEPTION_OCCURED;

        }

        @Override
        protected void onPostExecute(Object o) {
            int code = (int) o;
            if(code != SUCCESS) {
                // Toast.makeText(getActivity(), "error: UpdateTask with errorcode" + code, Toast.LENGTH_SHORT).show();
            } else {
                synchronizeWithServer();
            }
        }
    }

}

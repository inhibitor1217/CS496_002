package com.example.user.cs496_002;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class TabFragment1 extends Fragment {

    private FloatingActionButton folderButton, facebookButton;

    private EditText editTextSearch;

    private ListView listView;

    private HashMap<String, Contact> contact_list;
    private ArrayList<String> name_list;

    private CustomViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View returnView = inflater.inflate(R.layout.tab_fragment_1, container, false);

        folderButton   = (FloatingActionButton) returnView.findViewById(R.id.button_folder);
        facebookButton = (FloatingActionButton) returnView.findViewById(R.id.button_facebook);

        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
                // retrieve contact information from local device

                // upload information to server
                //   - new contacts should be added using POST query
                //   - existing contacts should be updated using PUT query

                // (done) synchronize with server
                synchronizeWithServer();
            }
        });

        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO\
                // facebook login

                // retrieve contact information from user's facebook account

                // upload information to server
                //   - new contacts should be added using POST query
                //   - existing contacts should be updated using PUT query

                // (done) synchronize with server
                synchronizeWithServer();
            }
        });


        editTextSearch = (EditText) returnView.findViewById(R.id.edittext_search);

        // Modifies 'return key' of EditText to remove focus
        editTextSearch.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editTextSearch.setImeActionLabel("", EditorInfo.IME_ACTION_DONE);
        editTextSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);

        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event == null) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // Capture soft enters in a singleLine EditText that is the last EditText
                        // This one is useful for the new list case, when there are no existing ListItems
                        editTextSearch.clearFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    } else if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        // Capture soft enters in other singleLine EditTexts
                    } else if (actionId == EditorInfo.IME_ACTION_GO) {
                    } else {
                        // Let the system handle all other null KeyEvents
                        return false;
                    }
                } else if (actionId == EditorInfo.IME_NULL) {
                    // Capture most soft enters in multi-line EditTexts and all hard enters;
                    // They supply a zero actionId and a valid keyEvent rather than
                    // a non-zero actionId and a null event like the previous cases.
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        // We capture the event when the key is first pressed.
                    } else {
                        // We consume the event when the key is released.
                        return true;
                    }
                } else {
                    // We let the system handle it when the listener is triggered by something that
                    // wasn't an enter.
                    return false;
                }
                return true;
            }
        });

        // grants search feature for list view
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = editTextSearch.getText().toString().toLowerCase();
                adapter.filter(text);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        listView = (ListView) returnView.findViewById(R.id.listview_contact);

        // creates new popup activity when item is clicked by user
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ContactPopUpAcitivity.class);
                intent.putExtra("name", (String) adapterView.getAdapter().getItem(i));
                intent.putExtra("phone", contact_list.get(adapterView.getAdapter().getItem(i)).phone);
                intent.putExtra("email", contact_list.get(adapterView.getAdapter().getItem(i)).email);
                intent.putExtra("facebook", contact_list.get(adapterView.getAdapter().getItem(i)).facebook);
                startActivity(intent);
            }
        });

        // initial synchronization with server
        synchronizeWithServer();

        return returnView;

    }

    public void synchronizeWithServer() {
        if (MainActivity.INTERNET_ALLOWED) {
            new SynchronizeTask().execute();
        } else {
            Toast.makeText(getActivity(), "서버 접속을 위해 [설정]>[애플리케이션 관리]에서 관련 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private class SynchronizeTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {

            contact_list = new HashMap<String, Contact>();
            String jsonResponse = "";
            try {

                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://13.125.74.66:8082/api/contacts/";
                URI url = new URI(urlString);

                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                // parse JSON String into HashMap
                JSONArray arr = new JSONArray(jsonResponse);
                for(int i = 0; i < arr.length(); i++) {

                    String obj_id           = arr.getJSONObject(i).getString("_id");
                    String obj_name         = arr.getJSONObject(i).getString("name");
                    String obj_phone        = arr.getJSONObject(i).getString("phone");
                    String obj_email        = arr.getJSONObject(i).getString("email");
                    String obj_facebook     = arr.getJSONObject(i).getString("facebook");
                    String obj_profileImage = arr.getJSONObject(i).getString("profileImage");

                    contact_list.put(obj_name, new Contact(obj_name, obj_phone, obj_email, obj_facebook, obj_profileImage));

                }
                return true;

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
            return false;

        }

        @Override
        protected void onPostExecute(Object o) {
            name_list = new ArrayList<>(contact_list.keySet());
            adapter = new CustomViewAdapter(getActivity(), R.layout.listview_item, name_list);
            listView.setAdapter(adapter);
            if((Boolean) o) Toast.makeText(getActivity(), "갱신 완료!", Toast.LENGTH_SHORT).show();
        }
    }

}

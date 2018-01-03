package com.example.user.cs496_002;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabFragment1 extends Fragment {

    private FloatingActionButton folderButton, facebookButton;

    private EditText editTextSearch;

    private ListView listView;

    private HashMap<String, Contact> contact_list;
    private ArrayList<String> name_list;

    private CustomViewAdapter adapter;

    private static int UPDATE_COUNTER, UPDATE_MAX;

    private CallbackManager callbackManager;

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
                HashMap<String, Contact> localContact = readContactFromLocal();

                new CheckConnectionAndContinueTask(localContact).execute();

            }
        });

        facebookButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (AccessToken.getCurrentAccessToken() != null) {
                    Log.d("Tag", "Logout");
                    LoginManager.getInstance().logOut();
                } else {
                    callbackManager = CallbackManager.Factory.create();
                    LoginManager.getInstance().logInWithReadPermissions(TabFragment1.this, Arrays.asList("public_profile", "user_friends"));
                    // Log.d("Tag", "myaong");
                    LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            GraphRequest request = new GraphRequest(loginResult.getAccessToken(),
                                    "/me/taggable_friends",
                                    null,
                                    HttpMethod.GET,
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            // Log.d("response", response.toString());
                                            JSONObject object = response.getJSONObject();
                                            getData(object);
                                            GraphRequest nextReq = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                                            if(nextReq != null){
                                                nextReq.setCallback(this);
                                                nextReq.executeAsync();
                                            }
                                        }
                                    });

                            Bundle paramaters = new Bundle();
                            paramaters.putString("fields", "name");
                            request.setParameters(paramaters);
                            request.executeAsync();

                        }

                        @Override
                        public void onCancel() {
                            // Log.d("Tag", "실패");
                        }

                        @Override
                        public void onError(FacebookException error) {
                            // Log.d("Tag", "error");
                        }
                    });
                }
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
                String name = (String) adapterView.getAdapter().getItem(i);
                intent.putExtra("name", name);
                intent.putExtra("phone", contact_list.get(name).phone);
                intent.putExtra("email", contact_list.get(name).email);
                intent.putExtra("facebook", contact_list.get(name).facebook);
                startActivity(intent);
            }
        });

        // deletes contact from server when item is long-clicked
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder del_btn = new AlertDialog.Builder(getActivity());
                del_btn.setMessage("연락처를 삭제하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {
                                String name = (String) adapterView.getAdapter().getItem(i);
                                new FindByNameAndContinueTask(name, contact_list.get(name), FindByNameAndContinueTask.DELETE).execute();
                            };
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                    }
                });
                del_btn.show();
                return true;
            }
        });

        // initial synchronization with server
        synchronizeWithServer();

        return returnView;

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
        // Log.d("myaong", permissionNeeds.toString());
        synchronizeWithServer();
    }

    public HashMap<String, Contact> readContactFromLocal() {

        HashMap<String, Contact> ret = new HashMap<>();

        if(MainActivity.READ_CONTACTS_ALLOWED) {

            Cursor c = getActivity().getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " asc");

            while (c.moveToNext()) {

                String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));

                Contact contact = new Contact(name);

                Cursor cursorPhone = getActivity().getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                        null, null);

                if (cursorPhone.moveToFirst()) {
                    contact.setPhone(cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                }

                Cursor cursorEmail = getActivity().getContentResolver().query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                        null, null);

                if (cursorEmail.moveToFirst()) {
                    contact.setEmail(cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
                }

                ret.put(name, contact);

                cursorPhone.close();
                cursorEmail.close();

            }

        } else {
            Toast.makeText(getActivity(), "연락처 접근을 위해 [설정]>[애플리케이션 관리]에서 주소록 접근 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show();
        }

        return ret;

    }

    private void getData(JSONObject object) {

        try {
            StringBuilder names = new StringBuilder();
            JSONArray jsonArrayFriends = object.getJSONArray("data");
            JSONObject jsonObjectPages = object.getJSONObject("paging");

            UPDATE_COUNTER = 0;
            UPDATE_MAX = jsonArrayFriends.length();
            for (int i = 0; i < jsonArrayFriends.length(); i++) {
                String name = jsonArrayFriends.getJSONObject(i).getString("name");
                updateContactToServer(name, new Contact(name));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void updateContactToServer(String name, Contact contact) {

        if(MainActivity.INTERNET_ALLOWED) {
            name = name.replace(' ', '+');
            new FindByNameAndContinueTask(name, contact, FindByNameAndContinueTask.POST_OR_UPDATE).execute();
        } else {
            Toast.makeText(getActivity(), "서버 접속을 위해 [설정]>[애플리케이션 관리]에서 인터넷 접속 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show();
        }

    }

    public void synchronizeWithServer() {
        if (MainActivity.INTERNET_ALLOWED) {
            new SynchronizeTask().execute();
        } else {
            Toast.makeText(getActivity(), "서버 접속을 위해 [설정]>[애플리케이션 관리]에서 인터넷 접속 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private class CheckConnectionAndContinueTask extends AsyncTask {

        private HashMap<String, Contact> mHashMap;

        public CheckConnectionAndContinueTask(HashMap<String, Contact> mHashMap) {
            this.mHashMap = mHashMap;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String jsonResponse = "";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://13.125.74.66:8082/api/contacts/";
                URI url = new URI(urlString);
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Object o) {
            boolean chkConnection = (Boolean) o;
            if(chkConnection) {
                UPDATE_COUNTER = 0;
                UPDATE_MAX = mHashMap.size();
                // upload information to server and synchronize
                for (Map.Entry<String, Contact> entry : mHashMap.entrySet()) {
                    updateContactToServer(entry.getKey(), entry.getValue());
                }
            } else {
                Toast.makeText(getActivity(), "서버 DB에 접속할 수 없습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FindByNameAndContinueTask extends AsyncTask {

        private String mName;
        private Contact mContact;
        private int next;

        public static final int POST_OR_UPDATE = 1;
        public static final int DELETE = 2;

        public FindByNameAndContinueTask(String name, Contact contact, int next) {
            this.mName = name;
            this.mContact = contact;
            this.next = next;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            String jsonResponse = "";

            try {

                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://13.125.74.66:8082/api/contacts/name/" + mName;
                URI url = new URI(urlString);

                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                if(jsonResponse.contains("contact not found")) {
                    return null;
                } else {

                    JSONArray arr = new JSONArray(jsonResponse);

                    // TODO
                    // for now, we retrieve first contact information with given name.
                    // we may improve this (i.e. check similarity)

                    return arr.getJSONObject(0).getString("_id");

                }

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

            return null;

        }

        @Override
        protected void onPostExecute(Object o) {
            switch(next) {
                case POST_OR_UPDATE:
                    if(o == null) {
                        new PostTask(mContact).execute();
                    }
                    else {
                        new UpdateTask((String) o, mContact).execute();
                    }
                    break;
                case DELETE:
                    new DeleteTask((String) o).execute();
                    break;
            }

        }
    }

    private class UpdateTask extends AsyncTask {

        private String id;
        private Contact mContact;

        private final int UPDATE_SUCCESS = 1;
        private final int DATABASE_FAILURE = 2;
        private final int CONTACT_NOT_FOUND = 3;
        private final int FAILED_TO_UPDATE = 4;

        public UpdateTask(String id, Contact contact) {
            this.id = id;
            this.mContact = contact;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            String jsonResponse = "";

            try {

                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://13.125.74.66:8082/api/contacts/" + id;
                URI url = new URI(urlString);

                HttpPut httpPut = new HttpPut(url);

                List<NameValuePair> params = new ArrayList<>();
                if(mContact.name.equals(""))         params.add(new BasicNameValuePair("name"        , mContact.name));
                if(mContact.phone.equals(""))        params.add(new BasicNameValuePair("phone"       , mContact.phone));
                if(mContact.email.equals(""))        params.add(new BasicNameValuePair("email"       , mContact.email));
                if(mContact.facebook.equals(""))     params.add(new BasicNameValuePair("facebook"    , mContact.facebook));
                if(mContact.profileImage.equals("")) params.add(new BasicNameValuePair("profileImage", mContact.profileImage));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPut.setEntity(ent);

                HttpResponse response = httpClient.execute(httpPut);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                if(jsonResponse.contains("update successful")) {
                    return UPDATE_SUCCESS;
                } else if(jsonResponse.contains("database failure")) {
                    return DATABASE_FAILURE;
                } else if(jsonResponse.contains("contact not found")) {
                    return CONTACT_NOT_FOUND;
                } else if(jsonResponse.contains("failed to update")) {
                    return FAILED_TO_UPDATE;
                } else {
                    return null;
                }

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

            return null;

        }

        @Override
        protected void onPostExecute(Object o) {
            if(o == null) {
                // Toast.makeText(getActivity(), "error: UpdateTask", Toast.LENGTH_SHORT).show();
            } else {
                int code = (int) o;
                if(code != UPDATE_SUCCESS) {
                    // Toast.makeText(getActivity(), "error: UpdateTask with errorcode" + code, Toast.LENGTH_SHORT).show();
                } else {
                    UPDATE_COUNTER++;
                    if(UPDATE_COUNTER == UPDATE_MAX) synchronizeWithServer();
                }
            }
        }
    }

    private class PostTask extends AsyncTask {

        private Contact mContact;

        public PostTask(Contact mContact) {
            this.mContact = mContact;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            String jsonResponse = "";

            try {

                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://13.125.74.66:8082/api/contacts/";
                URI url = new URI(urlString);

                HttpPost httpPost = new HttpPost(url);

                // include parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("name"        , mContact.name));
                params.add(new BasicNameValuePair("phone"       , mContact.phone));
                params.add(new BasicNameValuePair("email"       , mContact.email));
                params.add(new BasicNameValuePair("facebook"    , mContact.facebook));
                params.add(new BasicNameValuePair("profileImage", mContact.profileImage));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPost.setEntity(ent);

                HttpResponse response = httpClient.execute(httpPost);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

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
            if((int) o == 1) {
                UPDATE_COUNTER++;
                if(UPDATE_COUNTER == UPDATE_MAX) synchronizeWithServer();
            }
            else {
                // Toast.makeText(getActivity(), "error : PostTask", Toast.LENGTH_SHORT).show();
            }
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
                String urlString = "http://13.125.74.66:8082/api/contacts/" + id;
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

                    obj_name = obj_name.replace('+', ' ');

                    contact_list.put(obj_name, new Contact(obj_name, obj_phone, obj_email, obj_facebook, obj_profileImage));

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
            name_list = new ArrayList<>(contact_list.keySet());
            adapter = new CustomViewAdapter(getActivity(), R.layout.listview_item, name_list);
            listView.setAdapter(adapter);
            if((Boolean) o) {
                Toast.makeText(getActivity(), "갱신 완료", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(getActivity(), "서버 DB에 접속할 수 없습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

}

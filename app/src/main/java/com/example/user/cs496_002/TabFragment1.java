package com.example.user.cs496_002;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TabFragment1 extends Fragment {

    private FloatingActionButton folderButton, facebookButton;

    private EditText editTextSearch;

    private ListView listView;

    private CustomViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View returnView = inflater.inflate(R.layout.tab_fragment_1, container, false);

        folderButton = (FloatingActionButton) returnView.findViewById(R.id.button_folder);
        facebookButton = (FloatingActionButton) returnView.findViewById(R.id.button_facebook);

        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "click : folder button", Toast.LENGTH_SHORT).show();
            }
        });

        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "click : facebook button", Toast.LENGTH_SHORT).show();
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

            }
        });

        // TODO
        // get contact information from local repository and facebook server, and pass it here

        // remove dummy array when finished
        ArrayList<Pair<String, String>> contact_list = new ArrayList<>();
        contact_list.add(Pair.create("김경훈", "010-1111-1111"));
        contact_list.add(Pair.create("김유현", "010-2222-2222"));
        contact_list.add(Pair.create("남휘종", "010-3333-3333"));
        contact_list.add(Pair.create("신아영", "010-4444-4444"));
        contact_list.add(Pair.create("오현민", "010-5555-5555"));
        contact_list.add(Pair.create("장동민", "010-6666-6666"));
        contact_list.add(Pair.create("하연주", "010-7777-7777"));
        contact_list.add(Pair.create("최연승", "010-8888-8888"));
        contact_list.add(Pair.create("유수진", "010-9999-9999"));

        Collections.sort(contact_list, new Comparator<Pair<String, String>>() {
            @Override
            public int compare(Pair<String, String> t1, Pair<String, String> t2) {
                return t1.first.compareTo(t2.first);
            }
        });

        ArrayList<String> name_list = new ArrayList<>();
        for(Pair<String, String> person: contact_list) {
            name_list.add(person.first);
        }

        adapter = new CustomViewAdapter(getActivity(), R.layout.listview_item, name_list);
        listView.setAdapter(adapter);

        return returnView;

    }

}

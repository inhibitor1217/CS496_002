package com.example.user.cs496_002;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

public class TabFragment2 extends Fragment {

    private GridView gridView;

    private FloatingActionButton folderButton;

    private GridViewAdapter adapter;
    private ArrayList<Bitmap> dataset;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View resultView = inflater.inflate(R.layout.tab_fragment_2, container, false);

        dataset = new ArrayList<>();

        gridView = (GridView) resultView.findViewById(R.id.gridView);

        adapter = new GridViewAdapter(getActivity(), R.layout.grid_item, dataset);
        gridView.setAdapter(adapter);

        folderButton = (FloatingActionButton) resultView.findViewById(R.id.button_folder);

        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // collect image from local device
                // parse images and post to server
                // retrieve image data from server, in parsed form
                // parse data to Bitmap form

                if(MainActivity.READ_EXTERNAL_STORAGE_ALLOWED) {

                } else {
                    Toast.makeText(getActivity(), "서버 접속을 위해 [설정]>[애플리케이션 관리]에서 저장소 접속 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show();
                }

            }

        });

        return resultView;

    }

}

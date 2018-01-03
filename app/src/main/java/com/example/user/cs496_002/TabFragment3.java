package com.example.user.cs496_002;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class TabFragment3 extends Fragment {

    private Socket socket;

    private Camera mCamera;
    private CameraPreview cameraPreview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        try {
            socket = IO.socket("http://13.125.74.66:8080/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisConnect);
        socket.on("newImage", onImageReceived);

        socket.connect();

        if(MainActivity.CAMERA_ALLOWED) {
            // live camera
            mCamera = Camera.open();

            if(mCamera != null) {
                cameraPreview = new CameraPreview(getActivity(), mCamera, socket);
            }

            cameraPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes, Camera camera) {

                            String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);

                            socket.emit("image", encodedString);
                            Toast.makeText(getActivity(), "사진을 서버에 전송했습니다!", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            });

            return cameraPreview;
        }

        return inflater.inflate(R.layout.tab_fragment_3, container, false);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisConnect);
        socket.off("newImage", onImageReceived);
        mCamera.release();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socket.emit("user connected", "test");
                    Toast.makeText(getActivity(), "connected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Emitter.Listener onDisConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socket.emit("user disconnected", "test");
                    Toast.makeText(getActivity(), "disconnected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Emitter.Listener onImageReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String encodedString = (String) args[0];

                    final byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);

                    final String id = Integer.toString(new Random().nextInt(10000000));
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);

                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    saveImageBitmap(bitmap, id);

                    AlertDialog.Builder request = new AlertDialog.Builder(getActivity());
                    request.setMessage("누군가가 사진을 찍었습니다. 확인하시겠습니까?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getActivity(), ReceivedImageActivity.class);
                            intent.putExtra("id", id);
                            startActivity(intent);
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // ignores
                        }
                    });
                    request.show();


                }
            });
        }
    };

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

}

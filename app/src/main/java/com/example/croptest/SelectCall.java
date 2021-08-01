package com.example.croptest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.List;

public class SelectCall extends AppCompatActivity {

    private Boolean isPermission = false;
    private ImageView iv_GetMainImg;

    private RelativeLayout rl_loding;

    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;

    private Bitmap thum_bitmap = null;
    private Bitmap main_bitmap = null;

    private File photo_file;
    private String phtoFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_call);

        iv_GetMainImg = (ImageView) findViewById(R.id.iv_GetMainImg);
        rl_loding = (RelativeLayout) findViewById(R.id.rl_loding);

        tedPermission();

        iv_GetMainImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isPermission) {
                    makeDialog();
                } else {
                    Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    class QuestImageTask extends AsyncTask<Integer, Integer, Integer> {

        private Intent data ;
        private int requestCode;
        private int resultCode;
        private  Intent next_ativity_intent;

        QuestImageTask( Intent data, int requestCode, int resultCode ){
            this.data = data;
            this.requestCode = requestCode;
            this.resultCode = resultCode;
        }

        @Override
        protected void onPreExecute() {
            rl_loding.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            next_ativity_intent = new Intent(getBaseContext(), MainActivity.class);
            next_ativity_intent.putExtra("resultCode", resultCode);
            next_ativity_intent.putExtra("requestCode", requestCode);

            if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
                try {

                    next_ativity_intent.putExtra("data", data);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }  else if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
                next_ativity_intent.putExtra("photo_file", phtoFilePath);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(Integer integer) {
            rl_loding.setVisibility(View.INVISIBLE);
            startActivity(next_ativity_intent);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        QuestImageTask questImageTask = new QuestImageTask(data, requestCode, resultCode);
        questImageTask.execute();
    }

    //https://github.com/ArthurHub/Android-Image-Cropper
    private void imgGelleryCall(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    private void takePhoto() {
        File sdcard = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        photo_file = new File(sdcard, "capture.jpg");
        phtoFilePath = photo_file.getAbsolutePath();

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, "com.bignerdranch.android.test.fileprovider", photo_file));
        startActivityForResult(intent, PICK_FROM_CAMERA);

    }

    private void makeDialog(){

        Dialog dialogView = new Dialog(this);
        dialogView.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogView.setContentView(R.layout.dialog_picture_select);
        dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tv_Gellery = dialogView.findViewById(R.id.tv_Gellery);
        tv_Gellery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgGelleryCall();
                dialogView.dismiss();
            }
        });

        TextView tv_phto = dialogView.findViewById(R.id.tv_phto);
        tv_phto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
                dialogView.dismiss();
            }
        });

        TextView tv_cancle = dialogView.findViewById(R.id.tv_cancle);
        tv_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    iv_GetMainImg.setImageResource(R.drawable.img);

                    if(thum_bitmap !=null){
                        thum_bitmap = null;
                    }

                    if(main_bitmap !=null){
                        main_bitmap = null;
                    }

                dialogView.dismiss();
            }
        });

        dialogView.show();
    }

    /**
     *  권한 설정
     */
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                // 권한 요청 실패
                isPermission = false;
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }
}
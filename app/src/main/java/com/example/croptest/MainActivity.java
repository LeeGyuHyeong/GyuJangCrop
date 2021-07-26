package com.example.croptest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_cropArea;
    private ImageView iv_target;
    private ImageView iv_result;

    private SeekBar sb_squerSize;

    int mDegree = 0;

    private float startPointX;
    private float startPointY;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView btn_crop = (ImageView) findViewById(R.id.btn_crop);
        ImageView btn_rotae = (ImageView) findViewById(R.id.btn_rotae);

        iv_target = (ImageView) findViewById(R.id.iv_target);
        iv_result = (ImageView) findViewById(R.id.iv_result);
        iv_cropArea = (ImageView) findViewById(R.id.iv_cropArea);

        sb_squerSize = (SeekBar) findViewById(R.id.sb_squerSize);


        iv_target.post(initTargetImage());


        //크롭 상자 크기 조절
        sb_squerSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("Tag1", progress+"");

                iv_cropArea.getLayoutParams().width = progress;
                iv_cropArea.getLayoutParams().height = progress;
                iv_cropArea.requestLayout();

                iv_cropArea.setX( iv_target.getX() + (iv_target.getWidth()/2 - iv_cropArea.getWidth()/2) );
                iv_cropArea.setY(iv_target.getY() + (iv_target.getHeight()/2 - iv_cropArea.getHeight()/2) );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        //90도 회전
        btn_rotae.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDegree = 90;
                iv_target.setImageBitmap(rotatImage( ((BitmapDrawable) iv_target.getDrawable()).getBitmap(),mDegree )  );
                iv_target.post(initTargetImage());
               // iv_target.post(initTargetImage());
            }
        });

        // 크롭 영역만큼 자르기
        btn_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bigPictureBitmap = ((BitmapDrawable)iv_target.getDrawable()).getBitmap();

                float ratioWidth = bigPictureBitmap.getWidth() / (float)iv_target.getWidth();
                float ratioHeight = bigPictureBitmap.getHeight() / (float)iv_target.getHeight() ;
                Log.i("Tag1", "--ratio w h " + ratioWidth + "," + ratioHeight);
                Log.i("Tag1", "--bitmap w h " + bigPictureBitmap.getWidth() + "," + bigPictureBitmap.getHeight());


                Bitmap result = Bitmap.createBitmap(bigPictureBitmap,
                        (int)((iv_cropArea.getX()*ratioWidth)-(iv_target.getX()*ratioWidth)) ,(int)((iv_cropArea.getY()*ratioHeight)-(iv_target.getY()*ratioHeight)),
                        (int)(iv_cropArea.getWidth()*ratioWidth),(int)(iv_cropArea.getHeight()*ratioHeight) );

                iv_result.setImageBitmap(result);
            }
        });

        //크롭 이미지 이벤트 - 이동, 특정 영역(자르려고하는 이미지 영역) 벗어남 방지
        //https://m.blog.naver.com/PostView.naver?isHttpsRedirect=true&blogId=tkddlf4209&logNo=220734131855
        iv_cropArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startPointX = event.getRawX()-v.getX();
                    startPointY = event.getRawY()-v.getY();

                    Log.i("Tag1", "--ACTION_DOWN v xy " + v.getX() + "," + v.getY());
                    Log.i("Tag1", "--ACTION_DOWN raw xy " + event.getRawX() + "," + event.getRawY());
                    Log.i("Tag1", "--ACTION_DOWN startPoint xy " + startPointX + "," + startPointY);

                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    v.setX( (event.getRawX()-startPointX ) * +1.0f );
                    v.setY( (event.getRawY()-startPointY ) * +1.0f );

                    // 상측 왼쪽 모서리 에 닿았을때,
                    if(iv_target.getY() > v.getY() && iv_target.getX() > v.getX() ){
                        v.setX( iv_target.getX() );
                        v.setY( iv_target.getY() );
                    }

                    //하측 왼쪽 모서리에 닿았을 때
                    if( (iv_target.getY()+iv_target.getHeight() < v.getY()+iv_cropArea.getHeight()) && (iv_target.getX() > v.getX()) ){
                        v.setX( iv_target.getX() );
                        v.setY( iv_target.getY()+iv_target.getHeight()-iv_cropArea.getHeight( ) );
                    }

                    //상층 오른쪽 모서리에 닿았을 때
                    if(iv_target.getY() > v.getY() && (iv_target.getX()+iv_target.getWidth() < v.getX()+iv_cropArea.getWidth()) ){
                        v.setX( iv_target.getX()+iv_target.getWidth() -iv_cropArea.getWidth() );
                        v.setY( iv_target.getY() );
                    }

                    //하측 오른쪽 모서리에 닿았을 때
                    if( (iv_target.getY()+iv_target.getHeight() < v.getY()+iv_cropArea.getHeight()) && (iv_target.getX()+iv_target.getWidth() < v.getX()+iv_cropArea.getWidth()) ){
                        v.setX( iv_target.getX()+iv_target.getWidth() -iv_cropArea.getWidth() );
                        v.setY( iv_target.getY()+iv_target.getHeight()-iv_cropArea.getHeight() );
                    }

                    //상층 면에 닿았을때
                    if(iv_target.getY() > v.getY()){
                        v.setX( (event.getRawX()-startPointX ) * +1.0f );
                        v.setY( iv_target.getY() );
                    }

                    //왼쪽 면에 닿았을때
                    if(iv_target.getX() > v.getX()){
                        v.setX( iv_target.getX() );
                        v.setY( (event.getRawY()-startPointY ) * +1.0f );
                    }

                    //하측 면에 닿았을때
                    if(iv_target.getY()+iv_target.getHeight() < v.getY()+iv_cropArea.getHeight()){
                        v.setX( (event.getRawX()-startPointX ) * +1.0f );
                        v.setY( iv_target.getY()+iv_target.getHeight()-iv_cropArea.getHeight( ) );
                    }

                    //오른쪽 면에 닿았을때
                    if(iv_target.getX()+iv_target.getWidth() < v.getX()+iv_cropArea.getWidth()){
                        v.setX( iv_target.getX()+iv_target.getWidth() -iv_cropArea.getWidth() );
                        v.setY( (event.getRawY()-startPointY ) * +1.0f );
                    }


                    Log.i("Tag1", "---ACTION_MOVE v xy " + v.getX() + "," + v.getY());
                    Log.i("Tag1", "ACTION_MOVE raw xy " + event.getRawX() + "," + event.getRawY());
                    Log.i("Tag1", "ACTION_MOVE startPoint xy " + startPointX + "," + startPointY);

                }
                return true;
            }
        });

    }

    //비트맵을 해당 degree값만큼 회전시켜서 새로 만든다.
    private Bitmap rotatImage(Bitmap src, float degree){

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0,0, src.getWidth(), src.getHeight(), matrix, true);
    }

    //컷팅 상자 크기를 이미지의 가로 세로 중 작은 변의 길이를 구해서 정사각형으로 만듬.
    private Runnable initTargetImage(){
        return new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                Log.i("Tag1", "run--iv_target.getLayoutParams().width :" + iv_target.getLayoutParams().width);
                Log.i("Tag1", "run--iv_target.getLayoutParams().height :" + iv_target.getLayoutParams().height);

                Log.i("Tag1", "run--iv_target.getWidth():" + iv_target.getWidth());
                Log.i("Tag1", "run--iv_target.getHeight() :" + iv_target.getHeight());

                if(iv_target.getWidth() > iv_target.getHeight()){
                    iv_cropArea.getLayoutParams().width = iv_target.getHeight();
                    iv_cropArea.getLayoutParams().height = iv_target.getHeight();

                    sb_squerSize.setMin( (iv_target.getHeight()/5) );
                    sb_squerSize.setMax(iv_target.getHeight());
                    Log.i("Tag1", "iv_canvas2.getLayoutParams().height :" + iv_cropArea.getLayoutParams().height);
                } else {
                    iv_cropArea.getLayoutParams().width = iv_target.getWidth();
                    iv_cropArea.getLayoutParams().height = iv_target.getWidth();

                    sb_squerSize.setMin( (iv_target.getWidth()/5) );
                    sb_squerSize.setMax(iv_target.getWidth());
                    Log.i("Tag1", "iv_canvas2.getLayoutParams().width :" + iv_cropArea.getLayoutParams().width);
                }

                iv_cropArea.requestLayout();
            }
        };
    }

}
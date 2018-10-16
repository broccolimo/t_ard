package com.yzmc.activity;

import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.VideoClient;

import java.io.IOException;

public class _Video extends AppCompatActivity {
    private boolean flag = false;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video);
        AllActivity.addActivity(this);
        surfaceView = findViewById(R.id.video);
        surfaceView.setZOrderOnTop(true);
        new Task().execute();
    }

    @Override
    protected void onResume() {
        if(flag){
            setContentView(R.layout.video);
            surfaceView = findViewById(R.id.video);
            surfaceView.setZOrderOnTop(true);
            new Task().execute();
        }
        flag = true;
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(_Video.this, Main.class);
        startActivity(intent);
    }

    private class Task extends AsyncTask{
        private MediaCodec mediaCodec_video;
        private MediaFormat mediaFormat_video;
        private SurfaceHolder surfaceHolder;

        @Override
        protected Object doInBackground(Object[] objects){
            try {
                mediaCodec_video = MediaCodec.createDecoderByType("video/avc");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaFormat_video = MediaFormat.createVideoFormat("video/avc", 400, 400);
            surfaceHolder = surfaceView.getHolder();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            mediaCodec_video.configure(mediaFormat_video, surfaceHolder.getSurface(), null, 0);
            mediaCodec_video.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //new VideoClient().Start("1ZRZ25ELW8BDDSWA111A", mediaCodec_video, null, null);

                    //new VideoClient().Start("C9SNG69HN5DV26J4111A", mediaCodec_video, null, null);
                    //杭州办公室
                    new VideoClient().Start("MXJJ1HR83R5ZGLN3111A", mediaCodec_video, null, null);
                    //new VideoClient().Start("6CM22MUB1WUPLTAC111A", mediaCodec_video, null, null);
                }
            }).start();
        }
    }
}

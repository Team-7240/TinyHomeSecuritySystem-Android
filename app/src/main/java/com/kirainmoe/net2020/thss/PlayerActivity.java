package com.kirainmoe.net2020.thss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerActivity extends AppCompatActivity {
    public StandardGSYVideoPlayer videoPlayer;
    OrientationUtils orientationUtils;

    private String name;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        ip = intent.getStringExtra(ip);

        setTitle("摄像头 " + name + " 的画面");

        init();
    }

    private void init() {
        videoPlayer = (StandardGSYVideoPlayer) findViewById(R.id.videoPlayer);

        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_media_types", "video"); //根据媒体类型来配置
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);

        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);

        SharedPreferences sharedPref = getSharedPreferences("THSS", Context.MODE_PRIVATE);
        String server = sharedPref.getString("server", "");
        if (server.equals(""))
            return;
        System.out.println(server);
        System.out.println(name);
        String url = "rtmp://" + server + "/live/" + name;
        System.out.println(url);
        try {
            videoPlayer.setUp(url, true, "Camera Scene");
            videoPlayer.getTitleTextView().setVisibility(View.GONE);
            videoPlayer.startPlayLogic();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "播放视频时发生问题，可能是摄像头已下线。", Toast.LENGTH_SHORT).show();
        }
    }
}
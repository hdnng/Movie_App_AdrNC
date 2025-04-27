package com.example.movieapp;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerActivity extends AppCompatActivity {

//    private YouTubePlayerView youTubePlayerView;
    private VideoView v;
    private String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


//        youTubePlayerView = findViewById(R.id.youtube_player_view);
        v = findViewById(R.id.videoView);
//        getLifecycle().addObserver(youTubePlayerView);

        String fullUrl = getIntent().getStringExtra("videoUrl"); // lấy link từ intent
        Log.d("PlayerActivity", "Full URL nhận: " + fullUrl);

//        videoId = extractYoutubeVideoId(fullUrl);
//        Log.d("PlayerActivity", "Video ID cắt được: " + videoId);

//        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
//            @Override
//            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
//                if (videoId != null && !videoId.isEmpty()) {
//                    youTubePlayer.loadVideo(videoId, 0);
//                } else {
//                    Toast.makeText(PlayerActivity.this, "Không lấy được Video ID", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


        if (fullUrl != null && !fullUrl.isEmpty()) {
            Uri uri = Uri.parse(fullUrl);
            v.setVideoURI(uri);

            // Thêm MediaController để có nút Play, Pause, tua
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(v);
            v.setMediaController(mediaController);

            // Bắt đầu phát
            v.start();
        } else {
            Toast.makeText(this, "Không có link video!", Toast.LENGTH_SHORT).show();
        }
    }

//    private String extractYoutubeVideoId(String url) {
//        if (url == null || url.trim().isEmpty()) return null;
//        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
//        Pattern compiledPattern = Pattern.compile(pattern);
//        Matcher matcher = compiledPattern.matcher(url);
//        if (matcher.find()) {
//            return matcher.group();
//        }
//        return null;
//    }
}

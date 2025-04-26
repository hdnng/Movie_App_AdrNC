package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.movieapp.model.Episode;
import com.example.movieapp.model.Movie;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity {

    TextView title, description, year, tvGenres;;
    ImageView thumbnail;
    Button btnPlay;
    LinearLayout episodesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String movieId = getIntent().getStringExtra("movieId");

        title = findViewById(R.id.detail_title);
        description = findViewById(R.id.detail_description);
        year = findViewById(R.id.detail_year);
        thumbnail = findViewById(R.id.detail_thumbnail);
        tvGenres = findViewById(R.id.tvGenres);
        btnPlay = findViewById(R.id.btnPlay);
        episodesLayout = findViewById(R.id.episodesLayout);

        // Lấy dữ liệu từ Intent

        Intent intent = getIntent();

// Lấy thêm dữ liệu từ Intent
        boolean isSeries = intent.getBooleanExtra("isSeries", false);
        ArrayList<Episode> episodes = (ArrayList<Episode>) intent.getSerializableExtra("episodes"); // Nếu có

        if (isSeries) {
            // Truy vấn collection con EPISODES của movie đó
            db.collection("MOVIES")
                    .document(movieId)
                    .collection("EPISODES")
                    .orderBy("episodeNumber") // Nếu muốn hiện theo số tập
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            TextView noEpisodes = new TextView(this);
                            noEpisodes.setText("Chưa có danh sách tập phim.");
                            episodesLayout.addView(noEpisodes);
                        } else {
                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                String title = doc.getString("title");
                                Long episodeNumber = doc.getLong("episodeNumber");
                                String videoUrl = doc.getString("videoUrl");

                                TextView episodeText = new TextView(this);
                                episodeText.setText(episodeNumber + ". " + title);
                                episodeText.setTextSize(16);
                                episodeText.setPadding(8, 8, 8, 8);
                                episodesLayout.addView(episodeText);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải tập phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Nếu là phim lẻ thì hiện nút Play
            btnPlay.setVisibility(View.VISIBLE);
//            btnPlay.setOnClickListener(v -> {
//                // Ở đây bạn có thể mở video bằng intent hoặc chuyển qua PlayerActivity
//                String videoUrl = intent.getStringExtra("videoUrl");
//                // Ví dụ: mở trang phát video
//                Intent playIntent = new Intent(MovieDetailActivity.this, PlayerActivity.class);
//                playIntent.putExtra("videoUrl", videoUrl);
//                startActivity(playIntent);
//            });
        }



        String movieTitle = getIntent().getStringExtra("title");
        String movieDesc = getIntent().getStringExtra("description");
        int movieYear = getIntent().getIntExtra("year", 0);
        String movieThumb = getIntent().getStringExtra("thumbnail");

        ArrayList<String> genres = intent.getStringArrayListExtra("genres");

        if (genres != null && !genres.isEmpty()) {
            tvGenres.setText("Genres: " + String.join(", ", genres));  // Hiển thị các thể loại cách nhau bằng dấu phẩy
        } else {
            tvGenres.setText("Genres: Chưa có thông tin thể loại");
        }

        // Gán dữ liệu
        title.setText(movieTitle);
        description.setText(movieDesc);
        year.setText(String.valueOf(movieYear));
        Glide.with(this).load(movieThumb).into(thumbnail);
    }

}

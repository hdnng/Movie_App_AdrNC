package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.movieapp.model.Episode;
import com.example.movieapp.model.Movie;

import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity {

    TextView title, description, year, tvGenres;;
    ImageView thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        title = findViewById(R.id.detail_title);
        description = findViewById(R.id.detail_description);
        year = findViewById(R.id.detail_year);
        thumbnail = findViewById(R.id.detail_thumbnail);
        tvGenres = findViewById(R.id.tvGenres);

        // Lấy dữ liệu từ Intent

        Intent intent = getIntent();
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

    private void displayMovieDetails(Movie movie) {
        // Hiển thị thông tin phim (title, description, v.v.)

        if (movie.isSeries() && movie.getEpisodes() != null) {
            for (Episode episode : movie.getEpisodes()) {
                TextView episodeText = new TextView(this);
                episodeText.setText(episode.getEpisodeNumber() + ". " + episode.getTitle());
                episodeText.setTextSize(16);
                episodeText.setPadding(8, 8, 8, 8);
                //episodesLayout.addView(episodeText);
            }
        }
    }

}

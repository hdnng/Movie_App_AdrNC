package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    private TextView title, description, year, tvGenres;
    private ImageView thumbnail;
    private Button btnPlay;
    private ImageButton btnFavorite;
    private LinearLayout episodesLayout;
    private String movieId;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        initViews();
        loadMovieData();
        ifSeries();
        favoriteMovie();
        clickFavoriteMovie();
    }

    private void initViews(){
        db = FirebaseFirestore.getInstance();
        movieId = getIntent().getStringExtra("movieId");
        title = findViewById(R.id.detail_title);
        description = findViewById(R.id.detail_description);
        year = findViewById(R.id.detail_year);
        thumbnail = findViewById(R.id.detail_thumbnail);
        tvGenres = findViewById(R.id.tvGenres);
        btnPlay = findViewById(R.id.btnPlay);
        btnFavorite =findViewById(R.id.btnFavoriteMovie);
        episodesLayout = findViewById(R.id.episodesLayout);
    }

    private void setupListeners(){}

    private void loadMovieData(){
        Intent intent = getIntent();
        String movieTitle = intent.getStringExtra("title");
        String movieDesc = intent.getStringExtra("description");
        int movieYear = intent.getIntExtra("year", 0);
        String movieThumb = intent.getStringExtra("thumbnail");
        ArrayList<String> genres = intent.getStringArrayListExtra("genres");
        // Gán dữ liệu
        title.setText(movieTitle);
        description.setText(movieDesc);
        year.setText(String.valueOf(movieYear));
        Glide.with(this).load(movieThumb).into(thumbnail);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (genres != null && !genres.isEmpty()) {
            tvGenres.setText("Genres: " + String.join(", ", genres));  // Hiển thị các thể loại cách nhau bằng dấu phẩy
        } else {
            tvGenres.setText("Genres: Chưa có thông tin thể loại");
        }
    }

    private void ifSeries(){
        boolean isSeries = getIntent().getBooleanExtra("isSeries", false);
        if (isSeries) {
            btnPlay.setVisibility(View.GONE);
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
                                // Tạo nút Play cho mỗi tập
                                Button episodeButton = new Button(this);
                                episodeButton.setText(episodeNumber + ". " + title);
                                episodeButton.setTextSize(16);
                                episodeButton.setPadding(8, 8, 8, 8);
                                episodeButton.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_light));
                                //set margin
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(0, 0, 0, 16); // 16dp khoảng cách dưới
                                episodeButton.setLayoutParams(params);
                                episodeButton.setOnClickListener(v -> {
                                    Intent playIntent = new Intent(MovieDetailActivity.this, PlayerActivity.class);
                                    playIntent.putExtra("videoUrl", videoUrl); // Gửi videoUrl của tập phim
                                    startActivity(playIntent);
                                });
                                episodesLayout.addView(episodeButton);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải tập phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            btnPlay.setOnClickListener(v -> {
                String videoUrl = getIntent().getStringExtra("videoUrl");
                Intent playIntent = new Intent(MovieDetailActivity.this, PlayerActivity.class);
                playIntent.putExtra("videoUrl", videoUrl);
                startActivity(playIntent);
            });
        }
    }


    //hien thi icon yeu thich phim
    public void favoriteMovie(){
        if (user != null) {
            String uid = user.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> favorite = (List<String>) documentSnapshot.get("favoriteMovie");
                            if (favorite != null && favorite.contains(movieId)) {
                                btnFavorite.setImageResource(R.drawable.favoritechecked); // Đã yêu thích
                            } else {
                                btnFavorite.setImageResource(R.drawable.favorite); // Chưa yêu thích
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải favorite: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    //Thêm phim vào yêu thích
    public void addFavoriteMovie(boolean isAdd) {
        if (user != null) {
            String uid = user.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> favorite = (List<String>) documentSnapshot.get("favoriteMovie");
                            if (favorite == null) {
                                favorite = new ArrayList<>();
                            }
                            if (isAdd) {
                                // Thêm movieId vào danh sách nếu chưa có
                                if (!favorite.contains(movieId)) {
                                    favorite.add(movieId);
                                }
                            } else {
                                // Gỡ movieId ra khỏi danh sách nếu đã có
                                favorite.remove(movieId);
                            }
                            // Cập nhật lại field favoriteMovie
                            db.collection("users").document(uid)
                                    .update("favoriteMovie", favorite)
                                    .addOnSuccessListener(aVoid -> {
                                        if (isAdd) {
                                            btnFavorite.setImageResource(R.drawable.favoritechecked);
                                            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                                        } else {
                                            btnFavorite.setImageResource(R.drawable.favorite);
                                            Toast.makeText(this, "Đã gỡ khỏi yêu thích", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    //click yeu thich phim
    public void clickFavoriteMovie() {
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    String uid = user.getUid();
                    db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    List<String> favorite = (List<String>) documentSnapshot.get("favoriteMovie");
                                    if (favorite == null) {
                                        favorite = new ArrayList<>();
                                    }

                                    if (favorite.contains(movieId)) {
                                        // Đã có -> gỡ ra
                                        addFavoriteMovie(false);
                                    } else {
                                        // Chưa có -> thêm vào
                                        addFavoriteMovie(true);
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MovieDetailActivity.this, "Lỗi khi kiểm tra yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });
    }
}

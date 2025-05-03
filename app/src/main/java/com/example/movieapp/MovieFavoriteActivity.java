package com.example.movieapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.adapter.MovieAdapter;
import com.example.movieapp.model.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovieFavoriteActivity extends AppCompatActivity {

    private DrawerLayout drawerLayoutFavorite;
    private ImageView menu;
    private TextView hello;

    //Hiện Movie
    private RecyclerView recyclerView;
    private List<Movie> movieList;
    private MovieAdapter movieAdapter;

    private EditText searchEditText;
    private LinearLayout logout,movie,series,type,home;
    private FirebaseUser user;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_favorite);

        initViews();
        setupListeners();
        loadUsername();
        loadMovies();
    }

    private void initViews(){
        recyclerView = findViewById(R.id.recyclerViewMoviesFavorite);
        //Cấu hình adapter
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList,movie->openDetail(movie));
        searchEditText = findViewById(R.id.searchEditText);
        menu = findViewById(R.id.menu);
        logout = findViewById(R.id.logout);
        movie = findViewById(R.id.movie);
        type = findViewById(R.id.type);
        series = findViewById(R.id.tvSeries);
        home = findViewById(R.id.homepage);
        drawerLayoutFavorite = findViewById(R.id.drawerLayoutFavorite);
        hello = findViewById(R.id.hello);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    private  void setupListeners(){
        searchEditText.setVisibility(View.GONE);
        menu.setOnClickListener(view -> openDrawer(drawerLayoutFavorite));
        movie.setOnClickListener(v ->{
            startActivity(new Intent(MovieFavoriteActivity.this, MovieSingleActivity.class));
            finish();
        });
        type.setOnClickListener(v ->{
            startActivity(new Intent(MovieFavoriteActivity.this, MovieTypeActivity.class));
            finish();
        });

        home.setOnClickListener(v ->{
            startActivity(new Intent(MovieFavoriteActivity.this, MainActivity.class));
            finish();
        });
        series.setOnClickListener(v ->{
            startActivity(new Intent(MovieFavoriteActivity.this, MovieSeriesActivity.class));
            finish();
        });
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MovieFavoriteActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadUsername(){
        if (user != null) {
            String uid = user.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            hello.setText("Xin chào: " + username);
                        }
                    })
                    .addOnFailureListener(e -> {
                        hello.setText("Lỗi khi lấy thông tin người dùng");
                    });
        }
    }

    //Hiện danh sách và click details
    private void loadMovies() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Chia lm 2 cột

        // Lấy danh sách favoriteMovies của người dùng
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Lấy danh sách các ID phim yêu thích của người dùng
                        List<String> favoriteMovies = (List<String>) documentSnapshot.get("favoriteMovie");

                        if (favoriteMovies != null && !favoriteMovies.isEmpty()) {
                            // Tiến hành tải danh sách phim từ Firestore
                            db.collection("MOVIES")
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        List<Movie> allMovies = new ArrayList<>();
                                        for (QueryDocumentSnapshot doc : querySnapshot) {
                                            Movie movie = doc.toObject(Movie.class);
                                            movie.setId(doc.getId());

                                            // Kiểm tra nếu phim này có trong danh sách yêu thích
                                            if (favoriteMovies.contains(movie.getId())) {
                                                allMovies.add(movie);
                                            }
                                        }
                                        // Kiểm tra nếu không có phim nào trong danh sách yêu thích
                                        if (allMovies.isEmpty()) {
                                            Toast.makeText(this, "Không có phim yêu thích nào để hiển thị", Toast.LENGTH_SHORT).show();
                                            return;  // Dừng lại và không thực hiện tiếp các thao tác
                                        }
                                        movieList.clear(); // Làm sạch danh sách cũ
                                        movieList.addAll(allMovies); // Thêm phim yêu thích vào danh sách
                                        movieAdapter.notifyDataSetChanged();
                                        recyclerView.setAdapter(movieAdapter);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Lỗi tải phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Bạn chưa thêm phim yêu thích nào", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lấy dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openDetail(Movie movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movieId", movie.getId());
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("description", movie.getDescription());
        intent.putExtra("year", movie.getYear());
        intent.putExtra("thumbnail", movie.getThumbnail());
        intent.putExtra("videoUrl", movie.getVideoUrl());
        intent.putExtra("isSeries", movie.isSeries());
        intent.putStringArrayListExtra("genres", new ArrayList<>(movie.getTypeName()));
        startActivity(intent);
    }

    public static void openDrawer(DrawerLayout drawerLayout)
    {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout)
    {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayoutFavorite);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
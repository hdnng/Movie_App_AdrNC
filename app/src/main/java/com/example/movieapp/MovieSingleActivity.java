package com.example.movieapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

public class MovieSingleActivity extends AppCompatActivity {
    DrawerLayout drawerLayoutMovie;
    ImageView menu;
    TextView hello;
    RecyclerView recyclerView;
    List<Movie> movieList;
    MovieAdapter movieAdapter;
    LinearLayout logout,movie,series,type,favorite,home;

    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_single);

        recyclerView = findViewById(R.id.recyclerViewMovies);

        menu = findViewById(R.id.menu);
        logout = findViewById(R.id.logout);
        series = findViewById(R.id.tvSeries);
        type = findViewById(R.id.type);
        favorite = findViewById(R.id.favorite);
        home = findViewById(R.id.homepage);
        drawerLayoutMovie = findViewById(R.id.drawerLayoutMovie);
        hello = findViewById(R.id.hello);
        menu.setOnClickListener(view -> openDrawer(drawerLayoutMovie));

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList,movie->openDetail(movie));

        series.setOnClickListener(v ->{
            startActivity(new Intent(MovieSingleActivity.this, MovieSeriesActivity.class));
            finish();
        });
        type.setOnClickListener(v ->{
            startActivity(new Intent(MovieSingleActivity.this, MovieTypeActivity.class));
            finish();
        });
        favorite.setOnClickListener(v ->{
            startActivity(new Intent(MovieSingleActivity.this, MovieFavoriteActivity.class));
            finish();
        });
        home.setOnClickListener(v ->{
            startActivity(new Intent(MovieSingleActivity.this, MainActivity.class));
            finish();
        });
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MovieSingleActivity.this, LoginActivity.class));
            finish();
        });

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
            loadMovies();
        }
    }

    private void loadMovies() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));//Chia lm 2 cot

        db.collection("MOVIES")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Movie> allMovies = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Movie movie = doc.toObject(Movie.class);
                        movie.setId(doc.getId());
                        allMovies.add(movie);
                    }

                    // Kiểm tra nếu không có phim nào trong danh sách
                    if (allMovies.isEmpty()) {
                        // Thông báo cho người dùng hoặc xử lý theo cách khác
                        Toast.makeText(this, "Không có phim nào để hiển thị", Toast.LENGTH_SHORT).show();
                        return;  // Dừng lại và không thực hiện tiếp các thao tác
                    }

                    // Lấy tất cả thể loại từ danh sách phim
                    Set<String> allGenres = new HashSet<>();
                    for (Movie movie : allMovies) {
                        allGenres.addAll(movie.getTypeName());  // typeName là List<String>
                    }



                    // Lọc phim theo thể loại
                    for (Movie movie : allMovies) {
                        if (!movie.isSeries()) movieList.add(movie);//phim co series false
                    }
                    movieAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(movieAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public static void redirectActivity(Activity activity, Class seconActivity){
        Intent intent = new Intent(activity, seconActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayoutMovie);
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
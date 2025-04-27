package com.example.movieapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieapp.adapter.MovieAdapter;
import com.example.movieapp.model.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {


    DrawerLayout drawerLayout;
    ImageView menu;
    TextView  hello;

    LinearLayout logout,movie,series,type,favorite;
    private List<Movie> movieList;




    private RecyclerView recyclerGenre1, recyclerGenre2;
    private TextView genreTitle1, genreTitle2;
    private FrameLayout featuredMovieContainer;

    private MovieAdapter adapterGenre1, adapterGenre2;
    private List<Movie> listGenre1, listGenre2;


    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        featuredMovieContainer = findViewById(R.id.featuredMovieContainer);
        genreTitle1 = findViewById(R.id.genreTitle1);
        genreTitle2 = findViewById(R.id.genreTitle2);
        recyclerGenre1 = findViewById(R.id.recyclerGenre1);
        recyclerGenre2 = findViewById(R.id.recyclerGenre2);
        listGenre1 = new ArrayList<>();
        listGenre2 = new ArrayList<>();
        adapterGenre1 = new MovieAdapter(listGenre1, movie -> openDetail(movie));
        adapterGenre2 = new MovieAdapter(listGenre2, movie -> openDetail(movie));

        recyclerGenre1.setAdapter(adapterGenre1);
        recyclerGenre2.setAdapter(adapterGenre2);
        recyclerGenre1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerGenre2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        menu = findViewById(R.id.menu);
        logout = findViewById(R.id.logout);
        movie = findViewById(R.id.movie);
        type = findViewById(R.id.type);
        series = findViewById(R.id.tvSeries);
        favorite = findViewById(R.id.favorite);
        drawerLayout = findViewById(R.id.drawerLayout);
        hello = findViewById(R.id.hello);
        movieList = new ArrayList<>();
        menu.setOnClickListener(view -> openDrawer(drawerLayout));

        movie.setOnClickListener(v ->{
            startActivity(new Intent(MainActivity.this, MovieSingleActivity.class));
            finish();
        });
        series.setOnClickListener(v ->{
            startActivity(new Intent(MainActivity.this, MovieSeriesActivity.class));
            finish();
        });
        type.setOnClickListener(v ->{
            startActivity(new Intent(MainActivity.this, MovieTypeActivity.class));
            finish();
        });
        favorite.setOnClickListener(v ->{
            startActivity(new Intent(MainActivity.this, MovieFavoriteActivity.class));
            finish();
        });
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
        }

        loadMovies();  // Thêm gọi hàm tải phim
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


    private void loadMovies() {
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

                    // Chọn 1 phim nổi bật ngẫu nhiên
                    int featuredIndex = (int)(Math.random() * allMovies.size());
                    Movie featuredMovie = allMovies.get(featuredIndex);
                    showFeaturedMovie(featuredMovie);  // Xử lý hiển thị phim nổi bật

                    // Lấy tất cả thể loại từ danh sách phim
                    Set<String> allGenres = new HashSet<>();
                    for (Movie movie : allMovies) {
                        allGenres.addAll(movie.getTypeName());  // typeName là List<String>
                    }

                    // Chọn ngẫu nhiên 2 thể loại
                    List<String> genreList = new ArrayList<>(allGenres);
                    Collections.shuffle(genreList);
                    String genre1 = genreList.get(0);
                    String genre2 = genreList.size() > 1 ? genreList.get(1) : genreList.get(0);

                    genreTitle1.setText(genre1);
                    genreTitle2.setText(genre2);

                    // Lọc phim theo thể loại
                    for (Movie movie : allMovies) {
                        if (movie.getTypeName().contains(genre1)) listGenre1.add(movie);
                        if (movie.getTypeName().contains(genre2)) listGenre2.add(movie);
                    }

                    adapterGenre1.notifyDataSetChanged();
                    adapterGenre2.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void showFeaturedMovie(Movie movie) {
        // Kiểm tra xem Activity có còn tồn tại không trước khi thực hiện Glide
        if (!isFinishing() && !isDestroyed()) {
            View view = getLayoutInflater().inflate(R.layout.item_featured_movie, featuredMovieContainer, false);

            ImageView img = view.findViewById(R.id.imgThumbnail);
            TextView title = view.findViewById(R.id.txtTitle);

            Glide.with(this)
                    .load(movie.getThumbnail())
                    .into(img);
            title.setText(movie.getTitle());

            // 👉 Bắt sự kiện click để mở chi tiết phim
            view.setOnClickListener(v -> openDetail(movie));

            featuredMovieContainer.removeAllViews();
            featuredMovieContainer.addView(view);
        }
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
        closeDrawer(drawerLayout);
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

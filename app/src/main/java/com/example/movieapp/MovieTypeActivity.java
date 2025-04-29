package com.example.movieapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class MovieTypeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayoutType;
    private ImageView menu;
    private TextView hello;
    EditText searchEditText;
    private AutoCompleteTextView movietype;
    private LinearLayout logout, movie, series, favorite, home;
    private RecyclerView recyclerMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList = new ArrayList<>();
    private List<String> typeNames = new ArrayList<>();
    private List<String> typeIds = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_type);

        // Ánh xạ view
        menu = findViewById(R.id.menu);
        logout = findViewById(R.id.logout);
        movie = findViewById(R.id.movie);
        series = findViewById(R.id.tvSeries);
        favorite = findViewById(R.id.favorite);
        home = findViewById(R.id.homepage);
        drawerLayoutType = findViewById(R.id.drawerLayoutType);
        hello = findViewById(R.id.hello);
        movietype = findViewById(R.id.movietype);
        recyclerMovies = findViewById(R.id.recyclerViewTypes);
        searchEditText = findViewById(R.id.searchEditText);

        searchEditText.setVisibility(View.GONE);
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        movieAdapter = new MovieAdapter(movieList, this::openDetail);
        recyclerMovies.setAdapter(movieAdapter);
        recyclerMovies.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerMovies.setVisibility(View.GONE);

        // Setup Dropdown cho thể loại
        movietype.setOnClickListener(v -> movietype.showDropDown());

        menu.setOnClickListener(v -> openDrawer(drawerLayoutType));
        movie.setOnClickListener(v -> navigateTo(MovieSingleActivity.class));
        series.setOnClickListener(v -> navigateTo(MovieSeriesActivity.class));
        favorite.setOnClickListener(v -> navigateTo(MovieFavoriteActivity.class));
        home.setOnClickListener(v -> navigateTo(MainActivity.class));
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            navigateTo(LoginActivity.class);
        });

        getUserInfo();
        loadTypeAndSetDropdown();
        loadAllMovies();
    }

    private void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String username = documentSnapshot.getString("username");
                        hello.setText(username != null ? "Xin chào: " + username : "Xin chào!");
                    })
                    .addOnFailureListener(e -> hello.setText("Lỗi khi lấy thông tin người dùng"));
        }
    }

    private void loadTypeAndSetDropdown() {
        db.collection("TYPE")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    typeNames.clear();
                    typeIds.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String name = doc.getString("nameType");
                        if (name != null) {
                            typeNames.add(name);
                            typeIds.add(id);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, typeNames);
                    movietype.setAdapter(adapter);

                    movietype.setOnItemClickListener((parent, view, position, id) -> {
                        List<String> selectedTypeIds = new ArrayList<>();
                        selectedTypeIds.add(typeIds.get(position));
                        loadMoviesByTypes(selectedTypeIds);
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải thể loại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void loadAllMovies(){
        db.collection("MOVIES")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Movie> filteredMovies = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Movie movie = doc.toObject(Movie.class);
                        movie.setId(doc.getId());
                        filteredMovies.add(movie);

                    }
                    movieList.clear();
                    if (filteredMovies.isEmpty()) {
                        Toast.makeText(this, "Không có phim nào phù hợp", Toast.LENGTH_SHORT).show();
                        recyclerMovies.setVisibility(View.GONE);
                    } else {
                        movieList.addAll(filteredMovies);
                        movieAdapter.notifyDataSetChanged();
                        recyclerMovies.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải phim: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void loadMoviesByTypes(List<String> selectedTypeIds) {
        db.collection("MOVIES")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Movie> filteredMovies = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Movie movie = doc.toObject(Movie.class);
                        movie.setId(doc.getId());
                        List<String> movieTypeIds = (List<String>) doc.get("typeId");
                        if (movieTypeIds != null && !movieTypeIds.isEmpty()) {
                            for (String typeId : selectedTypeIds) {
                                if (movieTypeIds.contains(typeId)) {
                                    filteredMovies.add(movie);
                                    break;
                                }
                            }
                        }
                    }
                    movieList.clear();
                    if (filteredMovies.isEmpty()) {
                        Toast.makeText(this, "Không có phim nào phù hợp", Toast.LENGTH_SHORT).show();
                        recyclerMovies.setVisibility(View.GONE);
                    } else {
                        movieList.addAll(filteredMovies);
                        movieAdapter.notifyDataSetChanged();
                        recyclerMovies.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải phim: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

    private void navigateTo(Class<?> activity) {
        startActivity(new Intent(this, activity));
        finish();
    }

    private static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayoutType);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            navigateTo(LoginActivity.class);
        }
    }
}
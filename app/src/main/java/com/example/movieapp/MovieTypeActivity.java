package com.example.movieapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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

    DrawerLayout drawerLayoutType;
    ImageView menu;
    TextView hello;

    LinearLayout logout, movie, series, type, favorite, home;

    FirebaseFirestore db;

    private List<String> typeNames = new ArrayList<>();
    private List<String> typeIds = new ArrayList<>();
    private boolean[] checkedItems;

    private RecyclerView recyclerMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_type);

        // Ánh xạ
        menu = findViewById(R.id.menu);
        logout = findViewById(R.id.logout);
        movie = findViewById(R.id.movie);
        series = findViewById(R.id.tvSeries);
        favorite = findViewById(R.id.favorite);
        type = findViewById(R.id.type);
        home = findViewById(R.id.homepage);
        drawerLayoutType = findViewById(R.id.drawerLayoutType);
        hello = findViewById(R.id.hello);
        recyclerMovies = findViewById(R.id.recyclerViewTypes);

        // Set up RecyclerView
        movieAdapter = new MovieAdapter(movieList, movie -> openDetail(movie));
        recyclerMovies.setAdapter(movieAdapter);
        recyclerMovies.setLayoutManager(new LinearLayoutManager(this));
        recyclerMovies.setVisibility(View.GONE); // Ban đầu ẩn

        // Menu drawer
        menu.setOnClickListener(view -> openDrawer(drawerLayoutType));
        movie.setOnClickListener(v -> {
            startActivity(new Intent(MovieTypeActivity.this, MovieSingleActivity.class));
            finish();
        });
        series.setOnClickListener(v -> {
            startActivity(new Intent(MovieTypeActivity.this, MovieSeriesActivity.class));
            finish();
        });
        favorite.setOnClickListener(v -> {
            startActivity(new Intent(MovieTypeActivity.this, MovieFavoriteActivity.class));
            finish();
        });
        home.setOnClickListener(v -> {
            startActivity(new Intent(MovieTypeActivity.this, MainActivity.class));
            finish();
        });
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MovieTypeActivity.this, LoginActivity.class));
            finish();
        });

        type.setOnClickListener(v -> {
            closeDrawer(drawerLayoutType);
            loadTypeAndShowDialog();
        });
        // Firebase
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

        // 👉 Gọi load danh sách thể loại khi mở trang
        loadTypeAndShowDialog();
    }

    private void loadTypeAndShowDialog() {
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
                    showTypeSelectionDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thể loại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showTypeSelectionDialog() {
        checkedItems = new boolean[typeNames.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại");
        builder.setMultiChoiceItems(typeNames.toArray(new String[0]), checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            List<String> selectedTypeIds = new ArrayList<>();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    selectedTypeIds.add(typeIds.get(i));
                }
            }
            loadMoviesByTypes(selectedTypeIds);
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> finish()); // Nếu bấm Hủy thì thoát luôn
        builder.show();
    }

    private void loadMoviesByTypes(List<String> selectedTypeIds) {
        recyclerMovies.setLayoutManager(new GridLayoutManager(this, 2));//Chia lm 2 cot

        db.collection("MOVIES")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Movie> filteredMovies = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Movie movie = doc.toObject(Movie.class);
                        movie.setId(doc.getId());
                        List<String> movieTypeIds = (List<String>) doc.get("typeId");
                        if (movieTypeIds != null) {
                            for (String typeId : selectedTypeIds) {
                                if (movieTypeIds.contains(typeId)) {
                                    filteredMovies.add(movie);
                                    break;
                                }
                            }
                        }
                    }

                    if (filteredMovies.isEmpty()) {
                        Toast.makeText(this, "Không có phim nào phù hợp", Toast.LENGTH_SHORT).show();
                        recyclerMovies.setVisibility(View.GONE);
                    } else {
                        movieList.clear();
                        movieList.addAll(filteredMovies);
                        movieAdapter.notifyDataSetChanged();
                        recyclerMovies.setVisibility(View.VISIBLE);
                    }
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

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void redirectActivity(Activity activity, Class secondActivity) {
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
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
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}

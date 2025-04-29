package com.example.movieapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
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
   // private List<Movie> movieList;
    private List<Movie> allMovieList = new ArrayList<>();


    EditText searchEditText;
    private RecyclerView recyclerSearchResults;
    private MovieAdapter searchAdapter;





    private RecyclerView recyclerGenre1, recyclerGenre2, recyclerGenre3, recyclerGenre4;
    private TextView genreTitle1, genreTitle2, genreTitle3, genreTitle4;
    private FrameLayout featuredMovieContainer;

    private MovieAdapter adapterGenre1, adapterGenre2, adapterGenre3, adapterGenre4;
    private List<Movie> listGenre1, listGenre2, listGenre3, listGenre4;


    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        featuredMovieContainer = findViewById(R.id.featuredMovieContainer);
        genreTitle1 = findViewById(R.id.genreTitle1);
        genreTitle2 = findViewById(R.id.genreTitle2);
        genreTitle3 = findViewById(R.id.genreTitle3);
        genreTitle4 = findViewById(R.id.genreTitle4);
        recyclerGenre1 = findViewById(R.id.recyclerGenre1);
        recyclerGenre2 = findViewById(R.id.recyclerGenre2);
        recyclerGenre3 = findViewById(R.id.recyclerGenre3);
        recyclerGenre4 = findViewById(R.id.recyclerGenre4);
        listGenre1 = new ArrayList<>();
        listGenre2 = new ArrayList<>();
        listGenre3 = new ArrayList<>();
        listGenre4 = new ArrayList<>();
        adapterGenre1 = new MovieAdapter(listGenre1, movie -> openDetail(movie));
        adapterGenre2 = new MovieAdapter(listGenre2, movie -> openDetail(movie));
        adapterGenre3 = new MovieAdapter(listGenre3, movie -> openDetail(movie));
        adapterGenre4 = new MovieAdapter(listGenre4, movie -> openDetail(movie));

        recyclerGenre1.setAdapter(adapterGenre1);
        recyclerGenre2.setAdapter(adapterGenre2);
        recyclerGenre3.setAdapter(adapterGenre3);
        recyclerGenre4.setAdapter(adapterGenre4);
        recyclerGenre1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerGenre2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerGenre3.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerGenre4.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        menu = findViewById(R.id.menu);
        logout = findViewById(R.id.logout);
        movie = findViewById(R.id.movie);
        type = findViewById(R.id.type);
        series = findViewById(R.id.tvSeries);
        favorite = findViewById(R.id.favorite);
        drawerLayout = findViewById(R.id.drawerLayout);
        hello = findViewById(R.id.hello);
        menu.setOnClickListener(view -> openDrawer(drawerLayout));

        //search
        searchEditText = findViewById(R.id.searchEditText);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        recyclerSearchResults.setLayoutManager(new GridLayoutManager(this, 2)); // 2 phim 1 dòng
        searchAdapter = new MovieAdapter(new ArrayList<>(), movie -> openDetail(movie));
        recyclerSearchResults.setAdapter(searchAdapter);
        recyclerSearchResults.setVisibility(View.GONE); // Ban đầu ẩn đi




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


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMovies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

    }

    private void filterMovies(String query) {
        if (query.isEmpty()) {
            // Nếu ô tìm kiếm rỗng, HIỂN lại các thể loại
            genreTitle1.setVisibility(View.VISIBLE);
            genreTitle2.setVisibility(View.VISIBLE);
            genreTitle3.setVisibility(View.VISIBLE);
            genreTitle4.setVisibility(View.VISIBLE);
            recyclerGenre1.setVisibility(View.VISIBLE);
            recyclerGenre2.setVisibility(View.VISIBLE);
            recyclerGenre3.setVisibility(View.VISIBLE);
            recyclerGenre4.setVisibility(View.VISIBLE);
            featuredMovieContainer.setVisibility(View.VISIBLE);
            recyclerSearchResults.setVisibility(View.GONE); // Ẩn kết quả tìm kiếm
            return;
        }

        // Ngược lại (đang nhập tìm kiếm)
        genreTitle1.setVisibility(View.GONE);
        genreTitle2.setVisibility(View.GONE);
        genreTitle3.setVisibility(View.GONE);
        genreTitle4.setVisibility(View.GONE);
        recyclerGenre1.setVisibility(View.GONE);
        recyclerGenre2.setVisibility(View.GONE);
        recyclerGenre3.setVisibility(View.GONE);
        recyclerGenre4.setVisibility(View.GONE);
        featuredMovieContainer.setVisibility(View.GONE);
        recyclerSearchResults.setVisibility(View.VISIBLE);

        List<Movie> filteredList = new ArrayList<>();
        for (Movie movie : allMovieList) {
            if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(movie);
            }
        }

        searchAdapter.setList(filteredList);
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
                    allMovieList.clear();  // Xóa nếu có dữ liệu cũ
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Movie movie = doc.toObject(Movie.class);
                        movie.setId(doc.getId());
                        allMovieList.add(movie);
                    }

                    if (allMovieList.isEmpty()) {
                        Toast.makeText(this, "Không có phim nào để hiển thị", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Chọn phim nổi bật ngẫu nhiên
                    int featuredIndex = (int) (Math.random() * allMovieList.size());
                    Movie featuredMovie = allMovieList.get(featuredIndex);
                    showFeaturedMovie(featuredMovie);

                    // Lấy tất cả thể loại
                    Set<String> allGenres = new HashSet<>();
                    for (Movie movie : allMovieList) {
                        allGenres.addAll(movie.getTypeName());
                    }

                    // Chọn ngẫu nhiên 2 thể loại
                    List<String> genreList = new ArrayList<>(allGenres);
                    Collections.shuffle(genreList);
                    String genre1 = genreList.get(0);
                    String genre2 = genreList.size() > 1 ? genreList.get(1) : genreList.get(0);
                    String genre3 = genreList.size() > 2 ? genreList.get(2) : genreList.get(0);
                    String genre4 = genreList.size() > 3 ? genreList.get(3) : genreList.get(0);

                    genreTitle1.setText(genre1);
                    genreTitle2.setText(genre2);
                    genreTitle3.setText(genre3);
                    genreTitle4.setText(genre4);

                    listGenre1.clear();
                    listGenre2.clear();
                    listGenre3.clear();
                    listGenre4.clear();

                    for (Movie movie : allMovieList) {
                        if (movie.getTypeName().contains(genre1)) listGenre1.add(movie);
                        if (movie.getTypeName().contains(genre2)) listGenre2.add(movie);
                        if (movie.getTypeName().contains(genre3)) listGenre3.add(movie);
                        if (movie.getTypeName().contains(genre4)) listGenre4.add(movie);
                    }

                    adapterGenre1.notifyDataSetChanged();
                    adapterGenre2.notifyDataSetChanged();
                    adapterGenre3.notifyDataSetChanged();
                    adapterGenre4.notifyDataSetChanged();
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

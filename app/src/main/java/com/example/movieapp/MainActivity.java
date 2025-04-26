package com.example.movieapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.movieapp.adapter.MovieAdapter;
import com.example.movieapp.adapter.ViewPageAdapter;
import com.example.movieapp.fragment.Fragment_Home;
import com.example.movieapp.fragment.Fragment_Logout;
import com.example.movieapp.fragment.Fragment_Movie_Favorite;
import com.example.movieapp.fragment.Fragment_Movie_Series;
import com.example.movieapp.fragment.Fragment_Movie_Single;
import com.example.movieapp.fragment.Fragment_Movie_Type;
import com.example.movieapp.model.Movie;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    DrawerLayout drawerLayout;
    ImageView menu;
    TextView  logout,hello;
    private List<Movie> movieList;
    NavigationView navigationView;
    ViewPager viewPager;
    ViewPageAdapter adapter;
    Fragment_Home fragment_home;
    Fragment_Movie_Single fragment_movie_single;
    Fragment_Movie_Series fragment_movie_series;
    Fragment_Movie_Type fragment_movie_type;
    Fragment_Movie_Favorite fragment_movie_favorite;
    Fragment_Logout fragment_logout;



//    private RecyclerView recyclerGenre1, recyclerGenre2;
//    private TextView genreTitle1, genreTitle2;
//    private FrameLayout featuredMovieContainer;

//    private MovieAdapter adapterGenre1, adapterGenre2;
//    private List<Movie> listGenre1, listGenre2;


    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        featuredMovieContainer = findViewById(R.id.featuredMovieContainer);
//        genreTitle1 = findViewById(R.id.genreTitle1);
//        genreTitle2 = findViewById(R.id.genreTitle2);
//        recyclerGenre1 = findViewById(R.id.recyclerGenre1);
//        recyclerGenre2 = findViewById(R.id.recyclerGenre2);
//        listGenre1 = new ArrayList<>();
//        listGenre2 = new ArrayList<>();
//        adapterGenre1 = new MovieAdapter(listGenre1, movie -> openDetail(movie));
//        adapterGenre2 = new MovieAdapter(listGenre2, movie -> openDetail(movie));

//        recyclerGenre1.setAdapter(adapterGenre1);
//        recyclerGenre2.setAdapter(adapterGenre2);
//        recyclerGenre1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        recyclerGenre2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Add mới
        viewPager = findViewById(R.id.viewPager);
        navigationView = findViewById(R.id.menu_navigation);
        adapter = new ViewPageAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(adapter);
        //



        menu = findViewById(R.id.menu);
//        logout = findViewById(R.id.menu_logout);
        drawerLayout = findViewById(R.id.drawerLayout);
        hello = findViewById(R.id.hello);
        movieList = new ArrayList<>();
        menu.setOnClickListener(view -> openDrawer(drawerLayout));
//        logout.setOnClickListener(v -> {
//            FirebaseAuth.getInstance().signOut();
//            startActivity(new Intent(MainActivity.this, LoginActivity.class));
//            finish();
//        });

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

//        loadMovies();  // Thêm gọi hàm tải phim
        clickMenu();
        showPageView();
    }

    public void clickMenu(){
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        navigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                        break;
                    case 1:
                        navigationView.getMenu().findItem(R.id.menu_movie_single).setChecked(true);
                        break;
                    case 2:
                        navigationView.getMenu().findItem(R.id.menu_movie_series).setChecked(true);
                        break;
                    case 3:
                        navigationView.getMenu().findItem(R.id.menu_movie_type).setChecked(true);
                        break;
                    case 4:
                        navigationView.getMenu().findItem(R.id.menu_movie_favorite).setChecked(true);
                        break;
                    case 5:
                        navigationView.getMenu().findItem(R.id.menu_logout).setChecked(true);
                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void showPageView(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId()==R.id.menu_home){
                    viewPager.setCurrentItem(0);
                    fragment_home=(Fragment_Home) viewPager.getAdapter().instantiateItem(viewPager,0);
                    fragment_home.loadData();
                }
                if(item.getItemId()==R.id.menu_movie_single){
                    viewPager.setCurrentItem(1);
                    fragment_movie_single=(Fragment_Movie_Single) viewPager.getAdapter().instantiateItem(viewPager,1);
                }
                if(item.getItemId()==R.id.menu_movie_series){
                    viewPager.setCurrentItem(2);
                    fragment_movie_series=(Fragment_Movie_Series) viewPager.getAdapter().instantiateItem(viewPager,2);

                }
                if(item.getItemId()==R.id.menu_movie_type){
                    viewPager.setCurrentItem(3);
                    fragment_movie_type=(Fragment_Movie_Type) viewPager.getAdapter().instantiateItem(viewPager,3);

                }
                if(item.getItemId()==R.id.menu_movie_favorite){
                    viewPager.setCurrentItem(4);
                    fragment_movie_favorite=(Fragment_Movie_Favorite) viewPager.getAdapter().instantiateItem(viewPager,4);

                }
                if(item.getItemId()==R.id.menu_logout){
                    viewPager.setCurrentItem(5);
                    fragment_logout=(Fragment_Logout) viewPager.getAdapter().instantiateItem(viewPager,5);
                }

                return true;
            }
        });
    }

//    private void openDetail(Movie movie) {
//        Intent intent = new Intent(this, MovieDetailActivity.class);
//        intent.putExtra("movieId", movie.getId());
//        intent.putExtra("title", movie.getTitle());
//        intent.putExtra("description", movie.getDescription());
//        intent.putExtra("year", movie.getYear());
//        intent.putExtra("thumbnail", movie.getThumbnail());
//        intent.putExtra("videoUrl", movie.getVideoUrl());
//        intent.putExtra("isSeries", movie.isSeries());
//        intent.putStringArrayListExtra("genres", new ArrayList<>(movie.getTypeName()));
//
//        startActivity(intent);
//    }
//
//
//    private void loadMovies() {
//        db.collection("MOVIES")
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    List<Movie> allMovies = new ArrayList<>();
//                    for (QueryDocumentSnapshot doc : querySnapshot) {
//                        Movie movie = doc.toObject(Movie.class);
//                        movie.setId(doc.getId());
//                        allMovies.add(movie);
//                    }
//
//                    // Chọn 1 phim nổi bật ngẫu nhiên
//                    int featuredIndex = (int)(Math.random() * allMovies.size());
//                    Movie featuredMovie = allMovies.get(featuredIndex);
//                    showFeaturedMovie(featuredMovie);  // Xử lý hiển thị phim nổi bật
//
//                    // Lấy tất cả thể loại từ danh sách phim
//                    Set<String> allGenres = new HashSet<>();
//                    for (Movie movie : allMovies) {
//                        allGenres.addAll(movie.getTypeName());  // typeName là List<String>
//                    }
//
//                    // Chọn ngẫu nhiên 2 thể loại
//                    List<String> genreList = new ArrayList<>(allGenres);
//                    Collections.shuffle(genreList);
//                    String genre1 = genreList.get(0);
//                    String genre2 = genreList.size() > 1 ? genreList.get(1) : genreList.get(0);
//
//                    genreTitle1.setText(genre1);
//                    genreTitle2.setText(genre2);
//
//                    // Lọc phim theo thể loại
//                    for (Movie movie : allMovies) {
//                        if (movie.getTypeName().contains(genre1)) listGenre1.add(movie);
//                        if (movie.getTypeName().contains(genre2)) listGenre2.add(movie);
//                    }
//
//                    adapterGenre1.notifyDataSetChanged();
//                    adapterGenre2.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Lỗi tải phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void showFeaturedMovie(Movie movie) {
//        View view = getLayoutInflater().inflate(R.layout.item_featured_movie, featuredMovieContainer, false);
//
//        ImageView img = view.findViewById(R.id.imgThumbnail);
//        TextView title = view.findViewById(R.id.txtTitle);
//
//        Glide.with(this).load(movie.getThumbnail()).into(img);
//        title.setText(movie.getTitle());
//
//        // 👉 Bắt sự kiện click để mở chi tiết phim
//        view.setOnClickListener(v -> openDetail(movie));
//
//        featuredMovieContainer.removeAllViews();
//        featuredMovieContainer.addView(view);
//    }




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

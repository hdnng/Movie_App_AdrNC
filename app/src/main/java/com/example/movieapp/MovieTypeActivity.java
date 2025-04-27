package com.example.movieapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MovieTypeActivity extends AppCompatActivity {

    DrawerLayout drawerLayoutType;
    ImageView menu;
    TextView hello;

    LinearLayout logout,movie,series,type,favorite,home;

    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_type);

        menu = findViewById(R.id.menu);
        logout = findViewById(R.id.logout);
        movie = findViewById(R.id.movie);
        series = findViewById(R.id.tvSeries);
        favorite = findViewById(R.id.favorite);
        home = findViewById(R.id.homepage);
        drawerLayoutType = findViewById(R.id.drawerLayoutType);
        hello = findViewById(R.id.hello);
        menu.setOnClickListener(view -> openDrawer(drawerLayoutType));
        movie.setOnClickListener(v ->{
            startActivity(new Intent(MovieTypeActivity.this, MovieSingleActivity.class));
            finish();
        });
        series.setOnClickListener(v ->{
            startActivity(new Intent(MovieTypeActivity.this, MovieSeriesActivity.class));
            finish();
        });
        favorite.setOnClickListener(v ->{
            startActivity(new Intent(MovieTypeActivity.this, MovieFavoriteActivity.class));
            finish();
        });
        home.setOnClickListener(v ->{
            startActivity(new Intent(MovieTypeActivity.this, MainActivity.class));
            finish();
        });
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MovieTypeActivity.this, LoginActivity.class));
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
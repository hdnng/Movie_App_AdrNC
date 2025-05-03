package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.adapter.MovieAdminAdapter;
import com.example.movieapp.model.Movie;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminMovieListActivity extends AppCompatActivity {

    private FloatingActionButton btnAdd;
    private RecyclerView rvMovieList;
    private FirebaseFirestore db;
    private ImageButton btnBack;
    private List<Movie> movieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_movie_list);
        initViews();
        setupListeners();
        loadMovieList();
    }

    private void initViews(){
        //ánh xạ
        btnAdd = findViewById(R.id.btnAdd);
        rvMovieList = findViewById(R.id.rvMovieList);
        btnBack = findViewById(R.id.btnBack);
        db = FirebaseFirestore.getInstance();
    }

    private void setupListeners(){
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(AdminMovieListActivity.this, AddMovieActivity.class));
        });
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(AdminMovieListActivity.this, AdminActivity.class));
        });
    }

    private void loadMovieList(){
        //lấy danh sách phim từ firebase giảm dần theo thời gian tạo
        db.collection("MOVIES")
                .orderBy("createdTime", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(movieTask -> {
            //neu thanh cong
            if(movieTask.isSuccessful()){
                for (QueryDocumentSnapshot document : movieTask.getResult()) {
                    movieList.add(document.toObject(Movie.class));
                }
                //hien thi danh sách phim lên recyclerView
                MovieAdminAdapter movieAdminAdapter = new MovieAdminAdapter(movieList);
                rvMovieList.setAdapter(movieAdminAdapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                rvMovieList.setLayoutManager(linearLayoutManager);
            }
            //neu that bai
            else {
                Toast.makeText(this, "Lỗi khi lấy dánh sách movie", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
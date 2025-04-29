package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {

    Button btnLogout, btnManageGenres, btnManageMovies, btnAddMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        btnLogout = findViewById(R.id.btnLogout);
        btnManageGenres = findViewById(R.id.btnManageGenres);

        btnAddMovies = findViewById(R.id.btnAddMovies);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
        });
        btnManageGenres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminActivity.this, TypeActivity.class));
                finish();
            }
        });
        btnAddMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminActivity.this, AdminMovieListActivity.class));
                finish();
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long role = documentSnapshot.getLong("role");
                    if (role == null || role != 0) {
                        // Không phải admin → đá ra login
                        Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }
                });
    }

}
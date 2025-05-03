package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movieapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText email, password, comfirmPassword, username;
    private Button btnRegister;
    private TextView txtLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews(){
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        comfirmPassword = findViewById(R.id.confirmPassword);
        username = findViewById(R.id.username);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupListeners(){
        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
        btnRegister.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();
            String comfirmPass = comfirmPassword.getText().toString().trim();
            String userName = username.getText().toString().trim();
            // kiểm tra các trường
            if (userEmail.isEmpty() || userPass.isEmpty() || comfirmPass.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!userPass.equals(comfirmPass)){
                Toast.makeText(this,"Mật khẩu xác nhận không khớp",Toast.LENGTH_SHORT).show();
                return;
            }
            //đăng ký tài khoản
            mAuth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = task.getResult().getUser().getUid();

                            User user = new User(
                                    uid,
                                    userEmail,
                                    userName,
                                    1,null
                            );
                            db.collection("users").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish(); // Đảm bảo thoát khỏi RegisterActivity
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Lỗi khi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}

package com.example.movieapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.movieapp.model.Type;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.UUID;

public class CreateTypeActivity extends AppCompatActivity {

    private EditText edtNameType;
    private Button btnCreateType,btnbackCreateType;
    private Context context = this;
    private FirebaseFirestore database;
    private String id ="";
    private Type type = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_type);

        initViews();
        setupListeners();
    }
    public void initViews(){
        edtNameType = findViewById(R.id.edt_nameType);
        btnCreateType = findViewById(R.id.btn_createType);
        btnbackCreateType = findViewById(R.id.btn_backCreateType);
        id = UUID.randomUUID().toString();//Lay mot ma ngau nhien
        database = FirebaseFirestore.getInstance();
    }
    public boolean check(){
        String nameType = edtNameType.getText().toString().trim();
        if(nameType.isEmpty()){
            Toast.makeText(context, "Không được để trống", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    public void insertType(){

        type = new Type(id, edtNameType.getText().toString().trim());
        HashMap<String,Object> Typelists = type.convertHashMap();//Goi ham ConvertHashMap()
        database.collection("TYPE").document(id)//Dat ten cho document
                .set(Typelists).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context,"Them thanh cong",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Thêm thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace(); // Ghi log vào Logcat fix bug
            }
        });

    }
    public void setupListeners(){

            btnCreateType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!check()) {
                        insertType();
                        startActivity(new Intent(CreateTypeActivity.this, TypeActivity.class));
                        finish();
                    }

                }
            });

        btnbackCreateType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateTypeActivity.this, TypeActivity.class));
                finish();
            }
        });
    }


}
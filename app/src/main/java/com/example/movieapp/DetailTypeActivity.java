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

public class DetailTypeActivity extends AppCompatActivity {
    private EditText edtNameDetailType,edtNewNameDetailType;
    private Button btnBack,btnUpdate,btnDelete;
    private String idType ="";
    private String nameType ="";
    private Context context = this;
    private FirebaseFirestore database;
    private String id ="";
    private Type type = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_type);

        initViews();
        setupListeners();
    }
    public void initViews(){
        edtNameDetailType = findViewById(R.id.edt_nameDetailType);
        edtNewNameDetailType = findViewById(R.id.edt_newnameDetailType);
        btnBack = findViewById(R.id.btn_backDetailType);
        btnUpdate = findViewById(R.id.btn_updatedType);
        btnDelete = findViewById(R.id.btn_deletedType);
        // Nhận thông tin từ Intent
        idType = getIntent().getStringExtra("idType");
        nameType = getIntent().getStringExtra("nameType");
        Toast.makeText(context, "IdType"+idType, Toast.LENGTH_SHORT).show();

        if(!nameType.isEmpty()){
            edtNameDetailType.setText(nameType);
        }
        database = FirebaseFirestore.getInstance();
    }
    public boolean check(){
        String newname = edtNewNameDetailType.getText().toString().trim();
        String name = edtNameDetailType.getText().toString().trim();
        if(newname.compareTo(name)==0){
            Toast.makeText(context, "Tên mới đang trùng với tên cũ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(newname.isEmpty()){
            Toast.makeText(context, "Không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void updateType(){
        //Dua du lieu can update
        id = idType;
        type = new Type(id ,edtNewNameDetailType.getText().toString().trim());
        database.collection("TYPE")//Ten bang dl
                .document(type.getIdType())//Lay dong can update
                .update(type.convertHashMap())//update
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context,"Sua thanh cong",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Sua that bai",Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void deleteType(){
        id = idType;
        database.collection("TYPE")
                .document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context,"Xoa thanh cong",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Xoa that bai",Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void setupListeners(){
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailTypeActivity.this, TypeActivity.class));
                finish();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check()){
                    updateType();
                    startActivity(new Intent(DetailTypeActivity.this, TypeActivity.class));
                    finish();
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteType();
                startActivity(new Intent(DetailTypeActivity.this, TypeActivity.class));
                finish();
            }
        });
    }
}
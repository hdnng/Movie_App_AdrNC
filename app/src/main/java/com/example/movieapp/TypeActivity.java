package com.example.movieapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.movieapp.model.Type;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TypeActivity extends AppCompatActivity {

    Button btnBack, btnCreateType;
    ListView lvType;
    Context context = this;
    FirebaseFirestore database;
    ArrayList<String> typeNames;
    ArrayList<String> typeIds; // 👉 Thêm list id riêng
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_type);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getView();
        event();
        loadTypes(); // Gọi load data
    }

    public void getView() {
        btnBack = findViewById(R.id.btn_lsBackedType);
        btnCreateType = findViewById(R.id.btn_lsCreateType);
        lvType = findViewById(R.id.lv_ListType);

        typeNames = new ArrayList<>();
        typeIds = new ArrayList<>(); //  khởi tạo luôn list id

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, typeNames);
        lvType.setAdapter(adapter);

        database = FirebaseFirestore.getInstance();
    }

    // Load danh sách thể loại
    public void loadTypes() {
        database.collection("TYPE").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    typeNames.clear();
                    typeIds.clear(); // clear id luôn để tránh lỗi trùng

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Type type = document.toObject(Type.class);
                        String idType = document.getId();
                        String nameType = type.getNameType();

                        typeNames.add(nameType);
                        typeIds.add(idType); // lưu id ứng với vị trí
                    }

                    adapter.notifyDataSetChanged();
                    Toast.makeText(context, "Tải thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Tải thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        //  Set OnItemClickListener ở ngoài
        lvType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTypeName = typeNames.get(position);
            String selectedTypeId = typeIds.get(position);

            Intent intent = new Intent(TypeActivity.this, DetailTypeActivity.class);
            intent.putExtra("idType", selectedTypeId);
            intent.putExtra("nameType", selectedTypeName);
            startActivity(intent);
        });
    }

    public void event() {
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(TypeActivity.this, AdminActivity.class));
            finish();
        });

        btnCreateType.setOnClickListener(v -> {
            startActivity(new Intent(TypeActivity.this, CreateTypeActivity.class));
            finish();
        });
    }
}

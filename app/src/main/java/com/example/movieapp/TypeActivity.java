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
        loadTypes(); // <-- Gọi ở đây
    }

    public void getView() {
        btnBack = findViewById(R.id.btn_lsBackedType);
        btnCreateType = findViewById(R.id.btn_lsCreateType);
        lvType = findViewById(R.id.lv_ListType);

        typeNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, typeNames);
        lvType.setAdapter(adapter);

        database = FirebaseFirestore.getInstance();
    }

    //Load listView hien danh sach the loai
    public void loadTypes() {
        database.collection("TYPE").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    typeNames.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Lấy cả id và tên thể loại
                        Type type = document.toObject(Type.class);
                        String idType = document.getId();  // id của document trong Firestore
                        String nameType = type.getNameType();

                        // Lưu thông tin vào danh sách
                        typeNames.add(nameType);

                        // Thêm sự kiện click vào mỗi item trong ListView
                        lvType.setOnItemClickListener((parent, view, position, id) -> {
                            // Lấy tên thể loại và id của nó khi click vào item
                            String selectedType = typeNames.get(position);

                            // Truyền idType và nameType vào DetailTypeActivity
                            Intent intent = new Intent(TypeActivity.this, DetailTypeActivity.class);
                            intent.putExtra("idType", idType);  // Truyền idType
                            intent.putExtra("nameType", selectedType);  // Truyền nameType
                            startActivity(intent);
                        });
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(context, "Tải thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Tải thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

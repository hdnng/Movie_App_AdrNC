package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.adapter.EpisodeAdapter;
import com.example.movieapp.model.Episode;
import com.example.movieapp.model.Movie;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMovieActivity extends AppCompatActivity {

    EditText edtTitle, edtDescription, edtYear, edtThumbnail, edtVideoUrl;
    CheckBox chkIsSeries;
    LinearLayout layoutEpisodeList;
    RecyclerView rvEpisodes;
    Button btnAddEpisode, btnSaveMovie, btnBackToAdmin;
    TextView tvSelectTypes;

    List<Episode> episodeList = new ArrayList<>();
    EpisodeAdapter episodeAdapter;

    ArrayList<String> selectedTypeIds = new ArrayList<>();
    ArrayList<String> typeNames = new ArrayList<>();
    ArrayList<String> typeIds = new ArrayList<>();
    boolean[] checkedItems;

    FirebaseFirestore db;  // Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtYear = findViewById(R.id.edtYear);
        edtThumbnail = findViewById(R.id.edtThumbnail);
        edtVideoUrl = findViewById(R.id.edtVideoUrl);
        chkIsSeries = findViewById(R.id.chkIsSeries);
        layoutEpisodeList = findViewById(R.id.layoutEpisodeList);
        rvEpisodes = findViewById(R.id.rvEpisodes);
        btnAddEpisode = findViewById(R.id.btnAddEpisode);
        btnSaveMovie = findViewById(R.id.btnSaveMovie);
        btnBackToAdmin = findViewById(R.id.btnBackToAdmin);
        tvSelectTypes = findViewById(R.id.tvSelectTypes);

        // Load thể loại
        loadTypes();

        // Cài đặt RecyclerView cho tập phim
        episodeAdapter = new EpisodeAdapter(episodeList);
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        rvEpisodes.setAdapter(episodeAdapter);

        // Sự kiện chọn thể loại
        tvSelectTypes.setOnClickListener(v -> showTypeSelectionDialog());

        // Checkbox phim bộ/phim lẻ
        chkIsSeries.setOnCheckedChangeListener((buttonView, isChecked) -> {
            edtVideoUrl.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            layoutEpisodeList.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Thêm tập
        btnAddEpisode.setOnClickListener(v -> showAddEpisodeDialog());

        // Lưu phim
        btnSaveMovie.setOnClickListener(v -> saveMovieToFirestore());

        // Quay lại Admin
        btnBackToAdmin.setOnClickListener(v -> {
            startActivity(new Intent(AddMovieActivity.this, AdminActivity.class));
            finish();
        });
    }

    private void loadTypes() {
        db.collection("TYPE").get().addOnSuccessListener(querySnapshot -> {
            typeNames.clear();
            typeIds.clear();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                typeNames.add(doc.getString("nameType"));
                typeIds.add(doc.getId());
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi tải thể loại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void showTypeSelectionDialog() {
        checkedItems = new boolean[typeNames.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại");
        builder.setMultiChoiceItems(typeNames.toArray(new String[0]), checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedTypeIds.clear();
            List<String> selectedTypeDisplayNames = new ArrayList<>();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    selectedTypeIds.add(typeIds.get(i));
                    selectedTypeDisplayNames.add(typeNames.get(i));
                }
            }
            tvSelectTypes.setText(String.join(", ", selectedTypeDisplayNames));
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showAddEpisodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm tập mới");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_episode, null);
        EditText edtEpTitle = view.findViewById(R.id.edtEpTitle);
        EditText edtEpUrl = view.findViewById(R.id.edtEpUrl);

        builder.setView(view);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            int episodeNum = episodeList.size() + 1;
            String title = edtEpTitle.getText().toString().trim();
            String url = edtEpUrl.getText().toString().trim();

            if (!title.isEmpty() && !url.isEmpty()) {
                episodeList.add(new Episode(episodeNum, title, url));
                episodeAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin tập!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void saveMovieToFirestore() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String yearStr = edtYear.getText().toString().trim();
        String thumbnail = edtThumbnail.getText().toString().trim();
        boolean isSeries = chkIsSeries.isChecked();
        String videoUrl = isSeries ? null : edtVideoUrl.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || yearStr.isEmpty() || thumbnail.isEmpty() ||
                (!isSeries && (videoUrl == null || videoUrl.isEmpty()))) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        int year = Integer.parseInt(yearStr);

        List<String> selectedTypeNames = new ArrayList<>(List.of(tvSelectTypes.getText().toString().split("\\s*,\\s*")));

        Movie movie = new Movie("", title, description, year, thumbnail, isSeries, selectedTypeIds, selectedTypeNames, videoUrl);

        db.collection("MOVIES").add(movie).addOnSuccessListener(docRef -> {
            if (isSeries && !episodeList.isEmpty()) {
                List<Map<String, Object>> episodeDataList = new ArrayList<>();
                for (Episode ep : episodeList) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", ep.getTitle());
                    data.put("episodeNumber", ep.getEpisodeNumber());
                    data.put("videoUrl", ep.getVideoUrl());
                    episodeDataList.add(data);
                }

                final int[] counter = {0};
                for (Map<String, Object> data : episodeDataList) {
                    docRef.collection("EPISODES").add(data).addOnSuccessListener(doc -> {
                        counter[0]++;
                        if (counter[0] == episodeDataList.size()) {
                            Toast.makeText(this, "Đã lưu phim bộ và các tập!", Toast.LENGTH_SHORT).show();
                            clearForm();
                        }
                    }).addOnFailureListener(e -> Log.e("Firestore", "Lỗi lưu tập: " + e.getMessage()));
                }
            } else {
                Toast.makeText(this, "Đã lưu phim lẻ!", Toast.LENGTH_SHORT).show();
                clearForm();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi lưu phim: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void clearForm() {
        edtTitle.setText("");
        edtDescription.setText("");
        edtYear.setText("");
        edtThumbnail.setText("");
        edtVideoUrl.setText("");
        chkIsSeries.setChecked(false);
        tvSelectTypes.setText("");
        episodeList.clear();
        episodeAdapter.notifyDataSetChanged();
    }
}

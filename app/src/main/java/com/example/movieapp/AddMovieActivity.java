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
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.adapter.EpisodeAdapter;
import com.example.movieapp.model.Episode;
import com.example.movieapp.model.Movie;
import com.google.firebase.firestore.FieldValue;
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
    Button btnAddEpisode, btnSaveMovie;


    ArrayList<String> selectedTypeIds = new ArrayList<>();
    ArrayList<String> typeNames = new ArrayList<>();
    ArrayList<String> typeIds = new ArrayList<>();
    ArrayAdapter<String> typeAdapter;

    TextView tvSelectTypes;
    boolean[] checkedItems;


    List<Episode> episodeList = new ArrayList<>();

    EpisodeAdapter episodeAdapter;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

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

        tvSelectTypes = findViewById(R.id.tvSelectTypes);
        tvSelectTypes.setOnClickListener(v -> showTypeSelectionDialog());


        typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeNames);


        loadTypes();

        db = FirebaseFirestore.getInstance();

        episodeAdapter = new EpisodeAdapter(episodeList);
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        rvEpisodes.setAdapter(episodeAdapter);

        chkIsSeries.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            edtVideoUrl.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            layoutEpisodeList.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnAddEpisode.setOnClickListener(v -> showAddEpisodeDialog());

        btnSaveMovie.setOnClickListener(v -> saveMovieToFirestore());

        Button btnBackToAdmin = findViewById(R.id.btnBackToAdmin);
        btnBackToAdmin.setOnClickListener(v -> {
            startActivity(new Intent(AddMovieActivity.this, AdminActivity.class));
            finish();
        });
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
            ArrayList<String> selectedTypeDisplayNames = new ArrayList<>();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    selectedTypeIds.add(typeIds.get(i));
                    selectedTypeDisplayNames.add(typeNames.get(i));
                }
            }
            tvSelectTypes.setText(android.text.TextUtils.join(", ", selectedTypeDisplayNames));
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }



    private void loadTypes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("TYPE").get().addOnSuccessListener(querySnapshot -> {
            typeNames.clear();
            typeIds.clear();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                String name = doc.getString("nameType");
                String id = doc.getId();
                typeNames.add(name);
                typeIds.add(id);
            }
            typeAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải thể loại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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
            String title = edtEpTitle.getText().toString();
            String url = edtEpUrl.getText().toString();

            Episode episode = new Episode(episodeNum, title, url);
            episodeList.add(episode);
            episodeAdapter.notifyDataSetChanged();  // Cập nhật RecyclerView
        });


        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void saveMovieToFirestore() {
        String title = edtTitle.getText().toString();
        String description = edtDescription.getText().toString();
        int year = Integer.parseInt(edtYear.getText().toString());
        String thumbnail = edtThumbnail.getText().toString();
        boolean isSeries = chkIsSeries.isChecked();
        String videoUrl = isSeries ? null : edtVideoUrl.getText().toString();
        List<String> selectedTypeNames = new ArrayList<>(List.of(tvSelectTypes.getText().toString().split("\\s*,\\s*")));

        // Tạo đối tượng Movie
        Movie movie = new Movie(title, description, year, thumbnail, isSeries, selectedTypeIds, selectedTypeNames, videoUrl);

        db.collection("MOVIES").add(movie).addOnSuccessListener(docRef -> {
            if (isSeries) {
                if (episodeList != null && !episodeList.isEmpty()) {
                    List<Map<String, Object>> episodeDataList = new ArrayList<>();
                    for (Episode ep : episodeList) {
                        Map<String, Object> episodeData = new HashMap<>();
                        episodeData.put("title", ep.getTitle());
                        episodeData.put("episodeNumber", ep.getEpisodeNumber());
                        episodeData.put("videoUrl", ep.getVideoUrl());
                        episodeDataList.add(episodeData);
                    }

                    final int[] counter = {0};
                    for (Map<String, Object> episodeData : episodeDataList) {
                        docRef.collection("EPISODES").add(episodeData)
                                .addOnSuccessListener(doc -> {
                                    counter[0]++;
                                    if (counter[0] == episodeDataList.size()) {
                                        Toast.makeText(this, "Đã lưu phim bộ cùng các tập!", Toast.LENGTH_SHORT).show();
                                        clearForm();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Lỗi lưu tập phim: " + e.getMessage());
                                });
                    }
                } else {
                    Toast.makeText(this, "Không có tập phim nào để lưu.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Đã lưu phim lẻ!", Toast.LENGTH_SHORT).show();
                clearForm();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi lưu phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void clearForm() {
        edtTitle.setText("");
        edtDescription.setText("");
        edtYear.setText("");
        edtThumbnail.setText("");
        edtVideoUrl.setText("");
        tvSelectTypes.setText("");
        chkIsSeries.setChecked(false);
        episodeList.clear();
        episodeAdapter.notifyDataSetChanged();
    }

}

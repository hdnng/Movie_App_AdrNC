package com.example.movieapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.example.movieapp.Api.CloudinaryApi;
import com.example.movieapp.Api.CloudinaryHelper;
import com.example.movieapp.adapter.EpisodeAdapter;
import com.example.movieapp.model.Episode;
import com.example.movieapp.model.Movie;
import com.example.movieapp.model.Type;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddMovieActivity extends AppCompatActivity {

    EditText edtTitle, edtDescription, edtYear, edtVideoUrl;
    CheckBox chkIsSeries;
    LinearLayout layoutEpisodeList;
    RecyclerView rvEpisodes;
    Button btnAddEpisode, btnSaveMovie, btnBackToAdmin, btnSelectImage;
    ImageView ivThumbnail;
    TextView tvSelectTypes;
    List<Episode> episodeList = new ArrayList<>();
    EpisodeAdapter episodeAdapter;
    List<Type> typeList = new ArrayList<>();
    List<Type> selectedTypeList = new ArrayList<>();
    FirebaseFirestore db;

    Uri selectedImageUri;
    ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        // Init Cloudinary nếu chưa
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            MediaManager.init(this, CloudinaryApi.config);
        }

        initViews();
        setupListeners();
        loadTypes();
    }

    private void initViews() {
        db = FirebaseFirestore.getInstance();

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtYear = findViewById(R.id.edtYear);
        edtVideoUrl = findViewById(R.id.edtVideoUrl);
        chkIsSeries = findViewById(R.id.chkIsSeries);
        layoutEpisodeList = findViewById(R.id.layoutEpisodeList);
        rvEpisodes = findViewById(R.id.rvEpisodes);
        btnAddEpisode = findViewById(R.id.btnAddEpisode);
        btnSaveMovie = findViewById(R.id.btnSaveMovie);
        btnBackToAdmin = findViewById(R.id.btnBackToAdmin);
        tvSelectTypes = findViewById(R.id.tvSelectTypes);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivThumbnail = findViewById(R.id.ivThumbnail);

        episodeAdapter = new EpisodeAdapter(episodeList);
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        rvEpisodes.setAdapter(episodeAdapter);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                ivThumbnail.setVisibility(View.VISIBLE);
                Picasso.get().load(uri).into(ivThumbnail);
            }
        });
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        tvSelectTypes.setOnClickListener(v -> showTypeSelectionDialog());

        chkIsSeries.setOnCheckedChangeListener((buttonView, isChecked) -> {
            edtVideoUrl.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            layoutEpisodeList.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnAddEpisode.setOnClickListener(v -> showAddEpisodeDialog());

        btnSaveMovie.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh poster!", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadImageAndSaveMovie();
        });

        btnBackToAdmin.setOnClickListener(v -> {
            startActivity(new Intent(AddMovieActivity.this, AdminMovieListActivity.class));
            finish();
        });
    }

    private void loadTypes() {
        db.collection("TYPE").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Type type = doc.toObject(Type.class);
                        typeList.add(type);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thể loại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showTypeSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        List<CheckBox> checkBoxes = new ArrayList<>();
        List<Type> tempSelectedTypes = new ArrayList<>(selectedTypeList);

        for (Type type : typeList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(type.getNameType());
            checkBox.setChecked(tempSelectedTypes.contains(type));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tempSelectedTypes.add(type);
                } else {
                    tempSelectedTypes.remove(type);
                }
            });
            layout.addView(checkBox);
            checkBoxes.add(checkBox);
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            selectedTypeList.clear();
            selectedTypeList.addAll(tempSelectedTypes);
            StringBuilder sb = new StringBuilder();
            for (Type type : selectedTypeList) {
                sb.append(type.getNameType()).append(", ");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 2);
            tvSelectTypes.setText(sb.toString());
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
            String title = edtEpTitle.getText().toString().trim();
            String url = edtEpUrl.getText().toString().trim();
            if (!title.isEmpty() && !url.isEmpty()) {
                int epNumber = episodeList.size() + 1;
                episodeList.add(new Episode(UUID.randomUUID().toString(), epNumber, title, url));
                episodeAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin tập!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void uploadImageAndSaveMovie() {
        CloudinaryHelper.uploadImage(selectedImageUri, url -> {
            saveMovieToFirestore(url);
        }, errorMessage -> {
            Toast.makeText(this, "Lỗi upload ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void saveMovieToFirestore(String thumbnailUrl) {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String yearStr = edtYear.getText().toString().trim();
        String videoUrl = chkIsSeries.isChecked() ? null : edtVideoUrl.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin phim!", Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Năm không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> typeIds = new ArrayList<>();
        List<String> typeNames = new ArrayList<>();
        for (Type type : selectedTypeList) {
            typeIds.add(type.getIdType());
            typeNames.add(type.getNameType());
        }

        String movieId = UUID.randomUUID().toString();
        Movie movie = new Movie(movieId, title, description, year, thumbnailUrl, chkIsSeries.isChecked(), typeIds, typeNames, videoUrl);

        db.collection("MOVIES").document(movieId).set(movie)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Phim đã lưu thành công!", Toast.LENGTH_SHORT).show();
                    for (Episode episode : episodeList) {
                        saveEpisode(movieId, episode.getTitle(), episode.getVideoUrl(), episode.getEpisodeNumber());
                    }
                    startActivity(new Intent(AddMovieActivity.this, AdminMovieListActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveEpisode(String movieId, String title, String videoUrl, int episodeNumber) {
        String episodeId = UUID.randomUUID().toString();
        Episode episode = new Episode(episodeId, episodeNumber, title, videoUrl);
        db.collection("MOVIES").document(movieId).collection("EPISODES").document(episodeId).set(episode);
    }
}

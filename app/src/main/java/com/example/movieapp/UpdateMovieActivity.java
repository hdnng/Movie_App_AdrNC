package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.example.movieapp.adapter.MovieAdminAdapter;
import com.example.movieapp.model.Episode;
import com.example.movieapp.model.Movie;
import com.example.movieapp.model.Type;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpdateMovieActivity extends AppCompatActivity {

    EditText edtTitle, edtDescription, edtYear, edtThumbnail, edtVideoUrl;
    CheckBox chkIsSeries;
    LinearLayout layoutEpisodeList;
    RecyclerView rvEpisodes;
    Button btnAddEpisode, btnSaveMovie, btnBackToAdmin;
    TextView tvSelectTypes;
    List<Episode> episodeList = new ArrayList<>();
    EpisodeAdapter episodeAdapter;
    List<Type> typeList = new ArrayList<>();
    List<Type> selectedTypeList = new ArrayList<>();
    FirebaseFirestore db;  // Firestore
    Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_movie);

        //ánh xạ và load các view...
        init();

        //hiện dialog chon the loai
        tvSelectTypes.setOnClickListener(v -> showTypeSelectionDialog());

        // nếu chọn phim bộ thì hiện ds tập, phim lẻ thi hiện link video của 1 tập duy nhất
        chkIsSeries.setOnCheckedChangeListener((buttonView, isChecked) -> {
            edtVideoUrl.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            layoutEpisodeList.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Thêm tập
        btnAddEpisode.setOnClickListener(v -> showAddEpisodeDialog());

        // Lưu phim
        btnSaveMovie.setOnClickListener(v -> saveMovieToFirestore());

        btnBackToAdmin.setOnClickListener(v -> {
            startActivity(new Intent(UpdateMovieActivity.this, AdminActivity.class));
            finish();
        });
    }

    private void init(){
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

        // Cài đặt RecyclerView cho tập phim
        episodeAdapter = new EpisodeAdapter(episodeList);
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        rvEpisodes.setAdapter(episodeAdapter);

        //nhan movie tu intent
        Intent intent = getIntent();
        movie = ( Movie) intent.getSerializableExtra("movie");

        // Load thể loại
        loadTypes();

        //load ds the loai cua phim
        loadMovieTypes();

        //hien thi thong tin movie ra man hinh
        loadMovieInfoToView();
    }

    private void loadMovieInfoToView(){
        edtTitle.setText(movie.getTitle());
        edtDescription.setText(movie.getDescription());
        edtThumbnail.setText(movie.getThumbnail());
        //set ds thể loại cua phim
        StringBuilder selectedTypes = new StringBuilder();
        for (Type type : selectedTypeList) {
            selectedTypes.append(type.getNameType()).append(", ");
        }
        if (selectedTypes.length() > 0) {
            selectedTypes.setLength(selectedTypes.length() - 2); // Xóa dấu phẩy cuối
        }
        tvSelectTypes.setText(selectedTypes.toString());
        edtYear.setText(movie.getYear() + "");

        //hien thi tập phim le hoặc phim bo tuong ung
        if(movie.isSeries()){
            chkIsSeries.setChecked(true);
            layoutEpisodeList.setVisibility(View.VISIBLE);
            edtVideoUrl.setVisibility(View.GONE);
            //load ds episode
            db.collection("MOVIES").document(movie.getId()).collection("EPISODES").get()
                    .addOnCompleteListener(episodeTask -> {
                        if(episodeTask.isSuccessful()){
                            for (QueryDocumentSnapshot document : episodeTask.getResult()) {
                                episodeList.add(document.toObject(Episode.class));
                            }
                            episodeAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UpdateMovieActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }else{
            chkIsSeries.setChecked(false);
            layoutEpisodeList.setVisibility(View.GONE);
            edtVideoUrl.setVisibility(View.VISIBLE);
            edtVideoUrl.setText(movie.getVideoUrl()!=null?movie.getVideoUrl() : "");
        }
    }

    private void loadTypes() {
        db.collection("TYPE").get().addOnSuccessListener(querySnapshot -> {
            for (QueryDocumentSnapshot doc : querySnapshot) {
                // chuyen sang model va add vao ds type
                Type type = doc.toObject(Type.class);
                typeList.add(type);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi tải thể loại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void loadMovieTypes(){
        //từ ds typeid và typenme chuyê thành ds type tương ứng
        int size = movie.getTypeId().size();
        for (int i = 0; i < size; i++) {
            selectedTypeList.add(new Type(movie.getTypeId().get(i), movie.getTypeName().get(i)));
        }
    }

    private void showTypeSelectionDialog() {
        //Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại");

        // Tạo LinearLayout để chứa CheckBox
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Tạo danh sách CheckBox cho mỗi thể loại
        List<CheckBox> checkBoxList = new ArrayList<>();
        //tạo ds selected tạm thời copy từ ds selectedTypeList
        List<Type> tempSelectedTypeList =  new ArrayList<>(selectedTypeList);
        for (Type type : typeList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(type.getNameType());
            //nếu type tôn tại trong checkedTypeList thì set checked
            checkBox.setChecked(tempSelectedTypeList.contains(type));
            //xu ly su kien khi nhấn vào checkbox để chọn hoặc bo chọn
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tempSelectedTypeList.add(type);
                } else {
                    tempSelectedTypeList.remove(type);
                }
            });
            checkBoxList.add(checkBox);
            layout.addView(checkBox);//thêm checkBox vào layout
        }

        // Thêm LinearLayout vào Dialog
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            //Hiển thị ds đã chọn ra textview
            selectedTypeList.clear();
            selectedTypeList.addAll(tempSelectedTypeList);
            StringBuilder selectedTypes = new StringBuilder();
            for (Type type : tempSelectedTypeList) {
                selectedTypes.append(type.getNameType()).append(", ");
            }
            if (selectedTypes.length() > 0) {
                selectedTypes.setLength(selectedTypes.length() - 2); // Xóa dấu phẩy cuối
            }
            tvSelectTypes.setText(selectedTypes.toString());
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
                //tạo episode với id ngẫu nhiên
                episodeList.add(new Episode(UUID.randomUUID().toString() ,episodeNum, title, url));
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
        String year = edtYear.getText().toString().trim();
        String thumbnail = edtThumbnail.getText().toString().trim();
        String videoUrl = chkIsSeries.isChecked() ? null : edtVideoUrl.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || year.isEmpty() || thumbnail.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin phim!", Toast.LENGTH_SHORT).show();
            return;
        }

        //ds type id đã chọn
        List<String> selectedTypeIdList = new ArrayList<>();
        for (Type type : selectedTypeList) {
            selectedTypeIdList.add(type.getIdType());
        }
        //ds type name đã chọn
        List<String> selectedTypeNameList = new ArrayList<>();
        for (Type type : selectedTypeList) {
            selectedTypeNameList.add(type.getNameType());
        }
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setYear(Integer.parseInt(year));
        movie.setThumbnail(thumbnail);
        movie.setSeries(chkIsSeries.isChecked());
        movie.setTypeId(selectedTypeIdList);
        movie.setTypeName(selectedTypeNameList);
        movie.setVideoUrl(videoUrl);

// Then save the updated movie
        db.collection("MOVIES").document(movie.getId()).set(movie)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Phim đã được lưu thành công!", Toast.LENGTH_SHORT).show();
                    //clear ds episode cũ

                    //Lưu từng tập phim vào Firestore
                    for (Episode episode : episodeList) {
                        saveEpisode(movie.getId(), episode.getTitle(), episode.getVideoUrl(), episode.getEpisodeNumber());
                    }
                    //chuyen ve movielistadmin
                    startActivity(new Intent(UpdateMovieActivity.this, AdminMovieListActivity.class));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void saveEpisode(String movieId, String title, String videoUrl, int episodeNumber) {
        //Lưu tập phim với id ngẫu nhiên vào subcollection của movies
        String randomID = UUID.randomUUID().toString();
        Episode episode = new Episode(randomID, episodeNumber, title, videoUrl);
        db.collection("MOVIES").document(movieId).collection("EPISODES").document(randomID).set(episode);
    }

}
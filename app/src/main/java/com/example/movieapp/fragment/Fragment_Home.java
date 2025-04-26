package com.example.movieapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.movieapp.MovieDetailActivity;
import com.example.movieapp.R;
import com.example.movieapp.adapter.MovieAdapter;
import com.example.movieapp.model.Movie;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Home extends Fragment {

    private RecyclerView recyclerGenre1, recyclerGenre2;
    private TextView genreTitle1, genreTitle2;
    private FrameLayout featuredMovieContainer;
    private MovieAdapter adapterGenre1, adapterGenre2;
    private List<Movie> listGenre1, listGenre2;


    FirebaseFirestore db;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Fragment_Home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_home.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Home newInstance(String param1, String param2) {
        Fragment_Home fragment = new Fragment_Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getView(view);
        loadData();
        return view;
    }
    public void getView(View view){
        featuredMovieContainer = view.findViewById(R.id.featuredMovieContainer);
        genreTitle1 = view.findViewById(R.id.genreTitle1);
        genreTitle2 = view.findViewById(R.id.genreTitle2);
        recyclerGenre1 = view.findViewById(R.id.recyclerGenre1);
        recyclerGenre2 = view.findViewById(R.id.recyclerGenre2);

        listGenre1 = new ArrayList<>();
        listGenre2 = new ArrayList<>();
        adapterGenre1 = new MovieAdapter(listGenre1, movie -> openDetail(movie));
        adapterGenre2 = new MovieAdapter(listGenre2, movie -> openDetail(movie));

        recyclerGenre1.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerGenre2.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerGenre1.setAdapter(adapterGenre1);
        recyclerGenre2.setAdapter(adapterGenre2);

        db = FirebaseFirestore.getInstance();
    }

    private void loadMovies() {
        db.collection("MOVIES")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Movie> allMovies = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Movie movie = doc.toObject(Movie.class);
                        movie.setId(doc.getId());
                        allMovies.add(movie);
                    }

                    // Chọn 1 phim nổi bật ngẫu nhiên
                    int featuredIndex = (int)(Math.random() * allMovies.size());
                    Movie featuredMovie = allMovies.get(featuredIndex);
                    showFeaturedMovie(featuredMovie);  // Xử lý hiển thị phim nổi bật

                    // Lấy tất cả thể loại từ danh sách phim
                    Set<String> allGenres = new HashSet<>();
                    for (Movie movie : allMovies) {
                        allGenres.addAll(movie.getTypeName());  // typeName là List<String>
                    }

                    // Chọn ngẫu nhiên 2 thể loại
                    List<String> genreList = new ArrayList<>(allGenres);
                    Collections.shuffle(genreList);
                    String genre1 = genreList.get(0);
                    String genre2 = genreList.size() > 1 ? genreList.get(1) : genreList.get(0);

                    genreTitle1.setText(genre1);
                    genreTitle2.setText(genre2);
                    listGenre1.clear();
                    listGenre2.clear();
//                     Lọc phim theo thể loại
                    for (Movie movie : allMovies) {
                        if (movie.getTypeName().contains(genre1)) listGenre1.add(movie);
                        if (movie.getTypeName().contains(genre2)) listGenre2.add(movie);
                    }


                    adapterGenre1.notifyDataSetChanged();
                    adapterGenre2.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void showFeaturedMovie(Movie movie) {
        View view = getLayoutInflater().inflate(R.layout.item_featured_movie, featuredMovieContainer, false);

        ImageView img = view.findViewById(R.id.imgThumbnail);
        TextView title = view.findViewById(R.id.txtTitle);

        Glide.with(this).load(movie.getThumbnail()).into(img);
        title.setText(movie.getTitle());

        // 👉 Bắt sự kiện click để mở chi tiết phim
        view.setOnClickListener(v -> openDetail(movie));

        featuredMovieContainer.removeAllViews();
        featuredMovieContainer.addView(view);
    }
    private void openDetail(Movie movie) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("movieId", movie.getId());
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("description", movie.getDescription());
        intent.putExtra("year", movie.getYear());
        intent.putExtra("thumbnail", movie.getThumbnail());
        intent.putExtra("videoUrl", movie.getVideoUrl());
        intent.putExtra("isSeries", movie.isSeries());
        intent.putStringArrayListExtra("genres", new ArrayList<>(movie.getTypeName()));

        startActivity(intent);
    }

    public void loadData(){
        loadMovies();
    }
}
package com.example.movieapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.movieapp.AddMovieActivity;
import com.example.movieapp.R;
import com.example.movieapp.UpdateMovieActivity;
import com.example.movieapp.model.Movie;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class MovieAdminAdapter extends RecyclerView.Adapter<MovieAdminAdapter.MovieAdminHolder> {
    private List<Movie> movieList;
    private FirebaseFirestore db;
    public MovieAdminAdapter(List<Movie> movieList) {
        this.movieList = movieList;
        this.db = FirebaseFirestore.getInstance();
    }
    @NonNull
    @Override
    public MovieAdminHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_admin, parent, false);
        return new MovieAdminHolder(view);//tạo ra viewholder dựa theo các itemview truyền vào danhMucHolder
    }
    @Override
    public void onBindViewHolder(@NonNull MovieAdminHolder holder, int position) {
        Movie movie = movieList.get(position);
        if (movie == null) return;
        else {
            //set thông tin cho item
            holder.tvMovieName.setText(movie.getTitle());
            holder.tvMovieType.setText(movie.isSeries()? "Phim Bộ" : "Phim Lẻ");
            holder.tvYear.setText(movie.getYear()+"");
            //gán ảnh( ảnh là đường dẫn )

            // Load ảnh
            Glide.with(holder.itemView.getContext())
                    .load(movie.getThumbnail())
                    .apply(new RequestOptions()
                            .centerCrop())
                    .into(holder.imgThumb);

            //chuyen quan man hinh cap nhat
            holder.btnUpdate.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, UpdateMovieActivity.class);
                intent.putExtra("movie", movie);
                context.startActivity(intent);
            });

            //xu ly xoa
            holder.btnDelete.setOnClickListener( v -> {
                Context context = holder.itemView.getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xác nhận xóa phim");
                builder.setMessage("Bạn có chắc chắn muốn xóa phim này không?");
                builder.setPositiveButton("Xóa", (dialog, which) -> {
                    deleteMovie(context, movie);
                });
                builder.setNegativeButton("Hủy", null);
                builder.show();
            });
        }
    }
    private void deleteMovie(Context context, Movie movie) {
        db.collection("MOVIES").document(movie.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    movieList.remove(movie);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Xóa phim thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
    class MovieAdminHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvMovieName, tvMovieType, tvYear;
        Button btnUpdate, btnDelete;
        ConstraintLayout layoutitem;//layout chua item
        public MovieAdminHolder(@NonNull View itemView) {
            super(itemView);
            //Anh xa
            tvMovieName = itemView.findViewById(R.id.tvMovieName);
            tvMovieType = itemView.findViewById(R.id.tvMovieType);
            tvYear = itemView.findViewById(R.id.tvYear);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            layoutitem = itemView.findViewById(R.id.item_movie_admin);//layout chua item
        }
    }
}

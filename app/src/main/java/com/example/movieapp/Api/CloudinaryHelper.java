package com.example.movieapp.Api;

import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;
import java.util.UUID;

public class CloudinaryHelper {

    public interface SuccessCallback {
        void onSuccess(String url);
    }

    public interface ErrorCallback {
        void onError(String errorMessage);
    }

    public static void uploadImage(Uri uri, SuccessCallback onSuccess, ErrorCallback onError) {
        MediaManager.get().upload(uri)
                .option("public_id", "upload_" + UUID.randomUUID().toString())
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("CloudinaryHelper", "Upload started");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d("CloudinaryHelper", "Upload progress: " + bytes + "/" + totalBytes);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        Log.d("CloudinaryHelper", "Upload successful. URL: " + url);
                        onSuccess.onSuccess(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("CloudinaryHelper", "Upload failed: " + error.getDescription());
                        onError.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.e("CloudinaryHelper", "Upload rescheduled: " + error.getDescription());
                        onError.onError("Upload rescheduled due to error: " + error.getDescription());
                    }
                })
                .dispatch();
    }
}
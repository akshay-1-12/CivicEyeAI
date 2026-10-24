package com.civiceye.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.civiceye.app.R;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class CaptureActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> { if (granted) startCamera(); else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show(); }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        previewView = findViewById(R.id.previewView);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA);
        }

        findViewById(R.id.btnShutter).setOnClickListener(v -> takePhoto());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        File photoFile = new File(getCacheDir(), "civic_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                Intent intent = new Intent(CaptureActivity.this, AiAnalyzingActivity.class);
                intent.putExtra("image_path", photoFile.getAbsolutePath());
                startActivity(intent);
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CaptureActivity.this, "Photo capture failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
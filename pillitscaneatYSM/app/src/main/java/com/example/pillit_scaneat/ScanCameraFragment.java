package com.example.pillit_scaneat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScanCameraFragment extends Fragment {

    private PreviewView cameraPreview;
    private ImageButton btnTakePicture;
    private ImageCapture imageCapture;

    private ActivityResultLauncher<String> permissionLauncher;

    private static final String SERVER_URL = "https://scaneat-ocr-server.onrender.com/ocr";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan_camera, container, false);

        cameraPreview = view.findViewById(R.id.cameraPreview);
        btnTakePicture = view.findViewById(R.id.btnTakePicture);

        setupPermissionLauncher();
        requestCameraPermission();

        btnTakePicture.setOnClickListener(v -> takePhoto());

        return view;
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) startCamera();
                    else showError("카메라 권한이 필요합니다.");
                });
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                // 🔥 Preview 빌드 (OCR 최적화: 16:9 비율 고정)
                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setTargetResolution(new Size(1280, 720))
                        .build();

                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                // 🔥 ImageCapture 빌드 (고화질 촬영)
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setTargetResolution(new Size(1280, 720))
                        .setJpegQuality(100)      // 👈 JPEG 품질 100%
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                showError("카메라 실행 실패");
            }

        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        Bitmap bitmap = ImageUtils.convertImageProxyToBitmap(imageProxy);

                        // 🔥 Rotation 보정 (중요)
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        bitmap = rotateBitmap(bitmap, rotationDegrees);

                        imageProxy.close();

                        if (bitmap != null) {
                            uploadToServer(bitmap);
                        } else {
                            showError("사진 처리 오류 발생");
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        showError("촬영 실패: " + exception.getMessage());
                    }
                }
        );
    }

    // 🔥 Rotation 보정 함수
    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    private void uploadToServer(Bitmap bitmap) {
        new Thread(() -> {
            try {

                // 🔥 PNG 또는 JPEG 100% 품질로 전송
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageBytes = stream.toByteArray();

                String boundary = "----ScanEatBoundary";
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n");
                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                dos.write(imageBytes);
                dos.writeBytes("\r\n--" + boundary + "--\r\n");

                dos.flush();
                dos.close();

                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                int b;
                while ((b = conn.getInputStream().read()) != -1) {
                    responseStream.write(b);
                }

                String responseText = responseStream.toString("UTF-8");

                String ocrText = responseText;
                try {
                    JSONObject obj = new JSONObject(responseText);
                    ocrText = obj.getString("text");
                } catch (Exception ignore) {}

                String finalOcrText = ocrText;

                requireActivity().runOnUiThread(() -> moveToResult(finalOcrText));

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> showError("서버 업로드 실패: " + e.getMessage()));
            }
        }).start();
    }

    private void moveToResult(String text) {
        ResultFragment fragment = new ResultFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ocrText", text);
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showError(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
        }
    }
}

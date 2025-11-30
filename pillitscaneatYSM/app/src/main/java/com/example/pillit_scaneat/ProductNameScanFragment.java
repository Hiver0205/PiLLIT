package com.example.pillit_scaneat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProductNameScanFragment extends Fragment {

    private PreviewView cameraPreview;
    private ImageCapture imageCapture;
    private ImageButton btnCapture;

    private ActivityResultLauncher<String> permissionLauncher;

    private FirebaseFirestore db;

    private static final String SERVER_URL = "http://10.0.2.2:8000/ocr/product";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_product_name_scan, container, false);

        Log.d("PRODUCT_FLOW", "▶ ProductNameScanFragment 시작됨");

        cameraPreview = view.findViewById(R.id.cameraPreview2);
        btnCapture = view.findViewById(R.id.btnCaptureProduct);

        db = FirebaseFirestore.getInstance();

        permissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                    Log.d("PRODUCT_FLOW", "카메라 권한 결과 = " + granted);
                    if (granted) startCamera();
                });

        requestPermission();

        btnCapture.setOnClickListener(v -> {
            Log.d("PRODUCT_FLOW", "📸 촬영 버튼 클릭됨");
            takePhoto();
        });

        view.findViewById(R.id.btnBackProduct).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("PRODUCT_FLOW", "카메라 권한 이미 허용됨 → startCamera()");
            startCamera();
        } else {
            Log.d("PRODUCT_FLOW", "카메라 권한 요청");
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        Log.d("PRODUCT_FLOW", "🎥 startCamera() 호출됨");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider provider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);

                Log.d("PRODUCT_FLOW", "🎥 CameraX 바인딩 성공!");

            } catch (Exception e) {
                Log.e("PRODUCT_FLOW", "CameraX 바인딩 실패: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Log.e("PRODUCT_FLOW", "❌ imageCapture == null → 촬영 불가");
            return;
        }

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        Log.d("PRODUCT_FLOW", "📸 사진 캡쳐 성공");
                        Bitmap bitmap = ImageUtils.convertImageProxyToBitmap(image);
                        image.close();
                        sendToServer(bitmap);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("PRODUCT_FLOW", "❌ 촬영 실패: " + exception.getMessage());
                    }
                }
        );
    }

    private void sendToServer(Bitmap bitmap) {
        Log.d("PRODUCT_FLOW", "⬆ 서버 전송 시작");

        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/octet-stream");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                byte[] bytes = bos.toByteArray();

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(bytes);
                dos.flush();
                dos.close();

                int code = conn.getResponseCode();
                Log.d("PRODUCT_FLOW", "서버 응답 코드 = " + code);

                if (code == 200) {
                    String result = StreamUtils.readStream(conn.getInputStream());
                    Log.d("PRODUCT_FLOW", "서버 응답 데이터 = " + result);

                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded() || getActivity() == null) {
                            Log.e("PRODUCT_FLOW", "❌ Fragment가 이미 사라짐 → 화면 이동 취소");
                            return;
                        }
                        processProductName(result);
                    });

                } else {
                    Log.e("PRODUCT_FLOW", "❌ 서버 오류: code=" + code);
                }

            } catch (Exception e) {
                Log.e("PRODUCT_FLOW", "❌ 서버 전송 중 오류: " + e.getMessage());
            }
        }).start();
    }

    private void processProductName(String productName) {

        Log.d("PRODUCT_FLOW", "🔍 OCR 제품명 처리 시작: " + productName);

        String clean = productName.trim().toLowerCase();

        db.collection("drugs_v2")
                .whereEqualTo("제품명_lower", clean)
                .get()
                .addOnSuccessListener(q -> {

                    Log.d("PRODUCT_FLOW", "🔍 Firestore 검색 완료");

                    if (q.isEmpty()) {
                        Log.e("PRODUCT_FLOW", "❌ Firestore에서 제품명 없음");
                        Toast.makeText(requireContext(), "등록되지 않은 제품입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = q.getDocuments().get(0);
                    String id = doc.getId();
                    Log.d("PRODUCT_FLOW", "➡ productId = " + id);

                    ProductDetailFragment fragment = new ProductDetailFragment();
                    Bundle b = new Bundle();
                    b.putString("productId", id);
                    fragment.setArguments(b);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();

                    Log.d("PRODUCT_FLOW", "➡ ProductDetailFragment로 이동 완료");
                });
    }
}

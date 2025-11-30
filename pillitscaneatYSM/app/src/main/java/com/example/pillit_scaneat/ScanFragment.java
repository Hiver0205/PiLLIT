package com.example.pillit_scaneat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScanFragment extends Fragment {

    private Button btnCamera, btnUpload;

    private ActivityResultLauncher<String> galleryPermissionLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    private ActivityResultLauncher<String> galleryGetContent;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private static final String SERVER_URL = "https://scaneat-ocr-server.onrender.com/ocr";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        btnCamera = view.findViewById(R.id.btnCamera);
        btnUpload = view.findViewById(R.id.btnUpload);

        setupPermissionLaunchers();
        setupGalleryLauncher();
        setupCameraLauncher();

        btnUpload.setOnClickListener(v -> requestGalleryPermission());
        btnCamera.setOnClickListener(v -> requestCameraPermission());

        return view;
    }

    // --------------------------------------------------------
    // 권한 런처
    // --------------------------------------------------------
    private void setupPermissionLaunchers() {

        galleryPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        galleryGetContent.launch("image/*");
                    } else {
                        showError("이미지 접근 권한이 필요합니다.");
                    }
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) openCamera();
                    else showError("카메라 권한이 필요합니다.");
                }
        );
    }

    private void requestGalleryPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // --------------------------------------------------------
    // 갤러리 실행
    // --------------------------------------------------------
    private void setupGalleryLauncher() {
        galleryGetContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireContext().getContentResolver(), uri);

                            uploadToServer(bitmap);
                        } catch (Exception e) {
                            showError("이미지 처리 실패");
                        }
                    }
                }
        );
    }

    private void setupCameraLauncher() {

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null &&
                            result.getData().getExtras() != null) {

                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");

                        if (bitmap != null) uploadToServer(bitmap);
                        else showError("카메라 이미지 처리 실패");
                    }
                }
        );
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            showError("카메라 앱을 찾을 수 없습니다.");
        }
    }

    // --------------------------------------------------------
    // 서버 업로드
    // --------------------------------------------------------
    private void uploadToServer(Bitmap bitmap) {

        new Thread(() -> {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageBytes = stream.toByteArray();

                String boundary = "----ScanEatBoundary";
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);

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

                requireActivity().runOnUiThread(() -> extractIngredient(responseText));

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        showError("서버 업로드 실패: " + e.getMessage()));
            }
        }).start();
    }

    // --------------------------------------------------------
    // 🔥 OCR 텍스트 분석 → 분기 처리 (3종)
    // --------------------------------------------------------
    private void extractIngredient(String rawJson) {

        String text = extractText(rawJson);

        // =============================================
        // 1️⃣ OCR 텍스트 자체가 없음 → OCRFailFragment
        // =============================================
        if (text == null || text.trim().length() < 2) {

            OCRFailFragment fragment = new OCRFailFragment();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            return;
        }

        String cleanOcr = normalize(text);

        FirebaseFirestore.getInstance()
                .collection("drugs_v2")
                .get()
                .addOnSuccessListener(q -> {

                    String matched = null;

                    for (QueryDocumentSnapshot doc : q) {
                        String ing = doc.getString("주성분");
                        if (ing == null) continue;

                        String cleanIng = normalize(ing);

                        if (cleanOcr.contains(cleanIng)) {
                            matched = ing;
                            break;
                        }
                    }

                    // =============================================
                    // 2️⃣ OCR 성공 + 성분 없음 → NotFoundFragment
                    // =============================================
                    if (matched == null) {

                        NotFoundFragment fragment = new NotFoundFragment();

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                        return;
                    }

                    // =============================================
                    // 3️⃣ 성분 정상 매칭 → ProductSelectFragment
                    // =============================================
                    goToSelectFragment(matched);
                });
    }

    // JSON text 추출
    private String extractText(String raw) {
        try {
            JSONObject json = new JSONObject(raw);
            return json.getString("text");
        } catch (Exception e) {
            return raw;
        }
    }

    private String normalize(String t) {
        if (t == null) return "";
        t = t.toLowerCase();
        t = t.replaceAll("[^가-힣a-z0-9]", "");
        return t.trim();
    }

    // --------------------------------------------------------
    // ProductSelectFragment로 이동
    // --------------------------------------------------------
    private void goToSelectFragment(String ingredient) {

        ProductSelectFragment fragment = new ProductSelectFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ingredient", ingredient);
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showError(String msg) {
        if (getView() != null)
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }
}

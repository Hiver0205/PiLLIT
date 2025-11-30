package com.example.pillit_scaneat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
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

public class ImageScanFragment extends Fragment {

    private Button btnCamera, btnUpload;

    private static final String SERVER_URL = "https://scaneat-ocr-server.onrender.com/ocr";

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        btnCamera = view.findViewById(R.id.btnCamera);
        btnUpload = view.findViewById(R.id.btnUpload);

        setupGalleryLauncher();
        setupCameraLauncher();

        btnCamera.setOnClickListener(v -> openCamera());
        btnUpload.setOnClickListener(v -> openGallery());

        return view;
    }

    // =================== 갤러리 ===================
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null) {

                        Uri uri = result.getData().getData();
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

    // =================== 카메라 ===================
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null) {

                        Bitmap bitmap = (Bitmap) result.getData()
                                .getExtras().get("data");

                        uploadToServer(bitmap);
                    }
                }
        );
    }

    // =================== 서버 업로드 ===================
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
                conn.setRequestProperty(
                        "Content-Type", "multipart/form-data; boundary=" + boundary);

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

                requireActivity().runOnUiThread(() ->
                        extractIngredientAndMove(responseText));

            } catch (Exception e) {
                requireActivity().runOnUiThread(
                        () -> showError("서버 업로드 실패: " + e.getMessage()));
            }
        }).start();
    }

    // =================== OCR 전체 → 성분명만 추출 ===================
    private void extractIngredientAndMove(String rawJson) {

        String ocrText = extractText(rawJson).toLowerCase().replace(" ", "");

        FirebaseFirestore.getInstance()
                .collection("drugs_v2")
                .get()
                .addOnSuccessListener(q -> {

                    String foundIngredient = null;

                    for (QueryDocumentSnapshot doc : q) {

                        String ingredient = doc.getString("주성분");
                        if (ingredient == null) continue;

                        String cleanIng = ingredient.toLowerCase().replace(" ", "");

                        if (ocrText.contains(cleanIng)) {
                            foundIngredient = ingredient;
                            break;
                        }
                    }

                    if (foundIngredient == null) {
                        showError("성분을 인식하지 못했습니다.");
                        return;
                    }

                    goToSelectFragment(foundIngredient);
                });
    }

    // JSON text만 추출
    private String extractText(String raw) {
        try {
            JSONObject json = new JSONObject(raw);
            return json.getString("text");
        } catch (Exception e) {
            return raw;
        }
    }

    // =================== ProductSelectFragment 이동 ===================
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

    // =================== 에러 ===================
    private void showError(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
        }
    }
}

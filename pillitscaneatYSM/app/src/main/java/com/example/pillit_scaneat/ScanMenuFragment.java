package com.example.pillit_scaneat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.Activity;   // ★ 반드시 필요!
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ScanMenuFragment extends Fragment {

    private ActivityResultLauncher<Intent> pdfPickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // PDF Launcher 초기화
        setupPdfPicker();

        // 카메라 촬영
        view.findViewById(R.id.btnCamera).setOnClickListener(
                v -> navigate(new ScanFragment())
        );

        // 이미지 업로드
        view.findViewById(R.id.btnUpload).setOnClickListener(
                v -> navigate(new ImageScanFragment())
        );


        return view;
    }

    /** PDF 선택 런처 등록 */
    private void setupPdfPicker() {
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri pdfUri = result.getData().getData();

                        if (pdfUri != null) {
                            uploadPdfToFirebase(pdfUri);
                        }
                    }
                }
        );
    }

    /** PDF 선택창 실행 */
    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfPickerLauncher.launch(intent);
    }

    /** Firebase Storage 업로드 */
    private void uploadPdfToFirebase(Uri pdfUri) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference pdfRef =
                storageRef.child("pdfs/" + System.currentTimeMillis() + ".pdf");

        pdfRef.putFile(pdfUri)
                .addOnSuccessListener(task ->
                        pdfRef.getDownloadUrl().addOnSuccessListener(uri -> {

                            String downloadUrl = uri.toString();

                            // Firestore 저장
                            FirebaseFirestore.getInstance()
                                    .collection("uploaded_pdfs")
                                    .add(new PdfInfo(downloadUrl));

                            Toast.makeText(requireContext(),
                                    "업로드 성공!", Toast.LENGTH_LONG).show();
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "업로드 실패: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /** Firestore 저장용 데이터 모델 */
    public static class PdfInfo {
        public String url;

        public PdfInfo() {}

        public PdfInfo(String url) {
            this.url = url;
        }
    }

    /** 다른 Fragment로 이동 */
    private void navigate(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
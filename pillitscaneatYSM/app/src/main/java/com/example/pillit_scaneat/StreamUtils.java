package com.example.pillit_scaneat;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamUtils {

    /** Bitmap → Base64 문자열 변환 */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    /** ⭐ InputStream → String 으로 변환 (서버 응답 읽기) */
    public static String readStream(InputStream is) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString("UTF-8");
        } catch (Exception e) {
            return "";
        }
    }
}

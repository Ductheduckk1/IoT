package com.example.iot2;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class PlantActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView resultTextView;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    imageView.setImageURI(imageUri);
                    analyzePlant(imageUri);
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    imageView.setImageURI(selectedImageUri);
                    String base64String = "data:image/jpeg;base64,"+encodeImageToBase64(selectedImageUri);

                    // Hiển thị chuỗi Base64 trên terminal (Logcat)

                    // Lưu chuỗi Base64 vào tệp txt
                    saveBase64ToFile(base64String);
                    analyzePlant(selectedImageUri);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_identify);

        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        Button cameraButton = findViewById(R.id.cameraButton);
        Button galleryButton = findViewById(R.id.galleryButton);

        cameraButton.setOnClickListener(v -> openCamera());
        galleryButton.setOnClickListener(v -> openGallery());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = new File(getExternalFilesDir(null), "photo.jpg");
        imageUri = Uri.fromFile(photoFile); // Deprecated, should use FileProvider for API >= 24
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void analyzePlant(Uri imageUri) {
        // Chìa khóa API của bạn
        String plantIDApiKey = "fCjvXrju6PkameyQhFjrNCt026M53AD2GLifCWy8ksml1z2maJ";
        String apiUrl = "https://plant.id/api/v3/identification";

        // Mã hóa ảnh thành base64
        String encodedImage = "data:image/jpeg;base64,"+encodeImageToBase64(imageUri);
        System.out.println(encodedImage);
        if (encodedImage == null) {
            runOnUiThread(() -> resultTextView.setText("Error: Unable to encode image"));
            return; // Dừng lại nếu không mã hóa được ảnh
        }

        // Tạo JSON request body với ảnh mã hóa
        String jsonBody = "{\n" +
                "\"images\": [\"" + encodedImage + "\"],\n" +
                "\"latitude\": 21.0278,\n" +
                "\"longitude\": 105.8342,\n" +
                "\"similar_images\": true\n}";

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonBody);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Api-Key", plantIDApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // Xử lý phản hồi
                    String responseBody = response.body().string();
                    // Lấy tên cây từ phản hồi JSON và hiển thị
                    String commonName = extractCommonName(responseBody); // Phương thức bạn đã viết để lấy tên cây
                    runOnUiThread(() -> resultTextView.setText(commonName));
                } else {
                    runOnUiThread(() -> resultTextView.setText("Error identifying plant"));
                }
                response.close(); // Đảm bảo đóng kết nối sau khi sử dụng
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> resultTextView.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private String extractCommonName(String responseBody) {
        try {
            // Phân tích JSON và lấy tên cây
            JSONObject responseJson = new JSONObject(responseBody);
            JSONArray suggestions = responseJson.optJSONObject("result")
                    .optJSONObject("classification")
                    .optJSONArray("suggestions");

            if (suggestions != null && suggestions.length() > 0) {
                JSONObject firstSuggestion = suggestions.getJSONObject(0);

                // Lấy tên cây từ trường "name" trong đối tượng "suggestion"
                String plantName = firstSuggestion.optString("name");

                if (plantName != null && !plantName.isEmpty()) {
                    return plantName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unable to extract plant name";
    }


    private String encodeImageToBase64(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;

        try {
            // Lấy InputStream từ Uri
            Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
            // initialize byte stream
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            // compress Bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
            // Initialize byte array
            byte[] bytes=stream.toByteArray();
            // get base64 encoded string
             String sImage= Base64.encodeToString(bytes,Base64.DEFAULT);
            // set encoded text on textview
            return sImage;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void saveBase64ToFile(String base64String) {
        try {
            // Đường dẫn của tệp txt
            File outputFile = new File("D:\\output.txt");

            // Tạo FileWriter để ghi vào tệp
            FileWriter writer = new FileWriter(outputFile);
            writer.write(base64String);
            writer.close();


            // Thông báo cho người dùng rằng tệp đã được lưu
            Log.d("FileOutput", "Base64 has been saved to: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.example.iot2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PlantActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView resultTextView;
    private TextView resultHealthTextView;
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

//                    String base64String = "data:image/jpeg;base64,"+encodeImageToBase64(selectedImageUri);

                    analyzePlant(selectedImageUri);
                    getHealth(selectedImageUri);
                }
            });

    private final Handler httpHandler = new Handler(Looper.myLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_identify);

        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        resultHealthTextView = findViewById(R.id.resultHealthTextView);

        Button galleryButton = findViewById(R.id.galleryButton);

        galleryButton.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void analyzePlant(Uri imageUri) {
        // Chìa khóa API
        String plantIDApiKey = "fCjvXrju6PkameyQhFjrNCt026M53AD2GLifCWy8ksml1z2maJ";
        String apiUrl = "https://plant.id/api/v3/identification";

        // Mã hóa ảnh thành base64
        String encodedString = encodeImageToBase64(imageUri);
        if (encodedString == null) {
            runOnUiThread(() -> resultTextView.setText("Error: Unable to encode image"));
            return; // Dừng lại nếu không mã hóa được ảnh
        }

        String encodedImage = "data:image/jpeg;base64," + encodeImageToBase64(imageUri);
        // Tạo JSON request body với ảnh mã hóa
        JSONObject jsonBody = getJsonObject(encodedImage);

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonBody.toString());
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Api-Key", plantIDApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // Xử lý phản hồi
                        String responseBody = response.body().string();
                        // Lấy tên cây từ phản hồi JSON và hiển thị
                        String commonName = getName(extractCommonName(responseBody)); // Phương thức bạn đã viết để lấy tên cây
                        runOnUiThread(() -> resultTextView.setText(commonName));
                        httpHandler.removeCallbacks(this);
                    } else {
                        runOnUiThread(() -> resultTextView.setText("Error identifying plant, response code" + response.code()));
                    }
                    response.close(); // Đảm bảo đóng kết nối sau khi sử dụng
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> resultTextView.setText("Error: " + e.getMessage()));
                }
            }
        };
        httpHandler.post(runnable);
    }

    private static @NotNull JSONObject getJsonObject(String encodedImage) {
        // Creating the JSON structure
        JSONObject jsonBody = new JSONObject();

        // Adding the images array
        JSONArray imagesArray = new JSONArray();
        imagesArray.put(encodedImage);
        try {
            jsonBody.put("images", imagesArray);
            // Adding latitude and longitude
            jsonBody.put("latitude", 49.207);
            jsonBody.put("longitude", 16.608);
            // Adding similar_images field
            jsonBody.put("similar_images", true);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonBody;
    }
    private void getHealth(Uri imageUri){
        String plantIDApiKey = "fCjvXrju6PkameyQhFjrNCt026M53AD2GLifCWy8ksml1z2maJ";
        String apiUrl = "https://plant.id/api/v3/health_assessment";

        // Mã hóa ảnh thành base64
        String encodedImage = "data:image/jpeg;base64,"+encodeImageToBase64(imageUri);
        System.out.println(encodedImage);
        if (encodedImage == null) {
            runOnUiThread(() -> resultTextView.setText("Error: Unable to encode image"));
            return; // Dừng lại nếu không mã hóa được ảnh
        }

        // Tạo JSON request body với ảnh mã hóa
        String imageBase64 = encodedImage;

        // Creating the JSON structure
        JSONObject jsonBody = new JSONObject();

        // Adding the images array
        JSONArray imagesArray = new JSONArray();
        imagesArray.put(imageBase64);
        try {
            jsonBody.put("images", imagesArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Adding latitude and longitude
        try {
            jsonBody.put("latitude", 49.207);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            jsonBody.put("longitude", 16.608);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Adding similar_images field
        try {
            jsonBody.put("similar_images", true);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonBody.toString());
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
                    String status = extractHealthStatus(responseBody); // Phương thức bạn đã viết để lấy tên cây
                    runOnUiThread(() -> resultHealthTextView.setText(status));
                } else {
                    runOnUiThread(() -> resultHealthTextView.setText("Error identifying plant, response code" + response.code()));
                }
                response.close(); // Đảm bảo đóng kết nối sau khi sử dụng
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> resultHealthTextView.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
    private String extractHealthStatus(String responseBody) {
        try {
            // Phân tích JSON và lấy thông tin sức khỏe cây
            JSONObject responseJson = new JSONObject(responseBody);
            JSONObject isHealthyObject = responseJson.optJSONObject("result").optJSONObject("is_healthy");

            if (isHealthyObject != null) {
                // Lấy giá trị binary từ is_healthy
                boolean isHealthyBinary = isHealthyObject.optBoolean("binary", false);

                // Trả về kết quả dưới dạng chuỗi
                return isHealthyBinary ? "Khỏe mạnh" : "Không khỏe mạnh";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unable to extract health status";
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

                if (!plantName.isEmpty()) {
                    return plantName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unable to extract plant name";
    }


    private String encodeImageToBase64(Uri imageUri) {
        try {
            // Lấy InputStream từ Uri
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            // initialize byte stream
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // compress Bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            // Initialize byte array
            byte[] bytes = stream.toByteArray();
            // get base64 encoded string
            // set encoded text on textview
            return Base64.encodeToString(bytes, Base64.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getName(String name) {
        String url = "https://vi.wikipedia.org/wiki/" + name.replaceAll(" ", "_");

        try {
            // Tải trang Wikipedia của loài cây
            Document doc = Jsoup.connect(url).get();

            // Tìm phần thông tin tên thường dùng trong tiêu đề
            Elements commonNameElement = doc.select("span.mw-page-title-main");

            return commonNameElement.text();
        } catch (IOException e) {
            e.printStackTrace();
            return "404";
        }
    }
}

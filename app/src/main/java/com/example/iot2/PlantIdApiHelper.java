package com.example.iot2;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class PlantIdApiHelper {
    public static String identifyPlant(String apiUrl, String apiKey, File imageFile) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("images", imageFile.getName(), fileBody)
                .addFormDataPart("key", apiKey)
                .build();

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        return response.body() != null ? response.body().string() : "No response";
    }
}


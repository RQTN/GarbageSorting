package com.garbagesorting.android.util;

import java.io.File;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    public static void uploadImageByOkHttpRequest(String address, File imgFile, okhttp3.Callback callback) {
        String imgFileName = imgFile.getName();
        String suffix = imgFileName.substring(imgFileName.lastIndexOf("."));

        String newName = null;
        RequestBody imgBody = null;
        if (suffix.equals(".png")) {
            newName = UUID.randomUUID() + ".png";
            imgBody = RequestBody.create(MediaType.parse("image/png"), imgFile);
        } else if (suffix.equals(".jpg")) {
            newName = UUID.randomUUID() + ".jpg";
            imgBody = RequestBody.create(MediaType.parse("image/jpeg"), imgFile);
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", newName, imgBody)
                .build();

        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);

    }

}

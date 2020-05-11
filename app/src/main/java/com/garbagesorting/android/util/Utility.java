package com.garbagesorting.android.util;


import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.garbagesorting.android.db.Garbage;
import com.garbagesorting.android.db.History;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Utility {

    public static boolean handleGarbageData(String jsonData) {
        if (!TextUtils.isEmpty(jsonData)) {
            try {
                JSONArray allGarbages = new JSONArray(jsonData);
                List<Garbage> garbageList = new ArrayList<>(allGarbages.length());
                for (int i = 0; i < allGarbages.length(); i++) {
                    JSONObject garbageObject = allGarbages.getJSONObject(i);
                    Garbage garbage = new Garbage();
                    garbage.setName(garbageObject.getString("name"));
                    garbage.setLabel(garbageObject.getInt("label"));
                    garbage.setSource("default");
                    garbageList.add(garbage);
                }
                DataSupport.saveAll(garbageList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static String labelMapping(int label) {

        if (label == 1) return "可回收物";
        if (label == 2) return "有害垃圾";
        if (label == 3) return "湿垃圾";
        if (label == 4) return "干垃圾";

        return null;
    }

    public static boolean saveHistory(String query) {
        List<History> tmp = DataSupport.findAll(History.class);
        for (History history : tmp) {
            if (history.getName().equals(query)) {
                return true;
            }
        }
        if (tmp.size() > 10) {
            History oldest = DataSupport.order("id asc").limit(1).find(History.class).get(0);
            DataSupport.delete(History.class, oldest.getId());
        }
        History history = new History();
        history.setName(query);
        history.save();

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4*1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("PytorchAndroid", "Error process asset " + assetName + " to file path");
        }
        return null;
    }
}

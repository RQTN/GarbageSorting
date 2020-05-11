package com.garbagesorting.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.garbagesorting.android.db.Garbage;
import com.garbagesorting.android.model.Classifier;
import com.garbagesorting.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhotoResultActivity extends AppCompatActivity {

    private static final String TAG = "PhotoResultActivity";

    private static final int LOAD_MODEL = 1;

    private static final int PREDICT = 2;

    private ImageView picture;

    private List<String> querys = new ArrayList<>();

    private ResultAdapter adapter;

    private List<Result> suggestions = new ArrayList<>();

    private ListView photoResult;

    Classifier classifier;

    private Bitmap bitmap;

    private ProgressDialog progressDialog;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_MODEL:
                    progressDialog.setMessage("垃圾识别中");
                    Message message = new Message();
                    message.what = PREDICT;
                    handler.sendMessage(message);
                    break;
                case PREDICT:
                    float[] scores = classifier.predict(bitmap);
                    float[] probs = classifier.softmax(scores);
                    int[] top5Index = classifier.argTop5(scores);
                    Log.d(TAG, Arrays.toString(scores));
                    for (int i = 0; i < top5Index.length; i++) {
                        Garbage garbage = DataSupport.where("name = ?", Constants.GARBAGE_CLASSES[top5Index[i]]).find(Garbage.class).get(0);
                        suggestions.add(new Result(garbage.getName(), garbage.getLabel(), probs[top5Index[i]]));
                    }
                    adapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_result);
        Intent intent = getIntent();
        String imgPath = intent.getStringExtra("imgPath");
        picture = (ImageView) findViewById(R.id.picture);
        photoResult = (ListView) findViewById(R.id.photo_result);
        progressDialog = new ProgressDialog(PhotoResultActivity.this);

        adapter = new ResultAdapter(PhotoResultActivity.this, R.layout.result_item, suggestions);
        photoResult.setAdapter(adapter);
        photoResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Result result = suggestions.get(position);
                Toast.makeText(PhotoResultActivity.this, Utility.labelMapping(result.getLabel()), Toast.LENGTH_SHORT).show();
            }
        });
        File imgFile = new File(imgPath);
        bitmap = BitmapFactory.decodeFile(imgPath);
        picture.setImageBitmap(bitmap);


        // model 读取图片并输出结果到 querys 中，querys 中包含概率 >0 的结果
        progressDialog.setMessage("加载模型中");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String modelPath = Utility.assetFilePath(PhotoResultActivity.this, "resnext_cbam_cpu.pt");
                classifier = new Classifier(modelPath);
                Message message = new Message();
                message.what = LOAD_MODEL;
                handler.sendMessage(message);
            }
        }).start();

//        // Online way
//        String address = "http://www.rqtn.xyz:8080/densenet/predict/";
//        HttpUtil.uploadImageByOkHttpRequest(address, imgFile, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseData = response.body().string();
//            }
//        });

    }

}

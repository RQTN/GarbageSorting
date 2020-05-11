package com.garbagesorting.android;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.garbagesorting.android.db.Garbage;
import com.garbagesorting.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    public static final int INIT_DATA = 0;

    public static final int TAKE_PHOTO = 1;

    public static final int CHOOSE_PHOTO = 2;

    public static final int CLIP_PHOTO = 3;

    private DrawerLayout mDrawerLayout;

    private ProgressDialog progressDialog;

    private LinearLayout searchLayout;

    private ImageView camera;

    private ImageView hazardousImg;

    private ImageView recyclableImg;

    private ImageView householdFoodImg;

    private ImageView residualImg;

    private Uri imageUri;

    private Dialog bottomDialog;

    private String imgPath;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_DATA:
                    closeProgressDialog();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_3x);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        searchLayout = (LinearLayout) findViewById(R.id.search_layout);
        searchLayout.setOnClickListener(this);

        camera = (ImageView) findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog();
            }
        });

        hazardousImg = (ImageView) findViewById(R.id.hazardous_img);
        recyclableImg = (ImageView) findViewById(R.id.recyclable_img);
        householdFoodImg = (ImageView) findViewById(R.id.household_food_img);
        residualImg = (ImageView) findViewById(R.id.residual_img);

        hazardousImg.setOnClickListener(this);
        recyclableImg.setOnClickListener(this);
        householdFoodImg.setOnClickListener(this);
        residualImg.setOnClickListener(this);

        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        imgPath = outputImage.getAbsolutePath();
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(MainActivity.this, "com.garbagesorting.android.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }

        InitGarbageData();
    }


    private void InitGarbageData() {
        List<Garbage> garbageList = DataSupport.findAll(Garbage.class);
        if (garbageList.size() > 0) return;
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder builder = new StringBuilder();
                AssetManager assetManager = getAssets();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("newproduct.json"), "utf-8"));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }
                    Utility.handleGarbageData(builder.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = INIT_DATA;
                handler.sendMessage(message);
            }
        }).start();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("正在加载垃圾数据...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void showBottomDialog() {
        bottomDialog = new Dialog(MainActivity.this, R.style.BottomDialog);
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_dialog_content, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        layoutParams.height = getResources().getDisplayMetrics().heightPixels / 5;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.show();

        LinearLayout takePhoto = (LinearLayout) bottomDialog.findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA }, 1);
                } else {
                    openCamera();
                }
            }
        });
        LinearLayout choosePhoto = (LinearLayout) bottomDialog.findViewById(R.id.choose_photo);
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA }, 2);
                } else {
                    openAlbum();
                }
            }
        });
    }

    private void closeBottomDialog() {
        bottomDialog.dismiss();
    }

    private void openCamera() {
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void openAlbum() {
//        Intent intent = new Intent("android.intent.action.GET_CONTENT");
//        intent.setType("image/*");
//        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(MainActivity.this, uri)) {
            // 如果是 document 类型的 Uri，则通过 document id 处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的 id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 file 类型的 Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        imgPath = imagePath;
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        imgPath = imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过 Uri 和 selection 来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        // return path 举例：
        // /storage/emulated/0/Pictures/Screenshots/Screenshot_20200401-185213.jpg
        // /storage/emulated/0/DCIM/Camera/IMG_20200131_141146.jpg
        return path;
    }

    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        if (Build.MANUFACTURER.equals("HUAWEI")) {
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        } else {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
        intent.putExtra("outputX", 224);
        intent.putExtra("outputY", 224);
        intent.putExtra("circleCrop", false);
        intent.putExtra("return-data", true);
        Log.d(TAG, "Before Clip");
        startActivityForResult(intent, CLIP_PHOTO);
    }

    public String saveImage(Bitmap bmp) {
        File file = new File(imgPath);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    closeBottomDialog();
                    photoClip(imageUri);
//                    Intent intent = new Intent(MainActivity.this, PhotoResultActivity.class);
//                    intent.putExtra("imgPath", imgPath);
//                    startActivity(intent);
                }

                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
//                    if (Build.VERSION.SDK_INT >= 19) {
//                        // 4.4 及以上系统使用这个方法处理图片
//                        handleImageOnKitKat(data);
//                    } else {
//                        // 4.4 以下系统使用这个方法处理图片
//                        handleImageBeforeKitKat(data);
//                    }
                    closeBottomDialog();
                    photoClip(data.getData());
//                    Intent intent = new Intent(MainActivity.this, PhotoResultActivity.class);
//                    intent.putExtra("imgPath", imgPath);
//                    startActivity(intent);
                }
                break;
            case CLIP_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();

                    if (bundle != null) {
                        Log.d(TAG, "CLIP_PHOTO RESULT");
                        Bitmap image = bundle.getParcelable("data");
                        String path = saveImage(image);
                        Log.d(TAG, "Save Success");
                        Intent intent = new Intent(MainActivity.this, PhotoResultActivity.class);
                        intent.putExtra("imgPath", imgPath);
                        startActivity(intent);
                    }
                }
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(MainActivity.this, "你拒绝了权限请求", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(MainActivity.this, "你拒绝了权限请求", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_layout:
                Intent intent = new Intent(MainActivity.this, SearchGarbageActivity.class);
                startActivity(intent);
                break;
            case R.id.hazardous_img:
                break;
            case R.id.recyclable_img:
                break;
            case R.id.household_food_img:
                break;
            case R.id.residual_img:
                break;
            default:
                break;
        }
    }
}

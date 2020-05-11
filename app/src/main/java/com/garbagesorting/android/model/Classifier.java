package com.garbagesorting.android.model;


import android.graphics.Bitmap;
import android.util.Log;

import com.garbagesorting.android.Constants;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.util.Arrays;

public class Classifier {

    private static final String TAG = "Classifier";

    Module model;
    float[] mean = {0.485f, 0.456f, 0.406f};
    float[] std = {0.229f, 0.224f, 0.225f};

    public Classifier(String modelPath) {
        model = Module.load(modelPath);
    }

    public void setMeanAndStd(float[] mean, float[] std) {
        this.mean = mean;
        this.std = std;
    }

    public Tensor preprocess(Bitmap bitmap, int size) {
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap, this.mean, this.std);
    }

    public float[] softmax(float[] inputs) {
        float[] ret = new float[inputs.length];
        float sum = 0.0f;
        for (int i = 0; i < inputs.length; i++) {
            ret[i] = (float) Math.exp(inputs[i]);
            sum += ret[i];
        }
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ret[i] / sum;
        }
        return ret;
    }

    public int argMax(float[] inputs) {
        int maxIndex = -1;
        float maxValue = 0.0f;

        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] > maxValue) {
                maxIndex = i;
                maxValue = inputs[i];
            }
        }
        return maxIndex;
    }

    public int[] argTop5(float[] inputs) {
        int[] top5 = new int[5];
        for (int i = 0; i < top5.length; i++) {
            int maxIndex = argMax(inputs);
            top5[i] = maxIndex;
            inputs[maxIndex] = -1.0f;
        }
        return top5;
    }

    public float[] predict(Bitmap bitmap) {
        Tensor tensor = preprocess(bitmap, 224);

        IValue inputs = IValue.from(tensor);
        Tensor outputs = model.forward(inputs).toTensor();
        float[] scores = outputs.getDataAsFloatArray();

        Log.d(TAG, Arrays.toString(scores));
        return scores;
    }
}

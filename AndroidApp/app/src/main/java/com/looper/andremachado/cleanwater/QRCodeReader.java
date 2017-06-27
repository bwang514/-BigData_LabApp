package com.looper.andremachado.cleanwater;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * Created by andremachado on 11/06/2017.
 */

public class QRCodeReader {

    //SurfaceView cameraPreview;
    TextView txtResult;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    private Context context;


    public QRCodeReader(final Context context) {

        this.context = context;

        barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource
                .Builder(context, barcodeDetector)
                .build();
    }

    public void startCamera(SurfaceView cameraPreview){
        try {
            cameraSource.start(cameraPreview.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void stopCamera(){
        cameraSource.stop();
    }

    public void setProcessor(final TextView txtResult, final Button button){

        final boolean[] detected = {false};

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if(qrcodes.size() != 0)
                {

                    txtResult.post(new Runnable() {
                        @Override
                        public void run() {
                            txtResult.setText(qrcodes.valueAt(0).displayValue);
                        }
                    });

                    if(!detected[0]) {
                        button.setClickable(true);
                        button.getBackground().setColorFilter(0xffa4ed9e, PorterDuff.Mode.MULTIPLY);
                        button.setBackgroundColor(0xffa4ed9e);
                    }

                    detected[0] = true;
                }
            }
        });
    }


}

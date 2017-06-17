package com.looper.andremachado.cleanwater.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.looper.andremachado.cleanwater.BaseActivityLocation;
import com.looper.andremachado.cleanwater.QRCodeReader;
import com.looper.andremachado.cleanwater.R;
import com.looper.andremachado.cleanwater.SplashActivity;

public class CameraActivity extends BaseActivityLocation {

    public static final String TAG = CameraActivity.class.getSimpleName();
    TextView mLocalTV, mLocationProviderTV, mlocationTimeTV, txtResult;
    public Context context;
    SurfaceView cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        toolbar.setTitle("Title");

        context = getApplicationContext();
        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        txtResult = (TextView) findViewById(R.id.txtResult);

        final QRCodeReader qrCodeReader = new QRCodeReader(context);

        //Add Event
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                qrCodeReader.startCamera(cameraPreview);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                qrCodeReader.stopCamera();
            }
        });

        qrCodeReader.setProcessor(txtResult);

        initLocationFetching(CameraActivity.this);
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent mainIntent = new Intent(CameraActivity.this, ProfileActivity.class);
                CameraActivity.this.startActivity(mainIntent);
                CameraActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void initViews() {
        mLocalTV = (TextView) findViewById(R.id.locationDisplayTV);
        mLocationProviderTV = (TextView) findViewById(R.id.locationProviderTV);
        mlocationTimeTV = (TextView) findViewById(R.id.locationTimeFetchedTV);
    }

    @Override
    public void locationFetched(Location mLocal, Location oldLocation, String time, String locationProvider) {
        super.locationFetched(mLocal, oldLocation, time, locationProvider);
        Toast.makeText(getApplication(), "Lat : " + mLocal.getLatitude() + " Lng : " + mLocal.getLongitude(), Toast.LENGTH_SHORT).show();
        if(mLocal.getAltitude() == 0.0 && mLocal.getLongitude() == 0.0){
            Toast.makeText(context, R.string.not_found, Toast.LENGTH_SHORT).show();
        }else{
            mLocalTV.setText("Lat : " + mLocal.getLatitude() + " Lng : " + mLocal.getLongitude());
        }
        mLocationProviderTV.setText(locationProvider);
        mlocationTimeTV.setText(time);
    }
}

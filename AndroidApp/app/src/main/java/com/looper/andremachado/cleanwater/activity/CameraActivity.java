package com.looper.andremachado.cleanwater.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.AlphabeticIndex;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.looper.andremachado.cleanwater.BaseActivityLocation;
import com.looper.andremachado.cleanwater.QRCodeReader;
import com.looper.andremachado.cleanwater.R;
import com.looper.andremachado.cleanwater.SplashActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static com.looper.andremachado.cleanwater.utils.AppUtils.baseUrl;

public class CameraActivity extends BaseActivityLocation {

    public static final String TAG = CameraActivity.class.getSimpleName();
    TextView mLocalTV, mLocationProviderTV, mlocationTimeTV, txtResult;
    public Context context;
    SurfaceView cameraPreview;


    private SendDataTask mSendDataTask = null;

    private String pk, username, first_name, last_name, email;

    private String token;
    static double latitude=-1000, longitude=-1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        token = sharedPref.getString(getString(R.string.user_token), null);

        Intent intent = getIntent();

        pk = intent.getStringExtra("pk");
        first_name = intent.getStringExtra("first_name");
        last_name = intent.getStringExtra("last_name");
        email = intent.getStringExtra("email");
        username = intent.getStringExtra("username");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        toolbar.setTitle("Title");

        context = getApplicationContext();
        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        txtResult = (TextView) findViewById(R.id.txtResult);


        Button mSendDataButton = (Button) findViewById(R.id.code_detected_button);
        mSendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSendData();
            }
        });
        //TODO Uncomment
        //mSendDataButton.setClickable(false);



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

        qrCodeReader.setProcessor(txtResult, mSendDataButton);

        initLocationFetching(CameraActivity.this);
        initViews();
    }

    private void attemptSendData() {
        if (mSendDataTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;

        //String qrCodeId = txtResult.getText().toString();
        String qrCodeId = "Hardcoded test";

        if (qrCodeId.equals("QRReader")) {
            Log.d("TAG2","Error2");
            txtResult.setError(getString(R.string.error_invalid_password));
            focusView = txtResult;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (latitude < -900 || longitude < -900) {
            Log.d("TAG2","Error1 " + latitude + " - " + longitude);
            mLocationProviderTV.setError(getString(R.string.error_invalid_password));
            focusView = mLocationProviderTV;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mSendDataTask = new SendDataTask(qrCodeId);
            mSendDataTask.execute((Void) null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent mainIntent = new Intent(CameraActivity.this, ProfileActivity.class);

                mainIntent.putExtra("pk",pk);
                mainIntent.putExtra("first_name",first_name);
                mainIntent.putExtra("last_name",last_name);
                mainIntent.putExtra("username",username);
                mainIntent.putExtra("email",email);

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
        this.latitude = mLocal.getLatitude();
        this.longitude = mLocal.getLongitude();
        Log.d("TAG2","Latitude: "+latitude);
        Toast.makeText(getApplication(), "TAG: Lat : " + mLocal.getLatitude() + " Lng : " + mLocal.getLongitude(), Toast.LENGTH_SHORT).show();
        if(mLocal.getAltitude() == 0.0 && mLocal.getLongitude() == 0.0){
            Toast.makeText(context, R.string.not_found, Toast.LENGTH_SHORT).show();
        }else{
            mLocalTV.setText("Lat : " + mLocal.getLatitude() + " Lng : " + mLocal.getLongitude());
        }
        mLocationProviderTV.setText(locationProvider);
        mlocationTimeTV.setText(time);
    }


    public class SendDataTask extends AsyncTask<Void, Void, Boolean> {

        private final String mQrCodeId;
        private final double mLatitude, mLongitude;

        SendDataTask(String qrCodeId) {

            mQrCodeId = qrCodeId;
            mLatitude = latitude;
            mLongitude = longitude;

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String url = baseUrl + "data/";
            try {
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("qrCodeId", mQrCodeId);
                jsonBody.put("latitude", mLatitude);
                jsonBody.put("longitude", mLongitude);
                jsonBody.put("txId", "0");
                final String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("TAG", "Response: " + response);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        try {
                            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                            return Response.success(json, HttpHeaderParser.parseCacheHeaders(response));
                        } catch (Exception e) {
                            return Response.error(new ParseError(e));
                        }
                    }
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Token " + token);

                        return params;
                    }
                };

                requestQueue.add(stringRequest);
            } catch (JSONException e) {
                e.printStackTrace();

            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSendDataTask = null;

            if (success) {
                Intent mainIntent = new Intent(getApplicationContext(), ConfirmationActivity.class);

                mainIntent.putExtra("pk",pk);
                mainIntent.putExtra("first_name",first_name);
                mainIntent.putExtra("last_name",last_name);
                mainIntent.putExtra("username",username);
                mainIntent.putExtra("email",email);

                getApplicationContext().startActivity(mainIntent);
                CameraActivity.this.finish();

            } else {

            }


            if(!success){

            }
        }

        @Override
        protected void onCancelled() {
            mSendDataTask = null;
        }
    }

}

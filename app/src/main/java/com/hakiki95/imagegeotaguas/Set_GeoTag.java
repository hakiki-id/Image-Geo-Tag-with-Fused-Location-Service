package com.hakiki95.imagegeotaguas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class Set_GeoTag extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,LocationListener{
    ImageView imgHandler ;
    Button btn_setGeo;
    Location mLastLocation ;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    String lat, lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_geo_tag);

        Intent getIntent = getIntent();

        imgHandler = (ImageView) findViewById(R.id.imageViewHandle);
        btn_setGeo = (Button) findViewById(R.id.btn_set_geo);

        final String urlPathImage = getIntent.getExtras().getString("url_image");

        //decoder resource image
        Bitmap loadBitmap = BitmapFactory.decodeFile(urlPathImage);
        imgHandler.setImageBitmap(loadBitmap);

        buildGoogleApiClient();

        btn_setGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_TAG_GEO(urlPathImage,Double.valueOf(lat),Double.valueOf(lng));
                Intent home = new Intent(Set_GeoTag.this, MainActivity.class);
                startActivity(home);
            }
        });

    }


    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null){
            btn_setGeo.setVisibility(View.VISIBLE);
            lat = String.valueOf(mLastLocation.getLatitude());
            lng = String.valueOf(mLastLocation.getLongitude());

            Toast.makeText(Set_GeoTag.this, "Sukses, coordinate ditemukan !! \n" + "Latitude : " + lat + "\n longitude : " + lng, Toast.LENGTH_SHORT).show();
        }
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
     //    Toast.makeText(Set_GeoTag.this, "Sukses, coordinate ditemukan !! \n" + "Latitude : " + lat + "\n longitude : " + lng, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    public  void add_TAG_GEO (String fileLocation, double lat, double lng){
        try{
            ExifInterface exif = new ExifInterface(fileLocation);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,dec2DMS(lat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,dec2DMS(lng));

            if (lat > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,"N");

            }else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,"S");
            }
            if (lng > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,"E");

            }else{
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,"W");
            }

            exif.saveAttributes();

            Toast.makeText(Set_GeoTag.this, "Berhasil Menambahkan Coordinat", Toast.LENGTH_SHORT).show();

        }catch (Exception e){

        }
    }

    private String dec2DMS(double coordinate) {

        // Get absolute value of the coordinate (if negative, make it positive).
        coordinate = Math.abs(coordinate);  // -105.9876543 -> 105.9876543

        // Place degrees into String.
        String stringCoord = Integer.toString((int)coordinate) + "/1,";  // 105/1,

        // Place minutes into String.
        coordinate = (coordinate % 1) * 60;  // .987654321 * 60 = 59.259258
        stringCoord = stringCoord + Integer.toString((int)coordinate) + "/1,";  // 105/1,59/1,

        // Place seconds into String.
        coordinate = (coordinate % 1) * 60000;  // .259258 * 60000 = 15555
        stringCoord = stringCoord + Integer.toString((int)coordinate) + "/1000";  // 105/1,59/1,15555/1000

        return stringCoord;
    }


    private double dms2Dec(String sDMS) {
        double dRV = 999.0;
        try {
            String[] DMSs = sDMS.split(",", 3);
            String s[] = DMSs[0].split("/", 2);
            dRV = (new Double(s[0]) / new Double(s[1]));
            s = DMSs[1].split("/", 2);
            dRV += ((new Double(s[0]) / new Double(s[1])) / 60);
            s = DMSs[2].split("/", 2);
            dRV += ((new Double(s[0]) / new Double(s[1])) / 3600);

        } catch (Exception e) {

        }
        return dRV;

    }

}

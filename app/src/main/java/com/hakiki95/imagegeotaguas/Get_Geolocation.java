package com.hakiki95.imagegeotaguas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Get_Geolocation extends AppCompatActivity implements OnMapReadyCallback {
    ImageView img_source;
    TextView tv_coordinat;
    private GoogleMap gMap;

    double slat, slng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_geolocation);

        MapFragment  fragmentMaps = MapFragment.newInstance();
        android.app.FragmentTransaction ftMaps = getFragmentManager().beginTransaction();
        ftMaps.add(R.id.maps_place, fragmentMaps);
        ftMaps.commit();
        fragmentMaps.getMapAsync(this);

        Intent getResource = getIntent();
        String pathImage = getResource.getExtras().getString("path");


        img_source = (ImageView) findViewById(R.id.imagesource);
        tv_coordinat = (TextView) findViewById(R.id.tvCoordinat);

        Bitmap bitmat = BitmapFactory.decodeFile(pathImage);
        img_source.setImageBitmap(bitmat);

        tv_coordinat.setText(pathImage+"\n");

       show_GEO_IMAGE(pathImage);

    }



   public void show_GEO_IMAGE(String fileLocation){
        String lat="", latR="",lng="", lngR="";
        try{
            ExifInterface exif = new ExifInterface(fileLocation);
            lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            latR = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            lngR = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(lat != null && latR != null && lng != null && lngR != null){
            slat = dms2Dec(lat);
            slng = dms2Dec(lng);

            slat = latR.contains("S") ? -slat : slat;
            slng = lngR.contains("W") ? -slng : slng;

            Toast.makeText(Get_Geolocation.this, "lat : " + slat + "\n" + "log = " + slng, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(Get_Geolocation.this, "Gambar ini tidak memiliki coordinat GPS", Toast.LENGTH_SHORT).show();
        }
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

            Toast.makeText(Get_Geolocation.this, "Berhasil Menambahkan Coordinat", Toast.LENGTH_SHORT).show();

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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(slat, slng);
        gMap.addMarker(new MarkerOptions().position(sydney).title("Photonya di Ambil disini"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,17));
    }

}

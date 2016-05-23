package com.hakiki95.imagegeotaguas;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ImageButton btn_takeCamera, btn_BrowseFile;

    private File photoFile ;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static  final int REQUEST_BROWSE_FILE = 2;
    private Uri picLoadUri ;
    private String photoName= null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_takeCamera = (ImageButton) findViewById(R.id.btn_takeCamera);
        btn_BrowseFile = (ImageButton) findViewById(R.id.btn_browsefile);
    }

    public void take_photo(View v) {
        Intent take = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (take.resolveActivity(getPackageManager()) != null) {
            photoFile = null ;
            photoFile = getFileName();

            if (photoFile != null   ) {
                take.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(take,REQUEST_TAKE_PHOTO);
            }
        }else{
            Log.e("TAKE A PICTURE","Tidak ada aplikasi teristal untuk photo handler");
        }
    }

    private File getFileName(){
        String folderName="/DCIM/IMAGE GEO";
        File appFolder = new File(Environment.getExternalStorageDirectory(),folderName);
        //membuat sebuah folder
        appFolder.mkdir();

        String timeStraps = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_"+timeStraps+".jpg";

        photoFile = new File(appFolder,imageFileName);
        photoName = photoFile.getAbsolutePath();

        return photoFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //cek result from code
        switch (requestCode){
            case REQUEST_TAKE_PHOTO :
                if (resultCode == RESULT_OK){
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(new File(photoName))));
                    Intent goImageView = new Intent(getApplicationContext(),Set_GeoTag.class);
                    goImageView.putExtra("url_image",photoName.toString());
                    startActivity(goImageView);
                }else {
                    Toast.makeText(MainActivity.this, "gagal Mengambil Photo", Toast.LENGTH_SHORT).show();
                }
                break;
            case  REQUEST_BROWSE_FILE :
                if (resultCode == RESULT_OK && null != data){
                    picLoadUri = data.getData();
                    String path = getRealPath(picLoadUri);

                    Intent go_Maps = new Intent(getApplicationContext(),Get_Geolocation.class);
                    go_Maps.putExtra("path",path);
                    startActivity(go_Maps);
                }
                break;
        }
    }


    public String getRealPath(Uri pathuri){
        String[] proj = {MediaStore.Images.Media.DATA};

        CursorLoader cursorLoader = new CursorLoader(this,
                pathuri,proj, null,null,null);

        Cursor cursor = cursorLoader.loadInBackground();

        int Column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(Column_index);
    }

    public void Open_Galery(View v){
        Intent open_galery = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        open_galery.setType("image/*");
        open_galery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(open_galery,"SELECT PICTURE"),REQUEST_BROWSE_FILE);
    }
}

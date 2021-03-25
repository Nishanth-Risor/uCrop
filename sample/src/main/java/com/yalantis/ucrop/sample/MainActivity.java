package com.yalantis.ucrop.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String SAMPLE_CROPPED_IMAGE_NAME ="SampleCropImage.jpg" ;
    private static final int DISPLAY_IMAGE = 2;
    Button selectimage;
    public static final int SELECT_IMAGE=1;

    public  static String filepath=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filepath=(new File(getCacheDir(), SAMPLE_CROPPED_IMAGE_NAME)).getAbsolutePath();
        selectimage=findViewById(R.id.selectimage);

        selectimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);




            }
        });




    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK )
        {

            if(requestCode==SELECT_IMAGE)
            {

                Uri uri = data.getData();





                Bitmap bm = null;
                InputStream is = null;
                BufferedInputStream bis = null;


                try
                {
                    bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);


                    //bm = BitmapFactory.decodeFile(uri.getPath());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                 if(bm!=null)
                 {
                     bm=bm.copy(bm.getConfig(), true);



                       bm= EditActivity.process(MainActivity.this, bm);


                     uri= Uri.fromFile(new File(getCacheDir(), SAMPLE_CROPPED_IMAGE_NAME));

                     Intent intent =new Intent(this, EditActivity.class);
                     intent.putExtra("imageuri", uri.toString());

                     startActivity(intent);





                 }





            }
        }
    }
}
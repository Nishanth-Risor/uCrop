package com.yalantis.ucrop.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ResultActivityNew extends AppCompatActivity {



    public  Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;
    }

    public void startWithUri(String resultUri) {

        Bitmap firstbitmap=null;
        try {

                URL url = new URL(resultUri);
                firstbitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }



        if(firstbitmap!=null) {

            int rwidth;
            int rheight;
            int y = firstbitmap.getWidth();
            int x = firstbitmap.getHeight();
            float aspectrxy = (float) x / y;
            float aspectryx = (float) y / x;


            int mrh = 600, mrw = 500;

            if (x <= mrh && y > mrw) {
                rwidth = mrw;
                rheight = (int) (aspectrxy * rwidth);
            } else if (x <= mrh && y <= mrw) {

                float aspectratioframe=(float) mrw/mrh;

                float aspectratioimage=(float ) y/x;

                if(aspectratioframe>aspectratioimage)
                {
                    rheight = mrh;
                    rwidth = (int) (aspectryx * rheight);


                }
                else
                {rwidth = mrw;
                    rheight = (int) (aspectrxy * rwidth);

                }


            } else if (x > mrh && y <= mrw) {
                rheight = mrh;
                rwidth = (int) (aspectryx * rheight);

            } else {
                if (mrw <= mrh) {
                    rwidth = mrw;
                    rheight = (int) (aspectrxy * rwidth);
                } else {
                    rheight = mrh;
                    rwidth = (int) (aspectryx * rheight);
                }

            }


            Bitmap finalbitmap = getResizedBitmap(firstbitmap, rwidth, rheight);


            Bitmap reqbitmap = Bitmap.createBitmap(mrw, mrh, finalbitmap.getConfig());

            // Instantiate a canvas and prepare it to paint to the new bitmap
            Canvas canvas = new Canvas(reqbitmap);

            // Paint it white (or whatever color you want)
            canvas.drawColor(Color.RED);

            // Draw the old bitmap ontop of the new white one
            //int minw = Math.min(mRequiredWidth, decodeSampledBitmap.getWidth()), minh = Math.min(mRequiredHeight, decodeSampledBitmap.getHeight());

            int spcw = (mrw - rwidth + 1) / 2;
            int spch = (mrh - rheight + 1) / 2;
            canvas.drawBitmap(finalbitmap, spcw, spch, null);
            ImageView image = findViewById(R.id.result);

            image.setImageBitmap(reqbitmap);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_new);
        Intent intent=getIntent();
        startWithUri(intent.getStringExtra("resulturi"));



    }
}


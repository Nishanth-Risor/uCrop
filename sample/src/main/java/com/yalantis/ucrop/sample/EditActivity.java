package com.yalantis.ucrop.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.divyanshu.colorseekbar.ColorSeekBar;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EditActivity extends AppCompatActivity {

    private static final float BLUR_RADIUS = 25f;
    private int textcolor=0;

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
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
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap blur(Context context, Bitmap image) {
        if (null == image) return null;


        Bitmap outputBitmap = Bitmap.createBitmap(image);

        final RenderScript renderScript = RenderScript.create(context);

        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        //Intrinsic Gausian blur filter


        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        for(int i=1;i<=30;i++)
        {


            // tmpIn = Allocation.createFromBitmap(renderScript, outputBitmap);
            tmpIn=tmpOut;
            tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

            //Intrinsic Gausian blur filter


            theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
            theIntrinsic.setRadius(BLUR_RADIUS);
            theIntrinsic.setInput(tmpIn);
            theIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);
        }




        return outputBitmap;
    }




    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public  static Bitmap process(Context context, Bitmap decodeSampledBitmap) {



        int rwidth;
        int rheight;
        int y=decodeSampledBitmap.getWidth();
        int x=decodeSampledBitmap.getHeight();
        float aspectrxy=(float)x/y;
        float aspectryx=(float)y/x;

        int mrh, mrw;

        mrh = 1600;
        mrw = 900;


        if(x==mrh && y==mrw)
        {
            rwidth=mrw;
            rheight=mrh;
        }
        else if(x<=mrh && y>mrw)
        {
            rwidth=mrw;
            rheight= (int) (aspectrxy*rwidth);
        }
        else if(x<mrh && y<mrw)
        {

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
        }
        else if(x>mrh && y<=mrw)
        {
            rheight=mrh;
            rwidth=(int)(aspectryx*rheight);

        }
        else
        {
            if(y>x)
            {
                rwidth=mrw;
                rheight=(int)(aspectrxy*rwidth);
            }
            else
            {
                rheight=mrh;
                rwidth=(int)(aspectryx*rheight);
            }

        }





        Bitmap finalbitmap= getResizedBitmap(decodeSampledBitmap, rwidth, rheight);
        Bitmap reqbitmap = Bitmap.createBitmap(mrw, mrh, Bitmap.Config.ARGB_4444);
        // reqbitmap=getResizedBitmap(reqbitmap, mrw, mrh);

        // Instantiate a canvas and prepare it to paint to the new bitmap
        Canvas canvas = new Canvas(reqbitmap);

        // Paint it white (or whatever color you want)

        int multiplier=1;
        while(rwidth*multiplier<mrw || rheight*multiplier<mrh)
        {

            multiplier*=2;
        }
        int tw=(int)rwidth/multiplier;
        int th=(int)rheight/multiplier;
        int cx=(rwidth-tw)/2;
        int cy=(rheight-th)/2;



        Bitmap backbitmap = getResizedBitmap(Bitmap.createBitmap(finalbitmap, cx, cy, tw, th), mrw, mrh);
        backbitmap=backbitmap.copy(backbitmap.getConfig(), true);
        if(multiplier!=1)
            backbitmap=blur(context, backbitmap);
        canvas.drawBitmap(backbitmap, 0, 0, null);

        int spcw=(mrw-rwidth+1)/2;
        int spch=(mrh-rheight+1)/2;
        canvas.drawBitmap(finalbitmap, spcw, spch, null);

        decodeSampledBitmap=reqbitmap.copy(reqbitmap.getConfig(), true);


        File file = new File(MainActivity.filepath);
        OutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);

            decodeSampledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
            fOut.flush();
            fOut.close(); // do not forget to close the stream
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }





        return decodeSampledBitmap;
    }

    ImageView imageview;
    ImageButton gobackbtn, cropimage;
    ToggleButton addtext;
    EditText edittext;
    TextView text;
    ColorSeekBar color_seek_bar;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ActionBar bar=getSupportActionBar();
        if(bar!=null)
            bar.hide();
        imageview=findViewById(R.id.imageview);
        gobackbtn=(ImageButton)findViewById(R.id.gobackbtn);
        addtext= (ToggleButton) findViewById(R.id.addtext);
        edittext=findViewById(R.id.edittext);
        text=findViewById(R.id.text);
        color_seek_bar=findViewById(R.id.color_seek_bar);
        cropimage=findViewById(R.id.cropimage);

        addtext.setText("T");


        addtext.setTextOff("T");


        addtext.setTextOn("T");




        cropimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UCrop uCrop = UCrop.of(Uri.fromFile(new File(getCacheDir(), "SampleCropImage.jpg")), Uri.fromFile(new File(getCacheDir(), "SampleCropImage.jpg")));

                uCrop.start(EditActivity.this);
            }
        });

        color_seek_bar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int i) {
                text.setTextColor(i);
                textcolor=i;
            }
        });

        Intent intent=getIntent();

        Uri uri= Uri.parse(intent.getStringExtra("imageuri"));
         display(uri);

      gobackbtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              finish();
          }
      });

        text.setOnTouchListener(new View.OnTouchListener() {




            int prevX, prevY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                final RelativeLayout.LayoutParams par = (RelativeLayout.LayoutParams) v.getLayoutParams();

                float startX, startY;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        // prevX = (int) Math.min(mGestureCropImageView.getCropRect().right-mGestureCropImageView.getPaddingRight(), Math.max(mGestureCropImageView.getCropRect().left+mGestureCropImageView.getPaddingLeft(), event.getRawX()));
                        // prevY = (int) Math.min(mGestureCropImageView.getCropRect().bottom-mGestureCropImageView.getPaddingBottom(), Math.max(mGestureCropImageView.getCropRect().top+mGestureCropImageView.getPaddingTop(), event.getRawY()));
                        prevX= (int) event.getRawX();
                        prevY= (int) event.getRawY();
                        par.bottomMargin = -2 * v.getHeight();
                        par.rightMargin = -2 * v.getWidth();
                        v.setLayoutParams(par);


                    case MotionEvent.ACTION_MOVE:

                        par.topMargin+=(int)event.getRawY()-prevY;
                        //par.topMargin += (int) Math.min(mGestureCropImageView.getCropRect().bottom-mGestureCropImageView.getPaddingBottom(), Math.max(mGestureCropImageView.getCropRect().top+mGestureCropImageView.getPaddingTop(), event.getRawY())) - prevY;
                        prevY= (int) event.getRawY();
                        //prevY = (int) Math.min(mGestureCropImageView.getCropRect().bottom-mGestureCropImageView.getPaddingBottom(), Math.max(mGestureCropImageView.getCropRect().top+mGestureCropImageView.getPaddingTop(), event.getRawY()));
                        par.leftMargin+=(int)event.getRawX()-prevX;
                        // par.leftMargin += (int) Math.min(mGestureCropImageView.getCropRect().right-mGestureCropImageView.getPaddingRight(), Math.max(mGestureCropImageView.getCropRect().left+mGestureCropImageView.getPaddingLeft(), event.getRawX()))- prevX;
                        prevX= (int) event.getRawX();
                        //  prevX =(int) Math.min(mGestureCropImageView.getCropRect().right-mGestureCropImageView.getPaddingRight(), Math.max(mGestureCropImageView.getCropRect().left+mGestureCropImageView.getPaddingLeft(), event.getRawX()));
                        v.setLayoutParams(par);
                        break;
                    case MotionEvent.ACTION_UP:


                        par.topMargin+=(int)event.getRawY()-prevY;
                        par.leftMargin+=(int)event.getRawX()-prevX;

                        //par.topMargin += (int) Math.min(mGestureCropImageView.getCropRect().bottom-mGestureCropImageView.getPaddingBottom(), Math.max(mGestureCropImageView.getCropRect().top+mGestureCropImageView.getPaddingTop(), event.getRawY())) - prevY;
                        //par.leftMargin += (int) Math.min(mGestureCropImageView.getCropRect().right-mGestureCropImageView.getPaddingRight(), Math.max(mGestureCropImageView.getCropRect().left+mGestureCropImageView.getPaddingLeft(), event.getRawX()))- prevX;
                        v.setLayoutParams(par);


                        break;
                }


                return true;
            }





        });


        final ScaleGestureDetector mscaleGestureDetector=new ScaleGestureDetector(EditActivity.this, new TextScaleListener());
        //final RotationGestureDetector mrotationGestureDetector =new RotationGestureDetector( new TextRotationListener());

        imageview.setOnTouchListener(new View.OnTouchListener() {
            int mLastAngle=0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getPointerCount()==2) {

                    mscaleGestureDetector.onTouchEvent(event);
                    // mrotationGestureDetector.onTouchEvent(event);



                    float deltaX = event.getX(0) - event.getX(1);
                    float deltaY = event.getY(0) - event.getY(1);
                    double radians = Math.atan(deltaY / deltaX);
                    //Convert to degrees
                    int degrees = (int) (radians * 180 / Math.PI);


                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_POINTER_DOWN:
                        case MotionEvent.ACTION_POINTER_UP:
                            //Mark the initial angle
                            mLastAngle = degrees;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // ATAN returns a converted value between -90deg and +90deg
                            // which creates a point when two fingers are vertical where the
                            // angle flips sign.  We handle this case by rotating a small amount
                            // (5 degrees) in the direction we were traveling
                            if ((degrees - mLastAngle) > 45) {
                                //Going CCW across the boundary
                                text.setRotation(text.getRotation() - 5);
                            } else if ((degrees - mLastAngle) < -45) {
                                //Going CW across the boundary
                                text.setRotation(text.getRotation() + 5);
                            } else {
                                //Normal rotation, rotate the difference

                                text.setRotation(text.getRotation() + degrees - mLastAngle);
                            }
                            //Post the rotation to the image

                            //Save the current angle
                            mLastAngle = degrees;
                            break;
                    }








                }






                return true;
            }
        });





        addtext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    buttonView.setBackgroundResource(R.drawable.selected_background);
                    color_seek_bar.setVisibility(View.VISIBLE);

                   dotextoperations();

                } else {
                    // The toggle is disabled
                    buttonView.setBackgroundResource(R.drawable.unselected_background);
                    savetextoperations();
                    color_seek_bar.setVisibility(View.GONE);


                }
            }
        });


    }
    public  class TextScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // TODO Auto-generated method stub
            float size = text.getTextSize();
            //Log.d("TextSizeStart", String.valueOf(size));

            float factor = detector.getScaleFactor();
            //Log.d("Factor", String.valueOf(factor));


            factor = Math.max(0.1f, Math.min(factor, 5.0f));
            float product = size*factor;
            //Log.d("TextSize", String.valueOf(product));
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, product);


            return true;
        }
    }
    private void dotextoperations() {


        if(edittext!=null && text!=null) {
            edittext.setVisibility(View.VISIBLE);
            edittext.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null){
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }

            /*
            InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm.isAcceptingText()) {

            } else {
                edittext.setFocusable(false);
            }

             */

            KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener()
            {
                @Override
                public void onToggleSoftKeyboard(boolean isVisible)
                {
                    if(!isVisible) {
                        if (edittext.getText().toString().isEmpty()) {

                            addtext.setChecked(false);
                        } else {
                            text.setText(edittext.getText().toString());
                            text.setVisibility(View.VISIBLE);

                            edittext.setVisibility(View.GONE);

                        }
                    }

                }
            });











        }
    }

    private void savetextoperations() {

        final float cropoffsetx=text.getLeft()-(imageview.getLeft());
        final float cropoffsety=text.getTop()-(imageview.getTop());


        String str=text.getText().toString();
        float size=text.getTextSize();
        float angle=text.getRotation();
        float textwidth=text.getWidth();
        float textheight=text.getHeight();

        if (str.isEmpty()) {
           return;
        } else {
            Bitmap firstbitmap = BitmapFactory.decodeFile((new File(getCacheDir(), "SampleCropImage.jpg")).getAbsolutePath());
            if(firstbitmap!=null) {
                firstbitmap = firstbitmap.copy(firstbitmap.getConfig(), true);
                Bitmap editablebitmap = Bitmap.createBitmap(firstbitmap.getWidth(), firstbitmap.getHeight(), firstbitmap.getConfig());


                Canvas canvas = new Canvas(editablebitmap);
                Paint paint = new Paint();


                paint.setTextSize(size);
                paint.setColor(textcolor);

                //paint.setColor(0xFF6300);
                canvas.drawText(str, cropoffsetx, cropoffsety + textheight, paint);


                Matrix mMatrix = new Matrix();
                mMatrix.setRotate(angle, cropoffsetx + (textwidth / 2), cropoffsety + (textheight / 2));
                canvas = new Canvas(firstbitmap);
                canvas.drawBitmap(editablebitmap, mMatrix, null);
                File file = new File(getCacheDir(), "SampleCropImage.jpg");
                OutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(file);

                    firstbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
                    fOut.flush();
                    fOut.close(); // do not forget to close the stream
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageview.setImageBitmap(firstbitmap);
            }
           if(text!=null && edittext!=null)
            text.setText("");
            text.setVisibility(View.GONE);
            edittext.setText("");
            edittext.setVisibility(View.GONE);

        }


    }

    private void display(Uri uri) {

        Bitmap bm=null;
        try {
            bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            imageview.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

             if (requestCode == UCrop.REQUEST_CROP) {
                 final Uri resultUri = UCrop.getOutput(data);
                 display(resultUri);
            }
        }

    }
}
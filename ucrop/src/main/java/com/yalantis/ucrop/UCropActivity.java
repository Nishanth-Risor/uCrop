package com.yalantis.ucrop;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.Filters.ImageFilters;
import com.yalantis.ucrop.callback.BitmapCropCallback;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.util.RotationGestureDetector;
import com.yalantis.ucrop.util.SelectedStateListDrawable;
import com.yalantis.ucrop.view.CropImageView;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.TransformImageView;
import com.yalantis.ucrop.view.UCropView;
import com.yalantis.ucrop.view.widget.AspectRatioTextView;
import com.yalantis.ucrop.view.widget.HorizontalProgressWheelView;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */

@SuppressWarnings("ConstantConditions")
public class UCropActivity extends AppCompatActivity {

    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

    public static final int NONETEXT = 0;
    public static final int NONEFILTER = 4;
    public static final int SCALE = 1;
    public static final int ROTATE = 2;
    public static final int ALL = 3;
    Intent intent;

   // public  static File file=new File(getCacheDir(), "SampleCropImage.jpg");
    @IntDef({NONETEXT, SCALE, ROTATE, ALL, NONEFILTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureTypes {

    }

    private static final String TAG = "UCropActivity";
    private static final long CONTROLS_ANIMATION_DURATION = 50;
    private static final int TABS_COUNT = 5;
    private static final int SCALE_WIDGET_SENSITIVITY_COEFFICIENT = 15000;
    private static final int ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42;

    private String mToolbarTitle;

    // Enables dynamic coloring
    private int mToolbarColor;
    private int mStatusBarColor;
    private int mActiveControlsWidgetColor;
    private int mToolbarWidgetColor;
    @ColorInt
    private int mRootViewBackgroundColor;
    @DrawableRes
    private int mToolbarCancelDrawable;
    @DrawableRes
    private int mToolbarCropDrawable;
    private int mLogoColor;

    private boolean mShowBottomControls;
    private boolean mShowLoader = true;

    private UCropView mUCropView;
    private GestureCropImageView mGestureCropImageView;
    private OverlayView mOverlayView;


    private ViewGroup mWrapperStateAspectRatio, mWrapperStateRotate, mWrapperStateScale, mWrapperStateAddText, mWrapperStateAddFilter;
    private ViewGroup mLayoutAspectRatio, mLayoutRotate, mLayoutScale, mLayoutAddText, mLayoutAddFilter;
    private List<ViewGroup> mCropAspectRatioViews = new ArrayList<>();
    private TextView mTextViewRotateAngle, mTextViewScalePercent;
    private View mBlockingView;

    private Transition mControlsTransition;

    private Bitmap.CompressFormat mCompressFormat = DEFAULT_COMPRESS_FORMAT;
    private int mCompressQuality = DEFAULT_COMPRESS_QUALITY;
    private int[] mAllowedGestures = new int[]{SCALE, ROTATE, ALL,NONETEXT, NONEFILTER};
    EditText edittext;
    TextView text;
    float textleft, textright, texttop, textbottom;
    float mMidPntX, mMidPntY;


    public Button type, add, save, remove, doneucropbtn, cancelucropbtn;

    public ImageView rotate90;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ucrop_activity_photobox);
       cancelucropbtn=findViewById(R.id.cancelucropbtn);
        doneucropbtn=findViewById(R.id.doneucropbtn);
        rotate90=findViewById(R.id.rotate90);

         intent = getIntent();

        setupViews(intent);
        setImageData(intent);
        setInitialState();
        addBlockingView();
        cancelucropbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        rotate90.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateByAngle(90);
            }
        });
        doneucropbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cropAndSaveImage();

            }
        });



        text=findViewById(R.id.text);
        edittext=findViewById(R.id.edittext);
        type=findViewById(R.id.type);
        add=findViewById(R.id.add);
        remove=findViewById(R.id.remove);
        save=findViewById(R.id.save);
        if(type!=null){
            type.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    text.setText("");
                    text.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) text.getLayoutParams();
                    params.leftMargin = (int) (mGestureCropImageView.getCropRect().left+mGestureCropImageView.getPaddingLeft() );
                    params.topMargin = (int) (mGestureCropImageView.getCropRect().top+mGestureCropImageView.getPaddingTop());
                    edittext.setLayoutParams(params);
                    edittext.setVisibility(View.VISIBLE);
                    edittext.requestFocus();


                }
            });}

              if(add!=null){
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String str = edittext.getText().toString();
                    edittext.setText("");
                    edittext.setVisibility(View.GONE);

                    // text.setText("");
                    if (str.isEmpty()) {
                        Toast.makeText(UCropActivity.this, "Please type some text and press add button", Toast.LENGTH_SHORT).show();
                    } else {
                        text.setText(str);

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) text.getLayoutParams();
                        params.leftMargin = (int) (mGestureCropImageView.getCropRect().left+mGestureCropImageView.getPaddingLeft() );
                        params.topMargin = (int) (mGestureCropImageView.getCropRect().top+mGestureCropImageView.getPaddingTop());
                        text.setLayoutParams(params);
                        text.setVisibility(View.VISIBLE);


                    }

                }
            });}

              if(text!=null){
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

        });}
             if(save!=null)
             {


                 save.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {

                         final float cropoffsetx=text.getLeft()-(mGestureCropImageView.getCropRect().left+mGestureCropImageView.getPaddingLeft());
                         final float cropoffsety=text.getTop()-(mGestureCropImageView.getCropRect().top+mGestureCropImageView.getPaddingTop());


                         String str=text.getText().toString();
                         float size=text.getTextSize();
                         float angle=text.getRotation();
                         float textwidth=text.getWidth();
                         float textheight=text.getHeight();

                         if (str.isEmpty()) {
                        Toast.makeText(UCropActivity.this, "Please type some text and press save button", Toast.LENGTH_SHORT).show();
                    } else {
                             Bitmap firstbitmap = BitmapFactory.decodeFile((new File(getCacheDir(), "SampleCropImage.jpg")).getAbsolutePath());
                             firstbitmap=firstbitmap.copy(firstbitmap.getConfig(), true);
                             Bitmap editablebitmap = Bitmap.createBitmap(firstbitmap.getWidth(), firstbitmap.getHeight(),firstbitmap.getConfig() );


                             Canvas canvas = new Canvas(editablebitmap);
                             Paint paint = new Paint();


                             paint.setTextSize(size);


                             //paint.setColor(0xFF6300);
                             canvas.drawText(str, cropoffsetx, cropoffsety + textheight, paint);


                             Matrix mMatrix = new Matrix();
                             mMatrix.setRotate (angle, cropoffsetx+(textwidth/2), cropoffsety+(textheight/2));
                             canvas=new Canvas(firstbitmap);
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

                             Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                             Uri outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                             try {
                                 mGestureCropImageView.setImageUri(inputUri, outputUri);
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }

                             text.setText("");
                             text.setVisibility(View.GONE);
                             edittext.setVisibility(View.GONE);
                             edittext.setText("");
                         }

                     }
                 });
             }
           if(remove!=null)
           {
               remove.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {

                       String str=text.getText().toString();

                       if (str.isEmpty()) {
                        Toast.makeText(UCropActivity.this, "Please type some text and press remove button", Toast.LENGTH_SHORT).show();
                    } else {

                           text.setText("");
                           text.setVisibility(View.GONE);
                           edittext.setVisibility(View.GONE);
                           edittext.setText("");
                       }
                   }
               });
           }




































    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.ucrop_menu_activity, menu);

        // Change crop & loader menu icons color to match the rest of the UI colors

        MenuItem menuItemLoader = menu.findItem(R.id.menu_loader);
        Drawable menuItemLoaderIcon = menuItemLoader.getIcon();
        if (menuItemLoaderIcon != null) {
            try {
                menuItemLoaderIcon.mutate();
                menuItemLoaderIcon.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
                menuItemLoader.setIcon(menuItemLoaderIcon);
            } catch (IllegalStateException e) {
                Log.i(TAG, String.format("%s - %s", e.getMessage(), getString(R.string.ucrop_mutate_exception_hint)));
            }
            ((Animatable) menuItemLoader.getIcon()).start();
        }

        MenuItem menuItemCrop = menu.findItem(R.id.menu_crop);
        Drawable menuItemCropIcon = ContextCompat.getDrawable(this, mToolbarCropDrawable);
        if (menuItemCropIcon != null) {
            menuItemCropIcon.mutate();
            menuItemCropIcon.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
            menuItemCrop.setIcon(menuItemCropIcon);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_crop).setVisible(!mShowLoader);
        menu.findItem(R.id.menu_loader).setVisible(mShowLoader);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_crop) {
            cropAndSaveImage();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGestureCropImageView != null) {
            mGestureCropImageView.cancelAllAnimations();
        }
    }


    /**
     * This method extracts all data from the incoming intent and setups views properly.
     */
    private void setImageData(@NonNull Intent intent) {
        Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_INPUT_URI);
        Uri outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
        processOptions(intent);

        if (inputUri != null && outputUri != null) {
            try {
                mGestureCropImageView.setImageUri(inputUri, outputUri);
            } catch (Exception e) {
                setResultError(e);
                finish();
            }
        } else {
            setResultError(new NullPointerException(getString(R.string.ucrop_error_input_data_is_absent)));
            finish();
        }
    }

    /**
     * This method extracts {@link com.yalantis.ucrop.UCrop.Options #optionsBundle} from incoming intent
     * and setups Activity, {@link OverlayView} and {@link CropImageView} properly.
     */
    @SuppressWarnings("deprecation")
    private void processOptions(@NonNull Intent intent) {
        // Bitmap compression options
        String compressionFormatName = intent.getStringExtra(UCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME);
        Bitmap.CompressFormat compressFormat = null;
        if (!TextUtils.isEmpty(compressionFormatName)) {
            compressFormat = Bitmap.CompressFormat.valueOf(compressionFormatName);
        }
        mCompressFormat = (compressFormat == null) ? DEFAULT_COMPRESS_FORMAT : compressFormat;

        mCompressQuality = intent.getIntExtra(UCrop.Options.EXTRA_COMPRESSION_QUALITY, UCropActivity.DEFAULT_COMPRESS_QUALITY);

        // Gestures options
        int[] allowedGestures = intent.getIntArrayExtra(UCrop.Options.EXTRA_ALLOWED_GESTURES);
        if (allowedGestures != null && allowedGestures.length == TABS_COUNT) {
            mAllowedGestures = allowedGestures;
        }

        // Crop image view options
        mGestureCropImageView.setMaxBitmapSize(intent.getIntExtra(UCrop.Options.EXTRA_MAX_BITMAP_SIZE, CropImageView.DEFAULT_MAX_BITMAP_SIZE));
        mGestureCropImageView.setMaxScaleMultiplier(intent.getFloatExtra(UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER, CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER));
        mGestureCropImageView.setImageToWrapCropBoundsAnimDuration(intent.getIntExtra(UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION));

        // Overlay view options
        mOverlayView.setFreestyleCropEnabled(intent.getBooleanExtra(UCrop.Options.EXTRA_FREE_STYLE_CROP, OverlayView.DEFAULT_FREESTYLE_CROP_MODE != OverlayView.FREESTYLE_CROP_MODE_DISABLE));

        mOverlayView.setDimmedColor(intent.getIntExtra(UCrop.Options.EXTRA_DIMMED_LAYER_COLOR, getResources().getColor(R.color.ucrop_color_default_dimmed)));
        mOverlayView.setCircleDimmedLayer(intent.getBooleanExtra(UCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER, OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER));

        mOverlayView.setShowCropFrame(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_FRAME, OverlayView.DEFAULT_SHOW_CROP_FRAME));
        mOverlayView.setCropFrameColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_COLOR, getResources().getColor(R.color.ucrop_color_default_crop_frame)));
        mOverlayView.setCropFrameStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_STROKE_WIDTH, getResources().getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width)));

        mOverlayView.setShowCropGrid(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_GRID, OverlayView.DEFAULT_SHOW_CROP_GRID));
        mOverlayView.setCropGridRowCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_ROW_COUNT, OverlayView.DEFAULT_CROP_GRID_ROW_COUNT));
        mOverlayView.setCropGridColumnCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLUMN_COUNT, OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT));
        mOverlayView.setCropGridColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLOR, getResources().getColor(R.color.ucrop_color_default_crop_grid)));
        mOverlayView.setCropGridCornerColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_CORNER_COLOR, getResources().getColor(R.color.ucrop_color_default_crop_grid)));
        mOverlayView.setCropGridStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_STROKE_WIDTH, getResources().getDimensionPixelSize(R.dimen.ucrop_default_crop_grid_stoke_width)));

        // Aspect ratio options
        float aspectRatioX = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_X, 0);
        float aspectRatioY = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_Y, 0);

        int aspectRationSelectedByDefault = intent.getIntExtra(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0);
        ArrayList<AspectRatio> aspectRatioList = intent.getParcelableArrayListExtra(UCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS);

        if (aspectRatioX > 0 && aspectRatioY > 0) {
            if (mWrapperStateAspectRatio != null) {
                mWrapperStateAspectRatio.setVisibility(View.GONE);
            }
            mGestureCropImageView.setTargetAspectRatio(aspectRatioX / aspectRatioY);
        } else if (aspectRatioList != null && aspectRationSelectedByDefault < aspectRatioList.size()) {
            mGestureCropImageView.setTargetAspectRatio(aspectRatioList.get(aspectRationSelectedByDefault).getAspectRatioX() /
                    aspectRatioList.get(aspectRationSelectedByDefault).getAspectRatioY());
        } else {
            mGestureCropImageView.setTargetAspectRatio(CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
        }

        // Result bitmap max size options
        int maxSizeX = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_X, 0);
        int maxSizeY = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_Y, 0);

        if (maxSizeX > 0 && maxSizeY > 0) {
            mGestureCropImageView.setMaxResultImageSizeX(maxSizeX);
            mGestureCropImageView.setMaxResultImageSizeY(maxSizeY);
        }
    }

    private void setupViews(@NonNull Intent intent) {
        mStatusBarColor = intent.getIntExtra(UCrop.Options.EXTRA_STATUS_BAR_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_statusbar));
        mToolbarColor = intent.getIntExtra(UCrop.Options.EXTRA_TOOL_BAR_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_toolbar));
        mActiveControlsWidgetColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_COLOR_CONTROLS_WIDGET_ACTIVE, ContextCompat.getColor(this, R.color.ucrop_color_active_controls_color));

        mToolbarWidgetColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_COLOR_TOOLBAR, ContextCompat.getColor(this, R.color.ucrop_color_toolbar_widget));
        mToolbarCancelDrawable = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_CANCEL_DRAWABLE, R.drawable.ucrop_ic_cross);
        mToolbarCropDrawable = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_CROP_DRAWABLE, R.drawable.ucrop_ic_done);
        mToolbarTitle = intent.getStringExtra(UCrop.Options.EXTRA_UCROP_TITLE_TEXT_TOOLBAR);
        mToolbarTitle = mToolbarTitle != null ? mToolbarTitle : getResources().getString(R.string.ucrop_label_edit_photo);
        mLogoColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_LOGO_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_default_logo));
      mShowBottomControls = !intent.getBooleanExtra(UCrop.Options.EXTRA_HIDE_BOTTOM_CONTROLS, false);

        //mShowBottomControls=false;
        mRootViewBackgroundColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_ROOT_VIEW_BACKGROUND_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_crop_background));

        setupAppBar();
        initiateRootViews();

        if (mShowBottomControls) {

            ViewGroup viewGroup = findViewById(R.id.ucrop_photobox);
            ViewGroup wrapper = viewGroup.findViewById(R.id.controls_wrapper);
            //wrapper.setVisibility(View.VISIBLE);
            LayoutInflater.from(this).inflate(R.layout.ucrop_controls, wrapper, true);

            mControlsTransition = new AutoTransition();
            mControlsTransition.setDuration(CONTROLS_ANIMATION_DURATION);

            mWrapperStateAspectRatio = findViewById(R.id.state_aspect_ratio);
            mWrapperStateAspectRatio.setOnClickListener(mStateClickListener);

            mWrapperStateAddText= findViewById(R.id.state_addtext);
            mWrapperStateAddText.setOnClickListener(mStateClickListener);
            mWrapperStateAddFilter= findViewById(R.id.state_addfilters);
            mWrapperStateAddFilter.setOnClickListener(mStateClickListener);
            mWrapperStateRotate = findViewById(R.id.state_rotate);
            mWrapperStateRotate.setOnClickListener(mStateClickListener);
            mWrapperStateScale = findViewById(R.id.state_scale);
            mWrapperStateScale.setOnClickListener(mStateClickListener);

            mLayoutAspectRatio = findViewById(R.id.layout_aspect_ratio);
            mLayoutRotate = findViewById(R.id.layout_rotate_wheel);
            mLayoutScale = findViewById(R.id.layout_scale_wheel);
            mLayoutAddText= findViewById(R.id.layout_add_text);
            mLayoutAddFilter= findViewById(R.id.layout_add_filters);

            setupAspectRatioWidget(intent);
            setupRotateWidget();
            setupScaleWidget();
            setupAddTextWidget();

            setupStatesWrapper();
        }
    }

    private void setupAddTextWidget() {

        /*
        final GestureDetector mGestureDetector=new GestureDetector(new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });

        mGestureCropImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mGestureDetector.onTouchEvent(event);

                return true;
            }
        });

         */








    }

    /**
     * Configures and styles both status bar and toolbar.
     */
    private void setupAppBar() {
        setStatusBarColor(mStatusBarColor);

        final Toolbar toolbar = findViewById(R.id.toolbar);

        // Set all of the Toolbar coloring
        toolbar.setBackgroundColor(mToolbarColor);
        toolbar.setTitleTextColor(mToolbarWidgetColor);

        final TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setTextColor(mToolbarWidgetColor);
        toolbarTitle.setText(mToolbarTitle);

        // Color buttons inside the Toolbar
        Drawable stateButtonDrawable = ContextCompat.getDrawable(this, mToolbarCancelDrawable).mutate();
        stateButtonDrawable.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationIcon(stateButtonDrawable);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initiateRootViews() {
        mUCropView = findViewById(R.id.ucrop);
        mGestureCropImageView = mUCropView.getCropImageView();
        mOverlayView = mUCropView.getOverlayView();

        mGestureCropImageView.setTransformImageListener(mImageListener);

        ((ImageView) findViewById(R.id.image_view_logo)).setColorFilter(mLogoColor, PorterDuff.Mode.SRC_ATOP);

        findViewById(R.id.ucrop_frame).setBackgroundColor(mRootViewBackgroundColor);
        if (!mShowBottomControls) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.ucrop_frame).getLayoutParams();
            params.bottomMargin = 0;
            findViewById(R.id.ucrop_frame).requestLayout();
        }
    }

    private TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
        @Override
        public void onRotate(float currentAngle) {
            setAngleText(currentAngle);
        }

        @Override
        public void onScale(float currentScale) {
            setScaleText(currentScale);
        }

        @Override
        public void onLoadComplete() {
            mUCropView.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateInterpolator());
            mBlockingView.setClickable(false);
            mShowLoader = false;
            supportInvalidateOptionsMenu();
        }

        @Override
        public void onLoadFailure(@NonNull Exception e) {
            setResultError(e);
            finish();
        }

    };

    /**
     * Use {@link #mActiveControlsWidgetColor} for color filter
     */
    private void setupStatesWrapper() {
        ImageView stateScaleImageView = findViewById(R.id.image_view_state_scale);
        ImageView stateRotateImageView = findViewById(R.id.image_view_state_rotate);
        ImageView stateAspectRatioImageView = findViewById(R.id.image_view_state_aspect_ratio);
        ImageView stateAddTextImageView = findViewById(R.id.image_view_state_addtext);
        ImageView stateAddFilterImageView = findViewById(R.id.image_view_state_addfilters);


        stateScaleImageView.setImageDrawable(new SelectedStateListDrawable(stateScaleImageView.getDrawable(), mActiveControlsWidgetColor));
        stateRotateImageView.setImageDrawable(new SelectedStateListDrawable(stateRotateImageView.getDrawable(), mActiveControlsWidgetColor));
        stateAspectRatioImageView.setImageDrawable(new SelectedStateListDrawable(stateAspectRatioImageView.getDrawable(), mActiveControlsWidgetColor));
        stateAddTextImageView.setImageDrawable(new SelectedStateListDrawable(stateAddTextImageView.getDrawable(), mActiveControlsWidgetColor));
        stateAddFilterImageView.setImageDrawable(new SelectedStateListDrawable(stateAddFilterImageView.getDrawable(), mActiveControlsWidgetColor));
    }


    /**
     * Sets status-bar color for L devices.
     *
     * @param color - status-bar color
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
            }
        }
    }

    private void setupAspectRatioWidget(@NonNull Intent intent) {

        int aspectRationSelectedByDefault = intent.getIntExtra(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0);
        ArrayList<AspectRatio> aspectRatioList = intent.getParcelableArrayListExtra(UCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS);

        if (aspectRatioList == null || aspectRatioList.isEmpty()) {
            aspectRationSelectedByDefault = 2;

            aspectRatioList = new ArrayList<>();
            aspectRatioList.add(new AspectRatio(null, 1, 1));
            aspectRatioList.add(new AspectRatio(null, 3, 4));
            aspectRatioList.add(new AspectRatio(getString(R.string.ucrop_label_original).toUpperCase(),
                    CropImageView.SOURCE_IMAGE_ASPECT_RATIO, CropImageView.SOURCE_IMAGE_ASPECT_RATIO));
            aspectRatioList.add(new AspectRatio(null, 3, 2));
            aspectRatioList.add(new AspectRatio(null, 16, 9));
        }

        LinearLayout wrapperAspectRatioList = findViewById(R.id.layout_aspect_ratio);

        FrameLayout wrapperAspectRatio;
        AspectRatioTextView aspectRatioTextView;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        for (AspectRatio aspectRatio : aspectRatioList) {
            wrapperAspectRatio = (FrameLayout) getLayoutInflater().inflate(R.layout.ucrop_aspect_ratio, null);
            wrapperAspectRatio.setLayoutParams(lp);
            aspectRatioTextView = ((AspectRatioTextView) wrapperAspectRatio.getChildAt(0));
            aspectRatioTextView.setActiveColor(mActiveControlsWidgetColor);
            aspectRatioTextView.setAspectRatio(aspectRatio);

            wrapperAspectRatioList.addView(wrapperAspectRatio);
            mCropAspectRatioViews.add(wrapperAspectRatio);
        }

        mCropAspectRatioViews.get(aspectRationSelectedByDefault).setSelected(true);

        for (ViewGroup cropAspectRatioView : mCropAspectRatioViews) {
            cropAspectRatioView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mGestureCropImageView.setTargetAspectRatio(
                            ((AspectRatioTextView) ((ViewGroup) v).getChildAt(0)).getAspectRatio(v.isSelected()));
                    mGestureCropImageView.setImageToWrapCropBounds();
                    if (!v.isSelected()) {
                        for (ViewGroup cropAspectRatioView : mCropAspectRatioViews) {
                            cropAspectRatioView.setSelected(cropAspectRatioView == v);
                        }
                    }
                }
            });
        }
    }

    private void setupRotateWidget() {
        mTextViewRotateAngle = findViewById(R.id.text_view_rotate);
        ((HorizontalProgressWheelView) findViewById(R.id.rotate_scroll_wheel))
                .setScrollingListener(new HorizontalProgressWheelView.ScrollingListener() {
                    @Override
                    public void onScroll(float delta, float totalDistance) {
                        mGestureCropImageView.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT);
                    }

                    @Override
                    public void onScrollEnd() {
                        mGestureCropImageView.setImageToWrapCropBounds();
                    }

                    @Override
                    public void onScrollStart() {
                        mGestureCropImageView.cancelAllAnimations();
                    }
                });

        ((HorizontalProgressWheelView) findViewById(R.id.rotate_scroll_wheel)).setMiddleLineColor(mActiveControlsWidgetColor);


        findViewById(R.id.wrapper_reset_rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRotation();
            }
        });
        findViewById(R.id.wrapper_rotate_by_angle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateByAngle(90);
            }
        });
        setAngleTextColor(mActiveControlsWidgetColor);
    }

    private void setupScaleWidget() {
        mTextViewScalePercent = findViewById(R.id.text_view_scale);
        ((HorizontalProgressWheelView) findViewById(R.id.scale_scroll_wheel))
                .setScrollingListener(new HorizontalProgressWheelView.ScrollingListener() {
                    @Override
                    public void onScroll(float delta, float totalDistance) {
                        if (delta > 0) {
                            mGestureCropImageView.zoomInImage(mGestureCropImageView.getCurrentScale()
                                    + delta * ((mGestureCropImageView.getMaxScale() - mGestureCropImageView.getMinScale()) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT));
                        } else {
                            mGestureCropImageView.zoomOutImage(mGestureCropImageView.getCurrentScale()
                                    + delta * ((mGestureCropImageView.getMaxScale() - mGestureCropImageView.getMinScale()) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT));
                        }
                    }

                    @Override
                    public void onScrollEnd() {
                        mGestureCropImageView.setImageToWrapCropBounds();
                    }

                    @Override
                    public void onScrollStart() {
                        mGestureCropImageView.cancelAllAnimations();
                    }
                });
        ((HorizontalProgressWheelView) findViewById(R.id.scale_scroll_wheel)).setMiddleLineColor(mActiveControlsWidgetColor);

        setScaleTextColor(mActiveControlsWidgetColor);
    }

    private void setAngleText(float angle) {
        if (mTextViewRotateAngle != null) {
            mTextViewRotateAngle.setText(String.format(Locale.getDefault(), "%.1f°", angle));
        }
    }

    private void setAngleTextColor(int textColor) {
        if (mTextViewRotateAngle != null) {
            mTextViewRotateAngle.setTextColor(textColor);
        }
    }

    private void setScaleText(float scale) {
        if (mTextViewScalePercent != null) {
            mTextViewScalePercent.setText(String.format(Locale.getDefault(), "%d%%", (int) (scale * 100)));
        }
    }

    private void setScaleTextColor(int textColor) {
        if (mTextViewScalePercent != null) {
            mTextViewScalePercent.setTextColor(textColor);
        }
    }

    private void resetRotation() {
        mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private void rotateByAngle(int angle) {
        mGestureCropImageView.postRotate(angle);
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private final View.OnClickListener mStateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!v.isSelected()) {
                setWidgetState(v.getId());
            }
        }
    };

    private void setInitialState() {
        if (mShowBottomControls) {
            if (mWrapperStateAspectRatio.getVisibility() == View.VISIBLE) {
                setWidgetState(R.id.state_aspect_ratio);
            } else {
                setWidgetState(R.id.state_scale);
            }
        } else {
            setAllowedGestures(0);
        }
    }

    private void setWidgetState(@IdRes int stateViewId) {
        if (!mShowBottomControls) return;

        mWrapperStateAspectRatio.setSelected(stateViewId == R.id.state_aspect_ratio);
        mWrapperStateRotate.setSelected(stateViewId == R.id.state_rotate);
        mWrapperStateScale.setSelected(stateViewId == R.id.state_scale);
        mWrapperStateAddText.setSelected(stateViewId == R.id.state_addtext);
        mWrapperStateAddFilter.setSelected(stateViewId == R.id.state_addfilters);

        mLayoutAspectRatio.setVisibility(stateViewId == R.id.state_aspect_ratio ? View.VISIBLE : View.GONE);
        mLayoutRotate.setVisibility(stateViewId == R.id.state_rotate ? View.VISIBLE : View.GONE);
        mLayoutScale.setVisibility(stateViewId == R.id.state_scale ? View.VISIBLE : View.GONE);
        mLayoutAddText.setVisibility(stateViewId == R.id.state_addtext ? View.VISIBLE : View.GONE);
        mLayoutAddFilter.setVisibility(stateViewId == R.id.state_addfilters? View.VISIBLE : View.GONE);

        changeSelectedTab(stateViewId);

        if (stateViewId == R.id.state_scale) {
            setAllowedGestures(0);
        } else if (stateViewId == R.id.state_rotate) {
            setAllowedGestures(1);
        }
        else if (stateViewId == R.id.state_addtext) {
            setAllowedGestures(3);
        }
        else if(stateViewId == R.id.state_addfilters)
        {
            setAllowedGestures(4);
        }
        else {
            setAllowedGestures(2);
        }
    }

    private void changeSelectedTab(int stateViewId) {
        TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.ucrop_photobox), mControlsTransition);

        mWrapperStateScale.findViewById(R.id.text_view_scale).setVisibility(stateViewId == R.id.state_scale ? View.VISIBLE : View.GONE);
        mWrapperStateAspectRatio.findViewById(R.id.text_view_crop).setVisibility(stateViewId == R.id.state_aspect_ratio ? View.VISIBLE : View.GONE);
        mWrapperStateRotate.findViewById(R.id.text_view_rotate).setVisibility(stateViewId == R.id.state_rotate ? View.VISIBLE : View.GONE);
        mWrapperStateAddText.findViewById(R.id.text_view_addtext).setVisibility(stateViewId == R.id.state_addtext ? View.VISIBLE : View.GONE);
        mWrapperStateAddFilter.findViewById(R.id.text_view_addfilters).setVisibility(stateViewId == R.id.state_addfilters ? View.VISIBLE : View.GONE);

    }

    private void setAllowedGestures(int tab) {
        tab=2;
        if(mAllowedGestures[tab] == NONETEXT)
        {
            mGestureCropImageView.setScaleEnabled(false);
            mGestureCropImageView.setRotateEnabled(false);
            final GestureDetector mGestureDetector=new GestureDetector(new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    return false;
                }
            });

            mGestureCropImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    mGestureDetector.onTouchEvent(event);

                    return true;
                }
            });
            final ScaleGestureDetector mscaleGestureDetector=new ScaleGestureDetector(UCropActivity.this, new TextScaleListener());
            //final RotationGestureDetector mrotationGestureDetector =new RotationGestureDetector( new TextRotationListener());

            mGestureCropImageView.setOnTouchListener(new View.OnTouchListener() {
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




        }
        else if(mAllowedGestures[tab] == NONEFILTER)
        {
            Bitmap originalbitmap=BitmapFactory.decodeFile((new File(getCacheDir(), "SampleCropImage.jpg")).getAbsolutePath());
            originalbitmap=originalbitmap.copy(originalbitmap.getConfig(), true);



            Button none,grey, invert, sketch, highlight;

            none=findViewById(R.id.none);
            grey=findViewById(R.id.grey);
            invert=findViewById(R.id.invert);
            sketch=findViewById(R.id.sketch);
          highlight=findViewById(R.id.highlight);

            final Bitmap finalOriginalbitmap = originalbitmap;
            none.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Bitmap tempbitmap= finalOriginalbitmap.copy(finalOriginalbitmap.getConfig(), true);
                    File file = new File(getCacheDir(), "SampleCropImage.jpg");
                    OutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(file);

                        tempbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
                        fOut.flush();
                        fOut.close(); // do not forget to close the stream
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    Uri outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    try {
                        mGestureCropImageView.setImageUri(inputUri, outputUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }



                }
            });

            highlight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {



                    Bitmap tempbitmap= finalOriginalbitmap.copy(finalOriginalbitmap.getConfig(), true);

                    tempbitmap=(new ImageFilters()).applyHighlightEffect(tempbitmap);

                    File file = new File(getCacheDir(), "SampleCropImage.jpg");
                    OutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(file);

                        tempbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
                        fOut.flush();
                        fOut.close(); // do not forget to close the stream
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    Uri outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    try {
                        mGestureCropImageView.setImageUri(inputUri, outputUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            grey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {



                    Bitmap tempbitmap= finalOriginalbitmap.copy(finalOriginalbitmap.getConfig(), true);





                    if(tempbitmap!=null) {
                        int imageHeight = tempbitmap.getHeight();
                        int imageWidth = tempbitmap.getWidth();
                        for (int i = 0; i < imageWidth; i++) {

                            for (int j = 0; j < imageHeight; j++) {

                                // getting each pixel
                                int oldPixel = tempbitmap.getPixel(i, j);

                                // each pixel is made from RED_BLUE_GREEN_ALPHA
                                // so, getting current values of pixel
                                int oldRed = Color.red(oldPixel);
                                int oldBlue = Color.blue(oldPixel);
                                int oldGreen = Color.green(oldPixel);
                                int oldAlpha = Color.alpha(oldPixel);


                                // write your Algorithm for getting new values
                                // after calculation of filter


                                // applying new pixel values from above to newBitmap
                                int intensity = (oldRed + oldBlue + oldGreen) / 3;
                                int newRed = intensity;
                                int newBlue = intensity;
                                int newGreen = intensity;
                                int newPixel = Color.argb(oldAlpha, newRed, newGreen, newBlue);
                                tempbitmap.setPixel(i, j, newPixel);
                            }
                        }

                    }


                    File file = new File(getCacheDir(), "SampleCropImage.jpg");
                    OutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(file);

                        tempbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
                        fOut.flush();
                        fOut.close(); // do not forget to close the stream
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    Uri outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    try {
                        mGestureCropImageView.setImageUri(inputUri, outputUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            invert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {




                    Bitmap tempbitmap= finalOriginalbitmap.copy(finalOriginalbitmap.getConfig(), true);
                   tempbitmap=(new ImageFilters()).applyInvertEffect(tempbitmap);
                    File file = new File(getCacheDir(), "SampleCropImage.jpg");
                    OutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(file);

                        tempbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
                        fOut.flush();
                        fOut.close(); // do not forget to close the stream
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    Uri outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    try {
                        mGestureCropImageView.setImageUri(inputUri, outputUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            sketch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Bitmap tempbitmap= finalOriginalbitmap.copy(finalOriginalbitmap.getConfig(), true);


                    if(tempbitmap!=null) {
                        int imageHeight = tempbitmap.getHeight();
                        int imageWidth = tempbitmap.getWidth();
                        for (int i = 0; i < imageWidth; i++) {

                            for (int j = 0; j < imageHeight; j++) {

                                // getting each pixel
                                int oldPixel = tempbitmap.getPixel(i, j);

                                // each pixel is made from RED_BLUE_GREEN_ALPHA
                                // so, getting current values of pixel
                                int oldRed = Color.red(oldPixel);
                                int oldBlue = Color.blue(oldPixel);
                                int oldGreen = Color.green(oldPixel);
                                int oldAlpha = Color.alpha(oldPixel);


                                // write your Algorithm for getting new values
                                // after calculation of filter


                                // applying new pixel values from above to newBitmap
                                int intensity = (oldRed + oldBlue + oldGreen) / 3;

                                // applying new pixel value to newBitmap
                                // condition for Sketch
                                int newPixel = 0;
                                int INTENSITY_FACTOR = 120;

                                if (intensity > INTENSITY_FACTOR) {
                                    // apply white color
                                    newPixel = Color.argb(oldAlpha, 255, 255, 255);

                                } else if (intensity > 100) {
                                    // apply grey color
                                    newPixel = Color.argb(oldAlpha, 150, 150, 150);
                                } else {
                                    // apply black color
                                    newPixel = Color.argb(oldAlpha, 0, 0, 0);
                                }
                                tempbitmap.setPixel(i, j, newPixel);
                            }
                        }

                    }
                    File file = new File(getCacheDir(), "SampleCropImage.jpg");
                    OutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(file);

                        tempbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
                        fOut.flush();
                        fOut.close(); // do not forget to close the stream
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    Uri outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
                    try {
                        mGestureCropImageView.setImageUri(inputUri, outputUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });









        }
        else {

            mGestureCropImageView.setScaleEnabled(mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == SCALE);
            mGestureCropImageView.setRotateEnabled(mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == ROTATE);
            //mGestureCropImageView.setOnTouchListener();
            if(text!=null) {
                text.setText("");
                text.setVisibility(View.GONE);
            }
            if(edittext!=null) {
                edittext.setText("");
                edittext.setVisibility(View.GONE);
            }
            mGestureCropImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {

                   GestureCropImageView. mGestureDetector.onTouchEvent(event);

                    if ( GestureCropImageView. mIsScaleEnabled) {
                        GestureCropImageView. mScaleDetector.onTouchEvent(event);
                    }

                    if ( GestureCropImageView. mIsRotateEnabled) {
                        GestureCropImageView. mRotateDetector.onTouchEvent(event);
                    }
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                        mGestureCropImageView.setImageToWrapCropBounds();
                    }
                    return true;
                }
            });


        }
    }

    /**
     * Adds view that covers everything below the Toolbar.
     * When it's clickable - user won't be able to click/touch anything below the Toolbar.
     * Need to block user input while loading and cropping an image.
     */
    private void addBlockingView() {
        if (mBlockingView == null) {
            mBlockingView = new View(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.BELOW, R.id.toolbar);
            mBlockingView.setLayoutParams(lp);
            mBlockingView.setClickable(true);
        }

        ((RelativeLayout) findViewById(R.id.ucrop_photobox)).addView(mBlockingView);
    }

    protected void cropAndSaveImage() {
        mBlockingView.setClickable(true);
        mShowLoader = true;
        supportInvalidateOptionsMenu();

        mGestureCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, new BitmapCropCallback() {

            @Override
            public void onBitmapCropped(@NonNull Uri resultUri, int offsetX, int offsetY, int imageWidth, int imageHeight) {
                setResultUri(resultUri, mGestureCropImageView.getTargetAspectRatio(), offsetX, offsetY, imageWidth, imageHeight);
                finish();
            }

            @Override
            public void onCropFailure(@NonNull Throwable t) {
                setResultError(t);
                finish();
            }
        });
    }

    protected void setResultUri(Uri uri, float resultAspectRatio, int offsetX, int offsetY, int imageWidth, int imageHeight) {
        setResult(RESULT_OK, new Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY)
        );
    }

    protected void setResultError(Throwable throwable) {
        setResult(UCrop.RESULT_ERROR, new Intent().putExtra(UCrop.EXTRA_ERROR, throwable));
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

    private class TextRotationListener extends RotationGestureDetector.SimpleOnRotationGestureListener {

        @Override
        public boolean onRotation(RotationGestureDetector rotationDetector) {
            float angle=rotationDetector.getAngle();
            float prevangle =text.getRotation();

            text.setRotation(prevangle-angle);
            return true;
        }

    }

    public static float calculateAngleBetweenLines(float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY) {
        float angle1 = (float) Math.atan2( (fY - sY), (fX - sX) );
        float angle2 = (float) Math.atan2( (nfY - nsY), (nfX - nsX) );

        float angle = ((float)Math.toDegrees(angle1 - angle2)) % 360;
        if (angle < -180.f) angle += 360.0f;
        if (angle > 180.f) angle -= 360.0f;
        return angle;
    }
    public static float angleBetweenLines (float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY)
    {
        float angle1 = (float) Math.atan2( (fY - sY), (fX - sX) );
        float angle2 = (float) Math.atan2( (nfY - nsY), (nfX - nsX) );

        float angle = ((float)Math.toDegrees(angle1 - angle2)) % 360;
        if (angle < -180.f) angle += 360.0f;
        if (angle > 180.f) angle -= 360.0f;
        return angle;
    }

}

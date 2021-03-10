package com.yalantis.ucrop.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.callback.BitmapLoadCallback;
import com.yalantis.ucrop.model.ExifInfo;
import com.yalantis.ucrop.util.BitmapLoadUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

/**
 * Creates and returns a Bitmap for a given Uri(String url).
 * inSampleSize is calculated based on requiredWidth property. However can be adjusted if OOM occurs.
 * If any EXIF config is found - bitmap is transformed properly.
 */
public class BitmapLoadTask extends AsyncTask<Void, Void, BitmapLoadTask.BitmapWorkerResult> {

    private static final String TAG = "BitmapWorkerTask";

    private static final int MAX_BITMAP_SIZE = 100 * 1024 * 1024;   // 100 MB

    private final Context mContext;
    private Uri mInputUri;
    private Uri mOutputUri;
    private final int mRequiredWidth;
    private final int mRequiredHeight;

    private final BitmapLoadCallback mBitmapLoadCallback;

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
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


    public static class BitmapWorkerResult {

        Bitmap mBitmapResult;
        ExifInfo mExifInfo;
        Exception mBitmapWorkerException;



        public BitmapWorkerResult(@NonNull Bitmap bitmapResult, @NonNull ExifInfo exifInfo) {
            mBitmapResult = bitmapResult;
            mExifInfo = exifInfo;
        }

        public BitmapWorkerResult(@NonNull Exception bitmapWorkerException) {
            mBitmapWorkerException = bitmapWorkerException;
        }

    }

    public BitmapLoadTask(@NonNull Context context,
                          @NonNull Uri inputUri, @Nullable Uri outputUri,
                          int requiredWidth, int requiredHeight,
                          BitmapLoadCallback loadCallback) {
        mContext = context;
        mInputUri = inputUri;
        mOutputUri = outputUri;
        mRequiredWidth = requiredWidth;
        mRequiredHeight = requiredHeight;
        mBitmapLoadCallback = loadCallback;
    }

    @Override
    @NonNull
    protected BitmapWorkerResult doInBackground(Void... params) {
        if (mInputUri == null) {
            return new BitmapWorkerResult(new NullPointerException("Input Uri cannot be null"));
        }

        try {
            processInputUri();
        } catch (NullPointerException | IOException e) {
            return new BitmapWorkerResult(e);
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = BitmapLoadUtils.calculateInSampleSize(options, mRequiredWidth, mRequiredHeight);
        options.inJustDecodeBounds = false;

        Bitmap decodeSampledBitmap = null;

        boolean decodeAttemptSuccess = false;
        while (!decodeAttemptSuccess) {
            try {
                InputStream stream = mContext.getContentResolver().openInputStream(mInputUri);
                try {
                    decodeSampledBitmap = BitmapFactory.decodeStream(stream, null, options);
                    if (options.outWidth == -1 || options.outHeight == -1) {
                        return new BitmapWorkerResult(new IllegalArgumentException("Bounds for bitmap could not be retrieved from the Uri: [" + mInputUri + "]"));
                    }
                } finally {
                    BitmapLoadUtils.close(stream);
                }
                if (checkSize(decodeSampledBitmap, options)) continue;
                decodeAttemptSuccess = true;
            } catch (OutOfMemoryError error) {
                Log.e(TAG, "doInBackground: BitmapFactory.decodeFileDescriptor: ", error);
                options.inSampleSize *= 2;
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: ImageDecoder.createSource: ", e);
                return new BitmapWorkerResult(new IllegalArgumentException("Bitmap could not be decoded from the Uri: [" + mInputUri + "]", e));
            }
        }


        Long time=System.currentTimeMillis();
        Log.d("ishat0", time.toString());



        if(decodeSampledBitmap!=null)
        {



            int rwidth;
            int rheight;
            int y=decodeSampledBitmap.getWidth();
            int x=decodeSampledBitmap.getHeight();
            float aspectrxy=(float)x/y;
            float aspectryx=(float)y/x;


            int mrh=1600, mrw=900;

            if(x<=mrh && y>mrw)
            {
                rwidth=mrw;
                rheight= (int) (aspectrxy*rwidth);
            }
            else if(x<=mrh && y<=mrw)
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
                if(mrw<=mrh)
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
            Bitmap reqbitmap = Bitmap.createBitmap(mrw, mrh, finalbitmap.getConfig());

            // Instantiate a canvas and prepare it to paint to the new bitmap
            Canvas canvas = new Canvas(reqbitmap);

            // Paint it white (or whatever color you want)
            canvas.drawColor(Color.RED);

            // Draw the old bitmap ontop of the new white one
            //int minw = Math.min(mRequiredWidth, decodeSampledBitmap.getWidth()), minh = Math.min(mRequiredHeight, decodeSampledBitmap.getHeight());

            int spcw=(mrw-rwidth+1)/2;
            int spch=(mrh-rheight+1)/2;
            canvas.drawBitmap(finalbitmap, spcw, spch, null);

            /*
            int minw = Math.min(mRequiredWidth, decodeSampledBitmap.getWidth()), minh = Math.min(mRequiredHeight, decodeSampledBitmap.getHeight());

            int spcw=(mRequiredWidth-minw+1)/2;
            int spch=(mRequiredHeight-minh+1)/2;

            Bitmap reqbitmap= Bitmap.createBitmap(mRequiredWidth, mRequiredHeight, Bitmap.Config.ARGB_8888);


            int numPixels = finalbitmap.getWidth()* finalbitmap.getHeight();
            int[] pixels = new int[numPixels];

            finalbitmap.getPixels(pixels, 0, finalbitmap.getWidth(), 0, 0, finalbitmap.getWidth(),finalbitmap.getHeight());

            reqbitmap.setPixels(pixels, 0,finalbitmap.getWidth(), spcw, spch, finalbitmap.getWidth(), finalbitmap.getHeight());
            for(int i=0;i<mRequiredWidth;i++)
            {
                for(int j=0;j<mRequiredHeight;j++)
                {
                    if(reqbitmap.getPixel(i, j)==0)

                    {reqbitmap.setPixel(i, j, Color.RED);

                    }


                }
            }



            decodeSampledBitmap=reqbitmap;

             */

            decodeSampledBitmap=reqbitmap;

            File file = new File(mInputUri.getPath());
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





        }

         time=System.currentTimeMillis();
        Log.d("ishat1", time.toString());





        if (decodeSampledBitmap == null) {
            return new BitmapWorkerResult(new IllegalArgumentException("Bitmap could not be decoded from the Uri: [" + mInputUri + "]"));
        }

        int exifOrientation = BitmapLoadUtils.getExifOrientation(mContext, mInputUri);
        int exifDegrees = BitmapLoadUtils.exifToDegrees(exifOrientation);
        int exifTranslation = BitmapLoadUtils.exifToTranslation(exifOrientation);

        ExifInfo exifInfo = new ExifInfo(exifOrientation, exifDegrees, exifTranslation);

        Matrix matrix = new Matrix();
        if (exifDegrees != 0) {
            matrix.preRotate(exifDegrees);
        }
        if (exifTranslation != 1) {
            matrix.postScale(exifTranslation, 1);
        }
        if (!matrix.isIdentity()) {
            return new BitmapWorkerResult(BitmapLoadUtils.transformBitmap(decodeSampledBitmap, matrix), exifInfo);
        }

        return new BitmapWorkerResult(decodeSampledBitmap, exifInfo);
    }

    private void processInputUri() throws NullPointerException, IOException {
        String inputUriScheme = mInputUri.getScheme();
        Log.d(TAG, "Uri scheme: " + inputUriScheme);
        if ("http".equals(inputUriScheme) || "https".equals(inputUriScheme)) {
            try {
                downloadFile(mInputUri, mOutputUri);
            } catch (NullPointerException | IOException e) {
                Log.e(TAG, "Downloading failed", e);
                throw e;
            }
        } else if ("content".equals(inputUriScheme)) {
            try {
                copyFile(mInputUri, mOutputUri);
            } catch (NullPointerException | IOException e) {
                Log.e(TAG, "Copying failed", e);
                throw e;
            }
        } else if (!"file".equals(inputUriScheme)) {
            Log.e(TAG, "Invalid Uri scheme " + inputUriScheme);
            throw new IllegalArgumentException("Invalid Uri scheme" + inputUriScheme);
        }
    }

    private void copyFile(@NonNull Uri inputUri, @Nullable Uri outputUri) throws NullPointerException, IOException {
        Log.d(TAG, "copyFile");

        if (outputUri == null) {
            throw new NullPointerException("Output Uri is null - cannot copy image");
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = mContext.getContentResolver().openInputStream(inputUri);
            outputStream = new FileOutputStream(new File(outputUri.getPath()));
            if (inputStream == null) {
                throw new NullPointerException("InputStream for given input Uri is null");
            }

            byte buffer[] = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            BitmapLoadUtils.close(outputStream);
            BitmapLoadUtils.close(inputStream);

            // swap uris, because input image was copied to the output destination
            // (cropped image will override it later)
            mInputUri = mOutputUri;
        }
    }

    private void downloadFile(@NonNull Uri inputUri, @Nullable Uri outputUri) throws NullPointerException, IOException {
        Log.d(TAG, "downloadFile");

        if (outputUri == null) {
            throw new NullPointerException("Output Uri is null - cannot download image");
        }

        OkHttpClient client = new OkHttpClient();

        BufferedSource source = null;
        Sink sink = null;
        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url(inputUri.toString())
                    .build();
            response = client.newCall(request).execute();
            source = response.body().source();

            OutputStream outputStream = mContext.getContentResolver().openOutputStream(outputUri);
            if (outputStream != null) {
                sink = Okio.sink(outputStream);
                source.readAll(sink);
            } else {
                throw new NullPointerException("OutputStream for given output Uri is null");
            }
        } finally {
            BitmapLoadUtils.close(source);
            BitmapLoadUtils.close(sink);
            if (response != null) {
                BitmapLoadUtils.close(response.body());
            }
            client.dispatcher().cancelAll();

            // swap uris, because input image was downloaded to the output destination
            // (cropped image will override it later)
            mInputUri = mOutputUri;
        }
    }

    @Override
    protected void onPostExecute(@NonNull BitmapWorkerResult result) {
        if (result.mBitmapWorkerException == null) {
            mBitmapLoadCallback.onBitmapLoaded(result.mBitmapResult, result.mExifInfo, mInputUri.getPath(), (mOutputUri == null) ? null : mOutputUri.getPath());
        } else {
            mBitmapLoadCallback.onFailure(result.mBitmapWorkerException);
        }
    }

    private boolean checkSize(Bitmap bitmap, BitmapFactory.Options options) {
        int bitmapSize = bitmap != null ? bitmap.getByteCount() : 0;
        if (bitmapSize > MAX_BITMAP_SIZE) {
            options.inSampleSize *= 2;
            return true;
        }
        return false;
    }
}
package com.tigerbase.spotifystreamer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;

public class ImageLoaderTask extends AsyncTask<String, Void, Bitmap>
{
    private final String LOG_TAG = ImageLoaderTask.class.getSimpleName();
    private ImageView _imageView;

    public ImageLoaderTask(ImageView imageView)
    {
        _imageView = imageView;
        imageView.setImageResource(android.R.color.transparent);
    }

    protected Bitmap doInBackground(String... params) {
        if (params.length != 1)
        {
            Log.e(LOG_TAG, "Invalid parameters passed to ImageLoaderTask.doInBackground");
            return null;
        }

        String url = params[0];
        Bitmap image = null;

        try
        {
            InputStream inStream = new java.net.URL(url).openStream();
            image = BitmapFactory.decodeStream(inStream);
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "An error occurred loading the image '" + url + "': " + ex.getMessage());
        }

        return image;
    }

    @Override
    protected void onPostExecute(Bitmap image)
    {
        if (image != null)
        {
            _imageView.setImageBitmap(image);
        }
    }
}

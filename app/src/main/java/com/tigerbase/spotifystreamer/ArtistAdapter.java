package com.tigerbase.spotifystreamer;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class ArtistAdapter extends ArrayAdapter<Artist>
{
    private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    public ArtistAdapter(Context context, int textViewResourceId)
    {
        super(context, textViewResourceId);
    }

    public ArtistAdapter(Context context, int resource, List<Artist> artists)
    {
        super(context, resource, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;

        if (view == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.artist_list_item, parent, false);
        }

        Artist artist = getItem(position);

        if (artist != null)
        {
            ImageView thumbnailImageView = (ImageView) view.findViewById(R.id.artist_list_item_thumbnail);
            TextView nameTextView = (TextView) view.findViewById(R.id.artist_list_item_name);

            if (thumbnailImageView != null && !artist.images.isEmpty())
            {
                // Find the image with the smallest width but not less than 200
                Image thumbnailImage = getThumbnailImage(artist);

                // Load the image from its Url into the ImageView
                ImageLoaderTask task = new ImageLoaderTask(thumbnailImageView);
                task.execute(thumbnailImage.url);
            }

            if (nameTextView != null)
            {
                nameTextView.setText(artist.name);
            }
        }

        return view;
    }

    private Image getThumbnailImage(Artist artist) {
        Image thumbnailImage = null;
        for (Image image : artist.images)
        {
            //Log.v(LOG_TAG, "Artist: '" + artist.name + "': " + Integer.toString(image.width));
            if (thumbnailImage == null || image.width >= 200 && image.width < thumbnailImage.width)
            {
                thumbnailImage = image;
                //Log.v(LOG_TAG, "Artist: '" + artist.name + "': " + Integer.toString(image.width) + " - Selected");
            }
        }
        return thumbnailImage;
    }
}

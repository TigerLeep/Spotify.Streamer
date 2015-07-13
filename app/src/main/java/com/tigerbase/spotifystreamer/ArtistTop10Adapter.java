package com.tigerbase.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class ArtistTop10Adapter extends ArrayAdapter<Track>
{
    private final String LOG_TAG = ArtistTop10Adapter.class.getSimpleName();

    public ArtistTop10Adapter(Context context, int textViewResourceId)
    {
        super(context, textViewResourceId);
    }

    public ArtistTop10Adapter(Context context, int resource, List<Track> artists)
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
            view = inflater.inflate(R.layout.artist_top10_list_item, parent, false);
        }

        Track track = getItem(position);

        if (track != null)
        {
            ImageView thumbnailImageView = (ImageView) view.findViewById(R.id.artist_top10_list_item_thumbnail);
            TextView albumTextView = (TextView) view.findViewById(R.id.artist_top10_list_item_album);
            TextView trackTextView = (TextView) view.findViewById(R.id.artist_top10_list_item_track);

            if (thumbnailImageView != null && track.album != null && !track.album.images.isEmpty())
            {
                // Find the image with the smallest width but not less than 200
                Image thumbnailImage = getThumbnailImage(track.album);

                // Load the image from its Url into the ImageView
                Picasso.with(getContext()).load(thumbnailImage.url).into(thumbnailImageView);
            }

            if (albumTextView != null && track.album != null)
            {
                albumTextView.setText(track.album.name);
            }
            if (trackTextView != null)
            {
                trackTextView.setText(track.name);
            }
        }

        return view;
    }

    private Image getThumbnailImage(AlbumSimple album)
    {
        Image thumbnailImage = null;
        for (Image image : album.images)
        {
            //Log.v(LOG_TAG, "Album: '" + album.name + "': " + Integer.toString(image.width));
            if (thumbnailImage == null || image.width >= 640 && image.width < thumbnailImage.width)
            {
                thumbnailImage = image;
                //Log.v(LOG_TAG, "Album: '" + album.name + "': " + Integer.toString(image.width) + " - Selected");
            }
        }
        return thumbnailImage;
    }
}

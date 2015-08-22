package com.tigerbase.spotifystreamer.ArtistTop10;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tigerbase.spotifystreamer.R;
import com.tigerbase.spotifystreamer.TrackParcelable;

import java.util.ArrayList;

public class ArtistTop10Adapter extends ArrayAdapter<TrackParcelable>
{
    private final static String LOG_TAG = ArtistTop10Adapter.class.getSimpleName();

    public ArtistTop10Adapter(Context context, int textViewResourceId)
    {
        super(context, textViewResourceId);
    }

    public ArtistTop10Adapter(Context context, int resource, ArrayList<TrackParcelable> artists)
    {
        super(context, resource, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.v(LOG_TAG, "getView");
        View view = convertView;

        if (view == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.list_item_artist_top10, parent, false);
        }

        TrackParcelable track = getItem(position);

        if (track != null)
        {
            ImageView thumbnailImageView = (ImageView) view.findViewById(R.id.artist_top10_list_item_thumbnail);
            TextView trackTextView = (TextView) view.findViewById(R.id.artist_top10_list_item_track);
            TextView albumTextView = (TextView) view.findViewById(R.id.artist_top10_list_item_album);

            if (thumbnailImageView != null)
            {
                // Load the image from its Url into the ImageView
                if (track.ThumbnailImageUrl != null && !track.ThumbnailImageUrl.isEmpty())
                {
                    Picasso.with(getContext())
                            .load(track.ThumbnailImageUrl)
                            .into(thumbnailImageView);
                }
                else
                {
                    thumbnailImageView.setImageBitmap(null);
                }
            }

            if (albumTextView != null)
            {
                albumTextView.setText(track.AlbumName);
            }
            if (trackTextView != null)
            {
                trackTextView.setText(track.Name);
            }
        }

        return view;
    }
}
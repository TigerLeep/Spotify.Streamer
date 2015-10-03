package com.tigerbase.spotifystreamer.artisttop10;

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
import com.tigerbase.spotifystreamer.Track;

import java.util.ArrayList;

public class ArtistTop10Adapter extends ArrayAdapter<Track>
{
    private final static String LOG_TAG = ArtistTop10Adapter.class.getSimpleName();

    public ArtistTop10Adapter(Context context, ArrayList<Track> artists)
    {
        super(context, R.layout.list_item_artist_top10, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.v(LOG_TAG, "getView");

        View view = getListItemView(convertView, parent);
        Track track = getItem(position);

        if (track == null)
        {
            return view;
        }

        ImageView thumbnailImageView = (ImageView) view.findViewById(R.id.artist_top10_list_item_thumbnail);
        TextView trackTextView = (TextView) view.findViewById(R.id.artist_top10_list_item_track);
        TextView albumTextView = (TextView) view.findViewById(R.id.artist_top10_list_item_album);

        loadImageIntoImageView(thumbnailImageView, track.ThumbnailImageUrl);
        loadTextIntoTextView(albumTextView, track.AlbumName);
        loadTextIntoTextView(trackTextView, track.Name);

        return view;
    }

    private void loadTextIntoTextView(TextView albumTextView, String albumName)
    {
        Log.v(LOG_TAG, "loadTextIntoTextView");
        if (albumTextView != null)
        {
            albumTextView.setText(albumName);
        }
    }

    private void loadImageIntoImageView(ImageView thumbnailImageView, String imageUrl)
    {
        Log.v(LOG_TAG, "loadImageIntoImageView");
        if (thumbnailImageView != null)
        {
            if (imageUrl != null && !imageUrl.isEmpty())
            {
                Picasso.with(getContext())
                        .load(imageUrl)
                        .into(thumbnailImageView);
            }
            else
            {
                thumbnailImageView.setImageBitmap(null);
            }
        }
    }

    private View getListItemView(View convertView, ViewGroup parent)
    {
        Log.v(LOG_TAG, "getListItemView");
        View view = convertView;

        if (view == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.list_item_artist_top10, parent, false);
        }
        return view;
    }
}

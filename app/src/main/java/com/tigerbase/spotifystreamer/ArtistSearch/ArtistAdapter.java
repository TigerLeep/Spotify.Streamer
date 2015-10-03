package com.tigerbase.spotifystreamer.artistsearch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tigerbase.spotifystreamer.Artist;
import com.tigerbase.spotifystreamer.R;

import java.util.ArrayList;

public class ArtistAdapter extends ArrayAdapter<Artist>
{
    private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    public ArtistAdapter(Context context, ArrayList<Artist> artists)
    {
        super(context, R.layout.list_item_artist, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.v(LOG_TAG, "getView");

        View artistListItemView = getArtistListItemView(convertView, parent);
        Artist artist = getItem(position);
        loadArtistListItemView(artistListItemView, artist);

        return artistListItemView;
    }

    private View getArtistListItemView(View convertView, ViewGroup parent)
    {
        Log.v(LOG_TAG, "getArtistListItemView");
        return (convertView == null)
                ? getArtistListItemViewFromLayout(parent)
                : convertView;
    }

    private View getArtistListItemViewFromLayout(ViewGroup parent)
    {
        Log.v(LOG_TAG, "getArtistListItemViewFromLayout");
        View view;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.list_item_artist, parent, false);

        return view;
    }

    private void loadArtistListItemView(View artistListItemView, Artist artist)
    {
        Log.v(LOG_TAG, "loadArtistListItemView");

        if (artist == null)
        {
            return;
        }

        loadArtistImage(artistListItemView, artist);
        loadArtistName(artistListItemView, artist);
    }

    private void loadArtistImage(View artistListItemView, Artist artist)
    {
        Log.v(LOG_TAG, "loadArtistImage");

        ImageView thumbnailImageView = (ImageView) artistListItemView.findViewById(R.id.list_item_artist_thumbnail);
        if (thumbnailImageView != null)
        {
            loadImageIntoImageView(artist, thumbnailImageView);
        }
    }

    private void loadImageIntoImageView(Artist artist, ImageView thumbnailImageView)
    {
        Log.v(LOG_TAG, "loadImageIntoImageView");
        if (artist.ThumbnailImageUrl != null && !artist.ThumbnailImageUrl.isEmpty())
        {
            Picasso.with(getContext())
                    .load(artist.ThumbnailImageUrl)
                    .into(thumbnailImageView);
        }
        else
        {
            thumbnailImageView.setImageBitmap(null);
        }
    }

    private void loadArtistName(View artistListItemView, Artist artist)
    {
        Log.v(LOG_TAG, "loadArtistName");
        TextView nameTextView = (TextView) artistListItemView.findViewById(R.id.list_item_artist_name);
        if (nameTextView != null)
        {
            nameTextView.setText(artist.Name);
        }
    }

}

package com.tigerbase.spotifystreamer.ArtistSearch;

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

    public ArtistAdapter(Context context, int textViewResourceId)
    {
        super(context, textViewResourceId);
    }

    public ArtistAdapter(Context context, int resource, ArrayList<Artist> artists)
    {
        super(context, resource, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.v(LOG_TAG, "getView");

        View artistListItemView = (convertView == null)
                ? getArtistListItemViewFromLayout(parent)
                : convertView;

        Artist artist = getItem(position);
        loadArtistListItemView(artistListItemView, artist);

        return artistListItemView;
    }

    private View getArtistListItemViewFromLayout(ViewGroup parent)
    {
        View view;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.list_item_artist, parent, false);

        return view;
    }

    private void loadArtistListItemView(View artistListItemView, Artist artist)
    {
        if (artist != null)
        {
            ImageView thumbnailImageView = (ImageView) artistListItemView.findViewById(R.id.list_item_artist_thumbnail);
            TextView nameTextView = (TextView) artistListItemView.findViewById(R.id.list_item_artist_name);

            if (thumbnailImageView != null)
            {
                // Load the image from its Url into the ImageView
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

            if (nameTextView != null)
            {
                nameTextView.setText(artist.Name);
            }
        }
    }

}

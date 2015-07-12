package com.tigerbase.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ArtistAdapter extends ArrayAdapter<ArtistParcelable>
{
    private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    public ArtistAdapter(Context context, int textViewResourceId)
    {
        super(context, textViewResourceId);
    }

    public ArtistAdapter(Context context, int resource, ArrayList<ArtistParcelable> artists)
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

        ArtistParcelable artist = getItem(position);

        if (artist != null)
        {
            ImageView thumbnailImageView = (ImageView) view.findViewById(R.id.artist_list_item_thumbnail);
            TextView nameTextView = (TextView) view.findViewById(R.id.artist_list_item_name);

            if (thumbnailImageView != null)
            {
                // Load the image from its Url into the ImageView
                if (artist.ThumbnailImageUrl != null && !artist.ThumbnailImageUrl.isEmpty())
                {
                    Picasso.with(getContext()).load(artist.ThumbnailImageUrl).into(thumbnailImageView);
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

        return view;
    }
}

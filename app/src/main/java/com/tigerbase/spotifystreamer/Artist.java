package com.tigerbase.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import kaaes.spotify.webapi.android.models.Image;

public class Artist implements Parcelable
{
    private final static String LOG_TAG = Artist.class.getSimpleName();

    public String Id = "";
    public String Name = "";
    public String ThumbnailImageUrl = "";

    public Artist()
    {
    }

    public Artist(String id, String name, String thumbnailImageUrl)
    {
        Id = id;
        Name = name;
        ThumbnailImageUrl = thumbnailImageUrl;
    }

    public Artist(kaaes.spotify.webapi.android.models.Artist artist)
    {
        Id = artist.id;
        Name = artist.name;
        ThumbnailImageUrl = getThumbnailImageUrl(artist);
    }

    @Override
    public int describeContents()
    {
        Log.v(LOG_TAG, "describeContents");
        return 0;
    }

    public static final Parcelable.Creator<Artist> CREATOR =
            new Parcelable.Creator<Artist>()
            {
                public Artist createFromParcel(Parcel in)
                {
                    Log.v(LOG_TAG, "CREATOR.createFromParcel");
                    Artist artist = new Artist();
                    artist.Id = in.readString();
                    artist.Name = in.readString();
                    artist.ThumbnailImageUrl = in.readString();
                    return artist;
                }

                @Override
                public Artist[] newArray(int size)
                {
                    Log.v(LOG_TAG, "CREATOR.newArray");
                    return new Artist[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        Log.v(LOG_TAG, "writeToParcel");
        dest.writeString(Id);
        dest.writeString(Name);
        dest.writeString(ThumbnailImageUrl);
    }

    private String getThumbnailImageUrl(kaaes.spotify.webapi.android.models.Artist artist)
    {
        Log.v(LOG_TAG, "getThumbnailImageUrl");
        String thumbnailImageUrl = "";
        Image thumbnailImage = null;
        for (Image image : artist.images)
        {
            if (thumbnailImage == null || image.width >= 200 && image.width < thumbnailImage.width)
            {
                thumbnailImage = image;
            }
        }
        if (thumbnailImage != null && thumbnailImage.url != null)
        {
            thumbnailImageUrl = thumbnailImage.url;
        }
        return thumbnailImageUrl;
    }
}

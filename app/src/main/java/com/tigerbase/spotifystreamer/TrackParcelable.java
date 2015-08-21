package com.tigerbase.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TrackParcelable implements Parcelable
{
    private final static String LOG_TAG = TrackParcelable.class.getSimpleName();

    public String Id = "";
    public String Name = "";
    public String ThumbnailImageUrl = "";
    public String AlbumName = "";
    public String PreviewUrl = "";

    public TrackParcelable()
    {
    }

    public TrackParcelable(Track track)
    {
        PreviewUrl = track.preview_url;
        Id = track.id;
        Name = track.name;
        if (track.album != null)
        {
            ThumbnailImageUrl = getThumbnailImageUrl(track.album);
            AlbumName = track.album.name;
        }
    }

    @Override
    public int describeContents()
    {
        Log.v(LOG_TAG, "describeContents");
        return 0;
    }

    public static final Creator<TrackParcelable> CREATOR =
            new Creator<TrackParcelable>()
            {
                public TrackParcelable createFromParcel(Parcel in)
                {
                    Log.v(LOG_TAG, "CREATOR.createFromParcel");
                    TrackParcelable track = new TrackParcelable();
                    track.Id = in.readString();
                    track.Name = in.readString();
                    track.ThumbnailImageUrl = in.readString();
                    track.AlbumName = in.readString();
                    track.PreviewUrl = in.readString();
                    return track;
                }

                @Override
                public TrackParcelable[] newArray(int size)
                {
                    Log.v(LOG_TAG, "CREATOR.newArray");
                    return new TrackParcelable[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        Log.v(LOG_TAG, "writeToParcel");
        dest.writeString(Id);
        dest.writeString(Name);
        dest.writeString(ThumbnailImageUrl);
        dest.writeString(AlbumName);
        dest.writeString(PreviewUrl);
    }

    private String getThumbnailImageUrl(AlbumSimple album)
    {
        Log.v(LOG_TAG, "getThumbnailImageUrl");
        String thumbnailImageUrl = "";
        Image thumbnailImage = null;
        for (Image image : album.images)
        {
            if (thumbnailImage == null || image.width >= 640 && image.width < thumbnailImage.width)
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

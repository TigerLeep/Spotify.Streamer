package com.tigerbase.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TrackParcelable implements Parcelable
{
    public String Id = "";
    public String Name = "";
    public String ThumbnailImageUrl = "";
    public String AlbumName = "";

    public TrackParcelable()
    {
    }

    public TrackParcelable(String id, String name, String thumbnailImageUrl, String albumName)
    {
        Id = id;
        Name = name;
        ThumbnailImageUrl = thumbnailImageUrl;
        AlbumName = albumName;
    }

    public TrackParcelable(Track track)
    {
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
        return 0;
    }

    public static final Creator<TrackParcelable> CREATOR =
            new Creator<TrackParcelable>()
            {
                public TrackParcelable createFromParcel(Parcel in)
                {
                    TrackParcelable track = new TrackParcelable();
                    track.Id = in.readString();
                    track.Name = in.readString();
                    track.ThumbnailImageUrl = in.readString();
                    track.AlbumName = in.readString();
                    return track;
                }

                @Override
                public TrackParcelable[] newArray(int size)
                {
                    return new TrackParcelable[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(Id);
        dest.writeString(Name);
        dest.writeString(ThumbnailImageUrl);
        dest.writeString(AlbumName);
    }

    private String getThumbnailImageUrl(AlbumSimple album)
    {
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

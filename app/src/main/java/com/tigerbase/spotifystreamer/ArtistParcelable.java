package com.tigerbase.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leep on 7/12/2015.
 * I implemented Parcelable here with the assumption it was needed, but I'm sure it was.  I'm
 * retaining (and restoring) the Fragment and it seems to retain it's data on rotation just fine.
 * In the ArtistTop10Activity and ArtistTop10ActivityFragment I retain and restore the Fragment in
 * exactly the same way and the data in ArtistTop10ActivityFragment's list doesn't implement
 * Parcelable and it works just fine on orientation change.
 */
public class ArtistParcelable implements Parcelable
{
    public String Id = "";
    public String Name = "";
    public String ThumbnailImageUrl = "";

    public ArtistParcelable()
    {
    }

    public ArtistParcelable(String id, String name, String thumbnailImageUrl)
    {
        Id = id;
        Name = name;
        ThumbnailImageUrl = thumbnailImageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ArtistParcelable> CREATOR =
            new Parcelable.Creator<ArtistParcelable>()
            {
                public ArtistParcelable createFromParcel(Parcel in)
                {
                    ArtistParcelable artist = new ArtistParcelable();
                    artist.Id = in.readString();
                    artist.Name = in.readString();
                    artist.ThumbnailImageUrl = in.readString();
                    return artist;
                }

                @Override
                public ArtistParcelable[] newArray(int size)
                {
                    return new ArtistParcelable[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(Id);
        dest.writeString(Name);
        dest.writeString(ThumbnailImageUrl);
    }
}

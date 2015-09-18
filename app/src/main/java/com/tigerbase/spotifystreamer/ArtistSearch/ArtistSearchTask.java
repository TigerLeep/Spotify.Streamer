package com.tigerbase.spotifystreamer.ArtistSearch;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.tigerbase.spotifystreamer.Artist;
import com.tigerbase.spotifystreamer.R;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class ArtistSearchTask extends AsyncTask<String, Void, ArrayList<Artist>>
{
    private final static String LOG_TAG = ArtistSearchTask.class.getSimpleName();

    private ArrayList<Artist> _artists = null;
    private ArtistAdapter _adapter;
    private Context _context;

    public ArtistSearchTask(ArrayList<Artist> artists,
                            ArtistAdapter adapter,
                            Context context
                            )
    {
        _artists = artists;
        _adapter = adapter;
        _context = context;
    }

    @Override
    protected ArrayList<Artist> doInBackground(String... params)
    {
        Log.v(LOG_TAG, "doInBackground");

        if (!areParametersValid(params))
        {
            return null;
        }

        delayToPreventRapidFireApiCalls();
        if (isCancelled())
        {
            return null;
        }

        String artistPartialName = params[0];
        ArtistsPager artistsPager = CallSpotifySearchApi(artistPartialName);

        return getArtistParcelablesFromArtistsPager(artistsPager);
    }

    @Override
    protected void onPostExecute(ArrayList<Artist> artists)
    {
        Log.v(LOG_TAG, "onPostExecute");

        loadArtists(artists);
        showMessageIfNoArtistsFound(artists);
    }


    private boolean areParametersValid(String[] params)
    {
        if (params.length != 1)
        {
            Log.e(LOG_TAG, "Invalid parameters passed to ArtistSearchTask.doInBackground");
            return false;
        }
        if(params[0].length() == 0)
        {
            return false;
        }
        return true;
    }

    private void delayToPreventRapidFireApiCalls()
    {
        try
        {
            Thread.sleep(200);
        }
        catch(InterruptedException ex)
        {
            cancel(true);
        }
    }

    private ArtistsPager CallSpotifySearchApi(String artistPartialName)
    {
        artistPartialName = GetStartsWithSearchQuery(artistPartialName);

        // Call the spotify API to get a list of matching artists
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        ArtistsPager artistsPager = null;
        try
        {
            artistsPager = spotify.searchArtists(artistPartialName);
        }
        catch (RetrofitError ex)
        {
            Log.e(LOG_TAG, ex.getMessage());
        }
        return artistsPager;
    }

    private String GetStartsWithSearchQuery(String param)
    {
        //
        return param.replace(" ", "* ") + "*";
    }

    private ArrayList<Artist> getArtistParcelablesFromArtistsPager(ArtistsPager artistsPager)
    {
        ArrayList<Artist> artists = new ArrayList<>();
        if (artistsPager != null)
        {
            for(kaaes.spotify.webapi.android.models.Artist artist : artistsPager.artists.items)
            {
                artists.add(new Artist(artist));
            }
        }
        return artists;
    }

    private void loadArtists(ArrayList<Artist> artists)
    {
        _artists.clear();
        _adapter.clear();

        _artists.addAll(artists);
        _adapter.addAll(artists);
    }

    private void showMessageIfNoArtistsFound(ArrayList<Artist> artists)
    {
        if (artists == null || artists.isEmpty())
        {
            String message = _context.getString(R.string.no_artists_found);
            Toast.makeText(_context, message, Toast.LENGTH_LONG).show();
        }
    }
}

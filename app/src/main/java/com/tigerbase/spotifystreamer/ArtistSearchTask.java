package com.tigerbase.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class ArtistSearchTask extends AsyncTask<String, Void, ArrayList<ArtistParcelable>>
{
    private final static String LOG_TAG = ArtistSearchTask.class.getSimpleName();

    private ArrayList<ArtistParcelable> _artists = null;
    private ArtistAdapter _adapter;
    private Context _context;

    public ArtistSearchTask(ArrayList<ArtistParcelable> artists,
                            ArtistAdapter adapter,
                            Context context
                            )
    {
        _artists = artists;
        _adapter = adapter;
        _context = context;
    }

    protected ArrayList<ArtistParcelable> doInBackground(String... params)
    {
        Log.v(LOG_TAG, "doInBackground");
        if (params.length != 1)
        {
            Log.e(LOG_TAG, "Invalid parameters passed to ArtistSearchTask.doInBackground");
            return null;
        }
        if(params[0].length() == 0)
        {
            return null;
        }

        // Add an * to the end of each word to make it a "Starts With" wild card search.
        String artistPartialName = params[0].replace(" ", "* ") + "*";

        // Pause for a short time to ensure we don't rapid-fire API calls to Spotify.
        try
        {
            Thread.sleep(200);
        }
        catch(InterruptedException ex)
        {
            cancel(true);
        }

        if (isCancelled())
        {
            // This task was cancelled, possibly due to the user typing additional characters
            // in the search field which will cancel this task and start a new one.
            return null;
        }

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

        ArrayList<ArtistParcelable> artists = new ArrayList<>();
        if (artistsPager != null)
        {
            for(Artist artist : artistsPager.artists.items)
            {
                artists.add(new ArtistParcelable(artist));
            }
        }

        return artists;
    }

    @Override
    protected void onPostExecute(ArrayList<ArtistParcelable> artists)
    {
        Log.v(LOG_TAG, "onPostExecute");
        _artists.clear();
        _adapter.clear();

        _artists.addAll(artists);
        _adapter.addAll(artists);

        if (artists == null || artists.isEmpty())
        {
            String message = _context.getString(R.string.no_artists_found);
            Toast.makeText(_context, message, Toast.LENGTH_LONG).show();
        }
    }
}

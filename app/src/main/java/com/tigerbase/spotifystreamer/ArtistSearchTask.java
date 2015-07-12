package com.tigerbase.spotifystreamer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.protocol.HTTP;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ArtistSearchTask extends AsyncTask<String, Void, ArtistsPager>
{
    private final String LOG_TAG = ArtistSearchTask.class.getSimpleName();
    private ArtistAdapter _artistAdapter;

    public ArtistSearchTask(ArtistAdapter artistAdapter)
    {
        _artistAdapter = artistAdapter;
    }

    protected ArtistsPager doInBackground(String... params)
    {
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
        //try
        //{
        //    artistPartialName = URLEncoder.encode(artistPartialName, HTTP.UTF_8);
        //}
        //catch (UnsupportedEncodingException ex)
        //{
        //    // I'm pretty sure UTF_8 will always be a supported encoding and this catch
        //    // will never be hit.  But if it is, we'll just have the un-encoded string.
        //}
        Log.v(LOG_TAG, artistPartialName);

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

        // Return the list of artists
        return artistsPager;
    }

    @Override
    protected void onPostExecute(ArtistsPager artistsPager)
    {
        if (artistsPager != null
                && artistsPager.artists != null
                && artistsPager.artists.items != null
                && artistsPager.artists.total != 0)
        {
            _artistAdapter.addAll(artistsPager.artists.items);
        }
    }
}

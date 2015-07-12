package com.tigerbase.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Albums;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistTop10ActivityFragment extends Fragment
{
    private final String LOG_TAG = ArtistTop10Activity.class.getSimpleName();
    private ArtistTop10Adapter _adapter = null;
    private String _artistId = "";
    private String _artistName = "";

    public ArtistTop10ActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_artist_top10, container, false);
        _adapter = new ArtistTop10Adapter(
                getActivity(),
                R.layout.artist_top10_list_item,
                new ArrayList<Track>());

        ListView listView = (ListView)rootView.findViewById(R.id.artist_top10_list);
        listView.setAdapter(_adapter);

        if (intent != null)
        {
            Bundle extras = intent.getExtras();
            if(extras != null)
            {
                _artistId = extras.getString(getString(R.string.intent_extra_artist_id));
                _artistName = extras.getString(getString(R.string.intent_extra_artist_name));
                ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
                if (actionBar != null)
                {
                    actionBar.setSubtitle(_artistName);
                }
            }
            LoadAlbums(_artistId);
        }

        return rootView;
    }

    private void LoadAlbums(String artistId)
    {

        _adapter.clear();
        if(artistId.length() == 0)
        {
            return;
        }
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        Map<String, Object> options = new HashMap<>();
        options.put("country", "US");
        spotify.getArtistTopTrack(artistId, options, new Callback<Tracks>()
        {
            @Override
            public void success(Tracks tracks, Response response) {
                if (tracks != null
                        && tracks.tracks != null
                        && tracks.tracks.size() > 0)
                {
                    _adapter.addAll(tracks.tracks);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}

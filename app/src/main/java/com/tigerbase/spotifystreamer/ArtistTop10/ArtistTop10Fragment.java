package com.tigerbase.spotifystreamer.ArtistTop10;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tigerbase.spotifystreamer.Player.PlayerActivity;
import com.tigerbase.spotifystreamer.R;
import com.tigerbase.spotifystreamer.TrackParcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ArtistTop10Fragment extends Fragment
{
    private final static String LOG_TAG = ArtistTop10Fragment.class.getSimpleName();

    private final String ARTIST_ID_STATE_TAG = "ArtistId";
    private final String ARTIST_NAME_STATE_TAG = "ArtistName";
    private final String TRACKS_STATE_TAG = "Tracks";
    private final String LIST_VIEW_STATE_TAG = "ListView";

    private String _artistId = "";
    private String _artistName = "";
    private ArrayList<TrackParcelable> _tracks = null;
    private int _currentTrack = 0;
    private ArtistTop10Adapter _adapter = null;
    private Boolean _loadedFromState = false;
    private ListView _listView = null;
    private Parcelable _listState = null;

    public ArtistTop10Fragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_artist_top10, container, false);
        _adapter = new ArtistTop10Adapter(
                getActivity(),
                R.layout.list_item_artist_top10,
                new ArrayList<TrackParcelable>());

        _listView = (ListView)rootView.findViewById(R.id.artist_top10_list);
        _listView.setAdapter(_adapter);

        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                onTrackSelected(position);
            }
        });

        if (savedInstanceState != null)
        {
            _artistId = savedInstanceState.getString(ARTIST_ID_STATE_TAG);
            _artistName = savedInstanceState.getString(ARTIST_NAME_STATE_TAG);
            _tracks = savedInstanceState.getParcelableArrayList(TRACKS_STATE_TAG);
            _listState = savedInstanceState.getParcelable(LIST_VIEW_STATE_TAG);
            _loadedFromState = true;
        }
        else
        {
            Intent intent = getActivity().getIntent();
            if (intent != null)
            {
                Bundle extras = intent.getExtras();
                if(extras != null)
                {
                    _artistId = extras.getString(getString(R.string.intent_extra_artist_id));
                    _artistName = extras.getString(getString(R.string.intent_extra_artist_name));
                }
            }
            _tracks = new ArrayList<>();
            _loadedFromState = false;
        }

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setSubtitle(_artistName);
        }
        loadAlbums();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.v(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString(ARTIST_ID_STATE_TAG, _artistId);
        outState.putString(ARTIST_NAME_STATE_TAG, _artistName);
        outState.putParcelableArrayList(TRACKS_STATE_TAG, _tracks);
        outState.putParcelable(LIST_VIEW_STATE_TAG, _listView.onSaveInstanceState());
    }

    private void loadAlbums()
    {
        Log.v(LOG_TAG, "loadAlbums");
        if(_artistId.length() == 0)
        {
            return;
        }

        if(_loadedFromState)
        {
            _adapter.clear();
            _adapter.addAll(_tracks);
            _listView.onRestoreInstanceState(_listState);
            checkTracksForEmpty();
            _loadedFromState = false;
        }
        else
        {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String, Object> options = new HashMap<>();
            options.put("country", "US");
            spotify.getArtistTopTrack(_artistId, options, new Callback<Tracks>() {
                @Override
                public void success(Tracks tracks, Response response) {
                    _adapter.clear();
                    _tracks.clear();
                    if (tracks != null
                            && tracks.tracks != null
                            && tracks.tracks.size() > 0) {
                        for (Track track : tracks.tracks) {
                            _tracks.add(new TrackParcelable(track));
                        }
                        _adapter.addAll(_tracks);
                    }
                    checkTracksForEmpty();
                }

                @Override
                public void failure(RetrofitError ex) {
                    Log.e(LOG_TAG, ex.getMessage());
                }
            });
        }
    }

    private void checkTracksForEmpty()
    {
        Log.v(LOG_TAG, "checkTracksForEmpty");
        if (_tracks == null || _tracks.isEmpty())
        {
            String message = getActivity().getString(R.string.no_tracks_found);
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }

    public void onArtistChange(String id, String name)
    {
        Log.v(LOG_TAG, "onArtistChange");
        _artistId = id;
        _artistName = name;
        loadAlbums();
    }

    public void onTrackSelected(int position)
    {
        _currentTrack = position;
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        Bundle extras = new Bundle();
        extras.putParcelableArrayList(getString(R.string.bundle_tracks), _tracks);
        extras.putInt(getString(R.string.bundle_current_track), _currentTrack);
        intent.putExtras(extras);
        startActivity(intent);
    }

}

package com.tigerbase.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchActivityFragment extends Fragment
{
    private final String LOG_TAG = ArtistSearchActivity.class.getSimpleName();
    private ArtistAdapter _adapter = null;
    private ArtistSearchTask _artistSearchTask = null;

    public ArtistSearchActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);
        _adapter = new ArtistAdapter(
                getActivity(),
                R.layout.artist_list_item,
                new ArrayList<Artist>());

        ListView listView = (ListView)rootView.findViewById(R.id.artist_search_list);
        listView.setAdapter(_adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                Artist artist = _adapter.getItem(position);
                Intent intent = new Intent(getActivity(), ArtistTop10Activity.class);
                Bundle extras = new Bundle();
                extras.putString(getString(R.string.intent_extra_artist_id), artist.id);
                extras.putString(getString(R.string.intent_extra_artist_name), artist.name);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        EditText artistSearchName = (EditText) rootView.findViewById(R.id.artist_search_name);
        artistSearchName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                SearchArtists(s.toString());
            }
        });

        return rootView;
    }

    private void SearchArtists(String artistPartialName) {

        if(_artistSearchTask != null && _artistSearchTask.getStatus() != AsyncTask.Status.FINISHED)
        {
            _artistSearchTask.cancel(true);
        }
        _adapter.clear();
        if(artistPartialName.length() == 0)
        {
            return;
        }
        _artistSearchTask = new ArtistSearchTask(_adapter);
        _artistSearchTask.execute(artistPartialName);
    }
}

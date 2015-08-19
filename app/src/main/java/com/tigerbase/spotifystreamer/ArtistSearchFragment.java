package com.tigerbase.spotifystreamer;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ArtistSearchFragment extends Fragment
{
    private final static String LOG_TAG = ArtistSearchFragment.class.getSimpleName();

    private final String ARTISTS_STATE_TAG = "Artists";
    private final String PARTIAL_NAME_STATE_TAG = "PartialName";
    private final String LIST_VIEW_STATE_TAG = "ListView";
    private String _artistPartialName = "";
    private ArrayList<ArtistParcelable> _artists = null;
    private ArtistAdapter _adapter = null;
    private ArtistSearchTask _artistSearchTask = null;
    private Boolean _loadedFromState = false;
    private ListView _listView = null;
    private Parcelable _listState = null;

    public ArtistSearchFragment()
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
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);
        _adapter = new ArtistAdapter(
                getActivity(),
                R.layout.list_item_artist,
                new ArrayList<ArtistParcelable>());

        _listView = (ListView)rootView.findViewById(R.id.artist_search_list);
        _listView.setAdapter(_adapter);

        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                ArtistParcelable artist = _adapter.getItem(position);
                if (getActivity() instanceof IArtistList) {
                    Log.v(LOG_TAG, "instanceof IArtistList");
                    ((IArtistList) getActivity()).onArtistSelected(artist.Id, artist.Name);
                }
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
                Log.v(LOG_TAG, "afterTextChanged");
                SearchArtists(s.toString());
            }
        });

        if (savedInstanceState != null)
        {
            _artistPartialName = savedInstanceState.getString(PARTIAL_NAME_STATE_TAG);
            _artists = savedInstanceState.getParcelableArrayList(ARTISTS_STATE_TAG);
            _listState = savedInstanceState.getParcelable(LIST_VIEW_STATE_TAG);
            _loadedFromState = true;
        }
        else
        {
            _artistPartialName = "";
            _artists = new ArrayList<>();
            _loadedFromState = false;
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.v(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString(PARTIAL_NAME_STATE_TAG, _artistPartialName);
        outState.putParcelableArrayList(ARTISTS_STATE_TAG, _artists);
        outState.putParcelable(LIST_VIEW_STATE_TAG, _listView.onSaveInstanceState());
    }

    private void SearchArtists(String artistPartialName)
    {
        Log.v(LOG_TAG, "SearchArtists");
        if(_artistSearchTask != null && _artistSearchTask.getStatus() != AsyncTask.Status.FINISHED)
        {
            _artistSearchTask.cancel(true);
        }

        if(artistPartialName.length() == 0)
        {
            return;
        }
        if (_loadedFromState)
        {
            _adapter.clear();
            _adapter.addAll(_artists);
            _listView.onRestoreInstanceState(_listState);
            _loadedFromState = false;
        }
        else
        {
            _artistPartialName = artistPartialName;
            _artistSearchTask = new ArtistSearchTask(_artists, _adapter, getActivity());
            _artistSearchTask.execute(artistPartialName);
        }
    }
}

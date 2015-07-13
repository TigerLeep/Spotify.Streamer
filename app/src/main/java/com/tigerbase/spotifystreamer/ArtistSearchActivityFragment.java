package com.tigerbase.spotifystreamer;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchActivityFragment extends Fragment
{
    private final String LOG_TAG = ArtistSearchActivity.class.getSimpleName();
    private final String ARTISTS_STATE_TAG = "Artists";
    private final String PARTIAL_NAME_STATE_TAG = "PartialName";
    private String _artistPartialName = "";
    private ArrayList<ArtistParcelable> _artists = null;
    private ArtistAdapter _adapter = null;
    private ArtistSearchTask _artistSearchTask = null;

    public ArtistSearchActivityFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null)
        {
            _artistPartialName = savedInstanceState.getString(PARTIAL_NAME_STATE_TAG);
            _artists = savedInstanceState.getParcelableArrayList(ARTISTS_STATE_TAG);
        }
        else
        {
            _artistPartialName = "";
            _artists = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);
        _adapter = new ArtistAdapter(
                getActivity(),
                R.layout.artist_list_item,
                new ArrayList<ArtistParcelable>());

        ListView listView = (ListView)rootView.findViewById(R.id.artist_search_list);
        listView.setAdapter(_adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                ArtistParcelable artist = _adapter.getItem(position);
                Intent intent = new Intent(getActivity(), ArtistTop10Activity.class);
                Bundle extras = new Bundle();
                extras.putString(getString(R.string.intent_extra_artist_id), artist.Id);
                extras.putString(getString(R.string.intent_extra_artist_name), artist.Name);
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

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(PARTIAL_NAME_STATE_TAG, _artistPartialName);
        outState.putParcelableArrayList(ARTISTS_STATE_TAG, _artists);
    }

    private void SearchArtists(String artistPartialName)
    {
        if(_artistSearchTask != null && _artistSearchTask.getStatus() != AsyncTask.Status.FINISHED)
        {
            _artistSearchTask.cancel(true);
        }

        if(artistPartialName.length() == 0)
        {
            return;
        }
        if (artistPartialName.equals(_artistPartialName))
        {
            _adapter.clear();
            _adapter.addAll(_artists);
        }
        else
        {
            _artistPartialName = artistPartialName;
            _artistSearchTask = new ArtistSearchTask(_artists, _adapter);
            _artistSearchTask.execute(artistPartialName);
        }
    }
}

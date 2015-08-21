package com.tigerbase.spotifystreamer.ArtistSearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tigerbase.spotifystreamer.ArtistTop10.ArtistTop10Activity;
import com.tigerbase.spotifystreamer.ArtistTop10.ArtistTop10Fragment;
import com.tigerbase.spotifystreamer.IArtistList;
import com.tigerbase.spotifystreamer.R;

public class ArtistSearchActivity extends ActionBarActivity implements IArtistList
{
    private static final String LOG_TAG = ArtistSearchActivity.class.getSimpleName();
    private static final String TOP10FRAGMENT_TAG = "TOP10FRAGMENTTAG";

    private boolean _twoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        if (findViewById(R.id.artist_top10_container) != null)
        {
            _twoPane = true;
            Log.v(LOG_TAG, "onCreate - _twoPane = true");
            if (savedInstanceState == null)
            {
                Log.v(LOG_TAG, "onCreate - savedInstanceState == null");
                ArtistTop10Fragment top10Fragment = new ArtistTop10Fragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.artist_top10_container,
                                top10Fragment,
                                TOP10FRAGMENT_TAG)
                        .commit();
            }
        }
        else
        {
            _twoPane = false;
            Log.v(LOG_TAG, "onCreate - _twoPane = false");
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    protected void onStart()
    {
        Log.v(LOG_TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onPause()
    {
        Log.v(LOG_TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        Log.v(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        Log.v(LOG_TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.v(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.v(LOG_TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_artist_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.v(LOG_TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onArtistSelected(String id, String name) {
        Log.v(LOG_TAG, "onArtistSelected: id=[" + id + "], name=[" + name + "]");
        if(!_twoPane)
        {
            Log.v(LOG_TAG, "onForecastSelected: !_twoPane");
            Intent intent = new Intent(this, ArtistTop10Activity.class);
            Bundle extras = new Bundle();
            extras.putString(getString(R.string.intent_extra_artist_id), id);
            extras.putString(getString(R.string.intent_extra_artist_name), name);
            intent.putExtras(extras);
            startActivity(intent);
        }
        else
        {
            Log.v(LOG_TAG, "onArtistSelected: _twoPane");
            ArtistTop10Fragment top10Fragment = (ArtistTop10Fragment)getSupportFragmentManager()
                    .findFragmentByTag(TOP10FRAGMENT_TAG);
            if(top10Fragment != null)
            {
                top10Fragment.onArtistChange(id, name);
            }
        }
    }

}

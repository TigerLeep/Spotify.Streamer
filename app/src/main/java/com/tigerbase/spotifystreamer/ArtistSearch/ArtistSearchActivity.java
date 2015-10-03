package com.tigerbase.spotifystreamer.artistsearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tigerbase.spotifystreamer.artisttop10.ArtistTop10Activity;
import com.tigerbase.spotifystreamer.artisttop10.ArtistTop10Fragment;
import com.tigerbase.spotifystreamer.IArtistList;
import com.tigerbase.spotifystreamer.R;

public class ArtistSearchActivity extends AppCompatActivity implements IArtistList
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
        initializeOneOrTwoPane(savedInstanceState);
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
        //getMenuInflater().inflate(R.menu.menu_artist_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.v(LOG_TAG, "onOptionsItemSelected");
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
    public void onArtistSelected(String id, String name)
    {
        Log.v(LOG_TAG, "onArtistSelected: id=[" + id + "], name=[" + name + "]");

        navigateToArtistTop10Pane(id, name);
    }

    private void initializeOneOrTwoPane(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "initializeOneOrTwoPane");
        if (findViewById(R.id.artist_top10_container) != null)
        {
            initializeTwoPane(savedInstanceState);
        }
        else
        {
            initializeOnePane();
        }
    }

    private void initializeOnePane()
    {
        Log.v(LOG_TAG, "initializeOnePane");
        _twoPane = false;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setElevation(0f);
        }
    }

    private void initializeTwoPane(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "initializeTwoPane");
        _twoPane = true;
        if (savedInstanceState == null)
        {
            loadArtistTop10FragmentInTwoPane();
        }
    }

    private void loadArtistTop10FragmentInTwoPane()
    {
        Log.v(LOG_TAG, "loadArtistTop10FragmentInTwoPane");
        ArtistTop10Fragment top10Fragment = new ArtistTop10Fragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.artist_top10_container,
                        top10Fragment,
                        TOP10FRAGMENT_TAG)
                .commit();
    }

    private void navigateToArtistTop10Pane(String id, String name)
    {
        Log.v(LOG_TAG, "navigateToArtistTop10Pane");
        if(_twoPane)
        {
            informArtistTop10FragmentArtistChanged(id, name);
        }
        else
        {
            launchArtistTop10ActivityWithChangedArtist(id, name);
        }
    }

    private void informArtistTop10FragmentArtistChanged(String id, String name)
    {
        Log.v(LOG_TAG, "informArtistTop10FragmentArtistChanged");
        ArtistTop10Fragment top10Fragment = (ArtistTop10Fragment)getSupportFragmentManager()
                .findFragmentByTag(TOP10FRAGMENT_TAG);
        if(top10Fragment != null)
        {
            top10Fragment.onArtistChange(id, name);
        }
    }

    private void launchArtistTop10ActivityWithChangedArtist(String id, String name)
    {
        Log.v(LOG_TAG, "launchArtistTop10ActivityWithChangedArtist");

        Intent intent = createArtistTop10Intent(id, name);
        startActivity(intent);
    }

    private Intent createArtistTop10Intent(String id, String name)
    {
        Log.v(LOG_TAG, "createArtistTop10Intent");
        Intent intent = new Intent(this, ArtistTop10Activity.class);
        Bundle extras = CreateArtistTop10IntentBundle(id, name);
        intent.putExtras(extras);
        return intent;
    }

    private Bundle CreateArtistTop10IntentBundle(String id, String name)
    {
        Log.v(LOG_TAG, "CreateArtistTop10IntentBundle");
        Bundle extras = new Bundle();
        extras.putString(getString(R.string.intent_extra_artist_id), id);
        extras.putString(getString(R.string.intent_extra_artist_name), name);
        return extras;
    }

}

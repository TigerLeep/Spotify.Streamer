package com.tigerbase.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class ArtistSearchActivity extends AppCompatActivity
{

    private final String ARTIST_SEARCH_FRAGMENT_TAG = ArtistSearchActivityFragment.class.getSimpleName();

    private ArtistSearchActivityFragment _artistSearchActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        _artistSearchActivityFragment =
                (ArtistSearchActivityFragment)getFragmentManager()
                        .findFragmentByTag(ARTIST_SEARCH_FRAGMENT_TAG);
        if (_artistSearchActivityFragment == null)
        {
            _artistSearchActivityFragment = new ArtistSearchActivityFragment();
        }
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_artist_search_container,
                        _artistSearchActivityFragment,
                        ARTIST_SEARCH_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_search, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

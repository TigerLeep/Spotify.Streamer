package com.tigerbase.spotifystreamer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class ArtistSearchActivity extends ActionBarActivity
{

    private final String ARTIST_SEARCH_FRAGMENT_TAG = ArtistSearchActivityFragment.class.getSimpleName();

    private ArtistSearchActivityFragment _artistSearchActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        if (savedInstanceState == null)
        {
            _artistSearchActivityFragment = new ArtistSearchActivityFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_artist_search_fragment,
                             _artistSearchActivityFragment,
                             ARTIST_SEARCH_FRAGMENT_TAG)
                    .commit();
        }
        else
        {
            _artistSearchActivityFragment =
                    (ArtistSearchActivityFragment)getFragmentManager()
                    .findFragmentByTag(ARTIST_SEARCH_FRAGMENT_TAG);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_search, menu);
        return true;
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

package com.tigerbase.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class ArtistTop10Activity extends AppCompatActivity
{

    private final String ARTIST_TOP10_FRAGMENT_TAG = ArtistTop10ActivityFragment.class.getSimpleName();

    private ArtistTop10ActivityFragment _artistTop10ActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_top10);

        _artistTop10ActivityFragment =
                (ArtistTop10ActivityFragment)getFragmentManager()
                        .findFragmentByTag(ARTIST_TOP10_FRAGMENT_TAG);
        if (_artistTop10ActivityFragment == null)
        {
            _artistTop10ActivityFragment = new ArtistTop10ActivityFragment();
        }
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_artist_top10_container,
                        _artistTop10ActivityFragment,
                        ARTIST_TOP10_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_top10, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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

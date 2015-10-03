package com.tigerbase.spotifystreamer.artisttop10;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tigerbase.spotifystreamer.R;


public class ArtistTop10Activity extends AppCompatActivity
{
    private final String LOG_TAG = ArtistTop10Activity.class.getSimpleName();
    private final String ARTIST_TOP10_FRAGMENT_TAG = ArtistTop10Fragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_top10);
        initializeArtistTop10Fragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.v(LOG_TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_top10, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
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


    private void initializeArtistTop10Fragment()
    {
        Log.v(LOG_TAG, "initializeArtistTop10Fragment");
        ArtistTop10Fragment artistTop10Fragment = getArtistTop10Fragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_artist_top10_container,
                        artistTop10Fragment,
                        ARTIST_TOP10_FRAGMENT_TAG)
                .commit();
    }

    private ArtistTop10Fragment getArtistTop10Fragment()
    {
        Log.v(LOG_TAG, "getArtistTop10Fragment");
        ArtistTop10Fragment artistTop10Fragment =
                (ArtistTop10Fragment)getSupportFragmentManager()
                        .findFragmentByTag(ARTIST_TOP10_FRAGMENT_TAG);
        if (artistTop10Fragment == null)
        {
            artistTop10Fragment = new ArtistTop10Fragment();
        }
        return artistTop10Fragment;
    }

}

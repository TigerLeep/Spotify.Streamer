package com.tigerbase.spotifystreamer.player;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tigerbase.spotifystreamer.R;

public class PlayerActivity extends AppCompatActivity
{
    private final String LOG_TAG = PlayerActivity.class.getSimpleName();
    private final String PLAYER_FRAGMENT_TAG = PlayerFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);
        Bundle bundle = getPlayerBundle();
        PlayerFragment playerFragment = getPlayerFragment(bundle);
        loadPlayerFragment(playerFragment);
    }

    @NonNull
    private PlayerFragment getPlayerFragment(Bundle bundle)
    {
        PlayerFragment playerFragment =
                (PlayerFragment)getSupportFragmentManager()
                        .findFragmentByTag(PLAYER_FRAGMENT_TAG);
        if (playerFragment == null)
        {
            playerFragment = new PlayerFragment();
            playerFragment.setArguments(bundle);
        }
        return playerFragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    private Bundle getPlayerBundle()
    {
        Intent intent = getIntent();
        return (intent != null ? intent.getExtras() : new Bundle());
    }

    private PlayerFragment getPlayerFragment()
    {
        PlayerFragment playerFragment =
                (PlayerFragment)getSupportFragmentManager()
                        .findFragmentByTag(PLAYER_FRAGMENT_TAG);
        if (playerFragment == null)
        {
            playerFragment = new PlayerFragment();
        }
        return playerFragment;
    }

    private void loadPlayerFragment(PlayerFragment playerFragment)
    {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_player_container,
                        playerFragment,
                        PLAYER_FRAGMENT_TAG)
                .commit();
    }
}

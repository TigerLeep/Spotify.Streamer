package com.tigerbase.spotifystreamer.Player;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tigerbase.spotifystreamer.R;
import com.tigerbase.spotifystreamer.Track;

import java.util.ArrayList;

public class PlayerFragment extends DialogFragment
{
    private final static String LOG_TAG = PlayerFragment.class.getName();

    private TextView _artistName;
    private TextView _albumName;
    private TextView _trackName;
    private ImageView _albumThumbnail;
    private SeekBar _timeSlider;
    private ImageButton _skipBackButton;
    private ImageButton _previousButton;
    private ImageButton _playButton;
    private ImageButton _nextButton;
    private ImageButton _skipForwardButton;

    private ArrayList<Track> _tracks = null;
    private int _currentTrack = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_player, null);
        _artistName = (TextView)view.findViewById(R.id.player_artist_name);
        _albumName = (TextView)view.findViewById(R.id.player_album_name);
        _trackName = (TextView)view.findViewById(R.id.player_track_name);
        _albumThumbnail = (ImageView)view.findViewById(R.id.player_album_thumbnail);
        _timeSlider = (SeekBar)view.findViewById(R.id.player_time_slider);
        _skipBackButton = (ImageButton)view.findViewById(R.id.player_skipback_button);
        _previousButton = (ImageButton)view.findViewById(R.id.player_previous_button);
        _playButton = (ImageButton)view.findViewById(R.id.player_play_button);
        _nextButton = (ImageButton)view.findViewById(R.id.player_next_button);
        _skipForwardButton = (ImageButton)view.findViewById(R.id.player_skipforward_button);

        Bundle bundle;
        if (savedInstanceState != null)
        {
            Log.v(LOG_TAG, "onCreateView: savedInstanceState");
            bundle = savedInstanceState;
        }
        else
        {
            Log.v(LOG_TAG, "onCreateView: getArguments");
            bundle = getArguments();
        }
        if (bundle != null)
        {
            Log.v(LOG_TAG, "onCreateView: bundle");
            _tracks = bundle.getParcelableArrayList(getString(R.string.bundle_tracks));
            _currentTrack = bundle.getInt(getString(R.string.bundle_current_track));
            displayCurrentTrack();
        }

        _previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.v(LOG_TAG, "_previousButton.onClick");
                if (_currentTrack > 0)
                {
                    _currentTrack--;
                }
                displayCurrentTrack();
            }
        });
        _nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.v(LOG_TAG, "_nextButton.onClick");
                if (_currentTrack < _tracks.size() - 1)
                {
                    _currentTrack++;
                }
                displayCurrentTrack();
            }
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "onCreateDialog");
        Dialog playerDialog = super.onCreateDialog(savedInstanceState);
        playerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRetainInstance(true);
        return playerDialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.v(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.bundle_tracks), _tracks);
        outState.putInt(getString(R.string.bundle_current_track), _currentTrack);
    }

    private void displayCurrentTrack()
    {
        Track track = _tracks.get(_currentTrack);
        _artistName.setText(track.ArtistName); // Why no artist name on phone?
        _albumName.setText(track.AlbumName);
        _trackName.setText(track.Name);

    }

}


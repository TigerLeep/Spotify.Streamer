package com.tigerbase.spotifystreamer.Player;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tigerbase.spotifystreamer.R;
import com.tigerbase.spotifystreamer.Track;
import com.tigerbase.spotifystreamer.Player.PlayerService.PlayerBinder;

import java.util.ArrayList;

public class PlayerFragment extends DialogFragment
{
    private final static String LOG_TAG = PlayerFragment.class.getSimpleName();

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
    private PlayerService _playerService = null;
    private Intent _playerServiceIntent = null;
    private boolean _isPlayerServiceBound = false;

    private ServiceConnection _playerServiceConnection = new ServiceConnection()
    {
        private final String LOG_TAG = PlayerFragment.class.getName();

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            Log.v(LOG_TAG, "onServiceConnected");
            bindToPlayerService(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.v(LOG_TAG, "onServiceDisconnected");
            _isPlayerServiceBound = false;
        }
    };

    @Override
    public void onStart()
    {
        Log.v(LOG_TAG, "onStart");
        super.onStart();
        startPlayerService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_player, null);

        getViews(view);
        bindButtonClickListeners();

        Bundle bundle = getIncomingStateBundle(savedInstanceState);
        if (bundle != null)
        {
            Log.v(LOG_TAG, "onCreateView: bundle");
            _tracks = bundle.getParcelableArrayList(getString(R.string.bundle_tracks));
            _currentTrack = bundle.getInt(getString(R.string.bundle_current_track));
            displayCurrentTrack();
        }


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

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        Log.v(LOG_TAG, "onDismiss");
        super.onDismiss(dialog);
    }

    private void displayCurrentTrack()
    {
        Log.v(LOG_TAG, "displayCurrentTrack");
        Track track = _tracks.get(_currentTrack);
        _artistName.setText(track.ArtistName); // Why no artist name on phone?
        _albumName.setText(track.AlbumName);
        _trackName.setText(track.Name);
        if (track.ThumbnailImageUrl != null && !track.ThumbnailImageUrl.isEmpty())
        {
            Picasso.with(getActivity())
                    .load(track.ThumbnailImageUrl)
                    .into(_albumThumbnail);
        }
        else
        {
            _albumThumbnail.setImageBitmap(null);
        }

    }

    private void bindToPlayerService(IBinder binder)
    {
        Log.v(LOG_TAG, "bindToPlayerService");
        PlayerBinder playerBinder = (PlayerBinder)binder;
        _playerService = playerBinder.getService();
        _playerService.setTracks(_tracks);
        _playerService.setCurrentTrack(_currentTrack);
        _playerService.startPlayback();
        _isPlayerServiceBound = true;
    }

    private Bundle getIncomingStateBundle(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "getIncomingStateBundle");
        Bundle bundle;
        if (savedInstanceState != null)
        {
            Log.v(LOG_TAG, "getIncomingStateBundle: savedInstanceState");
            bundle = savedInstanceState;
        }
        else
        {
            Log.v(LOG_TAG, "getIncomingStateBundle: getArguments");
            bundle = getArguments();
        }
        return bundle;
    }

    private void getViews(View view)
    {
        Log.v(LOG_TAG, "getViews");
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
    }

    private void skipBack()
    {
        Log.v(LOG_TAG, "skipBack");
        _playerService.skipBack();
    }

    private void goToPreviousTrack()
    {
        Log.v(LOG_TAG, "goToPreviousTrack");
        _playerService.previousTrack();
        _currentTrack = _playerService.getCurrentTrack();
        displayCurrentTrack();
    }

    private void startPlayback()
    {
        Log.v(LOG_TAG, "startPlayback");
        displayCurrentTrack();
        _playerService.setCurrentTrack(_currentTrack);
        _playerService.startPlayback();
    }

    private void goToNextTrack()
    {
        Log.v(LOG_TAG, "goToNextTrack");
        _playerService.nextTrack();
        _currentTrack = _playerService.getCurrentTrack();
        displayCurrentTrack();
    }

    private void skipForward()
    {
        Log.v(LOG_TAG, "skipForward");
        _playerService.skipForward();
    }

    private void startPlayerService()
    {
        Log.v(LOG_TAG, "startPlayerService");
        if(_playerServiceIntent == null)
        {
            Context context = getActivity().getApplicationContext();
            _playerServiceIntent = new Intent(context, PlayerService.class);
            context.bindService(_playerServiceIntent, _playerServiceConnection, context.BIND_AUTO_CREATE);
            context.startService(_playerServiceIntent);
        }
    }

    private void bindButtonClickListeners()
    {
        _skipBackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                skipBack();
            }
        });
        _previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToPreviousTrack();
            }
        });
        _playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startPlayback();
            }
        });
        _nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToNextTrack();
            }
        });
        _skipForwardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                skipForward();
            }
        });
    }


}


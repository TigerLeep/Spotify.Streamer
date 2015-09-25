package com.tigerbase.spotifystreamer.Player;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
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
import com.tigerbase.spotifystreamer.Player.PlayerReceiver.Receivable;

import java.util.ArrayList;

public class PlayerFragment extends DialogFragment implements Receivable, SeekBar.OnSeekBarChangeListener
{
    private final static String LOG_TAG = PlayerFragment.class.getSimpleName();

    private TextView _artistName;
    private TextView _albumName;
    private TextView _trackName;
    private ImageView _albumThumbnail;
    private SeekBar _progressSlider;
    private TextView _playerCurrentTime;
    private TextView _playerTotalTime;
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
    private PlayerReceiver _playerReceiver = null;
    private boolean _shouldUpdateProgressBarAutomatically = true;
    private Handler _handler = new Handler();
    private Runnable _currentTimeUpdater = new Runnable()
    {
        private final String LOG_TAG = "_currentTimeUpdater";
        @Override
        public void run()
        {
            Log.v(LOG_TAG, "_currentTimeUpdater");
            if (!isAdded())
            {
                return;
            }
            int milliseconds = _playerService.getCurrentTime();
            updateCurrentPosition(milliseconds);
            _handler.postDelayed(_currentTimeUpdater, 250);
        }
    };

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
        bindListeners();

        Bundle bundle = getIncomingStateBundle(savedInstanceState);
        if (bundle != null)
        {
            Log.v(LOG_TAG, "onCreateView: bundle");
            _tracks = bundle.getParcelableArrayList(getString(R.string.bundle_tracks));
            _currentTrack = bundle.getInt(getString(R.string.bundle_current_track));
            if (bundle.containsKey(getString(R.string.player_receiver_tag)))
            {
                _playerReceiver = bundle.getParcelable(getString(R.string.player_receiver_tag));
            }
            displayCurrentTrack();
        }

        if (_playerReceiver == null)
        {
            _playerReceiver = new PlayerReceiver(new Handler());
        }
        _playerReceiver.setReceiver(this);

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
        outState.putParcelable(getString(R.string.player_receiver_tag), _playerReceiver);
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        Log.v(LOG_TAG, "onDismiss");
        super.onDismiss(dialog);
        stopTrackingTime();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        Log.v(LOG_TAG, "onReceiveResult");
        if (!isAdded())
        {
            return;
        }
        String resultType = resultData.getString(getString(R.string.player_receiver_type_tag));
        if (resultType == getString(R.string.player_receiver_duration))
        {
            Log.v(LOG_TAG, "onReceiveResult: duration");
            int duration = resultData.getInt(getString(R.string.player_receiver_duration, 0));
            updateDuration(duration);
            startTrackingTime();
        }
        else if (resultType == getString(R.string.player_receiver_playback_done))
        {
            Log.v(LOG_TAG, "onReceiveResult: playback_done");
            updateCurrentPosition(0);
            stopTrackingTime();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if(_isPlayerServiceBound && fromUser)
        {
            Log.v(LOG_TAG, "onProgressChanged: fromUser");
            _playerService.setCurrentTime(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
        Log.v(LOG_TAG, "onStartTrackingTouch");
        _shouldUpdateProgressBarAutomatically = false;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        Log.v(LOG_TAG, "onStopTrackingTouch");
        _shouldUpdateProgressBarAutomatically = true;
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
        _progressSlider = (SeekBar)view.findViewById(R.id.player_time_slider);
        _skipBackButton = (ImageButton)view.findViewById(R.id.player_skipback_button);
        _playerCurrentTime = (TextView)view.findViewById(R.id.player_current_time);
        _playerTotalTime = (TextView)view.findViewById(R.id.player_total_time);
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
            _playerServiceIntent.putExtra(getString(R.string.player_receiver_tag), _playerReceiver);
            context.bindService(_playerServiceIntent, _playerServiceConnection, context.BIND_AUTO_CREATE);
            context.startService(_playerServiceIntent);
        }
    }

    private void bindListeners()
    {
        _skipBackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.v(LOG_TAG, "_skipBackButton.onClick");
                skipBack();
            }
        });
        _previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.v(LOG_TAG, "_previousButton.onClick");
                goToPreviousTrack();
            }
        });
        _playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.v(LOG_TAG, "_playButton.onClick");
                startPlayback();
            }
        });
        _nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.v(LOG_TAG, "_nextButton.onClick");
                goToNextTrack();
            }
        });
        _skipForwardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.v(LOG_TAG, "_skipForwardButton.onClick");
                skipForward();
            }
        });
        _progressSlider.setOnSeekBarChangeListener(this);
    }

    private void updateDuration(int milliseconds)
    {
        Log.v(LOG_TAG, "updateDuration");
        _progressSlider.setMax(milliseconds);
        _playerTotalTime.setText(formatMilliseconds(milliseconds));
    }

    private void updateCurrentPosition(int milliseconds)
    {
        //Log.v(LOG_TAG, "updateCurrentPosition");
        if (_shouldUpdateProgressBarAutomatically)
        {
            _progressSlider.setProgress(milliseconds);
        }
        _playerCurrentTime.setText(formatMilliseconds(milliseconds));
    }

    private String formatMilliseconds(int milliseconds)
    {
        int minutes = milliseconds / 1000 / 60;
        int seconds = milliseconds / 1000 - (minutes * 60);

        return String.format("%d:%02d", minutes, seconds);
    }

    private void startTrackingTime()
    {
        stopTrackingTime();
        _currentTimeUpdater.run();
    }

    private void stopTrackingTime()
    {
        _handler.removeCallbacks(_currentTimeUpdater);
    }
}


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
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

    private TextView _artistNameTextView;
    private TextView _albumNameTextView;
    private TextView _trackNameTextView;
    private ProgressBar _playerBufferingProgressBar;
    private ImageView _albumThumbnailImageView;
    private SeekBar _progressSliderSeekBar;
    private TextView _playerCurrentTimeTextView;
    private TextView _playerTotalTimeTextView;
    private ImageButton _skipBackButton;
    private ImageButton _previousButton;
    private ImageButton _pauseButton;
    private ImageButton _playButton;
    private ImageButton _nextButton;
    private ImageButton _skipForwardButton;

    private ArrayList<Track> _tracks = null;
    private int _currentTrack = 0;
    private String _artistName = "";
    private int _duration = 0;
    private boolean _forceRestartIfDifferentTrack = false;
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
            Log.v(LOG_TAG, "run");
            if (!isAdded())
            {
                Log.v(LOG_TAG, "run: !isAdded");
                return;
            }
            int milliseconds = _playerService.getCurrentTime();
            if (milliseconds != PlayerService.DURATION_INVALID)
            {
                updateCurrentPosition(milliseconds);
                _handler.postDelayed(_currentTimeUpdater, 250);
            }
        }
    };

    private ServiceConnection _playerServiceConnection = new ServiceConnection()
    {
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
            loadStateFromBundle(bundle);
            if(_isPlayerServiceBound &&
                    (_playerService.getMode() == ServiceMode.Playing
                     || _playerService.getMode() == ServiceMode.Paused))
            {
                _currentTimeUpdater.run();
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

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "onCreateDialog");
        Dialog playerDialog = super.onCreateDialog(savedInstanceState);
        playerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRetainInstance(true);
        return playerDialog;
    }

    @Override
    public void onDestroyView()
    {
        Log.v(LOG_TAG, "onDestroyView");
        if (getDialog() != null && getRetainInstance())
        {
            Log.v(LOG_TAG, "onDestroyView: getDialog != null && getRetainInstance");
            getDialog().setOnDismissListener(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.v(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.bundle_tracks), _tracks);
        outState.putInt(getString(R.string.bundle_current_track), _currentTrack);
        outState.putInt(getString(R.string.bundle_duration), _duration);
        //outState.putParcelable(getString(R.string.player_receiver_tag), _playerReceiver);
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
        processServiceMessage(resultData);
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
        _artistNameTextView.setText(_artistName);
        _albumNameTextView.setText(track.AlbumName);
        _trackNameTextView.setText(track.Name);
        if (track.ThumbnailImageUrl != null && !track.ThumbnailImageUrl.isEmpty())
        {
            Picasso.with(getActivity())
                    .load(track.ThumbnailImageUrl)
                    .into(_albumThumbnailImageView);
        }
        else
        {
            _albumThumbnailImageView.setImageBitmap(null);
        }

    }

    private void bindToPlayerService(IBinder binder)
    {
        Log.v(LOG_TAG, "bindToPlayerService");
        PlayerBinder playerBinder = (PlayerBinder)binder;
        _playerService = playerBinder.getService();
        ServiceMode serviceMode = _playerService.getMode();

        if (serviceMode == ServiceMode.Stopped || (_forceRestartIfDifferentTrack && !isTrackPlaying()))
        {
            _playerService.setTracks(_tracks);
            _playerService.setCurrentTrack(_currentTrack);
            stopTrackingTime();
            _playerService.startPlayback();
            _forceRestartIfDifferentTrack = false;
        }
        else if (serviceMode == ServiceMode.Playing || serviceMode == ServiceMode.Paused)
        {
            _currentTimeUpdater.run();
            _duration = _playerService.getDuration();
            updateDuration(_duration);
        }
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
        _artistNameTextView = (TextView)view.findViewById(R.id.player_artist_name);
        _albumNameTextView = (TextView)view.findViewById(R.id.player_album_name);
        _trackNameTextView = (TextView)view.findViewById(R.id.player_track_name);
        _playerBufferingProgressBar = (ProgressBar)view.findViewById(R.id.player_buffering);
        _albumThumbnailImageView = (ImageView)view.findViewById(R.id.player_album_thumbnail);
        _progressSliderSeekBar = (SeekBar)view.findViewById(R.id.player_time_slider);
        _skipBackButton = (ImageButton)view.findViewById(R.id.player_skipback_button);
        _playerCurrentTimeTextView = (TextView)view.findViewById(R.id.player_current_time);
        _playerTotalTimeTextView = (TextView)view.findViewById(R.id.player_total_time);
        _previousButton = (ImageButton)view.findViewById(R.id.player_previous_button);
        _pauseButton = (ImageButton)view.findViewById(R.id.player_pause_button);
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
        _currentTrack = _playerService.getCurrentTrackIndex();
        displayCurrentTrack();
    }

    private void pausePlayback()
    {
        Log.v(LOG_TAG, "pausePlayback");
        if (_isPlayerServiceBound && _playerService.getMode() == ServiceMode.Playing)
        {
            _playerService.pausePlayback();
        }
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
        _currentTrack = _playerService.getCurrentTrackIndex();
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
        if(_playerService == null)
        {
            Log.v(LOG_TAG, "startPlayerService: _playerService == null");
        }
        if(_playerServiceIntent == null)
        {
            Log.v(LOG_TAG, "startPlayerService: _playerServiceIntent == null");
            Context context = getActivity().getApplicationContext();
            _playerServiceIntent = new Intent(context, PlayerService.class);
            _playerServiceIntent.putExtra(PlayerService.RECEIVER_TAG, _playerReceiver);
            context.bindService(_playerServiceIntent, _playerServiceConnection, context.BIND_AUTO_CREATE);
            context.startService(_playerServiceIntent);
        }
    }

    private void bindListeners()
    {
        Log.v(LOG_TAG, "bindListeners");
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
        _pauseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.v(LOG_TAG, "_pauseButton.onClick");
                pausePlayback();
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
        _progressSliderSeekBar.setOnSeekBarChangeListener(this);
    }

    private void updateDuration(int milliseconds)
    {
        Log.v(LOG_TAG, "updateDuration");
        _progressSliderSeekBar.setMax(milliseconds);
        _playerTotalTimeTextView.setText(formatMilliseconds(milliseconds));
    }

    private void updateCurrentPosition(int milliseconds)
    {
        //Log.v(LOG_TAG, "updateCurrentPosition");
        if (_shouldUpdateProgressBarAutomatically)
        {
            _progressSliderSeekBar.setMax(_duration);
            _progressSliderSeekBar.setProgress(milliseconds);
        }
        _playerCurrentTimeTextView.setText(formatMilliseconds(milliseconds));
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
        //
        _handler.removeCallbacks(_currentTimeUpdater);
    }

    private void loadStateFromBundle(Bundle bundle)
    {
        Log.v(LOG_TAG, "loadStateFromBundle");
        _tracks = bundle.getParcelableArrayList(getString(R.string.bundle_tracks));
        _currentTrack = bundle.getInt(getString(R.string.bundle_current_track));
        _artistName = bundle.getString(getString(R.string.bundle_artist_name));
        if (bundle.containsKey(getString(R.string.bundle_duration)))
        {
            Log.v(LOG_TAG, "loadStateFromBundle: duration");
            _duration = bundle.getInt(getString(R.string.bundle_duration));
            updateDuration(_duration);
        }
        if (bundle.containsKey(getString(R.string.bundle_force_restart_if_different_track)))
        {
            _forceRestartIfDifferentTrack = true;
        }

        // Works great on screen rotate, but throws an exception when the user hits Home and then
        // clicks this app's icon to return here.  It complains that it can't cast the
        // ResultReceiver in the bundle to a PlayerReceiver.  WTF?  It definitely *IS* a
        // PlayerReceiver when onSaveInstanceState stuffs it in there.:)
        //
        //if (bundle.containsKey(getString(R.string.player_receiver_tag)))
        //{
            //Log.v(LOG_TAG, "loadStateFromBundle: receiver_tag");
            //_playerReceiver = bundle.getParcelable(getString(R.string.player_receiver_tag));
        //}
    }

    private void processServiceMessage(Bundle resultData)
    {
        Log.v(LOG_TAG, "processServiceMessage");
        String messageType = resultData.getString(PlayerReceiver.MESSAGE_TYPE_TAG);
        switch (messageType)
        {
            case PlayerReceiver.MESSAGE_DURATION:
                Log.v(LOG_TAG, "processServiceMessage: MESSAGE_DURATION");
                _duration = resultData.getInt(PlayerReceiver.MESSAGE_DURATION, 0);
                updateDuration(_duration);
                startTrackingTime();
                break;
            case PlayerReceiver.MESSAGE_PLAYBACK_DONE:
                Log.v(LOG_TAG, "processServiceMessage: MESSAGE_PLAYBACK_DONE");
                updateCurrentPosition(0);
                stopTrackingTime();
                showPlayButton();
                break;
            case PlayerReceiver.MESSAGE_PLAYBACK_BUFFERING:
                Log.v(LOG_TAG, "processServiceMessage: MESSAGE_PLAYBACK_BUFFERING");
                showBuffering();
            case PlayerReceiver.MESSAGE_PLAYBACK_STARTED:
                Log.v(LOG_TAG, "processServiceMessage: MESSAGE_PLAYBACK_STARTED");
                hideBuffering();
                showPauseButton();
                break;
            case PlayerReceiver.MESSAGE_PLAYBACK_PAUSED:
                Log.v(LOG_TAG, "processServiceMessage: MESSAGE_PLAYBACK_PAUSED");
                showPlayButton();
        }
    }

    private void showPauseButton()
    {
        Log.v(LOG_TAG, "showPauseButton");
        _playButton.setVisibility(View.GONE);
        _pauseButton.setVisibility(View.VISIBLE);
    }

    private void showPlayButton()
    {
        Log.v(LOG_TAG, "showPlayButton");
        _playButton.setVisibility(View.VISIBLE);
        _pauseButton.setVisibility(View.GONE);
    }

    private void showBuffering()
    {
        Log.v(LOG_TAG, "showBuffering");
        _playerBufferingProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideBuffering()
    {
        Log.v(LOG_TAG, "hideBuffering");
        _playerBufferingProgressBar.setVisibility(View.INVISIBLE);
    }

    private boolean isTrackPlaying()
    {
        Log.v(LOG_TAG, "isTrackPlaying");
        boolean isPlaying = false;
        ServiceMode serviceMode = _playerService.getMode();
        if (serviceMode == ServiceMode.Playing || serviceMode == ServiceMode.Paused)
        {
            int currentPlayingTrack = _playerService.getCurrentTrackIndex();
            String currentPlayingName = _playerService.getCurrentTrackName();
            isPlaying = (currentPlayingTrack == _currentTrack
                        && currentPlayingName.equals(_tracks.get(_currentTrack).Name));
        }

        return isPlaying;
    }

}

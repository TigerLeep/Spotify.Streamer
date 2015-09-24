package com.tigerbase.spotifystreamer.Player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.tigerbase.spotifystreamer.ArtistSearch.ArtistSearchActivity;
import com.tigerbase.spotifystreamer.R;
import com.tigerbase.spotifystreamer.Track;

import java.io.IOException;
import java.util.ArrayList;

// The follow web pages were used to learn how to implement a service to facilitate audio playback.
// https://android.googlesource.com/platform/development/+/master/samples/RandomMusicPlayer/
// http://developer.android.com/guide/topics/media/mediaplayer.html
// http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile
public class PlayerService
        extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
                   MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener
{
    private static final String LOG_TAG = PlayerService.class.getSimpleName();
    private static final String WIFI_LOCK_TAG = "WifiLock";

    private static final String ACTION_PLAY = "com.tigerbase.spotifystreamer.play";
    private static final String ACTION_PAUSE = "com.tigerbase.spotifystreamer.pause";
    private static final String ACTION_PREVIOUS_TRACK = "com.tigerbase.spotifystreamer.previous_track";
    private static final String ACTION_NEXT_TRACK = "com.tigerbase.spotifystreamer.next_track";
    private static final String ACTION_STOP = "com.tigerbase.spotifystreamer.stop";
    private static final String ACTION_SKIP_BACK = "com.tigerbase.spotifystreamer.skip_back";
    private static final String ACTION_SKIP_FORWARD = "com.tigerbase.spotifystreamer.skip_forward";

    private static final int NOTIFICATION_ID = 1;
    private static final float DUCK_VOLUME = 0.1f;

    private AudioManager _audioManager;
    private NotificationManager _notificationManager;
    private Notification _notification;
    private MediaPlayer _mediaPlayer;
    private IBinder _playerBinder = new PlayerBinder();

    private WifiLock _wifiLock;
    private AudioFocus _audioFocus;
    private ServiceMode _serviceMode = ServiceMode.Stopped;

    private ArrayList<Track> _tracks;
    private int _currentTrack;

    @Override
    public void onCreate()
    {
        Log.v(LOG_TAG, "onCreate");

        initializeWifiLock();

        _audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        _notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        initializeMediaPlayerIfNeeded();

        _audioFocus = AudioFocus.NotFocused;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.v(LOG_TAG, "onStartCommand");
        String action = intent.getAction();
        if (action == null)
        {
            Log.v(LOG_TAG, "onStartCommand: action == null");
            return START_NOT_STICKY;
        }

        switch (action)
        {
            case ACTION_PLAY:
                startPlayback();
                break;
            case ACTION_PAUSE:
                pausePlayback();
                break;
            case ACTION_PREVIOUS_TRACK:
                previousTrack();
                break;
            case ACTION_NEXT_TRACK:
                nextTrack();
                break;
            case ACTION_STOP:
                stopPlayback();
                break;
            case ACTION_SKIP_BACK:
                skipBack();
                break;
            case ACTION_SKIP_FORWARD:
                skipForward();
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onAudioFocusChange(int focusChange)
    {
        Log.v(LOG_TAG, "onAudioFocusChange");
        switch (focusChange)
        {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                duckPlayback();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                startPlayback();
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        Log.v(LOG_TAG, "onCompletion");
        releaseAllResources();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        Log.v(LOG_TAG, "onError");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        Log.v(LOG_TAG, "onPrepared");
        _serviceMode = ServiceMode.Playing;
        updateExistingNotification(_tracks.get(_currentTrack).Name + " (Playing)");
        configureAndStartMediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.v(LOG_TAG, "onBind");
        return _playerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.v(LOG_TAG, "onUnbind");
        super.onUnbind(intent);
        releaseAllResources();

        return false;
    }

    @Override
    public void onDestroy()
    {
        Log.v(LOG_TAG, "onDestroy");
        releaseAllResources();
    }

    public void setTracks(ArrayList<Track> tracks)
    {
        Log.v(LOG_TAG, "setTracks");
        _tracks = tracks;
    }

    public ArrayList<Track> getTracks()
    {
        Log.v(LOG_TAG, "getTracks");
        return _tracks;
    }

    private void initializeWifiLock()
    {
        Log.v(LOG_TAG, "initializeWifiLock");
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        _wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);
    }

    private void initializeMediaPlayerIfNeeded()
    {
        Log.v(LOG_TAG, "initializeMediaPlayerIfNeeded");
        if (_mediaPlayer == null)
        {
            Log.v(LOG_TAG, "initializeMediaPlayerIfNeeded: mediaPlayer == null");
            _mediaPlayer = new MediaPlayer();

            _mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            _mediaPlayer.setOnPreparedListener(this);
            _mediaPlayer.setOnCompletionListener(this);
            _mediaPlayer.setOnErrorListener(this);
        }
        else
        {
            Log.v(LOG_TAG, "initializeMediaPlayerIfNeeded: mediaPlayer != null");
            _mediaPlayer.reset();
        }
    }

    private void requestAudioFocus()
    {
        Log.v(LOG_TAG, "requestAudioFocus");
        if (_audioFocus != AudioFocus.Focused)
        {
            int status = _audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            {
                _audioFocus = AudioFocus.Focused;
            }
        }
    }

    private void releaseAudioFocus()
    {
        Log.v(LOG_TAG, "releaseAudioFocus");
        if (_audioFocus == AudioFocus.Focused)
        {
            int status = _audioManager.abandonAudioFocus(this);
            if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            {
                _audioFocus = AudioFocus.NotFocused;
            }
        }
    }

    public void startPlayback()
    {
        Log.v(LOG_TAG, "startPlayback");
        switch (_serviceMode)
        {
            case Paused:
                requestAudioFocus();
                _serviceMode = ServiceMode.Playing;
                bringServiceToForeground(getPlayingNotificationMessage());
                configureAndStartMediaPlayer();
                break;
            case Stopped:
            case Playing:
                stopPlayback();
                requestAudioFocus();
                releaseAllResourcesExceptMediaPlayer();
                try
                {
                    initializeMediaPlayerIfNeeded();
                    Track track = _tracks.get(getCurrentTrack());
                    if(track != null)
                    {
                        _mediaPlayer.setDataSource(track.PreviewUrl);
                    }
                    _serviceMode = ServiceMode.Buffering;
                    bringServiceToForeground(getBufferingNotificationMessage());
                    _mediaPlayer.prepareAsync();
                    _wifiLock.acquire();
                }
                catch (IOException ex)
                {
                    Log.e(LOG_TAG, ex.getMessage());
                    ex.printStackTrace();
                }
                break;
        }
    }

    public void pausePlayback()
    {
        Log.v(LOG_TAG, "pausePlayback");
        if (_serviceMode == ServiceMode.Playing)
        {
            _serviceMode = ServiceMode.Paused;
            _mediaPlayer.pause();
            releaseAllResourcesExceptMediaPlayer();
        }
    }

    public void duckPlayback()
    {
        Log.v(LOG_TAG, "duckPlayback");
        setVolumeToLow();
    }

    public void stopPlayback()
    {
        Log.v(LOG_TAG, "stopPlayback");
        if(_serviceMode == ServiceMode.Playing || _serviceMode == ServiceMode.Paused)
        {
            releaseAllResources();
            releaseAudioFocus();

        }
    }

    public void previousTrack()
    {
        Log.v(LOG_TAG, "previousTrack");
        if(_serviceMode == ServiceMode.Buffering)
        {
            return;
        }
        if (_currentTrack > 0)
        {
            _currentTrack--;
        }
        startPlayback();
    }

    public void nextTrack()
    {
        Log.v(LOG_TAG, "nextTrack");
        if(_serviceMode == ServiceMode.Buffering)
        {
            return;
        }
        if (_currentTrack < _tracks.size() - 1)
        {
            _currentTrack++;
        }
        startPlayback();
    }

    public void skipBack()
    {
        Log.v(LOG_TAG, "skipBack");
        if(_serviceMode == ServiceMode.Buffering)
        {
            return;
        }
        if (_mediaPlayer != null)
        {
            int position = _mediaPlayer.getCurrentPosition();
            position -= 5000;
            if (position < 0)
            {
                position = 0;
            }
            _mediaPlayer.seekTo(position);
        }
    }

    public void skipForward()
    {
        Log.v(LOG_TAG, "skipForward");
        if(_serviceMode == ServiceMode.Buffering)
        {
            return;
        }
        if (_mediaPlayer != null)
        {
            int position = _mediaPlayer.getCurrentPosition();
            int duration = _mediaPlayer.getDuration();
            position += 5000;
            if (position > duration)
            {
                position = duration - 1000;
            }
            if (position < 0)
            {
                position = 0;
            }
            _mediaPlayer.seekTo(position);
        }
    }

    private void releaseAllResources()
    {
        Log.v(LOG_TAG, "releaseAllResources");
        releaseAllResourcesExceptMediaPlayer();
        releaseMediaPlayer();
    }

    private void releaseAllResourcesExceptMediaPlayer()
    {
        Log.v(LOG_TAG, "releaseAllResourcesExceptMediaPlayer");
        stopForeground(true);

        if (_wifiLock != null && _wifiLock.isHeld())
        {
            _wifiLock.release();
        }
    }

    private void releaseMediaPlayer()
    {
        Log.v(LOG_TAG, "releaseMediaPlayer");
        _serviceMode = ServiceMode.Stopped;
        if (_mediaPlayer != null)
        {
            _mediaPlayer.reset();
            _mediaPlayer.release();
            _mediaPlayer = null;
        }
    }

    private void updateExistingNotification(String notificationText)
    {
        Log.v(LOG_TAG, "updateExistingNotification");
        Intent intent = new Intent(getApplicationContext(), ArtistSearchActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        _notification.setLatestEventInfo(getApplicationContext(), "Spotify Streamer", notificationText, pendingIntent);
        _notificationManager.notify(NOTIFICATION_ID, _notification);
    }

    private void createNewNotification(String notificationText)
    {
        Log.v(LOG_TAG, "createNewNotification");
        Intent intent = new Intent(getApplicationContext(), ArtistSearchActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        _notification = new Notification();
        _notification.tickerText = notificationText;
        _notification.icon = android.R.drawable.ic_media_play;
        _notification.flags |= Notification.FLAG_ONGOING_EVENT;
        _notification.setLatestEventInfo(getApplicationContext(), "Spotify Streamer", notificationText, pendingIntent);
    }

    private void configureAndStartMediaPlayer()
    {
        Log.v(LOG_TAG, "configureAndStartMediaPlayer");
        if (_audioFocus == AudioFocus.NotFocused)
        {
            pauseMediaPlayerIfPlaying();
            return;
        }
        if (_audioFocus == AudioFocus.Ducked)
        {
            setVolumeToLow();
            return;
        }

        setVolumeToFull();
        startMediaPlayerIfNotPlaying();
    }

    private void startMediaPlayerIfNotPlaying()
    {
        Log.v(LOG_TAG, "startMediaPlayerIfNotPlaying");
        if (!_mediaPlayer.isPlaying())
        {
            _mediaPlayer.start();
        }
    }

    private void pauseMediaPlayerIfPlaying()
    {
        Log.v(LOG_TAG, "pauseMediaPlayerIfPlaying");
        if (_mediaPlayer.isPlaying())
        {
            _mediaPlayer.pause();
        }
    }

    private void setVolumeToLow()
    {
        Log.v(LOG_TAG, "setVolumeToLow");
        _mediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
    }

    private void setVolumeToFull()
    {
        Log.v(LOG_TAG, "setVolumeToFull");
        _mediaPlayer.setVolume(1.0f, 1.0f);
    }

    private void bringServiceToForeground(String notificationText)
    {
        Log.v(LOG_TAG, "bringServiceToForeground");
        createNewNotification(notificationText);
        startForeground(NOTIFICATION_ID, _notification);
    }

    private String getPlayingNotificationMessage()
    {
        Log.v(LOG_TAG, "getPlayingNotificationMessage");
        return getNotificationMessage(getString(R.string.notification_playing_message));
    }

    private String getBufferingNotificationMessage()
    {
        Log.v(LOG_TAG, "getBufferingNotificationMessage");
        return getNotificationMessage(getString(R.string.notification_buffering_message));
    }

    private String getNotificationMessage(String notificationFormatText)
    {
        Log.v(LOG_TAG, "getNotificationMessage");
        Track track = _tracks.get(getCurrentTrack());
        if(track == null)
        {
            return "";
        }
        return String.format(notificationFormatText, track.Name);
    }

    private boolean isCurrentTrackValid()
    {
        Log.v(LOG_TAG, "isCurrentTrackValid");
        return !(_tracks == null || _currentTrack < 0 || _currentTrack >= _tracks.size());
    }

    public int getCurrentTrack()
    {
        Log.v(LOG_TAG, "getCurrentTrack");
        return _currentTrack;
    }

    public void setCurrentTrack(int currentTrack)
    {
        Log.v(LOG_TAG, "setCurrentTrack");
        _currentTrack = currentTrack;
    }

    public class PlayerBinder extends Binder
    {
        private final String LOG_TAG = PlayerBinder.class.getSimpleName();

        PlayerService getService()
        {
            Log.v(LOG_TAG, "getService");
            return PlayerService.this;
        }
    }

}

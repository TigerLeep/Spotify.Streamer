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
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.tigerbase.spotifystreamer.ArtistSearch.ArtistSearchActivity;
import com.tigerbase.spotifystreamer.R;
import com.tigerbase.spotifystreamer.Track;

import java.io.IOException;
import java.util.ArrayList;

// The follow sample was used to learn how to implement a service to facilitate audio playback.
// https://android.googlesource.com/platform/development/+/master/samples/RandomMusicPlayer/
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
    private static final String ACTION_REWIND = "com.tigerbase.spotifystreamer.rewind";

    private static final int NOTIFICATION_ID = 1;
    private static final float DUCK_VOLUME = 0.1f;

    private AudioManager _audioManager;
    private NotificationManager _notificationManager;
    private Notification _notification;
    private MediaPlayer _mediaPlayer;

    private WifiLock _wifiLock;
    private AudioFocus _audioFocus;
    private ServiceMode _serviceMode;

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
        String action = intent.getAction();
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
            case ACTION_REWIND:
                rewindPlayback();
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onAudioFocusChange(int focusChange)
    {
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

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        _serviceMode = ServiceMode.Playing;
        updateExistingNotification(_tracks.get(_currentTrack).Name + " (Playing)");
        configureAndStartMediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void setTracks(ArrayList<Track> tracks)
    {
        _tracks = tracks;
    }

    public ArrayList<Track> getTracks()
    {
        return _tracks;
    }

    public void setCurrentTrack(String trackId)
    {
        for (int index = 0; index < _tracks.size(); index++)
        {
            if(_tracks.get(index).Id == trackId)
            {
                _currentTrack = index;
            }
        }
    }


    private void initializeWifiLock()
    {
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        _wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);
    }

    private void initializeMediaPlayerIfNeeded()
    {
        if (_mediaPlayer == null)
        {
            _mediaPlayer = new MediaPlayer();

            _mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            _mediaPlayer.setOnPreparedListener(this);
            _mediaPlayer.setOnCompletionListener(this);
            _mediaPlayer.setOnErrorListener(this);
        }
        else
        {
            _mediaPlayer.reset();
        }
    }

    private void requestAudioFocus()
    {
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
        if (_audioFocus == AudioFocus.Focused)
        {
            int status = _audioManager.abandonAudioFocus(this);
            if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            {
                _audioFocus = AudioFocus.NotFocused;
            }
        }
    }

    private void startPlayback()
    {
        if (_serviceMode == ServiceMode.Paused)
        {
            requestAudioFocus();
            _serviceMode = ServiceMode.Playing;
            bringServiceToForeground(getPlayingNotificationMessage());
            configureAndStartMediaPlayer();
        }
        else if (_serviceMode == ServiceMode.Stopped)
        {
            requestAudioFocus();
            releaseAllResourcesExceptMediaPlayer();
            try
            {
                initializeMediaPlayerIfNeeded();
                Track track = getCurrentTrack();
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
        }
    }

    private void pausePlayback()
    {
        if (_serviceMode == ServiceMode.Playing)
        {
            _serviceMode = ServiceMode.Paused;
            _mediaPlayer.pause();
            releaseAllResourcesExceptMediaPlayer();
        }
    }

    private void duckPlayback()
    {

    }

    private void stopPlayback()
    {
        if(_serviceMode == ServiceMode.Playing || _serviceMode == ServiceMode.Paused)
        {
            _serviceMode = ServiceMode.Stopped;
            releaseAllResources();
            releaseAudioFocus();

        }
    }

    private void previousTrack()
    {

    }

    private void nextTrack()
    {

    }

    private void rewindPlayback()
    {

    }

    private void releaseAllResources()
    {
        releaseAllResourcesExceptMediaPlayer();
        releaseMediaPlayer();
    }

    private void releaseAllResourcesExceptMediaPlayer()
    {
        stopForeground(true);

        if (_wifiLock.isHeld())
        {
            _wifiLock.release();
        }
    }

    private void releaseMediaPlayer()
    {
        _mediaPlayer.reset();
        _mediaPlayer.release();
        _mediaPlayer = null;
    }

    private void updateExistingNotification(String notificationText)
    {
        Intent intent = new Intent(getApplicationContext(), ArtistSearchActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        _notification.setLatestEventInfo(getApplicationContext(), "Spotify Streamer", notificationText, pendingIntent);
        _notificationManager.notify(NOTIFICATION_ID, _notification);
    }

    private void createNewNotification(String notificationText)
    {
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
        if (!_mediaPlayer.isPlaying())
        {
            _mediaPlayer.start();
        }
    }

    private void pauseMediaPlayerIfPlaying()
    {
        if (_mediaPlayer.isPlaying())
        {
            _mediaPlayer.pause();
        }
    }

    private void setVolumeToLow()
    {
        //
        _mediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
    }

    private void setVolumeToFull()
    {
        //
        _mediaPlayer.setVolume(1.0f, 1.0f);
    }

    private void bringServiceToForeground(String notificationText)
    {
        createNewNotification(notificationText);
        startForeground(NOTIFICATION_ID, _notification);
    }

    private String getPlayingNotificationMessage()
    {
        return getNotificationMessage(getString(R.string.notification_playing_message));
    }

    private String getBufferingNotificationMessage()
    {
        return getNotificationMessage(getString(R.string.notification_buffering_message));
    }

    private String getNotificationMessage(String notificationFormatText)
    {
        Track track = getCurrentTrack();
        if(track == null)
        {
            return "";
        }
        return String.format(notificationFormatText, track.Name);
    }

    private boolean isCurrentTrackValid()
    {
        return !(_tracks == null || _currentTrack < 0 || _currentTrack >= _tracks.size());
    }

    private Track getCurrentTrack()
    {
        if (!isCurrentTrackValid())
        {
            return null;
        }
        return _tracks.get(_currentTrack);
    }



}

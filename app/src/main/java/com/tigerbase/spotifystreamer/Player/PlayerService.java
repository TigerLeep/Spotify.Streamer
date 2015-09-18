package com.tigerbase.spotifystreamer.Player;

import android.app.NotificationManager;
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

import com.tigerbase.spotifystreamer.Track;

import java.util.ArrayList;

// The follow sample was used to learn how to implement a service to facilitate audio playback.
// https://android.googlesource.com/platform/development/+/master/samples/RandomMusicPlayer/
public class PlayerService
        extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
                   MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener
{
    private final static String LOG_TAG = PlayerService.class.getSimpleName();
    private final static String WIFI_LOCK_TAG = "WifiLock";

    private final static String ACTION_PLAY = "com.tigerbase.spotifystreamer.play";
    private final static String ACTION_PAUSE = "com.tigerbase.spotifystreamer.pause";
    private final static String ACTION_PREVIOUS_TRACK = "com.tigerbase.spotifystreamer.previous_track";
    private final static String ACTION_NEXT_TRACK = "com.tigerbase.spotifystreamer.next_track";
    private final static String ACTION_STOP = "com.tigerbase.spotifystreamer.stop";
    private final static String ACTION_REWIND = "com.tigerbase.spotifystreamer.rewind";

    private AudioManager _audioManager;
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

    private void tryToGetAudioFocus()
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

    private void startPlayback()
    {
        if (_serviceMode == ServiceMode.Paused)
        {
            // Resume playback
        }
        else if (_serviceMode == ServiceMode.Stopped)
        {
            tryToGetAudioFocus();
            
        }
    }

    private void pausePlayback()
    {

    }

    private void duckPlayback()
    {

    }

    private void stopPlayback()
    {

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

}

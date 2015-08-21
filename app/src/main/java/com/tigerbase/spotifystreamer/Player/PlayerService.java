package com.tigerbase.spotifystreamer.Player;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tigerbase.spotifystreamer.TrackParcelable;

import java.io.IOException;
import java.util.ArrayList;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener
{
    private final static String LOG_TAG = PlayerService.class.getName();

    private MediaPlayer _player = null;
    private ArrayList<TrackParcelable> _tracks = null;
    private int _playingTrack;

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (_player != null)
        {
            _player.start();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public class PlayerBinder extends Binder
    {
        public PlayerService GetService()
        {
            return PlayerService.this;
        }
    }

    public void SetTracks(ArrayList<TrackParcelable> tracks) {
        _tracks = tracks;
    }

    public void PlayTrack(int trackIndex)
    {
        _player.reset();

        _playingTrack = trackIndex;
        TrackParcelable track = _tracks.get(_playingTrack);

        try
        {
            _player.setDataSource(getApplicationContext(), Uri.parse(track.PreviewUrl));
            _player.prepareAsync();
        } catch (IOException ex)
        {
            Log.e(LOG_TAG, "An error occurred setting the media player data source: " + ex.getMessage());
        }
    }

}

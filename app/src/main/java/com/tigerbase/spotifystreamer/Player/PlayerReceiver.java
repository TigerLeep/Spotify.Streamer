package com.tigerbase.spotifystreamer.Player;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

// The follow web page was used to learn how to implement a ResultReceiver to allow the
// service to communicate back to the DialogFragment
// http://stackoverflow.com/questions/4510974/using-resultreceiver-in-android
public class PlayerReceiver extends ResultReceiver
{
    public static final String MESSAGE_TYPE_TAG = "TYPE";
    public static final String MESSAGE_DURATION = "DURATION";
    public static final String MESSAGE_PLAYBACK_BUFFERING = "PLAYBACK_BUFFERING";
    public static final String MESSAGE_PLAYBACK_STARTED = "PLAYBACK_STARTED";
    public static final String MESSAGE_PLAYBACK_PAUSED = "PLAYBACK_PAUSED";
    public static final String MESSAGE_PLAYBACK_DONE = "PLAYBACK_DONE";

    private final static String LOG_TAG = PlayerReceiver.class.getSimpleName();

    private Receivable _receiver = null;

    public PlayerReceiver(Handler handler)
    {
        super(handler);
    }

    public interface Receivable
    {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receivable receiver)
    {
        Log.v(LOG_TAG, "setReceiver");
        _receiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData)
    {
        Log.v(LOG_TAG, "onReceiveResult");
        if (_receiver != null)
        {
            _receiver.onReceiveResult(resultCode, resultData);
        }
    }
}

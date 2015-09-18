package com.tigerbase.spotifystreamer.Player;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.tigerbase.spotifystreamer.R;
import com.tigerbase.spotifystreamer.Track;

import java.util.ArrayList;

public class PlayerFragment extends DialogFragment
{
    private final static String LOG_TAG = PlayerFragment.class.getName();

    private TextView _textView;
    private Button _previousButton;
    private Button _nextButton;

    private ArrayList<Track> _tracks = null;
    private int _currentTrack = 0;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_player, null);
        _textView = (TextView)view.findViewById(R.id.player_text);
        _previousButton = (Button)view.findViewById(R.id.player_previous_button);
        _nextButton = (Button)view.findViewById(R.id.player_next_button);

        Bundle bundle = getArguments();
        if (bundle != null)
        {
            _tracks = bundle.getParcelableArrayList(getString(R.string.bundle_tracks));
            _currentTrack = bundle.getInt(getString(R.string.bundle_current_track));
        }

        _previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _textView.setText("Previous");
            }
        });
        _nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _textView.setText("Next");
            }
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog playerDialog = super.onCreateDialog(savedInstanceState);
        playerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return playerDialog;
    }

}


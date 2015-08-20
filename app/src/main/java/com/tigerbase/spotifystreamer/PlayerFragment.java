package com.tigerbase.spotifystreamer;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Leep on 8/19/2015.
 */
public class PlayerFragment extends DialogFragment
{
    public interface IPlayerHost
    {
        TrackParcelable getCurrentTrack();
        TrackParcelable getNextTrack();
        TrackParcelable getPreviousTrack();
    }

    private IPlayerHost _playerHost;
    private TextView _textView;
    private Button _previousButton;
    private Button _nextButton;



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        if (activity instanceof IPlayerHost)
        {
            _playerHost = (IPlayerHost)activity;
        }
        else
        {
            throw new UnsupportedOperationException(getString(R.string.iplayerhost_error, activity.toString()));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_player, null);
        _textView = (TextView)view.findViewById(R.id.player_text);
        _previousButton = (Button)view.findViewById(R.id.player_previous_button);
        _nextButton = (Button)view.findViewById(R.id.player_next_button);



        _previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TrackParcelable track = _playerHost.getPreviousTrack();
                _textView.setText(track.Name);
            }
        });
        _nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TrackParcelable track = _playerHost.getNextTrack();
                _textView.setText(track.Name);
            }
        });

        TrackParcelable track = _playerHost.getCurrentTrack();
        _textView.setText(track.Name);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.player_ok_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                    }
                });
        return builder.create();
    }
}


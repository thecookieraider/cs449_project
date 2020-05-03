package com.ketloz.lolbot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import no.stelar7.api.r4j.basic.constants.api.Platform;

public class RegionDialog extends DialogFragment {
    public RegionDialog() {
        // Required empty public constructor
    }

    public interface RegionDialogListener {
        public void onRegionClick(DialogFragment dialog, Platform platform);
    }

    // Use this instance of the interface to deliver action events
    RegionDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select region");

        CharSequence[] items = new CharSequence[Platform.getValidPlatforms().size()];
        List<Platform> validPlatforms = Platform.getValidPlatforms();

        for (int i = 0; i < validPlatforms.size(); i++) {
            items[i] = validPlatforms.get(i).prettyName();
        }

        builder.setItems(items, (dialog, which) -> {
            listener.onRegionClick(this, validPlatforms.get(which));
            dialog.dismiss();
        });

        return builder.create();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (RegionDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

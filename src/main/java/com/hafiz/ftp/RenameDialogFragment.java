package com.hafiz.ftp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Hafiz on 10/25/2015.
 */
public class RenameDialogFragment extends DialogFragment {

    public RenameDialogListener mListener = null;

    // Purpose of this interface is to make a way to give control to host activity
    public interface RenameDialogListener {
        void onDialogPositiveClick(RenameDialogFragment dialog, String filename, EditText rename);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Bundle args = getArguments();
        final String filename = args.getString("filename");

        View view = inflater.inflate(R.layout.dialog_rename, null);
        final EditText rename = (EditText)view.findViewById(R.id.rename);
        rename.setText(filename);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(RenameDialogFragment.this, filename, rename);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RenameDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}

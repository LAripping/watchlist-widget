package com.laripping.watchlistwidget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class URLDialog extends DialogFragment {
    private static final String TAG = "URLDialog";
    private OnCompleteListener mListener;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate View from XML and get EditText reference
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.dialog_url, null);
        EditText urlEditText = (EditText) inflatedView.findViewById(R.id.url);

        // Configure the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflatedView);
        builder.setTitle(R.string.dialog_header);

        builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor editor = getContext()
                                .getSharedPreferences(AppState.PREF_FILE_NAME, Context.MODE_PRIVATE)
                                .edit();
                        editor.putString(AppState.PREF_LIST_KEY, urlEditText.getText().toString());
                        editor.apply();
                        new ImdbListTask(getContext(),mListener).execute(ImdbListTask.TASK_CODE_INIT);
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
//        builder.setCancelable(false);                 THIS DEFINES THE BACK BUTTON BEHAVIOR
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);    //  THIS DEFINES THE OUTTOUCH BEHAVIOR
        return dialog;
    }


    /**
     * The Interface that MainActivity will implement, to pass back a signal from the dialog
     */
    public static interface OnCompleteListener {
        public abstract void onComplete(boolean success);
    }

    /**
     * Make sure MainActivity has implemented the interface, to receive the dialog signal
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnCompleteListener)context;
        } catch(ClassCastException e){
            Log.e(TAG, "Looks like the activity hasn't implemented the interface!");
        }
    }
}


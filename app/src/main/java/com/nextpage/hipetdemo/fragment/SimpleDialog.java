package com.nextpage.hipetdemo.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.nextpage.hipetdemo.R;

/**
 * Created by jacobsFactory on 2017-11-29.
 */

public class SimpleDialog extends DialogFragment {

    private SimpleDialogListener simpleDialogListener;
    private String msg;

    public static SimpleDialog newInstance(String msg, SimpleDialogListener listener) {
        SimpleDialog simpleDialog = new SimpleDialog();
        simpleDialog.setSimpleDialogListener(listener);
        simpleDialog.setMsg(msg);
        return simpleDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_missing_report)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    simpleDialogListener.onDialogPositiveClick();
                    dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    simpleDialogListener.onDialogNegativeClick();
                    dismiss();
                });
        return builder.create();
    }

    public void setSimpleDialogListener(SimpleDialogListener simpleDialogListener) {
        this.simpleDialogListener = simpleDialogListener;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public interface SimpleDialogListener {
        void onDialogPositiveClick();

        void onDialogNegativeClick();
    }

}
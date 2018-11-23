package com.folioreader.util;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;
import com.folioreader.R;

public class ProgressDialog {

    public static Dialog show(Context ctx, String text) {
        final Dialog dialog = new Dialog(ctx, R.style.full_screen_dialog);
        dialog.setContentView(R.layout.progress_dialog);
        ((TextView) dialog.findViewById(R.id.label_loading)).setText(text);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }
}

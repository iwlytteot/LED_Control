package com.example.ledcontrol.utils;

import android.content.Context;
import android.content.DialogInterface;

import com.example.ledcontrol.R;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

public class ColorPickerBuilder {
    public static ColorPickerDialog.Builder create(Context context, String title, String preferenceName) {
        return new ColorPickerDialog.Builder(context)
                .setTitle(title)
                .setPreferenceName(preferenceName)
                .setNegativeButton(context.getString(R.string.cancel),
                        (dialogInterface, i) -> dialogInterface.dismiss())
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(false)
                .setBottomSpace(12);
    }
}

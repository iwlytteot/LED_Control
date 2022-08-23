package com.example.ledcontrol.model;

import android.content.Context;

import com.example.ledcontrol.R;
import com.skydoves.colorpickerview.AlphaTileView;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.flag.FlagView;

public class CustomFlag extends FlagView {

    private final AlphaTileView alphaTileView;

    public CustomFlag(Context context, int layout) {
        super(context, layout);
        alphaTileView = findViewById(R.id.flag_color_layout);
    }

    @Override
    public void onRefresh(ColorEnvelope colorEnvelope) {
        alphaTileView.setPaintColor(colorEnvelope.getColor());
    }
}
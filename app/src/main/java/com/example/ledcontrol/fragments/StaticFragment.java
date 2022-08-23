package com.example.ledcontrol.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.ledcontrol.model.CustomFlag;
import com.example.ledcontrol.R;
import com.example.ledcontrol.databinding.FragmentStaticBinding;
import com.example.ledcontrol.utils.ColorPickerBuilder;
import com.example.ledcontrol.utils.TCPClient;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StaticFragment extends Fragment {
    private FragmentStaticBinding binding;
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStaticBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Get shared variables in internal memory of app
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        ColorPickerPreferenceManager manager = ColorPickerPreferenceManager.getInstance(getContext());

        //Create color picker and set its values
        ColorPickerDialog.Builder builder = ColorPickerBuilder.create(getContext(), "Pick a color", "staticPicker");
        builder.setPositiveButton(getString(R.string.ok),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                binding.buttonPreview.setBackgroundColor(envelope.getColor());
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        ColorPickerView colorPickerView = builder.getColorPickerView();
        colorPickerView.setLifecycleOwner(getActivity());
        colorPickerView.setPreferenceName("staticPicker");
        colorPickerView.setFlagView(new CustomFlag(getContext(), R.layout.color_picker_box));

        AlertDialog dialog = builder.create();

        //Take care of situation when user picks a color, but leaves fragment without activation,
        //then comes back and activates without choosing a color
        if (manager.getColor("staticPicker", Color.RED) != sharedPref.getInt("static_picker", Color.RED)) {
            manager.setColor("staticPicker", sharedPref.getInt("static_picker", Color.RED));
        }

        binding.buttonPreview.setBackgroundColor(sharedPref.getInt("static_picker", Color.RED));
        binding.intensitySlider.setTrackActiveTintList(ColorStateList.valueOf(Color.rgb(65, 153, 251)));
        binding.intensitySlider.setValue(sharedPref.getFloat("static_intensity", 75));

        binding.buttonPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        //Send data via TCP and save values
        binding.buttonActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = sharedPref.getString(getString(R.string.ip_address), "");
                String port = sharedPref.getString(getString(R.string.port), "");

                try {
                    if (ipAddress.isEmpty() || port.isEmpty()) {
                        throw new IllegalArgumentException("Missing values for connection.");
                    }

                    StringBuilder builder = new StringBuilder()
                            .append(Color.red(manager.getColor("staticPicker", Color.RED)))
                            .append(".")
                            .append(Color.green(manager.getColor("staticPicker", Color.RED)))
                            .append(".")
                            .append(Color.blue(manager.getColor("staticPicker", Color.RED)))
                            .append(".")
                            .append("0.0.0.0.0.")
                            .append(Math.round(binding.intensitySlider.getValue()))
                            .append(".\n");

                    Callable<Void> thread = new TCPClient(ipAddress, port, builder.toString());
                    executorService.submit(thread).get();

                    editor.putFloat("static_intensity", binding.intensitySlider.getValue());
                    editor.putInt("static_picker", manager.getColor("staticPicker", Color.RED));
                    editor.apply();

                    //Notify user about successful transmission
                    Snackbar snackbar = Snackbar.make(binding.buttonActivate,
                            R.string.activate_success, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.rgb(118, 248, 140));
                    snackbar.show();

                } catch (IllegalArgumentException | ExecutionException | InterruptedException ex) {
                    //Notify user about unsuccessful transmission and append error message
                    Snackbar snackbar = Snackbar.make(binding.buttonActivate,
                            getString(R.string.activate_failed) + "\n" + ex.getMessage(), Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.rgb(255, 0, 0));
                    snackbar.show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

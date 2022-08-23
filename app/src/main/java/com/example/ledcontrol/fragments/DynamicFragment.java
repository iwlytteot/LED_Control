package com.example.ledcontrol.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.ledcontrol.R;
import com.example.ledcontrol.databinding.FragmentDynamicBinding;
import com.example.ledcontrol.model.CustomFlag;
import com.example.ledcontrol.utils.ColorPickerBuilder;
import com.example.ledcontrol.utils.TCPClient;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DynamicFragment extends Fragment {
    private FragmentDynamicBinding binding;
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDynamicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        ColorPickerPreferenceManager manager = ColorPickerPreferenceManager.getInstance(getContext());

        //Create color pickers and set its values
        ColorPickerDialog.Builder builder = ColorPickerBuilder.create(getContext(), "Pick a color", "dynamicPickerFirst");
        builder.setPositiveButton(getString(R.string.ok),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                binding.buttonPickerLeft.setBackgroundColor(envelope.getColor());
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
        colorPickerView.setPreferenceName("dynamicPickerFirst");
        colorPickerView.setFlagView(new CustomFlag(getContext(), R.layout.color_picker_box));

        AlertDialog first_picker = builder.create();

        builder = ColorPickerBuilder.create(getContext(), "Pick a color", "dynamicPickerSecond");
        builder.setPositiveButton(getString(R.string.ok),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                binding.buttonPickerRight.setBackgroundColor(envelope.getColor());
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

        colorPickerView = builder.getColorPickerView();
        colorPickerView.setLifecycleOwner(getActivity());
        colorPickerView.setPreferenceName("dynamicPickerSecond");
        colorPickerView.setFlagView(new CustomFlag(getContext(), R.layout.color_picker_box));

        AlertDialog second_picker = builder.create();

        //Take care of situation when user picks a color, but leaves fragment without activation,
        //then comes back and activates without choosing a color
        if (manager.getColor("dynamicPickerFirst", Color.BLUE) != sharedPref.getInt("dynamicPickerFirst", Color.BLUE)) {
            manager.setColor("dynamicPickerFirst", sharedPref.getInt("dynamicPickerFirst", Color.BLUE));
        }
        if (manager.getColor("dynamicPickerSecond", Color.RED) != sharedPref.getInt("dynamicPickerSecond", Color.RED)) {
            manager.setColor("dynamicPickerSecond", sharedPref.getInt("dynamicPickerSecond", Color.RED));
        }

        binding.buttonPickerLeft.setBackgroundColor(manager.getColor("dynamicPickerFirst", Color.BLUE));
        binding.buttonPickerRight.setBackgroundColor(manager.getColor("dynamicPickerSecond", Color.RED));
        binding.speedSlider.setTrackActiveTintList(ColorStateList.valueOf(Color.rgb(65, 153, 251)));
        binding.speedSlider.setValue(sharedPref.getFloat("speed", 350));

        binding.buttonPickerLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                first_picker.show();
            }
        });
        binding.buttonPickerRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                second_picker.show();
            }
        });

        //Saving values and sending data via TCP
        binding.buttonActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = sharedPref.getString(getString(R.string.ip_address), "");
                String port = sharedPref.getString(getString(R.string.port), "");

                try {
                    if (ipAddress.isEmpty() || port.isEmpty()) {
                        throw new IllegalArgumentException("Missing values for connection.");
                    }

                    //Create required data (defined in https://github.com/iwlytteot/esp8266-rlc/blob/master/main.ino)
                    //for sending over TCP
                    StringBuilder builder = new StringBuilder()
                            .append(Color.red(manager.getColor("dynamicPickerFirst", Color.RED)))
                            .append(".")
                            .append(Color.green(manager.getColor("dynamicPickerFirst", Color.RED)))
                            .append(".")
                            .append(Color.blue(manager.getColor("dynamicPickerFirst", Color.RED)))
                            .append(".")
                            .append(Color.red(manager.getColor("dynamicPickerSecond", Color.RED)))
                            .append(".")
                            .append(Color.green(manager.getColor("dynamicPickerSecond", Color.RED)))
                            .append(".")
                            .append(Color.blue(manager.getColor("dynamicPickerSecond", Color.RED)))
                            .append(".1.")
                            .append(701 - Math.round(binding.speedSlider.getValue())) //speed value is converted to delay value, hence 701 minus value
                            .append(".0.\n");

                    Callable<Void> thread = new TCPClient(ipAddress, port, builder.toString());
                    executorService.submit(thread).get();

                    editor.putInt("dynamicPickerFirst", manager.getColor("dynamicPickerFirst", Color.BLUE));
                    editor.putInt("dynamicPickerSecond", manager.getColor("dynamicPickerSecond", Color.RED));
                    editor.putFloat("speed", binding.speedSlider.getValue());
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

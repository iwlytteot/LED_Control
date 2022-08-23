package com.example.ledcontrol.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ledcontrol.R;
import com.example.ledcontrol.databinding.FragmentSettingsBinding;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        binding.ipAddressTextField.setText(sharedPref.getString(getString(R.string.ip_address), ""));
        binding.portTextField.setText(sharedPref.getString(getString(R.string.port), ""));



        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.ip_address), binding.ipAddressTextField.getText().toString());
                editor.putString(getString(R.string.port), binding.portTextField.getText().toString());
                editor.apply();
                Snackbar snackbar = Snackbar.make(binding.buttonSave, R.string.saved, Snackbar.LENGTH_SHORT);
                snackbar.setBackgroundTint(Color.rgb(118, 248, 140));
                snackbar.show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

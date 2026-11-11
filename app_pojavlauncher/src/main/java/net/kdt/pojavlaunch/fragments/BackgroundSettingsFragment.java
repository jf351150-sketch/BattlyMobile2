package net.kdt.pojavlaunch.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.LauncherActivity;
import net.kdt.pojavlaunch.utils.BattlyBackgrounds;
import net.kdt.pojavlaunch.utils.BattlyPlusManager;

public class BackgroundSettingsFragment extends Fragment {
    public static final String TAG = "BackgroundSettingsFragment";

    private GridLayout mGrid;
    private TextView mPlusStatus;
    private final ActivityResultLauncher<String[]> mPickBackgroundLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onCustomBackgroundPicked);

    public BackgroundSettingsFragment() {
        super(R.layout.fragment_background_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mGrid = view.findViewById(R.id.background_settings_grid);
        mPlusStatus = view.findViewById(R.id.background_plus_status);
        bindPlusStatus();
        populateGrid();
        BattlyPlusManager.refreshAsync(requireContext(), plus -> {
            if (!isAdded()) {
                return;
            }
            bindPlusStatus();
            populateGrid();
        });
    }

    private void populateGrid() {
        BattlyBackgrounds.populateOptions(
                requireContext(),
                mGrid,
                requireActivity().findViewById(R.id.launcher_background),
                this::applyLauncherBackground,
                true,
                () -> {
                    mPickBackgroundLauncher.launch(new String[]{"image/*"});
                });
    }

    private void onCustomBackgroundPicked(@Nullable Uri uri) {
        if (uri == null || !isAdded()) {
            return;
        }
        try {
            BattlyBackgrounds.saveCustomBackground(requireContext(), uri);
            applyLauncherBackground();
            populateGrid();
            Toast.makeText(requireContext(), R.string.battly_background_applied, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Tools.showError(requireContext(), getString(R.string.battly_background_custom_failed), e);
        }
    }

    private void bindPlusStatus() {
        if (mPlusStatus == null) {
            return;
        }
        mPlusStatus.setText(R.string.nexus_background_free_desc);
        mPlusStatus.setTextColor(0xFF8BE7D4);
    }

    private void applyLauncherBackground() {
        if (requireActivity() instanceof LauncherActivity) {
            ((LauncherActivity) requireActivity()).applyLauncherBackground();
        } else {
            BattlyBackgrounds.applySelectedBackground(requireContext(), requireActivity().findViewById(R.id.launcher_background));
        }
    }
}

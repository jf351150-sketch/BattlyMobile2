package net.kdt.pojavlaunch.utils;

import android.content.Context;
import android.content.SharedPreferences;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

/**
 * NEXUS device profiles. Profiles adjust game render resolution and sustained
 * performance mode without changing the user's Java runtime, renderer or controls.
 */
public final class NexusPerformanceProfiles {
    public static final String PREF_PROFILE = "nexus_performance_profile";
    public static final String AUTO = "auto";
    public static final String LOW = "low";
    public static final String MEDIUM = "medium";
    public static final String HIGH = "high";

    private NexusPerformanceProfiles() { }

    public static String detect(Context context) {
        int ramMb = Tools.getTotalDeviceMemory(context);
        int cores = Runtime.getRuntime().availableProcessors();
        if (ramMb < 4096 || cores <= 4) return LOW;
        if (ramMb >= 8192 && cores >= 8) return HIGH;
        return MEDIUM;
    }

    public static String resolve(Context context, String requested) {
        if (requested == null || AUTO.equals(requested)) return detect(context);
        if (LOW.equals(requested) || MEDIUM.equals(requested) || HIGH.equals(requested)) {
            return requested;
        }
        return detect(context);
    }

    public static void apply(Context context, String requested) {
        String resolved = resolve(context, requested);
        int resolution;
        boolean sustained;
        if (LOW.equals(resolved)) {
            resolution = 50;
            sustained = true;
        } else if (HIGH.equals(resolved)) {
            resolution = 100;
            sustained = false;
        } else {
            resolution = 75;
            sustained = true;
        }

        SharedPreferences prefs = LauncherPreferences.DEFAULT_PREF;
        if (prefs == null) {
            prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        }
        prefs.edit()
                .putString(PREF_PROFILE, requested == null ? AUTO : requested)
                .putInt("resolutionRatio", resolution)
                .putBoolean("sustainedPerformance", sustained)
                .apply();
        LauncherPreferences.loadPreferences(context);
    }

    public static String describe(Context context, String requested) {
        String resolved = resolve(context, requested);
        if (LOW.equals(resolved)) return "Gama baja · resolución 50% · modo térmico";
        if (HIGH.equals(resolved)) return "Gama alta · resolución 100%";
        return "Gama media · resolución 75% · modo térmico";
    }
}

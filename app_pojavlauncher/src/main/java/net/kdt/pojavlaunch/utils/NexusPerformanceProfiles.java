package net.kdt.pojavlaunch.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;

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
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        int glEs = getGlEsVersion(context);
        if (ramMb < 4096 || cores <= 4 || glEs < 0x30000) return LOW;
        if (ramMb >= 8192 && cores >= 8 && glEs >= 0x30200) return HIGH;
        return MEDIUM;
    }

    private static int getGlEsVersion(Context context) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ConfigurationInfo info = manager == null ? null : manager.getDeviceConfigurationInfo();
            return info == null ? 0 : info.reqGlEsVersion;
        } catch (Exception ignored) {
            return 0;
        }
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
        int ramMb = Tools.getTotalDeviceMemory(context);
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        String tier;
        if (LOW.equals(resolved)) tier = "Bajo · resolución 50%";
        else if (HIGH.equals(resolved)) tier = "Alto · resolución 100%";
        else tier = "Medio · resolución 75%";
        return tier + " · " + (ramMb / 1024) + " GB RAM · " + cores + " núcleos";
    }
}

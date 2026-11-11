package net.kdt.pojavlaunch.customcontrols;

import net.kdt.pojavlaunch.LwjglGlfwKeycode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Imports the public Fold Craft Launcher controller repository.
 * Converts the basic keyboard-button subset into Nexus/Pojav v8 layouts.
 * Advanced FCL actions and direction widgets are intentionally not guessed.
 */
public final class FclControlsConverter {
    private static final String RAW = "https://raw.githubusercontent.com/FCL-Team/FCL-Controllers/main/";
    private FclControlsConverter() {}

    public static JSONArray fetchCatalog() throws Exception {
        String body = get(RAW + "index.json", 2 * 1024 * 1024);
        return new JSONArray(body);
    }

    public static JSONObject fetchLatest(String id) throws Exception {
        if (id == null || !id.matches("[a-fA-F0-9]{8}")) throw new IllegalArgumentException("Invalid FCL controller id");
        JSONObject version = new JSONObject(get(RAW + "repo_json/" + id + "/version.json", 256 * 1024));
        int code = version.optJSONObject("latest") != null
                ? version.optJSONObject("latest").optInt("versionCode", -1)
                : version.optInt("latestVersionCode", -1);
        if (code < 0) {
            JSONObject latest = version.optJSONObject("latestVersion");
            if (latest != null) code = latest.optInt("versionCode", -1);
        }
        if (code < 0) throw new Exception("FoldCraft controller has no published latest version");
        return new JSONObject(get(RAW + "repo_json/" + id + "/versions/" + code + ".json", 4 * 1024 * 1024));
    }

    public static CustomControls convert(JSONObject source) throws Exception {
        JSONArray groups = source.optJSONArray("viewGroups");
        if (groups == null) throw new Exception("Unsupported FoldCraft layout");
        CustomControls out = new CustomControls();
        out.version = 8;
        out.scaledAt = 100f;
        int converted = 0;
        for (int g = 0; g < groups.length(); g++) {
            JSONObject group = groups.optJSONObject(g);
            if (group == null || !"VISIBLE".equals(group.optString("visibility", "VISIBLE"))) continue;
            JSONObject viewData = group.optJSONObject("viewData");
            if (viewData == null) continue;
            JSONArray buttons = viewData.optJSONArray("buttonList");
            if (buttons == null) continue;
            for (int i = 0; i < buttons.length(); i++) {
                JSONObject button = buttons.optJSONObject(i);
                if (button == null) continue;
                JSONObject base = button.optJSONObject("baseInfo");
                JSONObject event = button.optJSONObject("event");
                if (base == null || event == null) continue;
                JSONObject press = event.optJSONObject("pressEvent");
                if (press == null || press.optJSONArray("outputKeycodes") == null
                        || press.optJSONArray("outputKeycodes").length() == 0) {
                    press = event.optJSONObject("clickEvent");
                }
                if (press == null) continue;
                JSONArray fclKeys = press.optJSONArray("outputKeycodes");
                if (fclKeys == null || fclKeys.length() == 0) continue;
                int[] keys = new int[Math.min(4, fclKeys.length())];
                int count = 0;
                for (int k = 0; k < fclKeys.length() && count < 4; k++) {
                    int mapped = mapLinuxKey(fclKeys.optInt(k, -1));
                    if (mapped != LwjglGlfwKeycode.GLFW_KEY_UNKNOWN) keys[count++] = mapped;
                }
                if (count == 0) continue;
                if (count != keys.length) {
                    int[] trimmed = new int[count];
                    System.arraycopy(keys, 0, trimmed, 0, count);
                    keys = trimmed;
                }
                int x = clamp(base.optInt("xPosition", 500), 0, 1000);
                int y = clamp(base.optInt("yPosition", 500), 0, 1000);
                ControlData data = new ControlData(
                        button.optString("text", "Key"), keys,
                        (x / 1000f) + " * ${screen_width}",
                        (y / 1000f) + " * ${screen_height}",
                        48f, 48f, false
                );
                String visibility = base.optString("visibilityType", "ALWAYS");
                data.displayInGame = !"MENU".equals(visibility);
                data.displayInMenu = !"IN_GAME".equals(visibility);
                data.opacity = 0.88f;
                data.bgColor = 0x990D1728;
                data.strokeColor = 0xFF58D8E8;
                data.strokeWidth = 1.5f;
                data.cornerRadius = 28f;
                out.mControlDataList.add(data);
                converted++;
            }
        }
        if (converted == 0) throw new Exception("This FoldCraft layout has no buttons supported by Nexus yet");
        return out;
    }

    private static int mapLinuxKey(int code) {
        if (code >= 16 && code <= 25) {
            int[] qwerty = {81, 87, 69, 82, 84, 89, 85, 73, 79, 80};
            return qwerty[code - 16];
        }
        if (code >= 30 && code <= 38) {
            int[] row = {65, 83, 68, 70, 71, 72, 74, 75, 76};
            return row[code - 30];
        }
        if (code >= 44 && code <= 50) {
            int[] row = {90, 88, 67, 86, 66, 78, 77};
            return row[code - 44];
        }
        if (code >= 2 && code <= 11) return code == 11 ? 48 : 49 + code - 2;
        switch (code) {
            case 1: return 256;
            case 14: return 259;
            case 15: return 258;
            case 28: return 257;
            case 42: return 340;
            case 54: return 344;
            case 29: return 341;
            case 97: return 345;
            case 56: return 342;
            case 100: return 346;
            case 57: return 32;
            case 12: return 45;
            case 13: return 61;
            case 26: return 91;
            case 27: return 93;
            case 39: return 59;
            case 40: return 39;
            case 41: return 96;
            case 43: return 92;
            case 51: return 44;
            case 52: return 46;
            case 53: return 47;
            case 103: return 265;
            case 108: return 264;
            case 105: return 263;
            case 106: return 262;
            case 102: return 268;
            case 107: return 269;
            case 110: return 260;
            case 111: return 261;
            case 59: return 290;
            case 60: return 291;
            case 61: return 292;
            case 62: return 293;
            case 63: return 294;
            case 64: return 295;
            case 65: return 296;
            case 66: return 297;
            case 67: return 298;
            case 68: return 299;
            case 87: return 300;
            case 88: return 301;
            default: return LwjglGlfwKeycode.GLFW_KEY_UNKNOWN;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String get(String address, int maxBytes) throws Exception {
        URL url = new URL(address);
        if (!"raw.githubusercontent.com".equals(url.getHost()) || !"https".equals(url.getProtocol())) {
            throw new SecurityException("Untrusted controller source");
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("User-Agent", "NexusLauncher-Controls");
        try {
            int status = connection.getResponseCode();
            if (status != 200) throw new Exception("FoldCraft repository HTTP " + status);
            try (InputStream input = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append('\n');
                    if (result.length() > maxBytes) throw new Exception("Controller file exceeds size limit");
                }
                return result.toString();
            }
        } finally {
            connection.disconnect();
        }
    }
}
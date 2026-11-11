package net.kdt.pojavlaunch.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Spotify Authorization Code + PKCE flow for the NEXUS Android launcher.
 * No client secret is embedded in the APK. Tokens are encrypted with Android Keystore.
 */
public final class NexusSpotifyAuth {
    public static final String REDIRECT_URI = "com.tecnobros.battlylauncher://callback";
    private static final String CLIENT_ID = "4fe2ba20507d4b8d8406c43a4ce8e860";
    private static final String PREFS = "nexus_spotify_auth";
    private static final String KEY_VERIFIER = "pkce_verifier";
    private static final String KEY_STATE = "oauth_state";
    private static final String KEY_TOKEN = "encrypted_token";
    private static final String KEYSTORE_ALIAS = "nexus_spotify_token_key";
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS).build();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private NexusSpotifyAuth() {}

    public static void start(Activity activity) {
        try {
            String verifier = randomUrlSafe(64);
            String state = randomUrlSafe(32);
            String challenge = Base64.encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)),
                    Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString(KEY_VERIFIER, verifier).putString(KEY_STATE, state).apply();

            Uri authUri = Uri.parse("https://accounts.spotify.com/authorize").buildUpon()
                    .appendQueryParameter("client_id", CLIENT_ID)
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("redirect_uri", REDIRECT_URI)
                    .appendQueryParameter("code_challenge_method", "S256")
                    .appendQueryParameter("code_challenge", challenge)
                    .appendQueryParameter("state", state)
                    .appendQueryParameter("scope", "user-read-private user-read-email")
                    .build();
            activity.startActivity(new Intent(Intent.ACTION_VIEW, authUri));
        } catch (Exception e) {
            Toast.makeText(activity, "No se pudo iniciar Spotify: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /** Returns true when the intent is the NEXUS Spotify callback. */
    public static boolean handleCallback(Context context, Intent intent) {
        if (intent == null || intent.getData() == null
                || !Intent.ACTION_VIEW.equals(intent.getAction())) return false;
        Uri uri = intent.getData();
        if (!"com.tecnobros.battlylauncher".equals(uri.getScheme())
                || !"callback".equals(uri.getHost())) return false;

        String error = uri.getQueryParameter("error");
        String code = uri.getQueryParameter("code");
        String returnedState = uri.getQueryParameter("state");
        android.content.SharedPreferences prefs =
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String expectedState = prefs.getString(KEY_STATE, null);
        String verifier = prefs.getString(KEY_VERIFIER, null);
        prefs.edit().remove(KEY_STATE).remove(KEY_VERIFIER).apply();
        intent.setData(null);

        if (error != null) {
            notify(context, "Spotify canceló la autorización: " + error);
            return true;
        }
        if (code == null || verifier == null || expectedState == null
                || returnedState == null || !constantTimeEquals(expectedState, returnedState)) {
            notify(context, "Respuesta de Spotify no válida. Intenta conectar de nuevo.");
            return true;
        }
        exchangeCode(context.getApplicationContext(), code, verifier);
        return true;
    }

    private static void exchangeCode(Context context, String code, String verifier) {
        FormBody body = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .add("code_verifier", verifier)
                .build();
        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token").post(body).build();
        HTTP.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                notify(context, "No se pudo conectar con Spotify. Revisa tu internet.");
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try (Response res = response) {
                    if (!res.isSuccessful() || res.body() == null) {
                        notify(context, "Spotify rechazó la conexión (HTTP " + res.code() + ").");
                        return;
                    }
                    JSONObject json = new JSONObject(res.body().string());
                    String token = json.getString("access_token");
                    String refresh = json.optString("refresh_token", "");
                    long expiresAt = System.currentTimeMillis()
                            + json.optLong("expires_in", 3600) * 1000L;
                    JSONObject stored = new JSONObject();
                    stored.put("access_token", token);
                    stored.put("refresh_token", refresh);
                    stored.put("expires_at", expiresAt);
                    String encrypted = encrypt(stored.toString());
                    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                            .putString(KEY_TOKEN, encrypted).apply();
                    notify(context, "Spotify conectado a NEXUS.");
                } catch (Exception e) {
                    notify(context, "No se pudo guardar la autorización de Spotify.");
                }
            }
        });
    }

    private static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] iv = cipher.getIV();
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (keyStore.containsAlias(KEYSTORE_ALIAS))
            return ((SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null));
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build());
        return generator.generateKey();
    }

    private static String randomUrlSafe(int bytes) {
        byte[] value = new byte[bytes];
        RANDOM.nextBytes(value);
        return Base64.encodeToString(value, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    private static void notify(Context context, String message) {
        MAIN.post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }
}

package com.primejarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_SPEECH = 101;
    private static final int REQ_MIC = 102;
    private TextView status, heard;
    private TextToSpeech tts;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean torchOn = false;
    private boolean pendingTorchState = false;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                Boolean flash = cameraManager.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (Boolean.TRUE.equals(flash)) { cameraId = id; break; }
            }
        } catch (Exception ignored) {}

        tts = new TextToSpeech(this, result -> { if (result == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("hi", "IN")); });
        buildScreen();
        say("Namaste! Main Prime JARVIS hoon. Mic button dabakar command boliye.");
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 36, 28, 28); root.setGravity(Gravity.CENTER_HORIZONTAL); root.setBackgroundColor(0xFF0B1220);
        TextView title = new TextView(this); title.setText("PRIME JARVIS"); title.setTextSize(28); title.setTextColor(0xFFFFFFFF); title.setGravity(Gravity.CENTER); title.setPadding(0, 20, 0, 8); root.addView(title, match());
        TextView subtitle = new TextView(this); subtitle.setText("Hindi + English Voice Assistant"); subtitle.setTextSize(15); subtitle.setTextColor(0xFFB7C4D8); subtitle.setGravity(Gravity.CENTER); root.addView(subtitle, match());
        status = new TextView(this); status.setText("Ready • Taiyaar hoon"); status.setTextSize(17); status.setTextColor(0xFF7DD3FC); status.setGravity(Gravity.CENTER); status.setPadding(0, 28, 0, 12); root.addView(status, match());
        Button mic = button("🎙  TAP TO SPEAK / बोलें"); mic.setOnClickListener(v -> listen()); root.addView(mic, match());
        heard = new TextView(this); heard.setText("Your command will appear here.\nAapki command yahan dikhegi."); heard.setTextColor(0xFFFFFFFF); heard.setTextSize(16); heard.setPadding(12, 20, 12, 24); heard.setGravity(Gravity.CENTER); root.addView(heard, match());
        addAction(root, "Torch On / टॉर्च चालू", () -> setTorch(true));
        addAction(root, "Torch Off / टॉर्च बंद", () -> setTorch(false));
        addAction(root, "Open YouTube", () -> openApp("com.google.android.youtube", "YouTube"));
        addAction(root, "Open WhatsApp", () -> openApp("com.whatsapp", "WhatsApp"));
        addAction(root, "Open Settings", () -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
        addAction(root, "Open Wi-Fi Settings", () -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        TextView help = new TextView(this); help.setText("Try: ‘YouTube kholo’, ‘WhatsApp kholo’, ‘Torch on’, ‘Torch off’, ‘Settings kholo’, ‘Wi-Fi settings’, ‘kitne baje hain?’\n\nNote: Voice recognition may use Google speech services and internet. This starter app does not continuously listen for ‘Hey Jarvis’ in the background."); help.setTextColor(0xFFB7C4D8); help.setTextSize(13); help.setPadding(8, 24, 8, 20); root.addView(help, match());
        scroll.addView(root); setContentView(scroll);
    }
    private LinearLayout.LayoutParams match() { return new LinearLayout.LayoutParams(-1, -2); }
    private Button button(String text) { Button b = new Button(this); b.setText(text); b.setAllCaps(false); return b; }
    private void addAction(LinearLayout root, String label, Runnable action) { Button b = button(label); b.setOnClickListener(v -> action.run()); root.addView(b, match()); }

    private void listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_MIC); return;
        }
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Command boliye / Speak a command");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            startActivityForResult(intent, REQ_SPEECH);
        } catch (ActivityNotFoundException e) {
            status.setText("Speech service not available"); say("Speech recognition service available nahi hai.");
        }
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_MIC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) listen();
        else if (requestCode == REQ_MIC) Toast.makeText(this, "Microphone permission is needed for voice commands.", Toast.LENGTH_LONG).show();
        else if (requestCode == 103 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) applyTorch(pendingTorchState);
        else if (requestCode == 103) Toast.makeText(this, "Camera permission is needed to control the flashlight.", Toast.LENGTH_LONG).show();
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) { String command = results.get(0); heard.setText("You said / Aapne kaha:\n" + command); handleCommand(command); }
        }
    }
    private void handleCommand(String raw) {
        String c = raw.toLowerCase(Locale.ROOT);
        if (containsAny(c, "torch on", "flashlight on", "टॉर्च चालू", "torch chalu", "flashlight chalu", "light on")) setTorch(true);
        else if (containsAny(c, "torch off", "flashlight off", "टॉर्च बंद", "torch band", "light off")) setTorch(false);
        else if (containsAny(c, "youtube", "यूट्यूब")) openApp("com.google.android.youtube", "YouTube");
        else if (containsAny(c, "whatsapp", "व्हाट्सएप")) openApp("com.whatsapp", "WhatsApp");
        else if (containsAny(c, "chrome", "browser", "ब्राउज़र")) openApp("com.android.chrome", "Chrome");
        else if (containsAny(c, "wifi settings", "wi-fi settings", "वाईफाई सेटिंग", "वाई फाई सेटिंग")) { say("Wi-Fi settings khol raha hoon."); startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); }
        else if (containsAny(c, "settings", "सेटिंग")) { say("Settings khol raha hoon."); startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        else if (containsAny(c, "time", "kitne baje", "समय", "टाइम")) { String time = new java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(new java.util.Date()); status.setText("Time: " + time); say("Abhi samay hai " + time); }
        else if (containsAny(c, "hello", "namaste", "नमस्ते", "jarvis")) { status.setText("Listening complete • Ready"); say("Namaste! Batayein, kya karna hai?"); }
        else { status.setText("Command not recognized"); say("Maaf kijiye, ye command abhi nahi aati. YouTube, WhatsApp, torch ya settings bolkar dekhein."); }
    }
    private boolean containsAny(String s, String... words) { for (String w : words) if (s.contains(w)) return true; return false; }
    private void openApp(String packageName, String label) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch != null) { status.setText("Opening " + label + "..."); say(label + " khol raha hoon."); startActivity(launch); }
        else { status.setText(label + " not installed"); say(label + " install nahi hai ya phone par available nahi hai."); }
    }
    private void setTorch(boolean on) {
        if (cameraId == null) { say("Is phone par flashlight control available nahi hai."); return; }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            pendingTorchState = on;
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 103);
            return;
        }
        applyTorch(on);
    }
    private void applyTorch(boolean on) {
        try { cameraManager.setTorchMode(cameraId, on); torchOn = on; status.setText(on ? "Torch is ON" : "Torch is OFF"); say(on ? "Torch chalu kar di." : "Torch band kar di."); }
        catch (CameraAccessException | SecurityException e) { say("Torch abhi control nahi ho pa rahi. Camera use karne wali app band karke dobara try karein."); }
    }
    private void say(String text) { if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "prime-jarvis"); }
    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}

package com.example.starcatcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView highScoreText;
    private MediaPlayer menuMusic;
    private boolean isSoundOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        highScoreText = findViewById(R.id.highScoreText);
        ImageButton btnSoundToggle = findViewById(R.id.btnSoundToggle);
        View startButton = findViewById(R.id.startButton);

        initMenuMusic();
        loadHighScore();

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("soundEnabled", isSoundOn);
            startActivity(intent);
        });

        btnSoundToggle.setOnClickListener(v -> {
            isSoundOn = !isSoundOn;
            updateSoundIcon(btnSoundToggle);
            if (menuMusic != null) {
                if (isSoundOn) {
                    menuMusic.start();
                } else {
                    menuMusic.pause();
                }
            }
            saveSoundPreference();
        });

        updateSoundIcon(btnSoundToggle);
    }

    private void initMenuMusic() {
        menuMusic = MediaPlayer.create(this, R.raw.happy_music);
        menuMusic.setLooping(true);
        menuMusic.setVolume(isSoundOn ? 0.3f : 0f, isSoundOn ? 0.3f : 0f);
        if (isSoundOn) {
            menuMusic.start();
        }
    }

    private void loadHighScore() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("HighScore", 0);
        highScoreText.setText("Рекорд: " + highScore);

        // Загружаем настройку звука
        isSoundOn = prefs.getBoolean("soundEnabled", true);
    }

    private void saveSoundPreference() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("soundEnabled", isSoundOn).apply();
    }

    private void updateSoundIcon(ImageButton btn) {
        btn.setImageResource(isSoundOn ? R.drawable.ic_volume_on : R.drawable.ic_volume_off);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (menuMusic != null && !menuMusic.isPlaying() && isSoundOn) {
            menuMusic.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (menuMusic != null) {
            menuMusic.release();
            menuMusic = null;
        }
    }
}
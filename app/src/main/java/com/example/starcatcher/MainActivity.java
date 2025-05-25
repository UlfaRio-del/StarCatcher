package com.example.starcatcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MediaPlayer menuMusic;
    private boolean isSoundOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            initViews();
            loadSettings();
            initMenuMusic();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error", e);
            Toast.makeText(this, "Ошибка инициализации", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        try {
            TextView highScoreText = findViewById(R.id.highScoreText);
            ImageButton btnSoundToggle = findViewById(R.id.btnSoundToggle);
            Button startButton = findViewById(R.id.startButton);

            // Загрузка рекорда
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            int highScore = prefs.getInt("HighScore", 0);
            highScoreText.setText("Рекорд: " + highScore);

            // Кнопка звука
            btnSoundToggle.setOnClickListener(v -> toggleSound());

            // Кнопка старта
            startButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    intent.putExtra("soundEnabled", isSoundOn);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Start game error", e);
                    Toast.makeText(this, "Ошибка запуска игры", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Views init error", e);
        }
    }

    private void loadSettings() {
        try {
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            isSoundOn = prefs.getBoolean("soundEnabled", true);
        } catch (Exception e) {
            Log.e(TAG, "Load settings error", e);
        }
    }

    private void initMenuMusic() {
        try {
            menuMusic = MediaPlayer.create(this, R.raw.game_music);
            menuMusic.setLooping(true);
            menuMusic.setVolume(isSoundOn ? 0.3f : 0f, isSoundOn ? 0.3f : 0f);
            if (isSoundOn) {
                menuMusic.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Menu music error", e);
        }
    }

    private void toggleSound() {
        try {
            isSoundOn = !isSoundOn;
            ImageButton btn = findViewById(R.id.btnSoundToggle);
            btn.setImageResource(isSoundOn ? R.drawable.ic_volume_on : R.drawable.ic_volume_off);

            if (menuMusic != null) {
                if (isSoundOn) {
                    menuMusic.start();
                } else {
                    menuMusic.pause();
                }
            }

            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("soundEnabled", isSoundOn).apply();
        } catch (Exception e) {
            Log.e(TAG, "Toggle sound error", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (menuMusic != null && menuMusic.isPlaying()) {
                menuMusic.pause();
            }
        } catch (Exception e) {
            Log.e(TAG, "Pause error", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (menuMusic != null && !menuMusic.isPlaying() && isSoundOn) {
                menuMusic.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Resume error", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (menuMusic != null) {
                menuMusic.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "Destroy error", e);
        }
    }
}
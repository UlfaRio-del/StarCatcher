package com.example.starcatcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "GameActivity";
    private int score = 0;
    private int timeLeft = 30;
    private Random random = new Random();
    private Handler handler = new Handler();
    private SoundPool soundPool;
    private int soundId;
    private boolean isGameActive = true;
    private MediaPlayer backgroundMusic;
    private RelativeLayout gameLayout;
    private boolean isSoundOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            isSoundOn = getIntent().getBooleanExtra("soundEnabled", true);
            gameLayout = findViewById(R.id.gameLayout);

            initSoundPool();
            initBackgroundMusic();
            startTimer();
            spawnStar();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error", e);
            Toast.makeText(this, "Ошибка инициализации игры", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initSoundPool() {
        try {
            if (soundPool != null) {
                soundPool.release();
            }

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(attributes)
                    .build();

            soundId = soundPool.load(this, R.raw.normal_sound, 1);
        } catch (Exception e) {
            Log.e(TAG, "SoundPool init failed", e);
        }
    }

    private void initBackgroundMusic() {
        try {
            backgroundMusic = MediaPlayer.create(this, R.raw.game_music);
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(isSoundOn ? 0.3f : 0f, isSoundOn ? 0.3f : 0f);
            if (isSoundOn) {
                backgroundMusic.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Background music error", e);
        }
    }

    private void spawnStar() {
        if (!isGameActive || gameLayout == null) return;

        try {
            StarType type = StarType.getRandomType();
            ImageView star = new ImageView(this);
            star.setImageResource(type.drawableId);
            star.setTag(type);

            if (gameLayout.getWidth() == 0 || gameLayout.getHeight() == 0) {
                handler.postDelayed(this::spawnStar, 100);
                return;
            }

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(150, 150);
            params.leftMargin = random.nextInt(gameLayout.getWidth() - 200);
            params.topMargin = random.nextInt(gameLayout.getHeight() - 400);
            star.setLayoutParams(params);

            star.setOnClickListener(v -> {
                if (!isGameActive) return;
                handleStarClick((StarType) v.getTag(), v);
            });

            gameLayout.addView(star);
            handler.postDelayed(this::spawnStar, 1500);
        } catch (Exception e) {
            Log.e(TAG, "Star spawn error", e);
        }
    }

    private void handleStarClick(StarType type, View starView) {
        try {
            score += type.scoreValue;
            updateScoreText();
            if (isSoundOn) {
                soundPool.play(soundId, 1, 1, 0, 0, 1);
            }
            animateStar(starView);
        } catch (Exception e) {
            Log.e(TAG, "Star click error", e);
        }
    }

    private void updateScoreText() {
        try {
            TextView scoreText = findViewById(R.id.scoreText);
            if (scoreText != null) {
                scoreText.setText("Очки: " + score);
            }
        } catch (Exception e) {
            Log.e(TAG, "Score update error", e);
        }
    }

    private void animateStar(View starView) {
        try {
            starView.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .alpha(0)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            gameLayout.removeView(starView);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Star animation error", e);
        }
    }

    private void startTimer() {
        try {
            new CountDownTimer(30000, 1000) {
                public void onTick(long millisUntilFinished) {
                    TextView timerText = findViewById(R.id.timerText);
                    if (timerText != null) {
                        timerText.setText("Время: " + timeLeft);
                    }
                    timeLeft--;
                }

                public void onFinish() {
                    endGame();
                }
            }.start();
        } catch (Exception e) {
            Log.e(TAG, "Timer error", e);
        }
    }

    private void endGame() {
        isGameActive = false;
        saveHighScore();
        showGameOver();
    }

    private void saveHighScore() {
        try {
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            int highScore = prefs.getInt("HighScore", 0);

            if (score > highScore) {
                prefs.edit().putInt("HighScore", score).apply();
                Toast.makeText(this, "Новый рекорд: " + score, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Save highscore error", e);
        }
    }

    private void showGameOver() {
        try {
            // Можно добавить диалог с результатами
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Game over error", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
        } catch (Exception e) {
            Log.e(TAG, "Pause error", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (backgroundMusic != null) {
                backgroundMusic.release();
            }
            if (soundPool != null) {
                soundPool.release();
            }
            handler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            Log.e(TAG, "Destroy error", e);
        }
    }
}
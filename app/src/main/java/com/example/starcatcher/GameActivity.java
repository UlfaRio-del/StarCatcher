package com.example.starcatcher;

import static java.lang.StrictMath.random;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private int score = 0;
    private int timeLeft = 60;
    private Random random = new Random();
    private Handler handler = new Handler();
    private SoundPool soundPool;
    private int soundId;
    private boolean isGameActive = true;
    private MediaPlayer backgroundMusic;
    private boolean isSoundOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        isSoundOn = getIntent().getBooleanExtra("soundEnabled", true);
        initSoundPool();
        initBackgroundMusic();
        startTimer();
        spawnStar();
    }

    private void initBackgroundMusic() {
        backgroundMusic = MediaPlayer.create(this, R.raw.happy_music);
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(isSoundOn ? 0.5f : 0f, isSoundOn ? 0.5f : 0f);
        if (isSoundOn) {
            backgroundMusic.start();
        }
    }

    private void initSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build();

        soundId = soundPool.load(this, R.raw.star_sound, 1);
    }

    @SuppressLint("NewApi")
    private void spawnStar() {
        if (!isGameActive) return;

        ImageView star = new ImageView(this);
        star.setImageResource(R.drawable.star_icon);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(150, 150);
        params.leftMargin = random.nextInt(getResources().getDisplayMetrics().widthPixels - 200);
        params.topMargin = random.nextInt(getResources().getDisplayMetrics().heightPixels - 400);

        star.setLayoutParams(params);

        star.setOnClickListener(v -> {
            if (!isGameActive) return;

            score += 10;
            updateScoreText();
            if (isSoundOn) {
                playStarSound();
            }
            animateStar(v);
        });

        ((RelativeLayout) findViewById(R.id.gameLayout)).addView(star);
        handler.postDelayed(this::spawnStar, random.nextInt(100, 1750));
    }

    private void updateScoreText() {
        ((TextView) findViewById(R.id.scoreText)).setText("Очки: " + score);
    }

    private void playStarSound() {
        soundPool.play(soundId, 1, 1, 0, 0, 1);
    }

    private void animateStar(View starView) {
        starView.animate()
                .scaleX(0)
                .scaleY(0)
                .alpha(0)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ((RelativeLayout) findViewById(R.id.gameLayout)).removeView(starView);
                    }
                });
    }

    private void startTimer() {
        final TextView timerText = findViewById(R.id.timerText);
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Время: " + timeLeft);
                timeLeft--;
            }

            public void onFinish() {
                endGame();
            }
        }.start();
    }



    private void endGame() {
        isGameActive = false;
        checkHighScore();
        finish();
    }

    private void checkHighScore() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("HighScore", 0);

        if (score > highScore) {
            prefs.edit().putInt("HighScore", score).apply();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null && !backgroundMusic.isPlaying() && isSoundOn) {
            backgroundMusic.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
        soundPool.release();
        handler.removeCallbacksAndMessages(null);
    }
}
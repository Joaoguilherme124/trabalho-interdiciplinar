package com.example.trabalhointerdiciplinar.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.trabalhointerdiciplinar.R;

public class SplashActivity extends AppCompatActivity {

    private static final long FALLBACK_MS = 3500L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean navegou;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView animation = findViewById(R.id.splash_animation);
        animation.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                irParaLogin();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                irParaLogin();
            }
        });

        handler.postDelayed(this::irParaLogin, FALLBACK_MS);
    }

    private void irParaLogin() {
        if (navegou || isFinishing()) {
            return;
        }
        navegou = true;
        handler.removeCallbacksAndMessages(null);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}

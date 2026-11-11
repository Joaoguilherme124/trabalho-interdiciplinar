package com.example.trabalhointerdiciplinar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;

/**
 * Abertura de marca: mostra o logo e segue para o login. Quem já tem sessão é
 * encaminhado à sua área pelo próprio {@link LoginActivity}, que preserva essa decisão.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long ATRASO_MS = 1500L;

    private boolean navegou;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View raiz = findViewById(android.R.id.content);
        raiz.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                raiz.getViewTreeObserver().removeOnPreDrawListener(this);
                raiz.postDelayed(SplashActivity.this::seguir, ATRASO_MS);
                return true;
            }
        });
    }

    private void seguir() {
        if (navegou || isFinishing()) return;
        navegou = true;
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

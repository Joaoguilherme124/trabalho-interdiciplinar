package com.example.trabalhointerdiciplinar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;

/**
 * Abertura de marca. Não há temporizador nem animação: a tela aparece durante o início do
 * processo e a navegação segue assim que o primeiro quadro é desenhado. Quem já tem sessão é
 * encaminhado à sua área pelo próprio {@link LoginActivity}, que preserva essa decisão.
 */
public class SplashActivity extends AppCompatActivity {

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
                raiz.post(SplashActivity.this::seguir);
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

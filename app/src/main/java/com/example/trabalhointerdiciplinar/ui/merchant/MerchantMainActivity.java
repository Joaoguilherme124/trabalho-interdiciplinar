package com.example.trabalhointerdiciplinar.ui.merchant;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.trabalhointerdiciplinar.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MerchantMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_merchant_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_merchant), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_produtos) {
                fragment = new MerchantProdutosFragment();
            } else if (id == R.id.nav_pedidos_loja) {
                fragment = new MerchantPedidosFragment();
            } else if (id == R.id.nav_perfil) {
                fragment = new MerchantPerfilFragment();
            } else {
                fragment = new MerchantDashboardFragment();
            }
            bottomNav.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
            // "Cozinha" já vem marcada no menu e não dispara o listener: garante o fragmento inicial.
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MerchantDashboardFragment()).commit();
            }
        }
    }

    /** Atalho da Central da Cozinha para a Fila de pedidos. */
    public void abrirFila() {
        ((BottomNavigationView) findViewById(R.id.bottom_nav)).setSelectedItemId(R.id.nav_pedidos_loja);
    }
}

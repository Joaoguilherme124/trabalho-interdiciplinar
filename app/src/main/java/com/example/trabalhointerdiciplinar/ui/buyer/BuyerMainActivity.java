package com.example.trabalhointerdiciplinar.ui.buyer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.trabalhointerdiciplinar.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BuyerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buyer_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_buyer), (v, insets) -> {
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
            if (id == R.id.nav_categorias) {
                fragment = new CategoriasFragment();
            } else if (id == R.id.nav_carrinho) {
                fragment = new CarrinhoFragment();
            } else if (id == R.id.nav_pedidos) {
                fragment = new PedidosFragment();
            } else if (id == R.id.nav_perfil) {
                fragment = new PerfilFragment();
            } else {
                fragment = new HomeFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    public void openCarrinhoTab() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_carrinho);
    }
}

package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.trabalhointerdiciplinar.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BuyerMainActivity extends AppCompatActivity {

    /**
     * Extra opcional (id de destino) para abrir direto em uma aba. Também aceita
     * {@code R.id.nav_carrinho}, que abre a tela do Carrinho (fora da barra inferior).
     */
    public static final String EXTRA_ABRIR_ABA = "abrir_aba";
    private static final String TAG_CARRINHO = "carrinho";

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
            } else if (id == R.id.nav_pedidos) {
                fragment = new PedidosFragment();
            } else if (id == R.id.nav_perfil) {
                fragment = new PerfilFragment();
            } else {
                fragment = new HomeFragment();
            }
            fecharCarrinho();
            bottomNav.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
            androidx.fragment.app.FragmentTransaction tx = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment);
            if (com.example.trabalhointerdiciplinar.util.Animacoes.ativas()) {
                tx.setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            }
            tx.commit();
            return true;
        });
        // Tocar na aba já ativa (ex.: Cardápio com o Carrinho aberto) volta ao conteúdo da aba.
        bottomNav.setOnItemReselectedListener(item -> {
            fecharCarrinho();
            // Segundo toque na aba ativa: só volta ao topo, sem recriar a tela nem os dados.
            Fragment atual = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (atual instanceof RolarAoTopo) ((RolarAoTopo) atual).rolarAoTopo();
        });

        if (savedInstanceState == null) {
            irPara(getIntent().getIntExtra(EXTRA_ABRIR_ABA, R.id.nav_home));
            // Início já vem marcado no menu: selecioná-lo de novo não dispara o listener.
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int destino = intent.getIntExtra(EXTRA_ABRIR_ABA, 0);
        if (destino != 0) irPara(destino);
    }

    /** Abre a tela do Carrinho (mantida; só saiu da barra inferior). */
    public void openCarrinhoTab() {
        irPara(R.id.nav_carrinho);
    }

    public void openTab(int menuId) {
        irPara(menuId);
    }

    private void irPara(int destino) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav == null) return;
        if (destino == R.id.nav_carrinho) {
            // O Carrinho nasce "dentro" do Cardápio: o Voltar retorna a ele.
            if (bottomNav.getSelectedItemId() != R.id.nav_categorias) {
                bottomNav.setSelectedItemId(R.id.nav_categorias);
            }
            abrirCarrinho();
        } else {
            bottomNav.setSelectedItemId(destino);
        }
    }

    private void abrirCarrinho() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.fragment_container) instanceof CarrinhoFragment) return;
        fm.beginTransaction()
                .replace(R.id.fragment_container, new CarrinhoFragment())
                .addToBackStack(TAG_CARRINHO)
                .commit();
    }

    private void fecharCarrinho() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}

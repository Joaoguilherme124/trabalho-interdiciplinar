package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;

/** Mapa ilustrativo do evento: cinco áreas tocáveis que abrem um painel de detalhes. */
public class MapaEventoActivity extends AppCompatActivity {
    private static final String ESTADO_ZONA = "zona";

    private static final int[] ZONAS = {R.id.zona_entrada, R.id.zona_alimentacao,
            R.id.zona_retirada, R.id.zona_palco, R.id.zona_banheiros};

    @IdRes
    private int zonaAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mapa_evento);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_mapa), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        for (int id : ZONAS) {
            findViewById(id).setOnClickListener(v -> selecionar(id));
        }
        findViewById(R.id.btn_zona_acao).setOnClickListener(v -> startActivity(
                EventoInfoActivity.novaIntent(this, SecaoEvento.PROGRAMACAO)));
        if (savedInstanceState != null) {
            selecionar(savedInstanceState.getInt(ESTADO_ZONA, 0));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_ZONA, zonaAtual);
    }

    private void selecionar(@IdRes int id) {
        zonaAtual = id;
        for (int zona : ZONAS) {
            findViewById(zona).setSelected(zona == id);
        }
        @StringRes int nome;
        @StringRes int descricao;
        if (id == R.id.zona_entrada) {
            nome = R.string.mapa_entrada_nome;
            descricao = R.string.mapa_entrada_desc;
        } else if (id == R.id.zona_alimentacao) {
            nome = R.string.mapa_alimentacao_nome;
            descricao = R.string.mapa_alimentacao_desc;
        } else if (id == R.id.zona_retirada) {
            nome = R.string.mapa_retirada_nome;
            descricao = R.string.mapa_retirada_desc;
        } else if (id == R.id.zona_palco) {
            nome = R.string.mapa_palco_nome;
            descricao = R.string.mapa_palco_desc;
        } else if (id == R.id.zona_banheiros) {
            nome = R.string.mapa_banheiros_nome;
            descricao = R.string.mapa_banheiros_desc;
        } else {
            return;
        }
        ((TextView) findViewById(R.id.txt_zona_titulo)).setText(nome);
        ((TextView) findViewById(R.id.txt_zona_desc)).setText(descricao);
        findViewById(R.id.btn_zona_acao).setVisibility(id == R.id.zona_palco ? View.VISIBLE : View.GONE);
    }
}

package com.example.trabalhointerdiciplinar.ui.buyer;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.util.Animacoes;

/**
 * Informações do evento: hero, data/local, propósito, ABADEUS, como participar e dúvidas.
 * Todo o conteúdo vem de recursos (strings_evento.xml) e é demonstrativo.
 */
public class EventoInfoActivity extends AppCompatActivity {
    public static final String EXTRA_SECAO = "secao";
    private static final long DURACAO_SETA_MS = 150L;

    private ScrollView scroll;

    /** Intent para abrir a tela, opcionalmente já rolada até uma seção. */
    public static Intent novaIntent(android.content.Context contexto, SecaoEvento secao) {
        Intent i = new Intent(contexto, EventoInfoActivity.class);
        i.putExtra(EXTRA_SECAO, secao.name());
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_evento_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_evento), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        scroll = findViewById(R.id.scroll_evento);
        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cardapio).setOnClickListener(v -> abrirCardapio());

        montarPassos();
        montarPerguntas();
        montarProgramacao();
        findViewById(R.id.btn_mapa).setOnClickListener(
                v -> startActivity(new Intent(this, MapaEventoActivity.class)));

        SecaoEvento secao = SecaoEvento.daString(getIntent().getStringExtra(EXTRA_SECAO));
        if (savedInstanceState == null) {
            Animacoes.entradaEmCascata(findViewById(R.id.evento_conteudo));
            rolarPara(secao);
        }
    }

    private void abrirCardapio() {
        Intent i = new Intent(this, BuyerMainActivity.class);
        i.putExtra(BuyerMainActivity.EXTRA_ABRIR_ABA, R.id.nav_categorias);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private void rolarPara(SecaoEvento secao) {
        final View alvo;
        switch (secao) {
            case ABADEUS:
                alvo = findViewById(R.id.secao_abadeus);
                break;
            case PROGRAMACAO:
                alvo = findViewById(R.id.secao_programacao);
                break;
            case DUVIDAS:
                alvo = findViewById(R.id.secao_duvidas);
                break;
            default:
                return;
        }
        // Após o primeiro layout, as posições das seções já existem.
        scroll.post(() -> {
            // getTop() é relativo ao pai: soma até o conteúdo do ScrollView.
            int y = 0;
            View v = alvo;
            while (v != null && v != scroll.getChildAt(0)) {
                y += v.getTop();
                v = v.getParent() instanceof View ? (View) v.getParent() : null;
            }
            scroll.smoothScrollTo(0, y);
        });
    }

    private void montarPassos() {
        LinearLayout lista = findViewById(R.id.lista_passos);
        String[] titulos = getResources().getStringArray(R.array.evento_passos_titulos);
        String[] descricoes = getResources().getStringArray(R.array.evento_passos_descricoes);
        TypedArray icones = getResources().obtainTypedArray(R.array.evento_passos_icones);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < titulos.length; i++) {
            View passo = inflater.inflate(R.layout.item_evento_passo, lista, false);
            ((TextView) passo.findViewById(R.id.passo_numero)).setText(String.format("%02d", i + 1));
            ((TextView) passo.findViewById(R.id.passo_titulo)).setText(titulos[i]);
            ((TextView) passo.findViewById(R.id.passo_descricao)).setText(descricoes[i]);
            ((ImageView) passo.findViewById(R.id.passo_icone))
                    .setImageResource(icones.getResourceId(i, 0));
            lista.addView(passo);
        }
        icones.recycle();
    }

    private void montarProgramacao() {
        LinearLayout lista = findViewById(R.id.lista_programacao);
        String[] titulos = getResources().getStringArray(R.array.evento_programacao_titulos);
        String[] descricoes = getResources().getStringArray(R.array.evento_programacao_descricoes);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < titulos.length; i++) {
            View bloco = inflater.inflate(R.layout.item_evento_programacao, lista, false);
            ((TextView) bloco.findViewById(R.id.prog_titulo)).setText(titulos[i]);
            ((TextView) bloco.findViewById(R.id.prog_descricao)).setText(descricoes[i]);
            lista.addView(bloco);
        }
    }

    private void montarPerguntas() {
        LinearLayout lista = findViewById(R.id.lista_faq);
        String[] perguntas = getResources().getStringArray(R.array.evento_faq_perguntas);
        String[] respostas = getResources().getStringArray(R.array.evento_faq_respostas);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < perguntas.length; i++) {
            View item = inflater.inflate(R.layout.item_evento_faq, lista, false);
            ((TextView) item.findViewById(R.id.faq_pergunta)).setText(perguntas[i]);
            TextView resposta = item.findViewById(R.id.faq_resposta);
            resposta.setText(respostas[i]);
            View seta = item.findViewById(R.id.faq_seta);
            View cabecalho = item.findViewById(R.id.faq_cabecalho);
            cabecalho.setOnClickListener(v -> alternar(resposta, seta));
            if (i == perguntas.length - 1) {
                item.findViewById(R.id.faq_divisor).setVisibility(View.GONE);
            }
            lista.addView(item);
        }
    }

    private void alternar(TextView resposta, View seta) {
        boolean abrir = resposta.getVisibility() != View.VISIBLE;
        resposta.setVisibility(abrir ? View.VISIBLE : View.GONE);
        float rotacao = abrir ? 180f : 0f;
        if (com.example.trabalhointerdiciplinar.util.Animacoes.ativas()) {
            seta.animate().rotation(rotacao).setDuration(DURACAO_SETA_MS).start();
        } else {
            seta.setRotation(rotacao);
        }
    }
}

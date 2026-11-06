package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.util.AcompanhamentoPedido;
import com.example.trabalhointerdiciplinar.util.AcompanhamentoPedido.Estado;
import com.example.trabalhointerdiciplinar.util.Animacoes;

/**
 * Cartão "Acompanhando seu pedido" do topo da Início: título, previsão e linha de progresso com
 * quatro etapas. Só desenha o que recebe de {@link AcompanhamentoPedido}; não guarda regra de
 * negócio nem dispara notificações.
 */
public class AcompanhamentoPedidoView extends FrameLayout {
    private static final int[] ICONES = {R.drawable.ic_etapa_check, R.drawable.ic_notif_paella,
            R.drawable.ic_home_cardapio, R.drawable.ic_home_sacola};
    private static final int[] FUNDOS = {R.drawable.bg_acomp_concluida, R.drawable.bg_acomp_atual,
            R.drawable.bg_acomp_futura};
    private static final int CONCLUIDA = 0;
    private static final int ATUAL = 1;
    private static final int FUTURA = 2;

    private final View[] etapas = new View[4];
    private final String[] rotulos;
    private final String[] frases;
    private View chip;
    private AcompanhamentoPedido exibido;
    private int etapaExibida = -1;
    private boolean chipNaColuna;

    public AcompanhamentoPedidoView(@NonNull Context context) {
        this(context, null);
    }

    public AcompanhamentoPedidoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_acompanhamento_pedido, this, true);
        rotulos = getResources().getStringArray(R.array.acomp_etapas);
        frases = getResources().getStringArray(R.array.acomp_frases);
        chip = findViewById(R.id.acomp_chip);
        LinearLayout linha = findViewById(R.id.acomp_linha_etapas);
        LayoutInflater inflater = LayoutInflater.from(context);
        for (int i = 0; i < etapas.length; i++) {
            etapas[i] = inflater.inflate(R.layout.item_acompanhamento_etapa, linha, false);
            ((ImageView) etapas[i].findViewById(R.id.etapa_icone)).setImageResource(ICONES[i]);
            linha.addView(etapas[i]);
        }
        // O cartão inteiro é o alvo de toque; as partes internas não são lidas uma a uma.
        findViewById(R.id.acomp_linha_etapas).setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        setVisibility(GONE);
    }

    public void aoTocarCartao(OnClickListener l) {
        findViewById(R.id.card_acompanhamento).setOnClickListener(l);
    }

    public void aoTocarQr(OnClickListener l) {
        findViewById(R.id.acomp_btn_qr).setOnClickListener(l);
    }

    /** Mostra, atualiza ou esconde o cartão. Só redesenha quando algo realmente mudou. */
    public void atualizar(@Nullable AcompanhamentoPedido a) {
        if (a == null) {
            esconder();
            return;
        }
        boolean novo = getVisibility() != VISIBLE;
        if (!novo && a.mesmoQue(exibido)) return;
        int etapaAnterior = etapaExibida;
        exibido = a;
        desenhar(a);
        etapaExibida = a.estado.etapa;
        if (novo) {
            setVisibility(VISIBLE);
            entrar();
        } else if (etapaAnterior >= 0 && etapaAnterior != etapaExibida) {
            destacarEtapaAtual(a.estado.etapa);
        }
    }

    private void esconder() {
        exibido = null;
        etapaExibida = -1;
        animate().cancel();
        setVisibility(GONE);
    }

    private void entrar() {
        animate().cancel();
        if (!Animacoes.ativas()) {
            setAlpha(1f);
            setTranslationY(0f);
            return;
        }
        setAlpha(0f);
        setTranslationY(10 * getResources().getDisplayMetrics().density);
        animate().alpha(1f).translationY(0f).setDuration(230).start();
    }

    private void destacarEtapaAtual(int etapa) {
        if (!Animacoes.ativas()) return;
        View c = etapas[etapa].findViewById(R.id.etapa_circulo);
        c.setScaleX(0.75f);
        c.setScaleY(0.75f);
        c.animate().scaleX(1.12f).scaleY(1.12f).setDuration(240).start();
    }

    private void desenhar(AcompanhamentoPedido a) {
        Context ctx = getContext();
        int atual = a.estado.etapa;
        String frase = frases[atual];

        TextView subtitulo = findViewById(R.id.acomp_subtitulo);
        boolean temTempo = a.minutos > 0;
        if (a.extras > 0) {
            subtitulo.setText(temTempo ? ctx.getString(R.string.acomp_subtitulo_mais, a.numero, a.extras, a.minutos)
                    : ctx.getString(R.string.acomp_subtitulo_mais_sem_tempo, a.numero, a.extras));
        } else {
            subtitulo.setText(temTempo ? ctx.getString(R.string.acomp_subtitulo_min, a.numero, a.minutos)
                    : ctx.getString(R.string.acomp_subtitulo, a.numero));
        }

        TextView chipTexto = findViewById(R.id.acomp_chip_texto);
        chipTexto.setText(a.pronto() ? ctx.getString(R.string.acomp_chip_pronto)
                : temTempo ? ctx.getString(R.string.acomp_chip_min, a.minutos)
                : ctx.getString(R.string.acomp_chip_sem_previsao));
        reorganizarSeApertado();

        ((TextView) findViewById(R.id.acomp_frase)).setText(frase);
        findViewById(R.id.acomp_btn_qr).setVisibility(a.pronto() ? VISIBLE : GONE);

        for (int i = 0; i < etapas.length; i++) {
            int estado = i < atual ? CONCLUIDA : (i == atual ? ATUAL : FUTURA);
            pintarEtapa(i, estado, a);
        }
        setContentDescription(descricao(a, frase));
    }

    private void pintarEtapa(int i, int estado, AcompanhamentoPedido a) {
        View item = etapas[i];
        Context ctx = getContext();
        View circulo = item.findViewById(R.id.etapa_circulo);
        ImageView icone = item.findViewById(R.id.etapa_icone);
        TextView label = item.findViewById(R.id.etapa_label);
        circulo.setBackgroundResource(FUNDOS[estado]);
        int corIcone = estado == CONCLUIDA ? R.color.brand_creme
                : estado == ATUAL ? R.color.brand_vinho : R.color.brand_grafite_muted;
        icone.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, corIcone)));
        boolean ultimaPronta = i == etapas.length - 1 && a.pronto();
        label.setText(ultimaPronta ? ctx.getString(R.string.acomp_etapa_pronta) : rotulos[i]);
        // Fonte ampliada: o rótulo cresce, mas com teto para a palavra não quebrar no meio.
        float escala = getResources().getConfiguration().fontScale;
        label.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, Math.min(11f * escala, 13f));
        label.setTextColor(ContextCompat.getColor(ctx,
                estado == FUTURA ? R.color.brand_grafite_muted : R.color.brand_vinho));
        label.setBackgroundResource(estado == ATUAL ? R.drawable.bg_acomp_label_atual : 0);
        label.setPadding(estado == ATUAL ? dp(4) : 0, estado == ATUAL ? dp(2) : 0,
                estado == ATUAL ? dp(4) : 0, estado == ATUAL ? dp(2) : 0);
        circulo.setScaleX(estado == ATUAL ? 1.12f : 1f);
        circulo.setScaleY(estado == ATUAL ? 1.12f : 1f);
        // Linhas: o trecho entre duas etapas fica cheio quando a anterior já foi concluída.
        View esq = item.findViewById(R.id.linha_esq);
        View dir = item.findViewById(R.id.linha_dir);
        esq.setVisibility(i == 0 ? INVISIBLE : VISIBLE);
        dir.setVisibility(i == etapas.length - 1 ? INVISIBLE : VISIBLE);
        esq.setBackgroundColor(corLinha(i <= a.estado.etapa));
        dir.setBackgroundColor(corLinha(i < a.estado.etapa));
    }

    private int corLinha(boolean cheia) {
        return ContextCompat.getColor(getContext(), cheia ? R.color.brand_vinho : R.color.brand_acafrao_apagado);
    }

    /** Fonte ampliada ou tela estreita: o selo de tempo desce para baixo do texto. */
    private void reorganizarSeApertado() {
        android.content.res.Configuration cfg = getResources().getConfiguration();
        boolean apertado = cfg.fontScale >= 1.3f || cfg.screenWidthDp < 360;
        ViewGroup cabecalho = findViewById(R.id.acomp_cabecalho);
        ViewGroup coluna = findViewById(R.id.acomp_coluna_texto);
        if (apertado && !chipNaColuna) {
            cabecalho.removeView(chip);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(6);
            coluna.addView(chip, lp);
            chipNaColuna = true;
        }
    }

    private String descricao(AcompanhamentoPedido a, String frase) {
        Context ctx = getContext();
        String etapa = a.pronto() ? ctx.getString(R.string.acomp_etapa_pronta) : rotulos[a.estado.etapa];
        String tempo = a.pronto() ? ctx.getString(R.string.acomp_cd_pronto)
                : a.minutos > 0 ? ctx.getString(R.string.acomp_cd_previsao, a.minutos)
                : ctx.getString(R.string.acomp_cd_sem_previsao);
        return ctx.getString(R.string.acomp_cd, a.numero.replace("#", ""), etapa,
                a.estado.etapa + 1, tempo);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}

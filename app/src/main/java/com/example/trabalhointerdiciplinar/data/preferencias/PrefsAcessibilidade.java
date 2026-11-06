package com.example.trabalhointerdiciplinar.data.preferencias;

import com.example.trabalhointerdiciplinar.data.simulacao.Armazenamento;

/** Preferências de acessibilidade do aplicativo, persistidas localmente. */
public final class PrefsAcessibilidade {
    public enum TamanhoTexto {
        PADRAO(1.0f), MAIOR(1.15f), MUITO_MAIOR(1.3f);

        public final float fator;

        TamanhoTexto(float fator) {
            this.fator = fator;
        }
    }

    private static final String K_TEXTO = "acess_texto";
    private static final String K_CONTRASTE = "acess_contraste";
    private static final String K_ANIMACOES = "acess_reduzir_animacoes";
    /** Escala máxima final: o ajuste do app complementa, e não ultrapassa, um limite razoável. */
    private static final float ESCALA_MAXIMA = 2.0f;

    private final Armazenamento armazenamento;

    public PrefsAcessibilidade(Armazenamento armazenamento) {
        this.armazenamento = armazenamento;
    }

    public TamanhoTexto tamanhoTexto() {
        String v = armazenamento.ler(K_TEXTO);
        for (TamanhoTexto t : TamanhoTexto.values()) {
            if (t.name().equals(v)) return t;
        }
        return TamanhoTexto.PADRAO;
    }

    public void definirTamanhoTexto(TamanhoTexto tamanho) {
        armazenamento.gravar(K_TEXTO, tamanho.name());
    }

    public boolean altoContraste() {
        return "1".equals(armazenamento.ler(K_CONTRASTE));
    }

    public void definirAltoContraste(boolean ativo) {
        armazenamento.gravar(K_CONTRASTE, ativo ? "1" : "0");
    }

    public boolean reduzirAnimacoes() {
        return "1".equals(armazenamento.ler(K_ANIMACOES));
    }

    public void definirReduzirAnimacoes(boolean ativo) {
        armazenamento.gravar(K_ANIMACOES, ativo ? "1" : "0");
    }

    /** Escala final de fonte: a do sistema multiplicada pelo ajuste do app (nunca reduz). */
    public float escalaFinal(float escalaDoSistema) {
        float base = escalaDoSistema <= 0 ? 1f : escalaDoSistema;
        return Math.min(ESCALA_MAXIMA, base * tamanhoTexto().fator);
    }
}

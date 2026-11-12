package com.example.trabalhointerdiciplinar.data;

import com.example.trabalhointerdiciplinar.model.Produto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DADOS DE DESENVOLVIMENTO. Nomes, descrições, preços e fotos abaixo são fictícios e existem
 * apenas para demonstrar o aplicativo; não correspondem ao cardápio oficial do evento.
 *
 * Para usar dados reais: substitua {@link #produtos()} (ou carregue de uma API) e ponha
 * {@link #AMBIENTE_DEMO} em false, o que também oculta os avisos de demonstração.
 *
 * O campo {@code urlImagem} do produto guarda o NOME de um drawable (ex.: "img_paella_valenciana").
 */
public final class DemoCatalog {

    /** Quando true, a interface avisa que preços, pedidos e pagamentos são simulados. */
    public static final boolean AMBIENTE_DEMO = false;

    public static final String LOJA_ID = "l1";
    public static final String LOJA_NOME = "Paella Beneficente ABADEUS";

    public static final String CAT_PAELLAS = "Paellas";
    public static final String CAT_COMBOS = "Combos";
    public static final String CAT_BEBIDAS = "Bebidas";

    private DemoCatalog() {
    }

    public static List<Produto> produtos() {
        return new ArrayList<>(Arrays.asList(
                item("p1", "Paella Mista",
                        "Arroz com frutos do mar e legumes, servida quente.",
                        42.00, "paella_hero", CAT_PAELLAS),
                item("p2", "Paella de Frutos do Mar",
                        "Arroz com camarão e ervas frescas, servida na paellera.",
                        48.00, "img_paella_frutos_do_mar", CAT_PAELLAS),
                item("p3", "Paella Valenciana",
                        "Receita tradicional preparada na panela.",
                        44.00, "img_paella_valenciana", CAT_PAELLAS),
                item("p4", "Combo Paella Mista + Suco",
                        "Uma Paella Mista acompanhada de suco de laranja.",
                        48.00, "img_combo_mista", CAT_COMBOS),
                item("p5", "Combo Frutos do Mar + Água",
                        "Uma Paella de Frutos do Mar com água com gás.",
                        52.00, "img_combo_frutos_do_mar", CAT_COMBOS),
                item("p6", "Suco de Laranja",
                        "Suco de laranja gelado.",
                        8.00, "img_bebida_suco", CAT_BEBIDAS),
                item("p7", "Água com Gás",
                        "Água com gás gelada, com limão.",
                        5.00, "img_bebida_agua", CAT_BEBIDAS)
        ));
    }

    private static Produto item(String id, String titulo, String descricao, double preco,
                                String imagem, String categoria) {
        return new Produto(id, LOJA_ID, LOJA_NOME, titulo, descricao, preco, 100, imagem, 0f,
                categoria);
    }
}

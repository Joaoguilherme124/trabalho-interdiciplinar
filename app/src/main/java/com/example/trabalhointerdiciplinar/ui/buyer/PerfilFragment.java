package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.ui.auth.LoginActivity;
import com.example.trabalhointerdiciplinar.util.Animacoes;
import com.example.trabalhointerdiciplinar.util.PerfilApresentacao;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Aba Perfil do comprador: identificação, atalhos da conta, evento e saída da conta. */
public class PerfilFragment extends Fragment {

    private View raiz;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_participante, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        raiz = view;
        view.findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);

        LinearLayout conta = view.findViewById(R.id.grupo_conta);
        adicionarOpcao(conta, R.drawable.ic_nav_orders, R.string.perfil_pedidos_titulo,
                R.string.perfil_pedidos_desc, false, () -> abrirAba(R.id.nav_pedidos));
        adicionarOpcao(conta, R.drawable.ic_nav_profile, R.string.perfil_dados_titulo,
                R.string.perfil_dados_desc, false, () -> startActivity(
                        new Intent(requireContext(), EditarPerfilActivity.class)));

        LinearLayout evento = view.findViewById(R.id.grupo_evento);
        adicionarOpcao(evento, R.drawable.ic_calendar, R.string.perfil_evento_titulo,
                R.string.perfil_evento_desc, false, () -> abrirEvento(SecaoEvento.TOPO));
        adicionarOpcao(evento, R.drawable.ic_heart, R.string.perfil_abadeus_titulo,
                R.string.perfil_abadeus_desc, false, () -> abrirEvento(SecaoEvento.ABADEUS));
        adicionarOpcao(evento, R.drawable.ic_help, R.string.perfil_ajuda_titulo,
                R.string.perfil_ajuda_desc, false, () -> abrirEvento(SecaoEvento.DUVIDAS));

        adicionarOpcao(evento, R.drawable.ic_acessibilidade, R.string.acess_titulo,
                R.string.acess_perfil_desc, false, () -> startActivity(
                        new Intent(requireContext(), AcessibilidadeActivity.class)));

        LinearLayout sair = view.findViewById(R.id.grupo_sair);
        View linhaSair = adicionarOpcao(sair, R.drawable.ic_logout, R.string.perfil_sair_titulo,
                0, false, this::confirmarSaida);
        ((TextView) linhaSair.findViewById(R.id.opcao_titulo)).setTextColor(
                ContextCompat.getColor(requireContext(), R.color.brand_paprica));
        ((ImageView) linhaSair.findViewById(R.id.opcao_icone)).setImageTintList(
                android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.brand_paprica)));
        linhaSair.findViewById(R.id.opcao_seta).setVisibility(View.GONE);
        linhaSair.findViewById(R.id.opcao_descricao).setVisibility(View.GONE);

        TextView versao = view.findViewById(R.id.txt_versao);
        versao.setText(getString(R.string.perfil_rodape_versao, versaoDoApp()));
        // Acesso discreto ao painel de demonstração: toque longo na versão.
        versao.setOnLongClickListener(v -> {
            startActivity(new Intent(requireContext(), SimulacaoPainelActivity.class));
            return true;
        });

        Animacoes.entradaEmCascata(view.findViewById(R.id.perfil_conteudo));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reflete edições feitas em "Meus dados".
        Usuario u = SessionManager.getInstance(requireContext()).getUsuario();
        if (u == null) return;
        ((TextView) raiz.findViewById(R.id.txt_nome)).setText(u.getNome());
        ((TextView) raiz.findViewById(R.id.txt_email)).setText(u.getEmail());
        TextView iniciais = raiz.findViewById(R.id.txt_iniciais);
        iniciais.setText(PerfilApresentacao.iniciais(u.getNome()));
        iniciais.setContentDescription(getString(R.string.cd_avatar, iniciais.getText()));
    }

    /** Infla uma linha de opção no grupo e esconde o divisor da última linha. */
    private View adicionarOpcao(LinearLayout grupo, @DrawableRes int icone, @StringRes int titulo,
                                @StringRes int descricao, boolean emBreve,
                                @Nullable Runnable aoTocar) {
        View linha = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_perfil_opcao, grupo, false);
        ((ImageView) linha.findViewById(R.id.opcao_icone)).setImageResource(icone);
        ((TextView) linha.findViewById(R.id.opcao_titulo)).setText(titulo);
        if (descricao != 0) {
            ((TextView) linha.findViewById(R.id.opcao_descricao)).setText(descricao);
        }

        View raizLinha = linha.findViewById(R.id.opcao_raiz);
        if (emBreve) {
            linha.findViewById(R.id.opcao_selo).setVisibility(View.VISIBLE);
            linha.findViewById(R.id.opcao_seta).setVisibility(View.GONE);
            raizLinha.setAlpha(0.6f);
            raizLinha.setOnClickListener(v -> Toast.makeText(requireContext(),
                    R.string.perfil_em_breve_toast, Toast.LENGTH_SHORT).show());
        } else if (aoTocar != null) {
            raizLinha.setOnClickListener(v -> aoTocar.run());
        }

        // O divisor da linha anterior volta a aparecer; a última linha não tem divisor.
        int total = grupo.getChildCount();
        if (total > 0) grupo.getChildAt(total - 1).findViewById(R.id.opcao_divisor)
                .setVisibility(View.VISIBLE);
        linha.findViewById(R.id.opcao_divisor).setVisibility(View.GONE);
        grupo.addView(linha);
        return linha;
    }

    private void abrirEvento(SecaoEvento secao) {
        startActivity(EventoInfoActivity.novaIntent(requireContext(), secao));
    }

    private void abrirAba(int menuId) {
        if (getActivity() instanceof BuyerMainActivity) {
            ((BuyerMainActivity) getActivity()).openTab(menuId);
        }
    }

    private void confirmarSaida() {
        androidx.appcompat.app.AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.perfil_sair_confirmar_titulo)
                .setMessage(R.string.perfil_sair_confirmar_texto)
                .setNegativeButton(R.string.perfil_cancelar, null)
                .setPositiveButton(R.string.sair, (d, w) -> sair())
                .show();
        // Açafrão sobre fundo claro não tem contraste suficiente: botões em vinho.
        int vinho = ContextCompat.getColor(requireContext(), R.color.brand_vinho);
        dialogo.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(vinho);
        dialogo.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(vinho);
    }

    private void sair() {
        // Avisos de sistema do pedido não podem ficar visíveis para a próxima conta.
        Simulador.avisos(requireContext()).cancelarTodas();
        SessionManager.getInstance(requireContext()).logout();
        // O carrinho é em memória e global: não pode vazar para a próxima conta.
        CarrinhoManager.getInstance().limpar();
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private String versaoDoApp() {
        try {
            String versao = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            return versao != null ? versao : "—";
        } catch (PackageManager.NameNotFoundException e) {
            return "—";
        }
    }
}

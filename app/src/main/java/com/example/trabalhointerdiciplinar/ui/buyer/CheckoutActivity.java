package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Checkout demonstrativo: resumo do carrinho, escolha entre Pix e cartão (ambos simulados) e
 * conclusão local. O estado da tentativa fica em {@link CheckoutViewModel}.
 */
public class CheckoutActivity extends AppCompatActivity {

    private CheckoutViewModel vm;
    private CarrinhoManager carrinho;
    private View opcaoPix;
    private View opcaoCartao;
    private View painelPix;
    private View painelCartao;
    private View progresso;
    private MaterialButton btnConfirmar;
    private boolean processando;
    /** Depois que a simulação começa, o botão não volta a habilitar (evita piscar na transição). */
    private boolean simulacaoIniciada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        Usuario usuario = SessionManager.getInstance(this).getUsuario();
        carrinho = CarrinhoManager.getInstance();
        vm = new ViewModelProvider(this).get(CheckoutViewModel.class);

        if (usuario == null) {
            finish();
            return;
        }
        // Carrinho vazio e nenhuma simulação em andamento: não há o que pagar.
        if (carrinho.getItens().isEmpty()
                && !Boolean.TRUE.equals(vm.getProcessando().getValue())) {
            Toast.makeText(this, R.string.checkout_carrinho_vazio, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());

        opcaoPix = findViewById(R.id.opcao_pix);
        opcaoCartao = findViewById(R.id.opcao_cartao);
        painelPix = findViewById(R.id.painel_pix);
        painelCartao = findViewById(R.id.painel_cartao);
        progresso = findViewById(R.id.progresso);
        btnConfirmar = findViewById(R.id.btn_confirmar);

        preencherResumo();
        opcaoPix.setOnClickListener(v -> escolher(FormaPagamento.PIX));
        opcaoCartao.setOnClickListener(v -> escolher(FormaPagamento.CARTAO));
        btnConfirmar.setOnClickListener(v -> vm.pagar(usuario, dataDeHoje()));

        vm.getProcessando().observe(this, ativo -> {
            processando = Boolean.TRUE.equals(ativo);
            if (processando) simulacaoIniciada = true;
            atualizarEstado();
        });
        vm.getPedidosCriados().observe(this, ids -> {
            if (ids == null) return;
            vm.consumirResultado();
            abrirConfirmacao(ids);
        });
        atualizarEstado();
    }

    private void preencherResumo() {
        PedidoViews.preencherItensCarrinho(findViewById(R.id.lista_itens), carrinho.getItens());
        String total = MoneyUtils.format(carrinho.getTotal());
        ((TextView) findViewById(R.id.txt_subtotal)).setText(total);
        ((TextView) findViewById(R.id.txt_total)).setText(total);
        ((TextView) findViewById(R.id.txt_valor_pix)).setText(total);
        ((TextView) findViewById(R.id.txt_valor_cartao)).setText(total);
    }

    private void escolher(FormaPagamento forma) {
        if (processando) return;
        if (vm.escolher(forma)) atualizarEstado();
    }

    /** Reflete no layout a forma escolhida e se há processamento em andamento. */
    private void atualizarEstado() {
        FormaPagamento forma = vm.getForma();
        opcaoPix.setSelected(forma == FormaPagamento.PIX);
        opcaoCartao.setSelected(forma == FormaPagamento.CARTAO);
        painelPix.setVisibility(forma == FormaPagamento.PIX ? View.VISIBLE : View.GONE);
        painelCartao.setVisibility(forma == FormaPagamento.CARTAO ? View.VISIBLE : View.GONE);

        progresso.setVisibility(processando ? View.VISIBLE : View.GONE);
        animarProgresso();
        btnConfirmar.setEnabled(forma != null && !processando && !simulacaoIniciada);
        if (processando || simulacaoIniciada) {
            btnConfirmar.setText(R.string.checkout_btn_processando);
        } else if (forma == FormaPagamento.PIX) {
            btnConfirmar.setText(R.string.checkout_btn_pix);
        } else if (forma == FormaPagamento.CARTAO) {
            btnConfirmar.setText(R.string.checkout_btn_cartao);
        } else {
            btnConfirmar.setText(R.string.checkout_btn_escolha);
        }
    }

    /** Liga o indicador só durante a espera da simulação; parado, não consome nada. */
    private void animarProgresso() {
        Drawable d = ((ImageView) progresso).getDrawable();
        if (!(d instanceof Animatable)) return;
        Animatable anim = (Animatable) d;
        if (processando && !anim.isRunning()) anim.start();
        if (!processando && anim.isRunning()) anim.stop();
    }

    private void abrirConfirmacao(List<String> ids) {
        if (ids.isEmpty()) {
            Toast.makeText(this, R.string.checkout_carrinho_vazio, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Intent confirmacao = new Intent(this, ConfirmacaoPedidoActivity.class);
        confirmacao.putStringArrayListExtra(ConfirmacaoPedidoActivity.EXTRA_PEDIDO_IDS,
                new ArrayList<>(ids));
        startActivity(confirmacao);
        finish();
    }

    private static String dataDeHoje() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }
}

package com.example.trabalhointerdiciplinar.ui.buyer;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;
import com.example.trabalhointerdiciplinar.util.SeloStatus;

import java.util.ArrayList;
import java.util.List;

/** Tela exibida logo após o checkout (demonstrativo): resumo do(s) pedido(s) recém-criado(s). */
public class ConfirmacaoPedidoActivity extends AppCompatActivity {
    public static final String EXTRA_PEDIDO_IDS = "pedido_ids";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacao_pedido);

        // Cabeçalho vinho vai até a borda superior: ícones da barra de status em tom claro.
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
        LinearLayout header = findViewById(R.id.header_confirmacao);
        int paddingTopOriginal = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_confirmacao), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(header.getPaddingLeft(), paddingTopOriginal + bars.top,
                    header.getPaddingRight(), header.getPaddingBottom());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            return insets;
        });

        animarSelo(savedInstanceState == null);

        List<Pedido> pedidos = carregarPedidos();
        if (pedidos.isEmpty()) {
            Toast.makeText(this, R.string.pedido_nao_encontrado, Toast.LENGTH_SHORT).show();
            irParaInicio();
            return;
        }

        preencher(pedidos);
        prepararSimulacao(pedidos, savedInstanceState == null);

        findViewById(R.id.aviso_demo).setVisibility(
                DemoCatalog.AMBIENTE_DEMO ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.btn_ver_pedido).setOnClickListener(v -> verPedido(pedidos.get(0)));
        findViewById(R.id.btn_voltar_inicio).setOnClickListener(v -> irParaInicio());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                irParaInicio();
            }
        });
    }

    private final ActivityResultLauncher<String> pedirPermissao = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedida -> iniciarSimulacao());
    private List<Pedido> pedidosDaTela = new ArrayList<>();

    /**
     * Começa a acompanhar os pedidos. Na primeira vez, e só nela, explica e pede a permissão de
     * notificações (Android 13+) antes de iniciar, para o primeiro aviso já poder aparecer. Se for
     * negada, nada quebra: a jornada e a central interna seguem funcionando.
     */
    private void prepararSimulacao(List<Pedido> pedidos, boolean primeiraExibicao) {
        pedidosDaTela = pedidos;
        boolean precisaPedir = primeiraExibicao
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
                && !Simulador.permissaoJaPedida(this);
        if (!precisaPedir) {
            iniciarSimulacao();
            return;
        }
        Simulador.marcarPermissaoPedida(this);
        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.notif_permissao_titulo)
                .setMessage(R.string.notif_permissao_texto)
                .setCancelable(false)
                .setNegativeButton(R.string.notif_agora_nao, (d, w) -> iniciarSimulacao())
                .setPositiveButton(R.string.notif_permitir,
                        (d, w) -> pedirPermissao.launch(Manifest.permission.POST_NOTIFICATIONS))
                .show();
        int vinho = ContextCompat.getColor(this, R.color.brand_vinho);
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(vinho);
        dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(vinho);
    }

    /** Idempotente: cada pedido tem um único cronograma, mesmo em rotação ou reabertura. */
    private void iniciarSimulacao() {
        for (Pedido p : pedidosDaTela) {
            Simulador.obter(this).iniciar(p);
        }
    }

    /**
     * Anima o selo uma única vez, na primeira exibição. Em recriações (rotação) e com animações
     * desativadas no sistema, fica o ícone estático já completo.
     */
    private void animarSelo(boolean primeiraExibicao) {
        if (!primeiraExibicao || !com.example.trabalhointerdiciplinar.util.Animacoes.ativas()) return;
        ImageView selo = findViewById(R.id.img_sucesso);
        selo.setImageResource(R.drawable.avd_confirmacao);
        if (selo.getDrawable() instanceof Animatable) {
            ((Animatable) selo.getDrawable()).start();
        }
    }

    private List<Pedido> carregarPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        ArrayList<String> ids = getIntent().getStringArrayListExtra(EXTRA_PEDIDO_IDS);
        if (ids == null) return pedidos;
        for (String id : ids) {
            Pedido p = MockDataRepository.getInstance().getPedidoById(id);
            if (p != null) pedidos.add(p);
        }
        return pedidos;
    }

    private void preencher(List<Pedido> pedidos) {
        StringBuilder numeros = new StringBuilder();
        double total = 0;
        for (Pedido p : pedidos) {
            if (numeros.length() > 0) numeros.append("  ·  ");
            numeros.append(PedidoApresentacao.numero(p));
            total += p.getValorTotal();
        }
        ((TextView) findViewById(R.id.txt_numero)).setText(numeros);
        ((TextView) findViewById(R.id.txt_total)).setText(MoneyUtils.format(total));
        PedidoViews.preencherItens(findViewById(R.id.lista_itens), pedidos);
        StatusPedido status = pedidos.get(0).getStatus();
        SeloStatus.aplicar(findViewById(R.id.txt_status), status);
    }

    /** Abre o detalhe com a aba Pedidos por baixo, para o "voltar" cair na lista de pedidos. */
    private void verPedido(Pedido pedido) {
        Intent lista = new Intent(this, BuyerMainActivity.class);
        lista.putExtra(BuyerMainActivity.EXTRA_ABRIR_ABA, R.id.nav_pedidos);
        lista.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Intent detalhe = new Intent(this, PedidoDetalheActivity.class);
        detalhe.putExtra(PedidoDetalheActivity.EXTRA_PEDIDO_ID, pedido.getId());
        startActivities(new Intent[]{lista, detalhe});
        finish();
    }

    private void irParaInicio() {
        Intent inicio = new Intent(this, BuyerMainActivity.class);
        inicio.putExtra(BuyerMainActivity.EXTRA_ABRIR_ABA, R.id.nav_home);
        inicio.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(inicio);
        finish();
    }
}

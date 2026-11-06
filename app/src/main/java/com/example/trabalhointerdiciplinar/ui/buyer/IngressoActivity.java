package com.example.trabalhointerdiciplinar.ui.buyer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;
import com.example.trabalhointerdiciplinar.util.QrCodes;
import com.example.trabalhointerdiciplinar.util.SeloStatus;

/**
 * Ingresso digital: reaproveita o QR Code demonstrativo do pedido (mesmo conteúdo e gerador da
 * Minha Jornada). Também oferece a retirada simulada, que não valida nenhum código real.
 */
public class IngressoActivity extends AppCompatActivity {
    public static final String EXTRA_PEDIDO_ID = "pedido_id";
    private static final int QR_TAMANHO_PX = 720;
    private static final long TICK_MS = 1_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            simulador.reconciliar();
            renderizar();
            handler.postDelayed(this, TICK_MS);
        }
    };

    private PedidoSimulador simulador;
    private Pedido pedido;
    private Usuario usuario;
    private boolean qrDesenhado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ingresso);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_ingresso), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        simulador = Simulador.obter(this);
        usuario = SessionManager.getInstance(this).getUsuario();
        pedido = MockDataRepository.getInstance().getPedidoById(getIntent().getStringExtra(EXTRA_PEDIDO_ID));
        if (pedido == null || usuario == null || !usuario.getId().equals(pedido.getIdComprador())) {
            Toast.makeText(this, R.string.ingresso_indisponivel, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.txt_ingresso_participante)).setText(usuario.getNome());
        ((TextView) findViewById(R.id.txt_ingresso_pedido)).setText(getString(
                R.string.ingresso_pedido_itens, PedidoApresentacao.numero(pedido),
                PedidoApresentacao.totalDeUnidades(pedido)));
        findViewById(R.id.btn_simular_retirada).setOnClickListener(v -> simularRetirada());
        findViewById(R.id.btn_ampliar_qr).setOnClickListener(v -> startActivity(
                new android.content.Intent(this, QrAmpliadoActivity.class)
                        .putExtra(QrAmpliadoActivity.EXTRA_PEDIDO_ID, pedido.getId())));
        renderizar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pedido == null) return;
        simulador.reconciliar();
        renderizar();
        handler.postDelayed(tick, TICK_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    private void simularRetirada() {
        if (simulador.avancarPara(pedido.getId(), usuario.getId(), StatusPedido.ENTREGUE)) {
            Toast.makeText(this, R.string.ingresso_simulada, Toast.LENGTH_SHORT).show();
        }
        renderizar();
    }

    private void renderizar() {
        StatusPedido status = pedido.getStatus();
        SeloStatus.aplicar(findViewById(R.id.txt_status), status);

        boolean retirado = status == StatusPedido.ENTREGUE;
        boolean pronto = status == StatusPedido.ENVIADO;
        boolean exibeQr = PedidoApresentacao.exibeQrDemo(status);

        ImageView qr = findViewById(R.id.img_qr);
        qr.setVisibility(exibeQr ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.bloco_retirado).setVisibility(retirado ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_ampliar_qr).setVisibility(exibeQr ? View.VISIBLE : View.GONE);
        findViewById(R.id.faixa_apresente).setVisibility(pronto ? View.VISIBLE : View.GONE);
        // A retirada simulada só é oferecida com o pedido pronto, nunca depois de retirado.
        findViewById(R.id.btn_simular_retirada).setVisibility(
                pronto && DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);

        if (exibeQr && !qrDesenhado) {
            Bitmap bitmap = QrCodes.gerarEmPretoEBranco(PedidoApresentacao.conteudoQrDemo(pedido), QR_TAMANHO_PX);
            if (bitmap != null) {
                qr.setImageBitmap(bitmap);
                qr.setContentDescription(getString(R.string.qr_cd, PedidoApresentacao.numero(pedido)));
                qrDesenhado = true;
            }
        }
    }
}

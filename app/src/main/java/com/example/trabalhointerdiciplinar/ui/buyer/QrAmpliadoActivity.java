package com.example.trabalhointerdiciplinar.ui.buyer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;
import com.example.trabalhointerdiciplinar.util.QrCodes;

import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * QR Code ampliado para a retirada: alto contraste, identificador do pedido em texto grande e
 * brilho máximo opcional somente nesta janela (volta ao normal ao sair).
 */
public class QrAmpliadoActivity extends AppCompatActivity {
    public static final String EXTRA_PEDIDO_ID = "pedido_id";
    private static final int QR_PX = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_ampliado);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_qr), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        Usuario u = SessionManager.getInstance(this).getUsuario();
        Pedido pedido = MockDataRepository.getInstance().getPedidoById(getIntent().getStringExtra(EXTRA_PEDIDO_ID));
        if (pedido == null || u == null || !u.getId().equals(pedido.getIdComprador())
                || !PedidoApresentacao.exibeQrDemo(pedido.getStatus())) {
            Toast.makeText(this, R.string.ingresso_indisponivel, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        findViewById(R.id.btn_fechar_qr).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.txt_qr_pedido)).setText(PedidoApresentacao.numero(pedido));

        ImageView qr = findViewById(R.id.img_qr_grande);
        Bitmap bitmap = QrCodes.gerarEmPretoEBranco(PedidoApresentacao.conteudoQrDemo(pedido), QR_PX);
        if (bitmap != null) {
            qr.setImageBitmap(bitmap);
            qr.setContentDescription(getString(R.string.qr_cd, PedidoApresentacao.numero(pedido)));
        }
        // Quadrado do tamanho da largura disponível.
        qr.post(() -> {
            qr.getLayoutParams().height = qr.getWidth();
            qr.requestLayout();
        });

        MaterialSwitch brilho = findViewById(R.id.switch_brilho);
        brilho.setOnCheckedChangeListener((b, marcado) -> definirBrilho(marcado));
        brilho.setChecked(savedInstanceState != null && savedInstanceState.getBoolean("brilho", false));
    }

    /** Só a janela desta tela: ao fechar, o brilho do aparelho continua como estava. */
    private void definirBrilho(boolean maximo) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = maximo ? 1f : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        getWindow().setAttributes(lp);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MaterialSwitch brilho = findViewById(R.id.switch_brilho);
        if (brilho != null) outState.putBoolean("brilho", brilho.isChecked());
    }
}

package com.example.trabalhointerdiciplinar.ui.buyer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.adapter.ProdutoAdapter;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.Loja;

public class LojaDetalheActivity extends AppCompatActivity implements ProdutoSheetFragment.Host {
    public static final String EXTRA_LOJA_ID = "loja_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loja_detalhe);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_loja_detalhe), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        String id = getIntent().getStringExtra(EXTRA_LOJA_ID);
        Loja loja = MockDataRepository.getInstance().getLojaById(id);
        if (loja == null) {
            Toast.makeText(this, R.string.loja_nao_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());

        ((TextView) findViewById(R.id.txt_nome)).setText(loja.getNome());
        ((TextView) findViewById(R.id.txt_categoria)).setText(loja.getCategoria());
        preencherOpcional(findViewById(R.id.txt_descricao), loja.getDescricao());
        preencherOpcional(findViewById(R.id.txt_endereco), loja.getEndereco());

        RecyclerView rv = findViewById(R.id.rv_produtos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ProdutoAdapter adapter = new ProdutoAdapter(produto ->
                ProdutoSheetFragment.mostrar(getSupportFragmentManager(), produto.getId()));
        rv.setAdapter(adapter);
        adapter.submit(MockDataRepository.getInstance().getProdutosPublicadosDaLoja(id));
    }

    private static void preencherOpcional(TextView campo, String valor) {
        boolean tem = valor != null && !valor.trim().isEmpty();
        campo.setVisibility(tem ? View.VISIBLE : View.GONE);
        if (tem) campo.setText(valor.trim());
    }

    @Override
    public void onProdutoAdicionado(com.example.trabalhointerdiciplinar.model.Produto produto, long alteracaoId) {
        com.example.trabalhointerdiciplinar.util.FeedbackPedido.adicionado(
                findViewById(android.R.id.content), null, alteracaoId, null);
    }

    @Override
    public void onSheetFechado() {
    }
}

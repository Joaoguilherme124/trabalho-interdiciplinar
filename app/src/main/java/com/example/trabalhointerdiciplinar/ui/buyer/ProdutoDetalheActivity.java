package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

import java.util.Locale;

public class ProdutoDetalheActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUTO_ID = "produto_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto_detalhe);

        String id = getIntent().getStringExtra(EXTRA_PRODUTO_ID);
        Produto produto = MockDataRepository.getInstance().getProdutoById(id);
        if (produto == null) {
            Toast.makeText(this, "Produto não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.txt_titulo)).setText(produto.getTitulo());
        ((TextView) findViewById(R.id.txt_preco)).setText(MoneyUtils.format(produto.getPreco()));
        ((TextView) findViewById(R.id.txt_avaliacao)).setText(
                String.format(Locale.getDefault(), "★ %.1f  ·  %s", produto.getAvaliacao(), produto.getNomeLoja()));
        ((TextView) findViewById(R.id.txt_descricao)).setText(produto.getDescricao());
        ((TextView) findViewById(R.id.txt_variacoes)).setText(
                "Categoria: " + produto.getCategoria() + "  |  Estoque: " + produto.getEstoque());

        findViewById(R.id.btn_add_carrinho).setOnClickListener(v -> {
            CarrinhoManager.getInstance().adicionar(produto, 1);
            Toast.makeText(this, "Adicionado ao carrinho", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_ver_loja).setOnClickListener(v -> {
            Intent i = new Intent(this, LojaDetalheActivity.class);
            i.putExtra(LojaDetalheActivity.EXTRA_LOJA_ID, produto.getIdLoja());
            startActivity(i);
        });
    }
}

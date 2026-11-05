package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.adapter.ProdutoAdapter;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.Loja;

import java.util.Locale;

public class LojaDetalheActivity extends AppCompatActivity {
    public static final String EXTRA_LOJA_ID = "loja_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loja_detalhe);

        String id = getIntent().getStringExtra(EXTRA_LOJA_ID);
        Loja loja = MockDataRepository.getInstance().getLojaById(id);
        if (loja == null) {
            Toast.makeText(this, "Loja não encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.txt_nome)).setText(loja.getNome());
        ((TextView) findViewById(R.id.txt_categoria)).setText(
                String.format(Locale.getDefault(), "%s  ·  ★ %.1f", loja.getCategoria(), loja.getAvaliacao()));
        ((TextView) findViewById(R.id.txt_descricao)).setText(loja.getDescricao());
        ((TextView) findViewById(R.id.txt_endereco)).setText(loja.getEndereco());

        RecyclerView rv = findViewById(R.id.rv_produtos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ProdutoAdapter adapter = new ProdutoAdapter(produto -> {
            Intent i = new Intent(this, ProdutoDetalheActivity.class);
            i.putExtra(ProdutoDetalheActivity.EXTRA_PRODUTO_ID, produto.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);
        adapter.submit(MockDataRepository.getInstance().getProdutosPorLoja(id));
    }
}

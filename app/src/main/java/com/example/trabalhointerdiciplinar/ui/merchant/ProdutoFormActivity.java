package com.example.trabalhointerdiciplinar.ui.merchant;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Produto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ProdutoFormActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUTO_ID = "produto_id";
    public static final String EXTRA_LOJA_ID = "loja_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto_form);

        String produtoId = getIntent().getStringExtra(EXTRA_PRODUTO_ID);
        String lojaId = getIntent().getStringExtra(EXTRA_LOJA_ID);
        Produto existente = produtoId != null
                ? MockDataRepository.getInstance().getProdutoById(produtoId) : null;

        if (existente != null) {
            lojaId = existente.getIdLoja();
            ((TextView) findViewById(R.id.txt_titulo_tela)).setText("Editar produto");
        }

        Loja loja = MockDataRepository.getInstance().getLojaById(lojaId);
        if (loja == null) {
            Toast.makeText(this, "Loja inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EditText edtTitulo = findViewById(R.id.edt_titulo);
        EditText edtPreco = findViewById(R.id.edt_preco);
        EditText edtEstoque = findViewById(R.id.edt_estoque);
        EditText edtDesc = findViewById(R.id.edt_descricao);
        Spinner spinner = findViewById(R.id.spinner_categoria);

        List<String> cats = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.categorias)));
        cats.remove("Todos");
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats));

        if (existente != null) {
            edtTitulo.setText(existente.getTitulo());
            edtPreco.setText(String.valueOf(existente.getPreco()));
            edtEstoque.setText(String.valueOf(existente.getEstoque()));
            edtDesc.setText(existente.getDescricao());
            int idx = cats.indexOf(existente.getCategoria());
            if (idx >= 0) spinner.setSelection(idx);
        }

        String finalLojaId = lojaId;
        findViewById(R.id.btn_salvar).setOnClickListener(v -> {
            try {
                String titulo = edtTitulo.getText().toString().trim();
                double preco = Double.parseDouble(edtPreco.getText().toString().replace(',', '.'));
                int estoque = Integer.parseInt(edtEstoque.getText().toString().trim());
                if (titulo.isEmpty()) {
                    Toast.makeText(this, "Informe o título", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (existente != null) {
                    existente.setTitulo(titulo);
                    existente.setPreco(preco);
                    existente.setEstoque(estoque);
                    existente.setDescricao(edtDesc.getText().toString().trim());
                    existente.setCategoria(spinner.getSelectedItem().toString());
                    MockDataRepository.getInstance().updateProduto(existente);
                } else {
                    Produto p = new Produto(
                            UUID.randomUUID().toString(),
                            finalLojaId,
                            loja.getNome(),
                            titulo,
                            edtDesc.getText().toString().trim(),
                            preco,
                            estoque,
                            "",
                            0f,
                            spinner.getSelectedItem().toString());
                    MockDataRepository.getInstance().addProduto(p);
                }
                Toast.makeText(this, "Produto salvo", Toast.LENGTH_SHORT).show();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Preço/estoque inválidos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

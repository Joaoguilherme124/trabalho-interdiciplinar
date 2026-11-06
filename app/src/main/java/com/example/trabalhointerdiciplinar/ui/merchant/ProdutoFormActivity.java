package com.example.trabalhointerdiciplinar.ui.merchant;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.ProdutoImagens;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Cria ou edita um produto da loja. Vale para o cardápio do participante (mesma fonte de dados). */
public class ProdutoFormActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUTO_ID = "produto_id";
    public static final String EXTRA_LOJA_ID = "loja_id";

    private Produto existente;
    private Loja loja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_produto_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_produto_form), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());

        MockDataRepository repo = MockDataRepository.getInstance();
        String produtoId = getIntent().getStringExtra(EXTRA_PRODUTO_ID);
        String lojaId = getIntent().getStringExtra(EXTRA_LOJA_ID);
        existente = produtoId != null ? repo.getProdutoById(produtoId) : null;
        if (existente != null) lojaId = existente.getIdLoja();
        loja = repo.getLojaById(lojaId);
        Usuario u = SessionManager.getInstance(this).getUsuario();
        // O produto só pode ser editado por quem é dono da loja.
        if (loja == null || u == null || !u.getId().equals(loja.getIdDono())) {
            Toast.makeText(this, R.string.produto_loja_invalida, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (existente != null) {
            ((TextView) findViewById(R.id.txt_titulo_tela)).setText(R.string.produto_form_editar);
        }

        List<String> cats = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.categorias)));
        cats.remove("Todos");
        Spinner categoria = findViewById(R.id.spinner_categoria);
        categoria.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats));
        Spinner imagem = findViewById(R.id.spinner_imagem);
        List<String> rotulos = new ArrayList<>();
        for (String[] o : ProdutoImagens.OPCOES) rotulos.add(o[1]);
        imagem.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rotulos));
        ImageView preview = findViewById(R.id.img_preview);
        imagem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                int res = getResources().getIdentifier(ProdutoImagens.OPCOES[pos][0], "drawable", getPackageName());
                preview.setImageResource(res != 0 ? res : R.drawable.bg_image_placeholder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        });

        EditText nome = findViewById(R.id.edt_titulo);
        EditText descricao = findViewById(R.id.edt_descricao);
        EditText preco = findViewById(R.id.edt_preco);
        EditText ingredientes = findViewById(R.id.edt_ingredientes);
        EditText porcao = findViewById(R.id.edt_porcao);
        EditText tempo = findViewById(R.id.edt_tempo);
        EditText estoque = findViewById(R.id.edt_estoque);
        MaterialSwitch disponivel = findViewById(R.id.switch_disponivel);

        if (existente != null && savedInstanceState == null) {
            nome.setText(existente.getTitulo());
            descricao.setText(existente.getDescricao());
            preco.setText(String.format(java.util.Locale.US, "%.2f", existente.getPreco()));
            ingredientes.setText(existente.getIngredientes());
            porcao.setText(existente.getPorcao());
            tempo.setText(existente.getTempoEstimadoMin() > 0 ? String.valueOf(existente.getTempoEstimadoMin()) : "");
            estoque.setText(String.valueOf(existente.getEstoque()));
            disponivel.setChecked(existente.isDisponivel());
            int idx = cats.indexOf(existente.getCategoria());
            if (idx >= 0) categoria.setSelection(idx);
            imagem.setSelection(ProdutoImagens.indiceDaChave(existente.getUrlImagem()));
        } else if (savedInstanceState == null) {
            estoque.setText("50");
            tempo.setText("15");
        }

        findViewById(R.id.btn_salvar).setOnClickListener(v -> {
            String titulo = nome.getText().toString().trim();
            if (titulo.isEmpty()) {
                nome.setError(getString(R.string.produto_erro_nome));
                nome.requestFocus();
                return;
            }
            double valor;
            try {
                valor = Double.parseDouble(preco.getText().toString().trim().replace(',', '.'));
                if (valor <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                preco.setError(getString(R.string.produto_erro_preco));
                preco.requestFocus();
                return;
            }
            int qtd;
            try {
                qtd = Integer.parseInt(estoque.getText().toString().trim());
                if (qtd < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                estoque.setError(getString(R.string.produto_erro_estoque));
                estoque.requestFocus();
                return;
            }
            int minutos = 0;
            try {
                minutos = Integer.parseInt(tempo.getText().toString().trim());
            } catch (NumberFormatException ignorado) {
                // tempo é opcional
            }
            Produto p = existente != null ? existente : new Produto(UUID.randomUUID().toString(),
                    loja.getId(), loja.getNome(), titulo, "", valor, qtd, "", 0f, "");
            p.setTitulo(titulo);
            p.setDescricao(descricao.getText().toString().trim());
            p.setPreco(valor);
            p.setEstoque(qtd);
            p.setCategoria(categoria.getSelectedItem().toString());
            p.setUrlImagem(ProdutoImagens.OPCOES[imagem.getSelectedItemPosition()][0]);
            p.setIngredientes(ingredientes.getText().toString().trim());
            p.setPorcao(porcao.getText().toString().trim());
            p.setTempoEstimadoMin(minutos);
            p.setDisponivel(disponivel.isChecked());
            p.setNomeLoja(loja.getNome());
            if (existente != null) MockDataRepository.getInstance().updateProduto(p);
            else MockDataRepository.getInstance().addProduto(p);
            Toast.makeText(this, R.string.produto_salvo, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

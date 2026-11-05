package com.example.trabalhointerdiciplinar.ui.merchant;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.StatusLoja;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CadastroLojaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_loja);

        Spinner spinner = findViewById(R.id.spinner_categoria);
        List<String> cats = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.categorias)));
        cats.remove("Todos");
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats));

        EditText edtNome = findViewById(R.id.edt_nome);
        EditText edtCnpj = findViewById(R.id.edt_cnpj);
        EditText edtTel = findViewById(R.id.edt_telefone);
        EditText edtEnd = findViewById(R.id.edt_endereco);
        EditText edtLogo = findViewById(R.id.edt_logo);
        EditText edtDesc = findViewById(R.id.edt_descricao);

        findViewById(R.id.btn_salvar).setOnClickListener(v -> {
            if (edtNome.getText().toString().trim().isEmpty() || edtCnpj.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Preencha nome e CNPJ", Toast.LENGTH_SHORT).show();
                return;
            }
            String donoId = SessionManager.getInstance(this).getUsuario().getId();
            Loja loja = new Loja(
                    UUID.randomUUID().toString(),
                    donoId,
                    edtNome.getText().toString().trim(),
                    edtCnpj.getText().toString().trim(),
                    spinner.getSelectedItem().toString(),
                    0f,
                    edtLogo.getText().toString().trim(),
                    StatusLoja.PENDENTE,
                    edtTel.getText().toString().trim(),
                    edtEnd.getText().toString().trim(),
                    edtDesc.getText().toString().trim());
            MockDataRepository.getInstance().addLoja(loja);
            Toast.makeText(this, "Solicitação enviada (status PENDENTE)", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}

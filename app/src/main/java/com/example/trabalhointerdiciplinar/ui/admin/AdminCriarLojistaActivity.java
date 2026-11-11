package com.example.trabalhointerdiciplinar.ui.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.ContaLojistaService;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;

public class AdminCriarLojistaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_criar_lojista);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        EditText edtNome = findViewById(R.id.edt_nome);
        EditText edtEmail = findViewById(R.id.edt_email);
        EditText edtSenha = findViewById(R.id.edt_senha);
        EditText edtTelefone = findViewById(R.id.edt_telefone);

        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_salvar).setOnClickListener(v -> {
            edtNome.setError(null);
            edtEmail.setError(null);
            edtSenha.setError(null);

            String senha = edtSenha.getText().toString();
            ContaLojistaService.Retorno r = ContaLojistaService.criar(
                    MockDataRepository.getInstance(),
                    edtNome.getText().toString(),
                    edtEmail.getText().toString(),
                    senha,
                    edtTelefone.getText().toString());

            switch (r.resultado) {
                case NOME_INVALIDO:
                    edtNome.setError(getString(R.string.cadastro_erro_nome));
                    edtNome.requestFocus();
                    return;
                case EMAIL_INVALIDO:
                    edtEmail.setError(getString(R.string.cadastro_erro_email));
                    edtEmail.requestFocus();
                    return;
                case EMAIL_EM_USO:
                    edtEmail.setError(getString(R.string.cadastro_erro_email_uso));
                    edtEmail.requestFocus();
                    return;
                case SENHA_INVALIDA:
                    edtSenha.setError(getString(R.string.cadastro_erro_senha));
                    edtSenha.requestFocus();
                    return;
                default:
                    Toast.makeText(this, getString(R.string.admin_criar_ok,
                            r.usuario.getEmail(), senha), Toast.LENGTH_LONG).show();
                    finish();
            }
        });
    }
}

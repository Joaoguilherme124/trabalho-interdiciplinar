package com.example.trabalhointerdiciplinar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.CadastroService;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.ui.buyer.BuyerMainActivity;
import com.example.trabalhointerdiciplinar.util.JanelaInsets;
import com.google.android.material.textfield.TextInputLayout;

/** Cadastro público demonstrativo: cria apenas contas de participante. */
public class CadastroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        JanelaInsets.aplicarComCabecalho(findViewById(R.id.scroll_auth), findViewById(R.id.auth_header));

        EditText edtNome = findViewById(R.id.edt_nome);
        EditText edtEmail = findViewById(R.id.edt_email);
        EditText edtSenha = findViewById(R.id.edt_senha);
        EditText edtTelefone = findViewById(R.id.edt_telefone);
        EditText edtEndereco = findViewById(R.id.edt_endereco);
        TextInputLayout tilNome = findViewById(R.id.til_nome);
        TextInputLayout tilEmail = findViewById(R.id.til_email);
        TextInputLayout tilSenha = findViewById(R.id.til_senha);

        findViewById(R.id.link_voltar_login).setOnClickListener(v -> finish());

        findViewById(R.id.btn_cadastrar).setOnClickListener(v -> {
            tilNome.setError(null);
            tilEmail.setError(null);
            tilSenha.setError(null);

            CadastroService.Retorno retorno = CadastroService.cadastrarParticipante(
                    MockDataRepository.getInstance(),
                    edtNome.getText().toString(), edtEmail.getText().toString(),
                    edtSenha.getText().toString(), edtTelefone.getText().toString(),
                    edtEndereco.getText().toString());

            switch (retorno.resultado) {
                case NOME_INVALIDO:
                    tilNome.setError(getString(R.string.cadastro_erro_nome));
                    edtNome.requestFocus();
                    return;
                case EMAIL_INVALIDO:
                    tilEmail.setError(getString(R.string.cadastro_erro_email));
                    edtEmail.requestFocus();
                    return;
                case EMAIL_EM_USO:
                    tilEmail.setError(getString(R.string.cadastro_erro_email_uso));
                    edtEmail.requestFocus();
                    return;
                case SENHA_INVALIDA:
                    tilSenha.setError(getString(R.string.cadastro_erro_senha));
                    edtSenha.requestFocus();
                    return;
                default:
                    SessionManager.getInstance(this).login(retorno.usuario);
                    Intent intent = new Intent(this, BuyerMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
            }
        });
    }
}

package com.example.trabalhointerdiciplinar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.RecuperarSenhaService;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Usuario;

public class RedefinirSenhaActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID = "usuario_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_redefinir_senha);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_redefinir), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        String usuarioId = getIntent().getStringExtra(EXTRA_USUARIO_ID);
        MockDataRepository repo = MockDataRepository.getInstance();
        Usuario alvo = usuarioId == null ? null : repo.getUsuarioById(usuarioId);
        if (alvo == null) {
            finish();
            return;
        }

        EditText edtSenha = findViewById(R.id.edt_senha);
        EditText edtConfirma = findViewById(R.id.edt_confirma);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_salvar).setOnClickListener(v -> {
            edtSenha.setError(null);
            edtConfirma.setError(null);
            RecuperarSenhaService.Resultado r = RecuperarSenhaService.redefinir(
                    repo, alvo.getId(), edtSenha.getText().toString(), edtConfirma.getText().toString());
            switch (r) {
                case SENHA_INVALIDA:
                    edtSenha.setError(getString(R.string.cadastro_erro_senha));
                    edtSenha.requestFocus();
                    return;
                case SENHA_DIFERENTE:
                    edtConfirma.setError(getString(R.string.conta_erro_senha_diferente));
                    edtConfirma.requestFocus();
                    return;
                case NAO_ENCONTRADO:
                    finish();
                    return;
                default:
                    Usuario sessao = SessionManager.getInstance(this).getUsuario();
                    if (sessao != null && alvo.getId().equals(sessao.getId())) {
                        sessao.setSenha(alvo.getSenha());
                        SessionManager.getInstance(this).login(sessao);
                    }
                    Toast.makeText(this, R.string.redefinir_ok, Toast.LENGTH_LONG).show();
                    Intent login = new Intent(this, LoginActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(login);
                    finish();
            }
        });
    }
}

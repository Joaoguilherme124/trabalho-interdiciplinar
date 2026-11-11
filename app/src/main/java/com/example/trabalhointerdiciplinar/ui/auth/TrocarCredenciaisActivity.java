package com.example.trabalhointerdiciplinar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.ContaLojistaService;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.ui.merchant.MerchantMainActivity;
import com.google.android.material.button.MaterialButton;

public class TrocarCredenciaisActivity extends AppCompatActivity {

    public static final String EXTRA_OBRIGATORIO = "obrigatorio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trocar_credenciais);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        Usuario usuario = SessionManager.getInstance(this).getUsuario();
        if (usuario == null) {
            finish();
            return;
        }

        boolean obrigatorio = getIntent().getBooleanExtra(EXTRA_OBRIGATORIO, false)
                || usuario.isPrecisaTrocarCredenciais();

        View voltar = findViewById(R.id.btn_voltar);
        TextView apoio = findViewById(R.id.txt_apoio);
        MaterialButton salvar = findViewById(R.id.btn_salvar);
        EditText edtEmail = findViewById(R.id.edt_email);
        EditText edtSenha = findViewById(R.id.edt_senha);
        EditText edtConfirma = findViewById(R.id.edt_confirma);

        if (savedInstanceState == null) {
            edtEmail.setText(usuario.getEmail());
        }

        if (obrigatorio) {
            voltar.setVisibility(View.GONE);
            apoio.setText(R.string.conta_trocar_apoio);
            salvar.setText(R.string.conta_trocar_salvar);
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // Primeiro acesso: não volta à Cozinha sem trocar as credenciais.
                }
            });
        } else {
            voltar.setVisibility(View.VISIBLE);
            voltar.setOnClickListener(v -> finish());
            apoio.setText(R.string.conta_trocar_apoio_livre);
            salvar.setText(R.string.conta_trocar_salvar_livre);
        }

        salvar.setOnClickListener(v -> {
            edtEmail.setError(null);
            edtSenha.setError(null);
            edtConfirma.setError(null);

            ContaLojistaService.Resultado resultado = ContaLojistaService.atualizarCredenciais(
                    usuario, MockDataRepository.getInstance(),
                    edtEmail.getText().toString(),
                    edtSenha.getText().toString(),
                    edtConfirma.getText().toString(),
                    obrigatorio);

            switch (resultado) {
                case EMAIL_INVALIDO:
                    edtEmail.setError(getString(R.string.conta_erro_email));
                    edtEmail.requestFocus();
                    return;
                case EMAIL_EM_USO:
                    edtEmail.setError(getString(R.string.conta_erro_email_uso));
                    edtEmail.requestFocus();
                    return;
                case SENHA_INVALIDA:
                    edtSenha.setError(getString(R.string.conta_erro_senha));
                    edtSenha.requestFocus();
                    return;
                case SENHA_DIFERENTE:
                    edtConfirma.setError(getString(R.string.conta_erro_senha_diferente));
                    edtConfirma.requestFocus();
                    return;
                case SENHA_IGUAL_ANTERIOR:
                    edtSenha.setError(getString(R.string.conta_erro_senha_igual));
                    edtSenha.requestFocus();
                    return;
                default:
                    SessionManager.getInstance(this).login(usuario);
                    Toast.makeText(this, R.string.conta_salva, Toast.LENGTH_SHORT).show();
                    if (obrigatorio) {
                        Intent i = new Intent(this, MerchantMainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                    finish();
            }
        });
    }
}

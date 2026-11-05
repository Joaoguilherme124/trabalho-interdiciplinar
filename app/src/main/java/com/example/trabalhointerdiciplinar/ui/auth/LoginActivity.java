package com.example.trabalhointerdiciplinar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.ui.admin.AdminMainActivity;
import com.example.trabalhointerdiciplinar.ui.buyer.BuyerMainActivity;
import com.example.trabalhointerdiciplinar.ui.merchant.MerchantMainActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = SessionManager.getInstance(this);
        if (session.isLoggedIn()) {
            navegarPorTipo(session.getUsuario().getTipo());
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        EditText edtEmail = findViewById(R.id.edt_email);
        EditText edtSenha = findViewById(R.id.edt_senha);

        findViewById(R.id.btn_entrar).setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();
            Usuario user = MockDataRepository.getInstance().autenticar(email, senha);
            if (user == null) {
                Toast.makeText(this, "E-mail ou senha inválidos", Toast.LENGTH_SHORT).show();
                return;
            }
            session.login(user);
            navegarPorTipo(user.getTipo());
            finish();
        });

        findViewById(R.id.link_cadastro).setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));

        findViewById(R.id.link_recuperar).setOnClickListener(v ->
                startActivity(new Intent(this, RecuperarSenhaActivity.class)));
    }

    private void navegarPorTipo(TipoUsuario tipo) {
        Intent intent;
        if (tipo == TipoUsuario.LOJISTA) {
            intent = new Intent(this, MerchantMainActivity.class);
        } else if (tipo == TipoUsuario.ADMIN) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, BuyerMainActivity.class);
        }
        startActivity(intent);
    }
}

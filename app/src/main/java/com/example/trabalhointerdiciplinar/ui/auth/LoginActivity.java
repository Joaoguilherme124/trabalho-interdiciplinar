package com.example.trabalhointerdiciplinar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.ui.admin.AdminMainActivity;
import com.example.trabalhointerdiciplinar.ui.buyer.BuyerMainActivity;
import com.example.trabalhointerdiciplinar.ui.merchant.MerchantMainActivity;
import com.example.trabalhointerdiciplinar.util.Animacoes;
import com.example.trabalhointerdiciplinar.util.JanelaInsets;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LoginActivity extends AppCompatActivity {

    private EditText edtSenha;
    private ImageButton btnOlho;
    private boolean senhaVisivel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = SessionManager.getInstance(this);
        if (session.isLoggedIn()) {
            navegarAposLogin(session.getUsuario());
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        JanelaInsets.aplicarNoLogin(findViewById(R.id.scroll_login),
                findViewById(R.id.topo_login), findViewById(R.id.zona_inferior));

        EditText edtEmail = findViewById(R.id.edt_email);
        edtSenha = findViewById(R.id.edt_senha);
        btnOlho = findViewById(R.id.btn_olho);
        View campoEmail = findViewById(R.id.campo_email);
        View campoSenha = findViewById(R.id.campo_senha);
        TextView erroEmail = findViewById(R.id.erro_email);
        TextView erroSenha = findViewById(R.id.erro_senha);

        // Erros somem assim que o usuário volta a digitar.
        limparErroAoDigitar(edtEmail, campoEmail, erroEmail);
        limparErroAoDigitar(edtSenha, campoSenha, erroSenha);

        btnOlho.setOnClickListener(v -> alternarVisibilidadeSenha());

        findViewById(R.id.btn_entrar).setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();

            mostrarErro(campoEmail, erroEmail, null);
            mostrarErro(campoSenha, erroSenha, null);
            if (email.isEmpty()) {
                mostrarErro(campoEmail, erroEmail, getString(R.string.login_erro_email_vazio));
                edtEmail.requestFocus();
                return;
            }
            if (senha.isEmpty()) {
                mostrarErro(campoSenha, erroSenha, getString(R.string.login_erro_senha_vazia));
                edtSenha.requestFocus();
                return;
            }

            Usuario user = MockDataRepository.getInstance().autenticar(email, senha);
            if (user == null) {
                mostrarErro(campoSenha, erroSenha, getString(R.string.login_erro_credenciais));
                return;
            }
            session.login(user);
            navegarAposLogin(user);
            finish();
        });

        findViewById(R.id.link_cadastro).setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));

        findViewById(R.id.link_recuperar).setOnClickListener(v ->
                startActivity(new Intent(this, RecuperarSenhaActivity.class)));

        // Contas de teste: acesso discreto (toque longo na marca), só no ambiente demonstrativo.
        if (DemoCatalog.AMBIENTE_DEMO) {
            findViewById(R.id.img_logo).setOnLongClickListener(v -> {
                mostrarContasDeTeste();
                return true;
            });
        }

        if (savedInstanceState == null) {
            Animacoes.entradaEmCascata((ViewGroup) findViewById(R.id.form_login));
        }
    }

    /** Alterna mostrar/ocultar sem perder o texto nem a posição do cursor. */
    private void alternarVisibilidadeSenha() {
        int inicio = edtSenha.getSelectionStart();
        int fim = edtSenha.getSelectionEnd();
        senhaVisivel = !senhaVisivel;
        edtSenha.setTransformationMethod(senhaVisivel
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
        edtSenha.setSelection(Math.max(inicio, 0), Math.max(fim, 0));
        btnOlho.setImageResource(senhaVisivel
                ? R.drawable.ic_login_olho_fechado : R.drawable.ic_login_olho);
        btnOlho.setContentDescription(getString(senhaVisivel
                ? R.string.login_senha_ocultar : R.string.login_senha_mostrar));
    }

    private void mostrarErro(View campo, TextView rotulo, String mensagem) {
        campo.setActivated(mensagem != null);
        rotulo.setText(mensagem);
        rotulo.setVisibility(mensagem != null ? View.VISIBLE : View.GONE);
    }

    private void limparErroAoDigitar(EditText edt, View campo, TextView rotulo) {
        edt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (campo.isActivated()) mostrarErro(campo, rotulo, null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void mostrarContasDeTeste() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.login_dev_titulo)
                .setMessage(R.string.login_dev_contas)
                .setPositiveButton(R.string.login_fechar, null)
                .show();
    }

    private void navegarAposLogin(Usuario user) {
        if (user.getTipo() == TipoUsuario.LOJISTA && user.isPrecisaTrocarCredenciais()) {
            Intent intent = new Intent(this, TrocarCredenciaisActivity.class);
            intent.putExtra(TrocarCredenciaisActivity.EXTRA_OBRIGATORIO, true);
            startActivity(intent);
            return;
        }
        navegarPorTipo(user.getTipo());
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

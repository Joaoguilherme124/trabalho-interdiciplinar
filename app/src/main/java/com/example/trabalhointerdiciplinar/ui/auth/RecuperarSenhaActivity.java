package com.example.trabalhointerdiciplinar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.RecuperarSenhaService;

public class RecuperarSenhaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_senha);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_recuperar), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        EditText edtEmail = findViewById(R.id.edt_email);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_enviar).setOnClickListener(v -> {
            edtEmail.setError(null);
            RecuperarSenhaService.Retorno r = RecuperarSenhaService.localizar(
                    MockDataRepository.getInstance(), edtEmail.getText().toString());
            switch (r.resultado) {
                case EMAIL_INVALIDO:
                    edtEmail.setError(getString(R.string.cadastro_erro_email));
                    edtEmail.requestFocus();
                    return;
                case NAO_ENCONTRADO:
                    edtEmail.setError(getString(R.string.recuperar_email_inexistente));
                    edtEmail.requestFocus();
                    return;
                default:
                    Intent i = new Intent(this, RedefinirSenhaActivity.class);
                    i.putExtra(RedefinirSenhaActivity.EXTRA_USUARIO_ID, r.usuario.getId());
                    startActivity(i);
            }
        });
    }
}

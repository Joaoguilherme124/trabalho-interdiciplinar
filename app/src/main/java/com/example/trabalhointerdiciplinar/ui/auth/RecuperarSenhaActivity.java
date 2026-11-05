package com.example.trabalhointerdiciplinar.ui.auth;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;

public class RecuperarSenhaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        EditText edtEmail = findViewById(R.id.edt_email);
        findViewById(R.id.btn_enviar).setOnClickListener(v -> {
            if (edtEmail.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Informe o e-mail", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Link de recuperação enviado (simulado)", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}

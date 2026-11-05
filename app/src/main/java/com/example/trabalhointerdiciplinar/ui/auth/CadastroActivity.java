package com.example.trabalhointerdiciplinar.ui.auth;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

import android.content.Intent;

public class CadastroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        EditText edtNome = findViewById(R.id.edt_nome);
        EditText edtEmail = findViewById(R.id.edt_email);
        EditText edtSenha = findViewById(R.id.edt_senha);
        EditText edtTelefone = findViewById(R.id.edt_telefone);
        EditText edtEndereco = findViewById(R.id.edt_endereco);
        Spinner spinner = findViewById(R.id.spinner_tipo);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_usuario, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        findViewById(R.id.btn_cadastrar).setOnClickListener(v -> {
            String nome = edtNome.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show();
                return;
            }
            TipoUsuario tipo = TipoUsuario.valueOf(spinner.getSelectedItem().toString());
            Usuario user = MockDataRepository.getInstance().cadastrarUsuario(
                    nome, email, senha, tipo,
                    edtEndereco.getText().toString().trim(),
                    edtTelefone.getText().toString().trim());
            SessionManager.getInstance(this).login(user);
            Intent intent;
            if (tipo == TipoUsuario.LOJISTA) {
                intent = new Intent(this, MerchantMainActivity.class);
            } else if (tipo == TipoUsuario.ADMIN) {
                intent = new Intent(this, AdminMainActivity.class);
            } else {
                intent = new Intent(this, BuyerMainActivity.class);
            }
            startActivity(intent);
            finish();
        });
    }
}

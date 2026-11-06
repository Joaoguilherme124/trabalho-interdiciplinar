package com.example.trabalhointerdiciplinar.ui.buyer;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.PerfilService;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Usuario;

/** "Meus dados": edição demonstrativa de nome, e-mail, telefone e endereço do usuário logado. */
public class EditarPerfilActivity extends AppCompatActivity {

    private EditText edtNome;
    private EditText edtEmail;
    private EditText edtTelefone;
    private EditText edtEndereco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);
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

        edtNome = findViewById(R.id.edt_nome);
        edtEmail = findViewById(R.id.edt_email);
        edtTelefone = findViewById(R.id.edt_telefone);
        edtEndereco = findViewById(R.id.edt_endereco);
        if (savedInstanceState == null) {
            edtNome.setText(usuario.getNome());
            edtEmail.setText(usuario.getEmail());
            edtTelefone.setText(usuario.getTelefone());
            edtEndereco.setText(usuario.getEndereco());
        }

        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_salvar).setOnClickListener(v -> salvar(usuario));
    }

    private void salvar(Usuario usuario) {
        edtNome.setError(null);
        edtEmail.setError(null);
        PerfilService.Resultado resultado = PerfilService.atualizar(usuario,
                MockDataRepository.getInstance(),
                edtNome.getText().toString(), edtEmail.getText().toString(),
                edtTelefone.getText().toString(), edtEndereco.getText().toString());

        switch (resultado) {
            case NOME_INVALIDO:
                edtNome.setError(getString(R.string.editar_erro_nome));
                edtNome.requestFocus();
                return;
            case EMAIL_INVALIDO:
                edtEmail.setError(getString(R.string.editar_erro_email));
                edtEmail.requestFocus();
                return;
            case EMAIL_EM_USO:
                edtEmail.setError(getString(R.string.editar_erro_email_uso));
                edtEmail.requestFocus();
                return;
            default:
                // Persiste a sessão local com os dados novos (mesmo mecanismo do login).
                SessionManager.getInstance(this).login(usuario);
                Toast.makeText(this, R.string.editar_sucesso, Toast.LENGTH_SHORT).show();
                finish();
        }
    }
}

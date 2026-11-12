package com.example.trabalhointerdiciplinar.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.adapter.AdminLojistaAdapter;
import com.example.trabalhointerdiciplinar.adapter.AdminLojaAdapter;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.StatusLoja;
import com.example.trabalhointerdiciplinar.ui.auth.LoginActivity;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

public class AdminMainActivity extends AppCompatActivity {

    private AdminLojaAdapter adapter;
    private AdminLojistaAdapter lojistaAdapter;
    private TextView txtComissao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_admin), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        txtComissao = findViewById(R.id.txt_comissao);
        RecyclerView rv = findViewById(R.id.rv_lojas);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminLojaAdapter(new AdminLojaAdapter.Listener() {
            @Override
            public void onAprovar(Loja loja) {
                MockDataRepository.getInstance().atualizarStatusLoja(loja.getId(), StatusLoja.APROVADA);
                Toast.makeText(AdminMainActivity.this, R.string.admin_loja_aprovada, Toast.LENGTH_SHORT).show();
                refresh();
            }

            @Override
            public void onRejeitar(Loja loja) {
                MockDataRepository.getInstance().atualizarStatusLoja(loja.getId(), StatusLoja.REJEITADA);
                Toast.makeText(AdminMainActivity.this, R.string.admin_loja_rejeitada, Toast.LENGTH_SHORT).show();
                refresh();
            }
        });
        rv.setAdapter(adapter);

        RecyclerView rvLojistas = findViewById(R.id.rv_lojistas);
        rvLojistas.setLayoutManager(new LinearLayoutManager(this));
        lojistaAdapter = new AdminLojistaAdapter();
        rvLojistas.setAdapter(lojistaAdapter);

        findViewById(R.id.btn_criar_lojista).setOnClickListener(v ->
                startActivity(new Intent(this, AdminCriarLojistaActivity.class)));

        findViewById(R.id.btn_sair).setOnClickListener(v -> {
            SessionManager.getInstance(this).logout();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        refresh();
    }

    private void refresh() {
        MockDataRepository repo = MockDataRepository.getInstance();
        txtComissao.setText(MoneyUtils.format(repo.getTotalComissoes(0.10)));
        adapter.submit(repo.getTodasLojas());
        lojistaAdapter.submit(repo.getUsuariosLojista());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lojistaAdapter != null) refresh();
    }
}

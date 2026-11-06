package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.preferencias.Preferencias;
import com.example.trabalhointerdiciplinar.data.preferencias.PrefsAcessibilidade;
import com.example.trabalhointerdiciplinar.data.preferencias.PrefsAcessibilidade.TamanhoTexto;

import com.google.android.material.materialswitch.MaterialSwitch;

/** Preferências de acessibilidade do aplicativo (texto, contraste e animações). */
public class AcessibilidadeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acessibilidade);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_acessibilidade), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());

        Preferencias prefs = Preferencias.obter(this);
        PrefsAcessibilidade a = prefs.acessibilidade();

        RadioGroup grupo = findViewById(R.id.grupo_texto);
        grupo.check(idDoTamanho(a.tamanhoTexto()));
        grupo.setOnCheckedChangeListener((g, id) -> {
            TamanhoTexto novo = tamanhoDoId(id);
            if (novo == a.tamanhoTexto()) return;
            a.definirTamanhoTexto(novo);
            prefs.acessibilidadeAlterada();
            recreate();
        });

        MaterialSwitch contraste = findViewById(R.id.switch_contraste);
        contraste.setChecked(a.altoContraste());
        contraste.setOnCheckedChangeListener((b, marcado) -> {
            a.definirAltoContraste(marcado);
            prefs.acessibilidadeAlterada();
            recreate();
        });

        MaterialSwitch animacoes = findViewById(R.id.switch_animacoes);
        animacoes.setChecked(a.reduzirAnimacoes());
        animacoes.setOnCheckedChangeListener((b, marcado) -> {
            a.definirReduzirAnimacoes(marcado);
            prefs.acessibilidadeAlterada();
        });

        findViewById(R.id.btn_config_sistema).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (RuntimeException e) {
                Toast.makeText(this, R.string.acess_sistema_indisponivel, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static int idDoTamanho(TamanhoTexto t) {
        switch (t) {
            case MAIOR:
                return R.id.rb_texto_maior;
            case MUITO_MAIOR:
                return R.id.rb_texto_muito_maior;
            default:
                return R.id.rb_texto_padrao;
        }
    }

    private static TamanhoTexto tamanhoDoId(int id) {
        if (id == R.id.rb_texto_maior) return TamanhoTexto.MAIOR;
        if (id == R.id.rb_texto_muito_maior) return TamanhoTexto.MUITO_MAIOR;
        return TamanhoTexto.PADRAO;
    }
}

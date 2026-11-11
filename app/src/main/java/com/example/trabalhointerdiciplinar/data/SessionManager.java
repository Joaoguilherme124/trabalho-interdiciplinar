package com.example.trabalhointerdiciplinar.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;

public class SessionManager {
    private static final String PREFS = "vitrine_session";
    private static SessionManager instance;
    private final SharedPreferences prefs;
    private Usuario usuarioLogado;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void login(Usuario usuario) {
        this.usuarioLogado = usuario;
        prefs.edit()
                .putString("id", usuario.getId())
                .putString("nome", usuario.getNome())
                .putString("email", usuario.getEmail())
                .putString("senha", usuario.getSenha())
                .putString("tipo", usuario.getTipo().name())
                .putString("endereco", usuario.getEndereco())
                .putString("telefone", usuario.getTelefone())
                .putBoolean("precisaTrocarCredenciais", usuario.isPrecisaTrocarCredenciais())
                .apply();
    }

    public void logout() {
        usuarioLogado = null;
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return usuarioLogado != null;
    }

    public Usuario getUsuario() {
        return usuarioLogado;
    }

    private void load() {
        String id = prefs.getString("id", null);
        if (id == null) return;
        usuarioLogado = new Usuario(
                id,
                prefs.getString("nome", ""),
                prefs.getString("email", ""),
                prefs.getString("senha", ""),
                TipoUsuario.valueOf(prefs.getString("tipo", TipoUsuario.COMPRADOR.name())),
                prefs.getString("endereco", ""),
                prefs.getString("telefone", ""),
                prefs.getBoolean("precisaTrocarCredenciais", false)
        );
    }
}

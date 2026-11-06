package com.example.trabalhointerdiciplinar.data.simulacao;

import android.content.Context;

import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Usuario;

/** Ponto único de acesso ao {@link PedidoSimulador} do aplicativo (montagem com Android). */
public final class Simulador {
    private static PedidoSimulador instancia;
    private static AvisosSistema avisos;
    private static PrefsArmazenamento prefs;

    private Simulador() {
    }

    public static synchronized PedidoSimulador obter(Context context) {
        if (instancia == null) {
            Context app = context.getApplicationContext();
            prefs = new PrefsArmazenamento(app);
            avisos = new AvisosSistema(app);
            instancia = new PedidoSimulador(prefs, MockDataRepository.getInstance(),
                    System::currentTimeMillis, new AgendadorAlarme(app), avisos, () -> {
                        Usuario u = SessionManager.getInstance(app).getUsuario();
                        return u == null ? null : u.getId();
                    });
            instancia.carregar();
        }
        return instancia;
    }

    public static AvisosSistema avisos(Context context) {
        obter(context);
        return avisos;
    }

    /** O pedido de permissão de notificação já foi apresentado (não repetir). */
    public static boolean permissaoJaPedida(Context context) {
        obter(context);
        return prefs.lerFlag("permissao_notif_pedida");
    }

    public static void marcarPermissaoPedida(Context context) {
        obter(context);
        prefs.gravarFlag("permissao_notif_pedida", true);
    }
}

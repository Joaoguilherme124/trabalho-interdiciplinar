package com.example.trabalhointerdiciplinar;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import com.example.trabalhointerdiciplinar.data.preferencias.Preferencias;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.util.AltoContraste;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Recarrega o andamento dos pedidos quando o processo nasce e aplica, em todas as telas, as
 * preferências de acessibilidade (tamanho de texto, alto contraste e animações reduzidas).
 */
public class PaellaApp extends Application {
    private final Map<Activity, Integer> versaoAplicada = new WeakHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Simulador.obter(this);
        Preferencias.obter(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // Roda antes do setContentView da tela: o novo tamanho vale para tudo que for inflado.
                Preferencias p = Preferencias.obter(activity);
                aplicarEscalaDeFonte(activity, p);
                versaoAplicada.put(activity, p.versao());
                if (p.acessibilidade().reduzirAnimacoes()) activity.overridePendingTransition(0, 0);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Preferencias p = Preferencias.obter(activity);
                Integer aplicada = versaoAplicada.get(activity);
                if (aplicada != null && aplicada != p.versao()) {
                    versaoAplicada.put(activity, p.versao());
                    activity.recreate();
                    return;
                }
                if (p.acessibilidade().altoContraste()) {
                    AltoContraste.observar(activity.getWindow().getDecorView());
                }
            }

            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        });
    }

    /** Tamanho final = escala do sistema x ajuste do app (nunca menor que a do sistema). */
    private static void aplicarEscalaDeFonte(Activity activity, Preferencias p) {
        float sistema = Resources.getSystem().getConfiguration().fontScale;
        float alvo = p.acessibilidade().escalaFinal(sistema);
        Resources res = activity.getResources();
        Configuration atual = res.getConfiguration();
        if (Math.abs(atual.fontScale - alvo) < 0.001f) return;
        Configuration nova = new Configuration(atual);
        nova.fontScale = alvo;
        res.updateConfiguration(nova, res.getDisplayMetrics());
    }
}

package com.example.trabalhointerdiciplinar.ui.buyer;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.CheckoutService;
import com.example.trabalhointerdiciplinar.data.CheckoutTentativa;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantém a tentativa de checkout viva durante rotações. O "processamento" é só um atraso
 * visual curto: nenhuma cobrança real existe. Sair da tela durante o processamento cancela.
 */
public class CheckoutViewModel extends ViewModel {
    private static final long PROCESSAMENTO_MS = 1500L;

    private final CheckoutTentativa tentativa = new CheckoutTentativa();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Boolean> processando = new MutableLiveData<>(false);
    private final MutableLiveData<List<String>> pedidosCriados = new MutableLiveData<>();
    private Runnable pendente;

    public LiveData<Boolean> getProcessando() {
        return processando;
    }

    /** Ids dos pedidos criados; null até a conclusão e novamente após {@link #consumirResultado()}. */
    public LiveData<List<String>> getPedidosCriados() {
        return pedidosCriados;
    }

    public FormaPagamento getForma() {
        return tentativa.getForma();
    }

    public boolean escolher(FormaPagamento forma) {
        return tentativa.escolher(forma);
    }

    public void consumirResultado() {
        pedidosCriados.setValue(null);
    }

    /** Inicia a simulação. Chamadas repetidas enquanto processa (ou depois de concluir) são ignoradas. */
    public void pagar(Usuario usuario, String data) {
        if (!tentativa.iniciar()) return;
        processando.setValue(true);
        FormaPagamento forma = tentativa.getForma();
        pendente = () -> {
            pendente = null;
            List<Pedido> criados = CheckoutService.finalizar(usuario, CarrinhoManager.getInstance(),
                    forma, MockDataRepository.getInstance(), data);
            tentativa.concluir();
            processando.setValue(false);
            List<String> ids = new ArrayList<>();
            for (Pedido p : criados) ids.add(p.getId());
            pedidosCriados.setValue(ids);
        };
        handler.postDelayed(pendente, PROCESSAMENTO_MS);
    }

    @Override
    protected void onCleared() {
        if (pendente != null) {
            handler.removeCallbacks(pendente);
            pendente = null;
            tentativa.cancelar();
        }
    }
}

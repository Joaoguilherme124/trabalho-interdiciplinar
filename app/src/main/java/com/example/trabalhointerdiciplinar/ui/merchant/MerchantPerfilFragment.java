package com.example.trabalhointerdiciplinar.ui.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.StatusLoja;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.ui.auth.LoginActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Minha Loja: edita os dados da loja do lojista. É a mesma loja exibida em "Quem prepara" nos
 * detalhes dos produtos; salvar atualiza o nome também nos produtos.
 */
public class MerchantPerfilFragment extends Fragment {
    private Loja loja;
    private List<String> categorias;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_loja, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        categorias = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.categorias)));
        categorias.remove("Todos");
        ((Spinner) view.findViewById(R.id.spinner_loja_categoria)).setAdapter(new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, categorias));

        Usuario u = SessionManager.getInstance(requireContext()).getUsuario();
        ((TextView) view.findViewById(R.id.txt_conta)).setText(u.getNome() + " · " + u.getEmail());
        view.findViewById(R.id.btn_cadastrar_loja).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CadastroLojaActivity.class)));
        view.findViewById(R.id.btn_salvar_loja).setOnClickListener(v -> salvar());
        view.findViewById(R.id.btn_sair).setOnClickListener(v -> sair());
    }

    @Override
    public void onResume() {
        super.onResume();
        View raiz = getView();
        if (raiz == null) return;
        String donoId = SessionManager.getInstance(requireContext()).getUsuario().getId();
        loja = MockDataRepository.getInstance().getLojaDoDono(donoId);
        boolean tem = loja != null;
        raiz.findViewById(R.id.bloco_form_loja).setVisibility(tem ? View.VISIBLE : View.GONE);
        raiz.findViewById(R.id.bloco_sem_loja).setVisibility(tem ? View.GONE : View.VISIBLE);
        TextView status = raiz.findViewById(R.id.txt_loja_status);
        if (!tem) {
            ((TextView) raiz.findViewById(R.id.txt_loja_nome)).setText(R.string.cozinha_sem_loja);
            ((TextView) raiz.findViewById(R.id.txt_loja_inicial)).setText("?");
            status.setVisibility(View.GONE);
            return;
        }
        status.setVisibility(View.VISIBLE);
        ((TextView) raiz.findViewById(R.id.txt_loja_nome)).setText(loja.getNome());
        ((TextView) raiz.findViewById(R.id.txt_loja_inicial)).setText(inicial(loja.getNome()));
        status.setText(rotuloStatus(loja.getStatus()));
        ((EditText) raiz.findViewById(R.id.edt_loja_nome)).setText(loja.getNome());
        ((EditText) raiz.findViewById(R.id.edt_loja_descricao)).setText(loja.getDescricao());
        ((EditText) raiz.findViewById(R.id.edt_loja_telefone)).setText(loja.getTelefone());
        ((EditText) raiz.findViewById(R.id.edt_loja_endereco)).setText(loja.getEndereco());
        ((EditText) raiz.findViewById(R.id.edt_loja_cnpj)).setText(loja.getCnpj());
        int idx = categorias.indexOf(loja.getCategoria());
        if (idx >= 0) ((Spinner) raiz.findViewById(R.id.spinner_loja_categoria)).setSelection(idx);
    }

    private static String inicial(String nome) {
        return nome == null || nome.isEmpty() ? "?" : nome.substring(0, 1).toUpperCase();
    }

    private int rotuloStatus(StatusLoja s) {
        switch (s) {
            case APROVADA:
                return R.string.minha_loja_status_aprovada;
            case REJEITADA:
                return R.string.minha_loja_status_rejeitada;
            default:
                return R.string.minha_loja_status_pendente;
        }
    }

    private void salvar() {
        View raiz = requireView();
        EditText nome = raiz.findViewById(R.id.edt_loja_nome);
        String novoNome = nome.getText().toString().trim();
        if (loja == null) return;
        if (novoNome.isEmpty()) {
            nome.setError(getString(R.string.minha_loja_erro_nome));
            nome.requestFocus();
            return;
        }
        loja.setNome(novoNome);
        loja.setDescricao(((EditText) raiz.findViewById(R.id.edt_loja_descricao)).getText().toString().trim());
        loja.setTelefone(((EditText) raiz.findViewById(R.id.edt_loja_telefone)).getText().toString().trim());
        loja.setEndereco(((EditText) raiz.findViewById(R.id.edt_loja_endereco)).getText().toString().trim());
        loja.setCategoria(((Spinner) raiz.findViewById(R.id.spinner_loja_categoria)).getSelectedItem().toString());
        MockDataRepository.getInstance().atualizarLoja(loja);
        ((TextView) raiz.findViewById(R.id.txt_loja_nome)).setText(loja.getNome());
        ((TextView) raiz.findViewById(R.id.txt_loja_inicial)).setText(inicial(loja.getNome()));
        Toast.makeText(requireContext(), R.string.minha_loja_salva, Toast.LENGTH_SHORT).show();
    }

    private void sair() {
        // Avisos de sistema e carrinho não podem vazar para a próxima conta.
        Simulador.avisos(requireContext()).cancelarTodas();
        SessionManager.getInstance(requireContext()).logout();
        CarrinhoManager.getInstance().limpar();
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}

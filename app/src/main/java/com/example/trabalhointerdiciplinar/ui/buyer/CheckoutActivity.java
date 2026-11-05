package com.example.trabalhointerdiciplinar.ui.buyer;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.ItemCarrinho;
import com.example.trabalhointerdiciplinar.model.ItemPedido;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Usuario user = SessionManager.getInstance(this).getUsuario();
        CarrinhoManager cart = CarrinhoManager.getInstance();

        ((TextView) findViewById(R.id.txt_endereco)).setText(user.getEndereco());
        ((TextView) findViewById(R.id.txt_total)).setText("Total: " + MoneyUtils.format(cart.getTotal()));

        StringBuilder resumo = new StringBuilder();
        Map<String, List<ItemCarrinho>> porLoja = cart.agruparPorLoja();
        for (Map.Entry<String, List<ItemCarrinho>> entry : porLoja.entrySet()) {
            String nomeLoja = entry.getValue().get(0).getProduto().getNomeLoja();
            resumo.append("Loja: ").append(nomeLoja).append("\n");
            for (ItemCarrinho item : entry.getValue()) {
                resumo.append("  • ").append(item.getProduto().getTitulo())
                        .append(" x").append(item.getQuantidade())
                        .append(" = ").append(MoneyUtils.format(item.getSubtotal()))
                        .append("\n");
            }
            resumo.append("\n");
        }
        ((TextView) findViewById(R.id.txt_resumo)).setText(resumo.toString().trim());

        Spinner spinner = findViewById(R.id.spinner_pagamento);
        spinner.setAdapter(ArrayAdapter.createFromResource(
                this, R.array.formas_pagamento, android.R.layout.simple_spinner_dropdown_item));

        findViewById(R.id.btn_confirmar).setOnClickListener(v -> {
            FormaPagamento pagamento = FormaPagamento.valueOf(spinner.getSelectedItem().toString());
            String data = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            for (Map.Entry<String, List<ItemCarrinho>> entry : porLoja.entrySet()) {
                List<ItemPedido> itens = new ArrayList<>();
                double total = 0;
                String nomeLoja = entry.getValue().get(0).getProduto().getNomeLoja();
                for (ItemCarrinho item : entry.getValue()) {
                    itens.add(new ItemPedido(
                            item.getProduto().getId(),
                            item.getProduto().getTitulo(),
                            item.getQuantidade(),
                            item.getProduto().getPreco()));
                    total += item.getSubtotal();
                }
                Pedido pedido = new Pedido(
                        "ped-" + UUID.randomUUID().toString().substring(0, 6),
                        user.getId(),
                        entry.getKey(),
                        nomeLoja,
                        itens,
                        total,
                        pagamento,
                        StatusPedido.PENDENTE,
                        data,
                        user.getEndereco());
                MockDataRepository.getInstance().addPedido(pedido);
            }

            cart.limpar();
            Toast.makeText(this, "Pedido(s) confirmado(s)!", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}

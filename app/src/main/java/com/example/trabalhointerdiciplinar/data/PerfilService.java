package com.example.trabalhointerdiciplinar.data;

import com.example.trabalhointerdiciplinar.model.Usuario;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Edição dos dados pessoais do usuário logado (demonstrativa, somente em memória/sessão local).
 * Só altera o usuário informado, nunca outros.
 */
public final class PerfilService {

    public enum Resultado {
        OK, NOME_INVALIDO, EMAIL_INVALIDO, EMAIL_EM_USO
    }

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private PerfilService() {
    }

    /**
     * Valida e aplica os novos dados. Atualiza o objeto da sessão e, se existir, o registro do
     * mesmo usuário no repositório (os dois podem ser instâncias diferentes após reiniciar o app).
     * Em qualquer erro nada é alterado.
     */
    public static Resultado atualizar(Usuario sessao, MockDataRepository repo, String nome,
                                      String email, String telefone, String endereco) {
        String nomeLimpo = nome == null ? "" : nome.trim();
        String emailLimpo = email == null ? "" : email.trim();

        if (nomeLimpo.isEmpty()) return Resultado.NOME_INVALIDO;
        if (!EMAIL.matcher(emailLimpo).matches()) return Resultado.EMAIL_INVALIDO;
        if (emailEmUsoPorOutro(repo, sessao.getId(), emailLimpo)) return Resultado.EMAIL_EM_USO;

        aplicar(sessao, nomeLimpo, emailLimpo, telefone, endereco);
        Usuario noRepositorio = repo.getUsuarioById(sessao.getId());
        if (noRepositorio != null && noRepositorio != sessao) {
            aplicar(noRepositorio, nomeLimpo, emailLimpo, telefone, endereco);
        }
        return Resultado.OK;
    }

    private static boolean emailEmUsoPorOutro(MockDataRepository repo, String idAtual, String email) {
        Usuario existente = repo.getUsuarioPorEmail(email.toLowerCase(Locale.ROOT));
        return existente != null && !existente.getId().equals(idAtual);
    }

    private static void aplicar(Usuario u, String nome, String email, String telefone, String endereco) {
        u.setNome(nome);
        u.setEmail(email);
        u.setTelefone(telefone == null ? "" : telefone.trim());
        u.setEndereco(endereco == null ? "" : endereco.trim());
    }
}

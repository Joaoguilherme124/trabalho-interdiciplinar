package com.example.trabalhointerdiciplinar.data;

import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;

import java.util.regex.Pattern;

/**
 * Cadastro público demonstrativo. Só cria contas de participante (COMPRADOR): o perfil nunca
 * vem do formulário, então não há como obter privilégios de loja ou administração por aqui.
 * Contas de lojista e admin continuam existindo apenas como contas de teste do repositório.
 */
public final class CadastroService {

    public enum Resultado {
        OK, NOME_INVALIDO, EMAIL_INVALIDO, EMAIL_EM_USO, SENHA_INVALIDA
    }

    /** Resultado do cadastro: o usuário só existe quando {@code resultado == OK}. */
    public static final class Retorno {
        public final Resultado resultado;
        public final Usuario usuario;

        Retorno(Resultado resultado, Usuario usuario) {
            this.resultado = resultado;
            this.usuario = usuario;
        }
    }

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private CadastroService() {
    }

    public static Retorno cadastrarParticipante(MockDataRepository repo, String nome, String email,
                                                String senha, String telefone, String endereco) {
        String nomeLimpo = nome == null ? "" : nome.trim();
        String emailLimpo = email == null ? "" : email.trim();

        if (nomeLimpo.isEmpty()) return new Retorno(Resultado.NOME_INVALIDO, null);
        if (!EMAIL.matcher(emailLimpo).matches()) return new Retorno(Resultado.EMAIL_INVALIDO, null);
        if (senha == null || senha.isEmpty()) return new Retorno(Resultado.SENHA_INVALIDA, null);
        if (repo.getUsuarioPorEmail(emailLimpo) != null) {
            return new Retorno(Resultado.EMAIL_EM_USO, null);
        }

        Usuario usuario = repo.cadastrarUsuario(nomeLimpo, emailLimpo, senha, TipoUsuario.COMPRADOR,
                endereco == null ? "" : endereco.trim(), telefone == null ? "" : telefone.trim());
        return new Retorno(Resultado.OK, usuario);
    }
}

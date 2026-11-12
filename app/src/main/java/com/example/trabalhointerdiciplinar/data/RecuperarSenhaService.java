package com.example.trabalhointerdiciplinar.data;

import com.example.trabalhointerdiciplinar.model.Usuario;

import java.util.regex.Pattern;

/**
 * Recuperação de senha no próprio app (sem e-mail). Confere se a conta existe e grava a senha nova.
 */
public final class RecuperarSenhaService {

    public enum Resultado {
        OK, EMAIL_INVALIDO, NAO_ENCONTRADO, SENHA_INVALIDA, SENHA_DIFERENTE
    }

    public static final class Retorno {
        public final Resultado resultado;
        public final Usuario usuario;

        Retorno(Resultado resultado, Usuario usuario) {
            this.resultado = resultado;
            this.usuario = usuario;
        }
    }

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private RecuperarSenhaService() {
    }

    public static Retorno localizar(MockDataRepository repo, String email) {
        String emailLimpo = email == null ? "" : email.trim();
        if (!EMAIL.matcher(emailLimpo).matches()) {
            return new Retorno(Resultado.EMAIL_INVALIDO, null);
        }
        Usuario usuario = repo.getUsuarioPorEmail(emailLimpo);
        if (usuario == null) return new Retorno(Resultado.NAO_ENCONTRADO, null);
        return new Retorno(Resultado.OK, usuario);
    }

    public static Resultado redefinir(MockDataRepository repo, String usuarioId, String senha,
                                      String confirmacao) {
        if (senha == null || senha.isEmpty()) return Resultado.SENHA_INVALIDA;
        if (!senha.equals(confirmacao == null ? "" : confirmacao)) return Resultado.SENHA_DIFERENTE;
        Usuario usuario = repo.getUsuarioById(usuarioId);
        if (usuario == null) return Resultado.NAO_ENCONTRADO;
        repo.atualizarSenha(usuarioId, senha);
        return Resultado.OK;
    }
}

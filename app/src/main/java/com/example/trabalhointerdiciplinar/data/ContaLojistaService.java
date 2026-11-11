package com.example.trabalhointerdiciplinar.data;

import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;

import java.util.regex.Pattern;

/**
 * Contas de lojista: só o admin cria. O cadastro público não usa esta classe.
 * No primeiro acesso o lojista troca e-mail e senha temporários.
 */
public final class ContaLojistaService {

    public enum Resultado {
        OK, NOME_INVALIDO, EMAIL_INVALIDO, EMAIL_EM_USO, SENHA_INVALIDA, SENHA_DIFERENTE, SENHA_IGUAL_ANTERIOR
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

    private ContaLojistaService() {
    }

    public static Retorno criar(MockDataRepository repo, String nome, String email, String senha,
                                String telefone) {
        String nomeLimpo = nome == null ? "" : nome.trim();
        String emailLimpo = email == null ? "" : email.trim();

        if (nomeLimpo.isEmpty()) return new Retorno(Resultado.NOME_INVALIDO, null);
        if (!EMAIL.matcher(emailLimpo).matches()) return new Retorno(Resultado.EMAIL_INVALIDO, null);
        if (senha == null || senha.isEmpty()) return new Retorno(Resultado.SENHA_INVALIDA, null);
        if (repo.getUsuarioPorEmail(emailLimpo) != null) {
            return new Retorno(Resultado.EMAIL_EM_USO, null);
        }

        Usuario usuario = repo.cadastrarUsuario(nomeLimpo, emailLimpo, senha, TipoUsuario.LOJISTA,
                "", telefone == null ? "" : telefone.trim());
        usuario.setPrecisaTrocarCredenciais(true);
        return new Retorno(Resultado.OK, usuario);
    }

    /**
     * Atualiza e-mail e, se informada, a senha. Com {@code senhaObrigatoria} a senha nova é
     * exigida e não pode ser a temporária. Sempre limpa o flag de primeiro acesso em caso de OK.
     */
    public static Resultado atualizarCredenciais(Usuario sessao, MockDataRepository repo,
                                                 String email, String senhaNova, String senhaConfirmacao,
                                                 boolean senhaObrigatoria) {
        String emailLimpo = email == null ? "" : email.trim();
        String nova = senhaNova == null ? "" : senhaNova;
        String confirma = senhaConfirmacao == null ? "" : senhaConfirmacao;

        if (!EMAIL.matcher(emailLimpo).matches()) return Resultado.EMAIL_INVALIDO;
        if (emailEmUsoPorOutro(repo, sessao.getId(), emailLimpo)) return Resultado.EMAIL_EM_USO;

        boolean vaiTrocarSenha = !nova.isEmpty() || senhaObrigatoria;
        if (senhaObrigatoria && nova.isEmpty()) return Resultado.SENHA_INVALIDA;
        if (vaiTrocarSenha) {
            if (nova.isEmpty()) return Resultado.SENHA_INVALIDA;
            if (!nova.equals(confirma)) return Resultado.SENHA_DIFERENTE;
            if (nova.equals(sessao.getSenha())) return Resultado.SENHA_IGUAL_ANTERIOR;
        }

        sessao.setEmail(emailLimpo);
        if (vaiTrocarSenha) sessao.setSenha(nova);
        sessao.setPrecisaTrocarCredenciais(false);

        Usuario noRepositorio = repo.getUsuarioById(sessao.getId());
        if (noRepositorio != null && noRepositorio != sessao) {
            noRepositorio.setEmail(emailLimpo);
            if (vaiTrocarSenha) noRepositorio.setSenha(nova);
            noRepositorio.setPrecisaTrocarCredenciais(false);
        }
        return Resultado.OK;
    }

    private static boolean emailEmUsoPorOutro(MockDataRepository repo, String idAtual, String email) {
        Usuario existente = repo.getUsuarioPorEmail(email);
        return existente != null && !existente.getId().equals(idAtual);
    }
}

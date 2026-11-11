package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.trabalhointerdiciplinar.data.CadastroService;
import com.example.trabalhointerdiciplinar.data.ContaLojistaService;
import com.example.trabalhointerdiciplinar.data.ContaLojistaService.Resultado;
import com.example.trabalhointerdiciplinar.data.ContaLojistaService.Retorno;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;

import org.junit.Test;

import java.util.UUID;

/** Só o admin cria lojista; o primeiro acesso troca e-mail e senha. */
public class ContaLojistaTest {

    private final MockDataRepository repo = MockDataRepository.getInstance();

    private String emailNovo() {
        return "loja" + UUID.randomUUID().toString().substring(0, 8) + "@temp.com";
    }

    @Test
    public void adminCriaLojistaQuePrecisaTrocarCredenciais() {
        String email = emailNovo();
        Retorno r = ContaLojistaService.criar(repo, "  Ana Loja ", email, "temp123", " 48 ");

        assertEquals(Resultado.OK, r.resultado);
        assertNotNull(r.usuario);
        assertEquals(TipoUsuario.LOJISTA, r.usuario.getTipo());
        assertTrue(r.usuario.isPrecisaTrocarCredenciais());
        assertEquals("Ana Loja", r.usuario.getNome());
        assertEquals(r.usuario, repo.autenticar(email, "temp123"));
    }

    @Test
    public void cadastroPublicoContinuaSoParticipante() {
        CadastroService.Retorno r = CadastroService.cadastrarParticipante(
                repo, "Joana", emailNovo(), "x", "", "");
        assertEquals(TipoUsuario.COMPRADOR, r.usuario.getTipo());
        assertFalse(r.usuario.isPrecisaTrocarCredenciais());
    }

    @Test
    public void emailDuplicadoNaoCriaSegundoLojista() {
        String email = emailNovo();
        ContaLojistaService.criar(repo, "A", email, "a", "");
        Retorno r = ContaLojistaService.criar(repo, "B", email, "b", "");
        assertEquals(Resultado.EMAIL_EM_USO, r.resultado);
        assertNull(r.usuario);
    }

    @Test
    public void primeiroAcessoTrocaEmailESenhaELiberaCozinha() {
        String email = emailNovo();
        Usuario u = ContaLojistaService.criar(repo, "Ana", email, "temp123", "").usuario;
        String novo = "ana" + UUID.randomUUID().toString().substring(0, 6) + "@loja.com";

        Resultado r = ContaLojistaService.atualizarCredenciais(
                u, repo, novo, "nova456", "nova456", true);

        assertEquals(Resultado.OK, r);
        assertFalse(u.isPrecisaTrocarCredenciais());
        assertEquals(novo, u.getEmail());
        assertEquals(u, repo.autenticar(novo, "nova456"));
        assertNull(repo.autenticar(email, "temp123"));
    }

    @Test
    public void primeiroAcessoRejeitaSenhaTemporaria() {
        String email = emailNovo();
        Usuario u = ContaLojistaService.criar(repo, "Ana", email, "temp123", "").usuario;

        assertEquals(Resultado.SENHA_IGUAL_ANTERIOR,
                ContaLojistaService.atualizarCredenciais(u, repo, email, "temp123", "temp123", true));
        assertTrue(u.isPrecisaTrocarCredenciais());
    }

    @Test
    public void perfilPodeTrocarSoEmailSemSenha() {
        String email = emailNovo();
        Usuario u = ContaLojistaService.criar(repo, "Ana", email, "temp123", "").usuario;
        u.setPrecisaTrocarCredenciais(false);
        String novo = "depois" + UUID.randomUUID().toString().substring(0, 6) + "@loja.com";

        Resultado r = ContaLojistaService.atualizarCredenciais(u, repo, novo, "", "", false);

        assertEquals(Resultado.OK, r);
        assertEquals(novo, u.getEmail());
        assertEquals(u, repo.autenticar(novo, "temp123"));
    }
}

package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;

import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.PerfilService;
import com.example.trabalhointerdiciplinar.data.PerfilService.Resultado;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.PerfilApresentacao;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/** Edição de dados do Perfil e iniciais do avatar. */
public class PerfilTest {

    private final MockDataRepository repo = MockDataRepository.getInstance();
    private Usuario usuario;

    @Before
    public void setUp() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        usuario = repo.cadastrarUsuario("Ana Souza", "ana" + sufixo + "@teste.com", "x",
                TipoUsuario.COMPRADOR, "Rua A, 1", "111");
    }

    @Test
    public void atualizacaoValidaAlteraNomeEmailTelefoneEEndereco() {
        Resultado r = PerfilService.atualizar(usuario, repo, "  Ana Maria  ", "ana.m@teste.com",
                " 222 ", " Rua B, 2 ");

        assertEquals(Resultado.OK, r);
        assertEquals("Ana Maria", usuario.getNome());
        assertEquals("ana.m@teste.com", usuario.getEmail());
        assertEquals("222", usuario.getTelefone());
        assertEquals("Rua B, 2", usuario.getEndereco());
    }

    @Test
    public void atualizacaoNaoAlteraOutrosUsuarios() {
        Usuario joao = repo.getUsuarioPorEmail("joao@email.com");
        String nomeAntes = joao.getNome();

        PerfilService.atualizar(usuario, repo, "Novo Nome", "novo@teste.com", "", "");

        assertEquals(nomeAntes, joao.getNome());
        assertEquals("joao@email.com", joao.getEmail());
    }

    @Test
    public void edicaoNaoQuebraAutenticacaoDoProprioUsuario() {
        PerfilService.atualizar(usuario, repo, "Ana", "ana.nova@teste.com", "", "");
        assertEquals(usuario, repo.autenticar("ana.nova@teste.com", "x"));
    }

    @Test
    public void copiaDaSessaoEORegistroDoRepositorioFicamSincronizados() {
        // Após reiniciar o app a sessão guarda uma cópia, não a instância do repositório.
        Usuario copiaDaSessao = new Usuario(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getSenha(), usuario.getTipo(), usuario.getEndereco(), usuario.getTelefone());

        PerfilService.atualizar(copiaDaSessao, repo, "Ana Lima", "ana.lima@teste.com", "", "");

        assertEquals("Ana Lima", repo.getUsuarioById(usuario.getId()).getNome());
        assertEquals("ana.lima@teste.com", repo.getUsuarioById(usuario.getId()).getEmail());
    }

    @Test
    public void nomeVazioERejeitadoSemAlterarNada() {
        String nomeAntes = usuario.getNome();
        assertEquals(Resultado.NOME_INVALIDO,
                PerfilService.atualizar(usuario, repo, "   ", usuario.getEmail(), "", ""));
        assertEquals(nomeAntes, usuario.getNome());
    }

    @Test
    public void emailInvalidoERejeitado() {
        String emailAntes = usuario.getEmail();
        assertEquals(Resultado.EMAIL_INVALIDO,
                PerfilService.atualizar(usuario, repo, "Ana", "sem-arroba", "", ""));
        assertEquals(Resultado.EMAIL_INVALIDO,
                PerfilService.atualizar(usuario, repo, "Ana", "a@b", "", ""));
        assertEquals(emailAntes, usuario.getEmail());
    }

    @Test
    public void emailDeOutroUsuarioERejeitado() {
        assertEquals(Resultado.EMAIL_EM_USO,
                PerfilService.atualizar(usuario, repo, "Ana", "JOAO@email.com", "", ""));
    }

    @Test
    public void manterOProprioEmailEPermitido() {
        assertEquals(Resultado.OK,
                PerfilService.atualizar(usuario, repo, "Ana Nova", usuario.getEmail(), "", ""));
    }

    @Test
    public void iniciaisUsamPrimeiroEUltimoNome() {
        assertEquals("JS", PerfilApresentacao.iniciais("João Silva"));
        assertEquals("JC", PerfilApresentacao.iniciais("  João Comprador "));
        assertEquals("MA", PerfilApresentacao.iniciais("maria de almeida"));
        assertEquals("A", PerfilApresentacao.iniciais("Ana"));
        assertEquals("?", PerfilApresentacao.iniciais("   "));
        assertEquals("?", PerfilApresentacao.iniciais(null));
    }

    @Test
    public void primeiroNomeParaSaudacao() {
        assertEquals("João", PerfilApresentacao.primeiroNome("João Comprador"));
        assertEquals("", PerfilApresentacao.primeiroNome(null));
    }
}

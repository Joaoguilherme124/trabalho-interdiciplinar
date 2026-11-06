package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.trabalhointerdiciplinar.data.CadastroService;
import com.example.trabalhointerdiciplinar.data.CadastroService.Resultado;
import com.example.trabalhointerdiciplinar.data.CadastroService.Retorno;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;

import org.junit.Test;

import java.util.UUID;

/** O cadastro público só cria participantes e valida os campos. */
public class CadastroTest {

    private final MockDataRepository repo = MockDataRepository.getInstance();

    private String emailNovo() {
        return "novo" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
    }

    @Test
    public void cadastroValidoCriaParticipanteQueConsegueEntrar() {
        String email = emailNovo();
        Retorno r = CadastroService.cadastrarParticipante(repo, "  Ana Souza ", email, "abc",
                " 111 ", " Rua A ");

        assertEquals(Resultado.OK, r.resultado);
        assertNotNull(r.usuario);
        assertEquals(TipoUsuario.COMPRADOR, r.usuario.getTipo());
        assertEquals("Ana Souza", r.usuario.getNome());
        assertEquals("111", r.usuario.getTelefone());
        assertEquals(r.usuario, repo.autenticar(email, "abc"));
    }

    @Test
    public void cadastroPublicoNuncaCriaContaAdminOuLojista() {
        // O serviço não recebe o perfil: qualquer cadastro sai como participante.
        for (int i = 0; i < 3; i++) {
            Retorno r = CadastroService.cadastrarParticipante(repo, "Teste", emailNovo(), "x", "", "");
            assertEquals(TipoUsuario.COMPRADOR, r.usuario.getTipo());
        }
    }

    @Test
    public void contasDeTesteDeLojistaEAdminContinuamExistindo() {
        assertEquals(TipoUsuario.LOJISTA, repo.autenticar("maria@loja.com", "123").getTipo());
        assertEquals(TipoUsuario.ADMIN, repo.autenticar("admin@vitrine.com", "123").getTipo());
    }

    @Test
    public void nomeVazioERejeitado() {
        Retorno r = CadastroService.cadastrarParticipante(repo, "  ", emailNovo(), "x", "", "");
        assertEquals(Resultado.NOME_INVALIDO, r.resultado);
        assertNull(r.usuario);
    }

    @Test
    public void emailInvalidoERejeitado() {
        assertEquals(Resultado.EMAIL_INVALIDO,
                CadastroService.cadastrarParticipante(repo, "Ana", "sem-arroba", "x", "", "").resultado);
        assertEquals(Resultado.EMAIL_INVALIDO,
                CadastroService.cadastrarParticipante(repo, "Ana", "", "x", "", "").resultado);
    }

    @Test
    public void senhaVaziaERejeitada() {
        assertEquals(Resultado.SENHA_INVALIDA,
                CadastroService.cadastrarParticipante(repo, "Ana", emailNovo(), "", "", "").resultado);
        assertEquals(Resultado.SENHA_INVALIDA,
                CadastroService.cadastrarParticipante(repo, "Ana", emailNovo(), null, "", "").resultado);
    }

    @Test
    public void emailJaCadastradoERejeitadoSemCriarSegundaConta() {
        Retorno r = CadastroService.cadastrarParticipante(repo, "Ana", "JOAO@email.com", "x", "", "");
        assertEquals(Resultado.EMAIL_EM_USO, r.resultado);
        assertNull(r.usuario);
        assertEquals("João Comprador", repo.autenticar("joao@email.com", "123").getNome());
    }

    @Test
    public void telefoneEEnderecoSaoOpcionais() {
        Retorno r = CadastroService.cadastrarParticipante(repo, "Ana", emailNovo(), "x", null, null);
        assertEquals(Resultado.OK, r.resultado);
        assertEquals("", r.usuario.getTelefone());
        assertEquals("", r.usuario.getEndereco());
    }
}

package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.RecuperarSenhaService;
import com.example.trabalhointerdiciplinar.data.RecuperarSenhaService.Resultado;
import com.example.trabalhointerdiciplinar.data.RecuperarSenhaService.Retorno;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;

import org.junit.Test;

import java.util.UUID;

public class RecuperarSenhaTest {

    private final MockDataRepository repo = MockDataRepository.getInstance();

    @Test
    public void emailCadastradoPodeRedefinirSenha() {
        String email = "recup" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
        repo.cadastrarUsuario("Ana", email, "antiga", TipoUsuario.COMPRADOR, "", "");

        Retorno local = RecuperarSenhaService.localizar(repo, "  " + email.toUpperCase() + " ");
        assertEquals(Resultado.OK, local.resultado);
        assertNotNull(local.usuario);

        Resultado r = RecuperarSenhaService.redefinir(repo, local.usuario.getId(), "nova123", "nova123");
        assertEquals(Resultado.OK, r);
        assertNotNull(repo.autenticar(email, "nova123"));
        assertNull(repo.autenticar(email, "antiga"));
    }

    @Test
    public void emailInexistenteERejeitado() {
        Retorno r = RecuperarSenhaService.localizar(repo, "naoexiste@teste.com");
        assertEquals(Resultado.NAO_ENCONTRADO, r.resultado);
        assertNull(r.usuario);
    }

    @Test
    public void senhasDiferentesNaoAlteramLogin() {
        String email = "dif" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
        Usuario u = repo.cadastrarUsuario("Ana", email, "antiga", TipoUsuario.COMPRADOR, "", "");
        assertEquals(Resultado.SENHA_DIFERENTE,
                RecuperarSenhaService.redefinir(repo, u.getId(), "abc", "xyz"));
        assertEquals("antiga", repo.getUsuarioPorEmail(email).getSenha());
    }
}

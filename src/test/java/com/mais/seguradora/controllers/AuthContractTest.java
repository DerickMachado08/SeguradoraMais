package com.mais.seguradora.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.CadastroClienteRequest;
import com.mais.seguradora.dto.ClienteResponse;
import com.mais.seguradora.dto.SessaoResponse;
import com.mais.seguradora.handler.GlobalExceptionHandler;
import com.mais.seguradora.model.Admin;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Corretor;
import com.mais.seguradora.services.AdminService;
import com.mais.seguradora.services.ClienteService;
import com.mais.seguradora.services.CorretorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import java.nio.charset.StandardCharsets;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Standalone MVC + mocks: não inicializa Spring Boot, JPA, SMTP ou banco. */
class AuthContractTest {
    private MockMvc mvc;
    private ClienteService clientes;
    private CorretorService corretores;
    private AdminService admins;
    private ObjectMapper mapper;

    @BeforeEach void setup() {
        clientes = mock(ClienteService.class);
        corretores = mock(CorretorService.class);
        admins = mock(AdminService.class);
        LoginController login = new LoginController();
        ReflectionTestUtils.setField(login, "clienteservice", clientes);
        ReflectionTestUtils.setField(login, "corretorService", corretores);
        ReflectionTestUtils.setField(login, "adminService", admins);
        ClienteController cadastro = new ClienteController();
        ReflectionTestUtils.setField(cadastro, "service", clientes);
        mapper = new ObjectMapper().findAndRegisterModules().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(login, cadastro, new SessaoController())
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator)
                .setMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8), new MappingJackson2HttpMessageConverter(mapper)).build();
    }

    @Test void loginMantemContratoWebERenovaSessao() throws Exception {
        Cliente cliente = cliente();
        when(clientes.autenticar("cliente@example.test", "Teste@123")).thenReturn(cliente);
        MockHttpSession session = new MockHttpSession();
        String previousId = session.getId();
        mvc.perform(post("/auten/login").session(session).param("email", "cliente@example.test").param("senha", "Teste@123"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.redirect").value("/dashboardCliente"))
                .andExpect(jsonPath("$.senha").doesNotExist());
        assertNotEquals(previousId, session.getId());
        assertSame(cliente, session.getAttribute(SessionSecurity.USER_ATTRIBUTE));
        assertNotNull(SessionSecurity.token(session));
    }

    @Test void loginDeCorretorEMecanismoAdminLegadoMantemPerfisServidor() throws Exception {
        Corretor corretor = new Corretor(); corretor.setId(20); corretor.setNome("Corretor de Teste");
        when(corretores.autenticar("corretor@example.test", "Teste@123")).thenReturn(corretor);
        mvc.perform(post("/auten/login").param("email", "corretor@example.test").param("senha", "Teste@123").param("perfil", "ADMIN"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("CORRETOR"));
        Admin admin = new Admin(); admin.setId(30); admin.setName("Admin de Teste");
        when(admins.autenticar("admin@example.test", "Teste@123")).thenReturn(admin);
        mvc.perform(post("/auten/login").param("email", "admin@example.test").param("senha", "Teste@123"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test void loginInvalidoPreserva400TextualELimpaSessaoAnterior() throws Exception {
        MockHttpSession session = session(cliente());
        mvc.perform(post("/auten/login").session(session).param("email", "invalido@example.test").param("senha", "Teste@123"))
                .andExpect(status().isBadRequest()).andExpect(content().string("Email ou senha inválidos"));
        assertTrue(session.isInvalid());
    }

    @Test void loginLimitaEntradaAntesDeConsultarServicos() throws Exception {
        mvc.perform(post("/auten/login").param("email", "cliente@example.test").param("senha", "á".repeat(37)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(clientes, corretores, admins);
    }

    @Test void sessaoAnonimaRetorna401SemCriarSessao() throws Exception {
        var result = mvc.perform(get("/auten/sessao")).andExpect(status().isUnauthorized()).andReturn();
        assertNull(result.getRequest().getSession(false));
    }

    @Test void sessaoRetornaSomenteProprioResumoSemHash() throws Exception {
        mvc.perform(get("/auten/sessao").session(session(cliente())).param("id", "999"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.id").value(10))
                .andExpect(jsonPath("$.usuario.nome").value("Cliente de Teste"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist())
                .andExpect(jsonPath("$.usuario.cpf").doesNotExist())
                .andExpect(jsonPath("$.csrfToken").isString());
    }

    @Test void logoutExigeTokenDaPropriaSessao() throws Exception {
        MockHttpSession session = session(cliente());
        SessionSecurity.renewToken(session);
        mvc.perform(post("/auten/logout").session(session)).andExpect(status().isForbidden());
        assertFalse(session.isInvalid());
        mvc.perform(post("/auten/logout").session(session).header("X-Session-Token", "token-incorreto"))
                .andExpect(status().isForbidden());
        assertFalse(session.isInvalid());
    }

    @Test void logoutInvalidaSessaoEExpiraCookie() throws Exception {
        MockHttpSession session = session(cliente());
        String token = SessionSecurity.renewToken(session);
        mvc.perform(post("/auten/logout").session(session).header("X-Session-Token", token))
                .andExpect(status().isNoContent()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().exists("Set-Cookie"));
        assertTrue(session.isInvalid());
        mvc.perform(post("/auten/logout")).andExpect(status().isNoContent());
    }

    @Test void cadastroIgnoraIdentidadeEPrivilegiosEnviadosENuncaRetornaSenha() throws Exception {
        when(clientes.cadastrar(any())).thenAnswer(invocation -> {
            Cliente saved = invocation.getArgument(0); assertNull(saved.getId()); saved.setId(42); saved.setSenha("hash-nao-deve-sair"); return saved;
        });
        String json = validCadastro().replace("{", "{\"id\":999,\"perfil\":\"ADMIN\",");
        mvc.perform(post("/cliente").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.nome").value("Cliente de Teste"))
                .andExpect(jsonPath("$.senha").doesNotExist()).andExpect(jsonPath("$.perfil").doesNotExist());
        // O ID do mock é atribuído somente após receber a entidade nova.
        ArgumentCaptor<Cliente> argument = ArgumentCaptor.forClass(Cliente.class);
        verify(clientes).cadastrar(argument.capture());
        assertEquals("Cliente de Teste", argument.getValue().getNome());
    }

    @Test void cadastroInvalidoRetornaErrosPorCampoSemSalvar() throws Exception {
        mvc.perform(post("/cliente").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"x\",\"email\":\"invalido\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.nome").isString()).andExpect(jsonPath("$.senha").isString());
        verify(clientes, never()).cadastrar(any());
    }

    @Test void dtoDeCadastroNaoTransportaIdPersistente() throws Exception {
        CadastroClienteRequest dto = mapper.readValue(validCadastro().replace("{", "{\"id\":999,"), CadastroClienteRequest.class);
        assertNull(dto.toEntity().getId());
        assertFalse(dto.toString().contains("Teste@123"));
        SessaoResponse resumo = new SessaoResponse("CLIENTE", new SessaoResponse.UsuarioResumo(10, "Cliente de Teste", "cliente@example.test"), "nonce-isolado");
        assertFalse(resumo.toString().contains("nonce-isolado"));
        assertFalse(ClienteResponse.from(cliente()).toString().contains("cliente@example.test"));
    }

    private Cliente cliente() {
        Cliente value = new Cliente(); value.setId(10); value.setNome("Cliente de Teste");
        value.setEmail("cliente@example.test"); value.setSenha("hash-nao-deve-sair"); return value;
    }
    private MockHttpSession session(Object user) {
        MockHttpSession session = new MockHttpSession(); session.setAttribute(SessionSecurity.USER_ATTRIBUTE, user); return session;
    }
    private String validCadastro() {
        return """
                {"nome":"Cliente de Teste","cpf":"529.982.247-25","dataNascimento":"1990-01-01",
                 "email":"cliente@example.test","telefone":"(11) 99999-0000","logradouro":"Rua de Teste",
                 "estado":"SP","cidade":"Cidade","bairro":"Bairro","numero":"100","cep":"01001-000","senha":"Teste@123"}
                """;
    }
}

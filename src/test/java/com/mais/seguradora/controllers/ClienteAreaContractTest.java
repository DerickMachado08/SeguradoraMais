package com.mais.seguradora.controllers;

import com.fasterxml.jackson.databind.*;
import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.enummerated.*;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Controllers + serviços reais, repositórios em memória. Não inicia Boot/JPA/SMTP. */
class ClienteAreaContractTest {
    MockMvc mvc;
    ClienteRepository clients;
    ContratacaoRepository requests;
    ApoliceRepository products;
    BemPatrimonialRepository assets;
    EmailService email;
    Cliente owner, other;
    Apolice product;
    Map<Integer, Contratacao> rows;
    AtomicInteger sequence;
    @BeforeEach void setup() {
        owner = Cliente.builder().id(10).nome("Cliente Teste").email("cliente@example.test").senha("HASH_PRIVADO").cpf("529.982.247-25").build();
        other = Cliente.builder().id(11).nome("Outro Cliente").email("outro@example.test").senha("OUTRO_HASH").build();
        product = new Apolice(); product.setId(1); product.setTipoApolice("CELULAR"); product.setStatus(StatusApolice.ATIVA); product.setNumeroApolice("AP-TESTE");
        Cobertura coverage = new Cobertura(); coverage.setNome("Danos acidentais"); coverage.setDescricao("Cobertura de teste"); product.setCoberturas(List.of(coverage));
        rows = new LinkedHashMap<>(); sequence = new AtomicInteger(100);
        clients = mock(ClienteRepository.class); requests = mock(ContratacaoRepository.class); products = mock(ApoliceRepository.class); assets = mock(BemPatrimonialRepository.class); email = mock(EmailService.class);
        when(clients.findForSolicitacao(10)).thenReturn(Optional.of(owner));
        when(products.findAll()).thenReturn(List.of(product)); when(products.findById(1)).thenReturn(Optional.of(product));
        when(requests.findByClienteId(anyInt())).thenAnswer(i -> rows.values().stream().filter(c -> c.getCliente().getId().equals(i.getArgument(0))).toList());
        when(requests.findById(anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))));
        when(requests.findByIdAndClienteId(anyInt(), anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))).filter(c -> c.getCliente().getId().equals(i.getArgument(1))));
        when(requests.findByClienteAndApolice_TipoApoliceAndStatusNot(any(), anyString(), any())).thenAnswer(i -> rows.values().stream().filter(c -> c.getCliente().getId().equals(((Cliente)i.getArgument(0)).getId()) && c.getApolice().getTipoApolice().equals(i.getArgument(1)) && c.getStatus() != i.getArgument(2)).toList());
        when(requests.findFirstByClienteAndApoliceAndStatus(any(), any(), any())).thenAnswer(i -> rows.values().stream().filter(c -> c.getCliente().getId().equals(((Cliente)i.getArgument(0)).getId()) && c.getApolice().getId().equals(((Apolice)i.getArgument(1)).getId()) && c.getStatus() == i.getArgument(2)).findFirst());
        when(requests.save(any())).thenAnswer(i -> { Contratacao c = i.getArgument(0); if (c.getId() == null) c.setId(sequence.incrementAndGet()); rows.put(c.getId(), c); return c; });
        when(assets.findByContratacaoId(anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))).flatMap(c -> c.getBensPatriomiais().stream().findFirst()));
        when(assets.save(any())).thenAnswer(i -> { BemPatrimonial b = i.getArgument(0); b.setId(50); b.getContratacao().getBensPatriomiais().add(b); return b; });
        ContratacaoService flow = new ContratacaoService(); ReflectionTestUtils.setField(flow, "contratacaoRepository", requests); ReflectionTestUtils.setField(flow, "bemPatrimonialRepository", assets); ReflectionTestUtils.setField(flow, "emailService", email);
        ClienteAreaService service = new ClienteAreaService(clients, products, requests, flow);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean(); validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new ClienteMobileController(service)).setValidator(validator).setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    }
    MockHttpSession session(Object who) { MockHttpSession value = new MockHttpSession(); value.setAttribute(SessionSecurity.USER_ATTRIBUTE, who); SessionSecurity.renewToken(value); return value; }
    Contratacao row(Cliente user, StatusContratacao status) {
        Contratacao c = new Contratacao(); c.setId(sequence.incrementAndGet()); c.setCliente(user); c.setApolice(product); c.setStatus(status); c.setDataContratacao(LocalDate.of(2026, 1, 2)); rows.put(c.getId(), c); return c;
    }
    String body() { return "{\"apoliceId\":1,\"descricao\":\"Telefone modelo teste\",\"valorEstimado\":1500.50,\"dataAquisicao\":\"2024-01-15\"}"; }
    @Test void rejectsAnonymousAndOtherRolesOnEveryReadRoute() throws Exception {
        for (String route : List.of("dashboard", "catalogo", "solicitacoes", "solicitacoes/1", "apolices")) {
            var result = mvc.perform(get("/api/cliente/" + route)).andExpect(status().isUnauthorized()).andExpect(header().string("Cache-Control", "no-store")).andReturn();
            assertNull(result.getRequest().getSession(false));
            mvc.perform(get("/api/cliente/" + route).session(session(new Admin()))).andExpect(status().isForbidden());
            mvc.perform(get("/api/cliente/" + route).session(session(new Corretor()))).andExpect(status().isForbidden());
        }
        verifyNoInteractions(requests, products);
    }
    @Test void listsOnlyOwnRowsAndNeverSerializesSensitiveEntities() throws Exception {
        Contratacao mine = row(owner, StatusContratacao.PENDENTE); row(other, StatusContratacao.APOLICE_EMITIDA);
        String json = mvc.perform(get("/api/cliente/solicitacoes").session(session(owner)).param("clienteId", "11"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(mine.getId()))
                .andExpect(jsonPath("$[0].dataSolicitacao").value("2026-01-02")).andReturn().getResponse().getContentAsString();
        for (String secret : List.of("HASH_PRIVADO", "OUTRO_HASH", "cpf", "senha", "email", "corretores", "cliente")) assertFalse(json.contains(secret));
    }
    @Test void foreignAndMissingDetailAreIndistinguishable() throws Exception {
        Contratacao foreign = row(other, StatusContratacao.PENDENTE);
        var a = mvc.perform(get("/api/cliente/solicitacoes/" + foreign.getId()).session(session(owner))).andExpect(status().isNotFound()).andReturn();
        var b = mvc.perform(get("/api/cliente/solicitacoes/9999").session(session(owner))).andExpect(status().isNotFound()).andReturn();
        assertEquals(a.getResponse().getContentAsString(), b.getResponse().getContentAsString());
        mvc.perform(get("/api/cliente/solicitacoes/invalido").session(session(owner))).andExpect(status().isBadRequest());
    }
    @Test void derivesDashboardAndPolicyListFromOwnActualStatuses() throws Exception {
        row(owner, StatusContratacao.PENDENTE); row(owner, StatusContratacao.ANALISANDO); row(owner, StatusContratacao.APOLICE_EMITIDA); row(owner, StatusContratacao.CANCELADA); row(other, StatusContratacao.APOLICE_EMITIDA);
        mvc.perform(get("/api/cliente/dashboard").session(session(owner))).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSolicitacoes").value(4)).andExpect(jsonPath("$.emAndamento").value(2)).andExpect(jsonPath("$.apolicesEmitidas").value(1));
        mvc.perform(get("/api/cliente/apolices").session(session(owner))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].status").value("APOLICE_EMITIDA"));
    }
    @Test void emptyStateIsEmptyAndCatalogHonorsAvailability() throws Exception {
        mvc.perform(get("/api/cliente/dashboard").session(session(owner))).andExpect(jsonPath("$.totalSolicitacoes").value(0)).andExpect(jsonPath("$.recentes.length()").value(0));
        mvc.perform(get("/api/cliente/catalogo").session(session(owner))).andExpect(jsonPath("$[0].disponivel").value(true));
        row(owner, StatusContratacao.PENDENTE);
        mvc.perform(get("/api/cliente/catalogo").session(session(owner))).andExpect(jsonPath("$[0].disponivel").value(false));
        rows.clear(); product.setStatus(StatusApolice.CANCELADA);
        mvc.perform(get("/api/cliente/catalogo").session(session(owner))).andExpect(jsonPath("$[0].disponivel").value(false));
    }
    @Test void postRequiresOwnSessionNonceAndClientRole() throws Exception {
        mvc.perform(post("/api/cliente/solicitacoes").contentType(MediaType.APPLICATION_JSON).content(body())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/cliente/solicitacoes").session(session(owner)).contentType(MediaType.APPLICATION_JSON).content(body())).andExpect(status().isForbidden());
        mvc.perform(post("/api/cliente/solicitacoes").session(session(owner)).header("X-Session-Token", SessionSecurity.token(session(other))).contentType(MediaType.APPLICATION_JSON).content(body())).andExpect(status().isForbidden());
        MockHttpSession staff = session(new Corretor());
        mvc.perform(post("/api/cliente/solicitacoes").session(staff).header("X-Session-Token", SessionSecurity.token(staff)).contentType(MediaType.APPLICATION_JSON).content(body())).andExpect(status().isForbidden());
        verifyNoInteractions(clients, requests, assets);
    }
    @Test void createsPendingRequestWithRealFlowAndIgnoresPrivilegeInjection() throws Exception {
        MockHttpSession session = session(owner);
        String json = body().replace("{", "{\"clienteId\":11,\"status\":\"APOLICE_EMITIDA\",\"valorSeguro\":0,\"id\":999,");
        mvc.perform(post("/api/cliente/solicitacoes").session(session).header("X-Session-Token", SessionSecurity.token(session)).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDENTE")).andExpect(jsonPath("$.valorSeguro").isEmpty())
                .andExpect(jsonPath("$.bens[0].descricao").value("Telefone modelo teste")).andExpect(jsonPath("$.bens[0].valorEstimado").value(1500.50));
        Contratacao saved = rows.values().iterator().next(); assertSame(owner, saved.getCliente()); assertEquals(1, saved.getBensPatriomiais().size()); assertNull(saved.getValorContratacao());
        verify(clients).findForSolicitacao(10); verifyNoInteractions(email);
    }
    @Test void repeatingPostDoesNotCreateAnotherRowOrAsset() throws Exception {
        MockHttpSession session = session(owner);
        for (int expected : List.of(201, 409)) mvc.perform(post("/api/cliente/solicitacoes").session(session).header("X-Session-Token", SessionSecurity.token(session)).contentType(MediaType.APPLICATION_JSON).content(body())).andExpect(status().is(expected));
        assertEquals(1, rows.size()); verify(assets, times(1)).save(any());
    }
    @Test void resumesOwnDraftWithoutReplacingOtherRequests() throws Exception {
        Contratacao draft = row(owner, StatusContratacao.AGUARDANDO_BEM); Contratacao foreign = row(other, StatusContratacao.APOLICE_EMITIDA);
        MockHttpSession session = session(owner);
        mvc.perform(post("/api/cliente/solicitacoes").session(session).header("X-Session-Token", SessionSecurity.token(session)).contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(draft.getId()));
        assertEquals(2, rows.size()); assertEquals(StatusContratacao.APOLICE_EMITIDA, foreign.getStatus());
    }
    @Test void invalidDatePrecisionValueAndDescriptionNeverPersist() throws Exception {
        MockHttpSession session = session(owner);
        for (String invalid : List.of(body().replace("2024-01-15", "2017-12-31"), body().replace("2024-01-15", LocalDate.now().plusDays(1).toString()), body().replace("1500.50", "1.001"), body().replace("1500.50", "100000000"), body().replace("Telefone modelo teste", "  a  "), body().replace("2024-01-15", "2024-02-30"))) {
            mvc.perform(post("/api/cliente/solicitacoes").session(session).header("X-Session-Token", SessionSecurity.token(session)).contentType(MediaType.APPLICATION_JSON).content(invalid)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(clients, requests, assets);
    }
    @Test void hidesInfrastructureErrorsAndNeverCallsLegacyEmission() throws Exception {
        when(requests.findByClienteId(10)).thenThrow(new RuntimeException("DATABASE_SECRET"));
        String body = mvc.perform(get("/api/cliente/solicitacoes").session(session(owner))).andExpect(status().isServiceUnavailable()).andReturn().getResponse().getContentAsString();
        assertFalse(body.contains("DATABASE_SECRET")); verifyNoInteractions(email);
    }
    @Test void neverReturnsAnAssetAssociatedWithAnotherClient() throws Exception {
        Contratacao mine = row(owner, StatusContratacao.APOLICE_EMITIDA); BemPatrimonial wrong = new BemPatrimonial(); wrong.setCliente(other); wrong.setDescricao("SEGREDO_DE_OUTRO_CLIENTE"); mine.setBensPatriomiais(List.of(wrong));
        mvc.perform(get("/api/cliente/solicitacoes/" + mine.getId()).session(session(owner))).andExpect(status().isOk()).andExpect(jsonPath("$.bens.length()").value(0));
    }
}

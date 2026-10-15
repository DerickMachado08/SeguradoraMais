package com.mais.seguradora.controllers;

import com.fasterxml.jackson.databind.*;
import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.enummerated.*;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.CorretorAreaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Contratos reais em MVC standalone; sem contexto Boot, banco, SMTP ou Stripe. */
class CorretorAreaContractTest {
    CorretorRepository brokers; ContratacaoRepository requests; BemPatrimonialRepository assets; PagamentoRepository payments;
    MockMvc mvc; Corretor broker, colleague, foreign; Cliente client; Apolice product; Contratacao row; BemPatrimonial asset;
    @BeforeEach void setup() {
        brokers = mock(CorretorRepository.class); requests = mock(ContratacaoRepository.class);
        assets = mock(BemPatrimonialRepository.class); payments = mock(PagamentoRepository.class);
        broker = new Corretor(); broker.setId(20); colleague = new Corretor(); colleague.setId(21); foreign = new Corretor(); foreign.setId(22);
        client = Cliente.builder().id(10).nome("Cliente Teste").email("PRIVATE_EMAIL").senha("PRIVATE_HASH").cpf("PRIVATE_CPF").build();
        product = new Apolice(); product.setId(1); product.setTipoApolice("Celular"); product.setNumeroApolice("REF-1"); product.setStatus(StatusApolice.ATIVA);
        product.setCorretores(List.of(broker, colleague)); product.setCoberturas(List.of());
        row = new Contratacao(); row.setId(101); row.setCliente(client); row.setApolice(product); row.setStatus(StatusContratacao.PENDENTE); row.setDataContratacao(LocalDate.now());
        asset = new BemPatrimonial(); asset.setId(1); asset.setCliente(client); asset.setContratacao(row); asset.setDescricao("Celular teste"); asset.setValorEstimado(BigDecimal.TEN);
        row.setBensPatriomiais(List.of(asset));
        when(brokers.existsById(anyInt())).thenReturn(true);
        when(requests.findDistinctByApolice_Corretores_Id(anyInt())).thenReturn(List.of(row));
        when(requests.findByIdAndApolice_Corretores_Id(anyInt(), anyInt())).thenAnswer(call -> call.<Integer>getArgument(0) == 101 ? Optional.of(row) : Optional.empty());
        when(requests.findForCorretorOperation(anyInt(), anyInt())).thenAnswer(call -> call.<Integer>getArgument(0) == 101 ? Optional.of(row) : Optional.empty());
        when(assets.findByContratacaoId(101)).thenReturn(Optional.of(asset));
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean(); validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new CorretorMobileController(new CorretorAreaService(brokers, requests, assets, payments)))
                .setValidator(validator).setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    }
    MockHttpSession session(Object user) { MockHttpSession s = new MockHttpSession(); s.setAttribute(SessionSecurity.USER_ATTRIBUTE, user); SessionSecurity.renewToken(s); return s; }
    MockHttpServletRequestBuilder operation(String action, String body, Object user) {
        MockHttpSession s = session(user);
        return (action.equals("preco") ? put("/api/corretor/solicitacoes/101/preco") : post("/api/corretor/solicitacoes/101/" + action))
                .session(s).header("X-Session-Token", SessionSecurity.token(s)).contentType(MediaType.APPLICATION_JSON).content(body);
    }
    @Test void anonymousAndWrongProfilesCannotReadOrMutate() throws Exception {
        mvc.perform(get("/api/corretor/dashboard")).andExpect(status().isUnauthorized()).andExpect(header().string("Cache-Control", "no-store"));
        for (Object user : List.of(client, new Admin())) {
            mvc.perform(get("/api/corretor/solicitacoes").session(session(user))).andExpect(status().isForbidden());
            mvc.perform(operation("emitir", "{\"valor\":10}", user)).andExpect(status().isForbidden());
            mvc.perform(operation("recusar", "", user)).andExpect(status().isForbidden());
            mvc.perform(operation("preco", "{\"valor\":10}", user)).andExpect(status().isForbidden());
        }
        verifyNoInteractions(requests, payments, assets, brokers);
    }
    @Test void deletedOrInvalidBrokerSessionIsRejected() throws Exception {
        when(brokers.existsById(20)).thenReturn(false);
        mvc.perform(get("/api/corretor/dashboard").session(session(broker))).andExpect(status().isUnauthorized());
        broker.setId(0); mvc.perform(get("/api/corretor/dashboard").session(session(broker))).andExpect(status().isUnauthorized());
        verifyNoInteractions(requests);
    }
    @Test void everyMutationRequiresOwnNonce() throws Exception {
        MockHttpSession s = session(broker);
        for (String token : List.of("", SessionSecurity.token(session(colleague)))) {
            mvc.perform(put("/api/corretor/solicitacoes/101/preco").session(s).header("X-Session-Token", token).contentType(MediaType.APPLICATION_JSON).content("{\"valor\":10}")).andExpect(status().isForbidden());
            for (String action : List.of("emitir", "recusar")) mvc.perform(post("/api/corretor/solicitacoes/101/" + action).session(s).header("X-Session-Token", token).contentType(MediaType.APPLICATION_JSON).content("{\"valor\":10}")).andExpect(status().isForbidden());
        }
        verifyNoInteractions(requests, payments, assets);
    }
    @Test void absentAndUnlinkedRequestsReturnSame404IncludingWrites() throws Exception {
        mvc.perform(get("/api/corretor/solicitacoes/999").session(session(broker))).andExpect(status().isNotFound());
        mvc.perform(get("/api/corretor/solicitacoes/101").session(session(foreign))).andExpect(status().isNotFound());
        for (String action : List.of("preco", "emitir", "recusar")) mvc.perform(operation(action, "{\"valor\":10}", foreign)).andExpect(status().isNotFound());
        verify(requests, never()).save(any()); verifyNoInteractions(payments);
    }
    @Test void bothLinkedBrokersSeeSameProductButNoCredentialsOrDrafts() throws Exception {
        for (Corretor r : List.of(broker, colleague)) {
            String json = mvc.perform(get("/api/corretor/dashboard").session(session(r))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSolicitacoes").value(1)).andExpect(jsonPath("$.emAndamento").value(1))
                    .andExpect(jsonPath("$.recentes[0].clienteNome").value("Cliente Teste")).andReturn().getResponse().getContentAsString();
            assertFalse(json.contains("PRIVATE_")); assertFalse(json.contains("corretores"));
        }
        mvc.perform(get("/api/corretor/solicitacoes").session(session(foreign))).andExpect(jsonPath("$.length()").value(0));
        row.setStatus(StatusContratacao.AGUARDANDO_BEM);
        mvc.perform(get("/api/corretor/solicitacoes").session(session(broker))).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(operation("recusar", "", broker)).andExpect(status().isNotFound());
    }
    @Test void invalidPricesAndMalformedIdsNeverPersist() throws Exception {
        for (String body : List.of("{}", "{\"valor\":0}", "{\"valor\":-1}", "{\"valor\":1.001}", "{\"valor\":100000000}", "{\"valor\":\"abc\"}")) {
            mvc.perform(operation("preco", body, broker)).andExpect(status().isBadRequest());
            mvc.perform(operation("emitir", body, broker)).andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/corretor/solicitacoes/abc").session(session(broker))).andExpect(status().isBadRequest());
        verifyNoInteractions(requests, assets, payments);
    }
    @Test void savePriceDoesNotEmitOrCreatePaymentAndIgnoresPrivilegeFields() throws Exception {
        mvc.perform(operation("preco", "{\"valor\":39.90,\"corretorId\":22,\"status\":\"APOLICE_EMITIDA\",\"clienteId\":99}", broker))
                .andExpect(status().isOk()).andExpect(jsonPath("$.solicitacao.valorSeguro").value(39.9)).andExpect(jsonPath("$.podeEmitir").value(true));
        assertEquals(StatusContratacao.PENDENTE, row.getStatus()); assertEquals(10, row.getCliente().getId());
        verify(requests).findForCorretorOperation(101, 20); verifyNoInteractions(payments);
    }
    @Test void emissionCreatesExactlyOnePendingPaymentAndPreservesAsset() throws Exception {
        row.setValorContratacao(new BigDecimal("39.90"));
        mvc.perform(operation("emitir", "{\"valor\":39.90}", broker)).andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitacao.status").value("APOLICE_EMITIDA")).andExpect(jsonPath("$.podeRecusar").value(false));
        mvc.perform(operation("emitir", "{\"valor\":39.90}", colleague)).andExpect(status().isConflict());
        verify(payments, times(1)).save(argThat(p -> p.getStatus() == StatusPagamento.PENDENTE && p.getDataPagamento() == null
                && p.getContratacao() == row && p.getValor().compareTo(new BigDecimal("39.90")) == 0));
        assertEquals(1, row.getBensPatriomiais().size()); verify(assets, never()).delete(any());
    }
    @Test void staleOrMissingPriceCannotEmit() throws Exception {
        mvc.perform(operation("emitir", "{\"valor\":39.90}", broker)).andExpect(status().isConflict());
        row.setValorContratacao(new BigDecimal("49.90"));
        mvc.perform(operation("emitir", "{\"valor\":39.90}", broker)).andExpect(status().isConflict());
        verify(payments, never()).save(any()); assertEquals(StatusContratacao.PENDENTE, row.getStatus());
    }
    @Test void missingOrForeignAssetAndExistingPaymentBlockEmission() throws Exception {
        row.setValorContratacao(BigDecimal.TEN);
        when(assets.findByContratacaoId(101)).thenReturn(Optional.empty());
        mvc.perform(operation("emitir", "{\"valor\":10}", broker)).andExpect(status().isConflict());
        when(assets.findByContratacaoId(101)).thenReturn(Optional.of(asset)); asset.setCliente(Cliente.builder().id(999).build());
        mvc.perform(operation("emitir", "{\"valor\":10}", broker)).andExpect(status().isConflict());
        asset.setCliente(client); when(payments.existsByContratacao_Id(101)).thenReturn(true);
        mvc.perform(operation("emitir", "{\"valor\":10}", broker)).andExpect(status().isConflict());
        assertEquals(StatusContratacao.PENDENTE, row.getStatus()); verify(payments, never()).save(any()); verify(requests, never()).save(any());
    }
    @Test void refusalRetainsHistoryAndIsIdempotent() throws Exception {
        mvc.perform(operation("recusar", "", broker)).andExpect(status().isOk()).andExpect(jsonPath("$.solicitacao.status").value("REJEITADA"));
        mvc.perform(operation("recusar", "", colleague)).andExpect(status().isOk());
        assertSame(client, row.getCliente()); assertEquals(1, row.getBensPatriomiais().size());
        verify(requests, times(1)).save(row); verify(requests, never()).delete(any()); verifyNoInteractions(payments);
        mvc.perform(get("/api/corretor/solicitacoes/101").session(session(broker))).andExpect(jsonPath("$.solicitacao.status").value("REJEITADA"));
    }
    @Test void issuedAndCancelledStatesBlockAllMutations() throws Exception {
        for (StatusContratacao s : List.of(StatusContratacao.APOLICE_EMITIDA, StatusContratacao.CANCELADA)) {
            row.setStatus(s);
            for (String action : List.of("emitir", "recusar", "preco")) mvc.perform(operation(action, "{\"valor\":10}", broker)).andExpect(status().isConflict());
        }
        verify(requests, never()).save(any()); verifyNoInteractions(payments);
    }
    @Test void policiesOnlyIncludeIssuedAndNoCrossClientAssetLeaks() throws Exception {
        mvc.perform(get("/api/corretor/apolices").session(session(broker))).andExpect(jsonPath("$.length()").value(0));
        row.setStatus(StatusContratacao.APOLICE_EMITIDA); asset.setCliente(Cliente.builder().id(999).build()); asset.setDescricao("PRIVATE_ASSET");
        mvc.perform(get("/api/corretor/apolices").session(session(broker))).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].solicitacao.bens.length()").value(0));
    }
    @Test void storageFailureIsSafeAndLockIsRequiredByContract() throws Exception {
        when(requests.findForCorretorOperation(101, 20)).thenThrow(new RuntimeException("DATABASE_SECRET"));
        String json = mvc.perform(operation("recusar", "", broker)).andExpect(status().isServiceUnavailable()).andReturn().getResponse().getContentAsString();
        assertFalse(json.contains("DATABASE_SECRET"));
        var method = ContratacaoRepository.class.getMethod("findForCorretorOperation", Integer.class, Integer.class);
        assertEquals(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE, method.getAnnotation(org.springframework.data.jpa.repository.Lock.class).value());
    }
}

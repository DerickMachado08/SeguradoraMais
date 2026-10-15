package com.mais.seguradora.controllers;

import com.fasterxml.jackson.databind.*;
import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.PagamentoMobileDtos.Confirmacao;
import com.mais.seguradora.enummerated.*;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** MVC/services reais com mocks, sem Boot, banco, Stripe ou envio de email. */
class PagamentoMobileContractTest {
    PagamentoRepository payments;ContratacaoRepository contracts;ClienteRepository clients;CorretorRepository brokers;AdminRepository admins;
    Cliente owner,other;Corretor broker,foreignBroker;Admin admin;Apolice product;Contratacao contract;Pagamento payment;
    PagamentoMobileService service;MockMvc mvc;
    @BeforeEach void setup() {
        payments=mock(PagamentoRepository.class);contracts=mock(ContratacaoRepository.class);clients=mock(ClienteRepository.class);brokers=mock(CorretorRepository.class);admins=mock(AdminRepository.class);
        owner=Cliente.builder().id(10).nome("Cliente Teste").email("PRIVATE_EMAIL").cpf("PRIVATE_CPF").senha("PRIVATE_HASH").build();other=Cliente.builder().id(11).nome("Outra Conta").build();
        broker=new Corretor();broker.setId(20);foreignBroker=new Corretor();foreignBroker.setId(21);admin=new Admin();admin.setId(30);
        product=new Apolice();product.setId(1);product.setTipoApolice("Celular");product.setNumeroApolice("REF-1");product.setCorretores(new ArrayList<>(List.of(broker)));
        contract=new Contratacao();contract.setId(101);contract.setCliente(owner);contract.setApolice(product);contract.setStatus(StatusContratacao.APOLICE_EMITIDA);contract.setValorContratacao(new BigDecimal("39.90"));
        payment=new Pagamento();payment.setId(501);payment.setContratacao(contract);payment.setValor(new BigDecimal("39.90"));payment.setStatus(StatusPagamento.PENDENTE);payment.setFormaPagamento("PENDENTE");payment.setDataVencimento(LocalDate.now().minusDays(2));
        when(clients.existsById(anyInt())).thenReturn(true);when(brokers.existsById(anyInt())).thenReturn(true);when(admins.existsById(30)).thenReturn(true);
        when(payments.findByContratacao_Cliente_IdOrderByIdDesc(anyInt())).thenReturn(List.of(payment));
        when(payments.findByIdAndContratacao_Cliente_Id(501,10)).thenReturn(Optional.of(payment));
        when(payments.findContratacaoForCliente(501,10)).thenReturn(Optional.of(101));
        when(payments.findByContratacao_IdOrderByIdAsc(101)).thenReturn(List.of(payment));when(payments.findForMobileOperation(101)).thenReturn(List.of(payment));
        when(payments.findDistinctByContratacao_Apolice_Corretores_IdOrderByIdDesc(anyInt())).thenReturn(List.of(payment));when(payments.findAll()).thenReturn(List.of(payment));
        when(contracts.findForPagamento(101,10)).thenReturn(Optional.of(contract));
        service=new PagamentoMobileService(payments,contracts,clients,brokers,admins);
        var mapper=new ObjectMapper().findAndRegisterModules().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var validator=new LocalValidatorFactoryBean();validator.afterPropertiesSet();
        mvc=MockMvcBuilders.standaloneSetup(new PagamentoMobileController(service)).setValidator(validator).setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    }
    MockHttpSession session(Object user){var s=new MockHttpSession();s.setAttribute(SessionSecurity.USER_ATTRIBUTE,user);SessionSecurity.renewToken(s);return s;}
    MockHttpServletRequestBuilder pay(int id,String body,Object user){var s=session(user);return post("/api/cliente/pagamentos/"+id+"/pagar").session(s).header("X-Session-Token",SessionSecurity.token(s)).contentType(MediaType.APPLICATION_JSON).content(body);}
    @Test void anonymousCannotReadAnyAreaOrPay() throws Exception {
        for(String path:List.of("cliente/pagamentos","cliente/pagamentos/501","corretor/pagamentos","admin/pagamentos")) mvc.perform(get("/api/"+path)).andExpect(status().isUnauthorized()).andExpect(header().string("Cache-Control","no-store"));
        mvc.perform(post("/api/cliente/pagamentos/501/pagar").contentType(MediaType.APPLICATION_JSON).content("{\"valor\":39.90}")).andExpect(status().isUnauthorized());verifyNoInteractions(payments,contracts,clients,brokers,admins);
    }
    @Test void profileGuardsProtectEveryRoute() throws Exception {
        for(Object user:List.of(broker,admin)) {mvc.perform(get("/api/cliente/pagamentos").session(session(user))).andExpect(status().isForbidden());mvc.perform(get("/api/cliente/pagamentos/501").session(session(user))).andExpect(status().isForbidden());mvc.perform(pay(501,"{\"valor\":39.90}",user)).andExpect(status().isForbidden());}
        for(Object user:List.of(owner,admin))mvc.perform(get("/api/corretor/pagamentos").session(session(user))).andExpect(status().isForbidden());
        for(Object user:List.of(owner,broker))mvc.perform(get("/api/admin/pagamentos").session(session(user))).andExpect(status().isForbidden());verifyNoInteractions(payments,contracts);
    }
    @Test void removedAndInvalidIdentitiesAreRejected() throws Exception {
        when(clients.existsById(10)).thenReturn(false);when(brokers.existsById(20)).thenReturn(false);when(admins.existsById(30)).thenReturn(false);
        mvc.perform(get("/api/cliente/pagamentos").session(session(owner))).andExpect(status().isUnauthorized());mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/corretor/pagamentos").session(session(broker))).andExpect(status().isUnauthorized());mvc.perform(get("/api/admin/pagamentos").session(session(admin))).andExpect(status().isUnauthorized());
        owner.setId(0);mvc.perform(get("/api/cliente/pagamentos").session(session(owner))).andExpect(status().isUnauthorized());verifyNoInteractions(payments,contracts);
    }
    @Test void paymentRequiresNonceOfSameSession() throws Exception {
        var s=session(owner);for(String token:List.of("",SessionSecurity.token(session(other))))mvc.perform(post("/api/cliente/pagamentos/501/pagar").session(s).header("X-Session-Token",token).contentType(MediaType.APPLICATION_JSON).content("{\"valor\":39.90}")).andExpect(status().isForbidden());verifyNoInteractions(payments,contracts);
    }
    @Test void foreignMissingAndInvalidIdsDoNotRevealPayment() throws Exception {
        for(int id:List.of(501,999,-1)){mvc.perform(get("/api/cliente/pagamentos/"+id).session(session(other))).andExpect(status().isNotFound());mvc.perform(pay(id,"{\"valor\":39.90}",other)).andExpect(status().isNotFound());}
        mvc.perform(get("/api/cliente/pagamentos/abc").session(session(owner))).andExpect(status().isBadRequest());verify(payments,never()).save(any());verifyNoInteractions(contracts);
    }
    @Test void clientListDefensivelyFiltersOtherOwnersAndSafeProjectionHasNoCredentials() throws Exception {
        mvc.perform(get("/api/cliente/pagamentos").session(session(other))).andExpect(jsonPath("$.length()").value(0));
        String body=mvc.perform(get("/api/cliente/pagamentos").session(session(owner))).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(501)).andExpect(jsonPath("$[0].podePagar").value(true)).andExpect(header().string("Cache-Control","no-store")).andReturn().getResponse().getContentAsString();
        assertFalse(body.contains("PRIVATE_"));assertFalse(body.contains("senha"));assertFalse(body.contains("cpf"));assertFalse(body.contains("usuarioLogado"));
    }
    @Test void staffReadsCannotOfferPaymentAndBrokerScopeIsFresh() throws Exception {
        mvc.perform(get("/api/corretor/pagamentos").session(session(broker))).andExpect(jsonPath("$[0].clienteNome").value("Cliente Teste")).andExpect(jsonPath("$[0].podePagar").value(false));
        mvc.perform(get("/api/corretor/pagamentos").session(session(foreignBroker))).andExpect(jsonPath("$.length()").value(0));product.getCorretores().clear();
        mvc.perform(get("/api/corretor/pagamentos").session(session(broker))).andExpect(jsonPath("$.length()").value(0));mvc.perform(get("/api/admin/pagamentos").session(session(admin))).andExpect(jsonPath("$[0].podePagar").value(false));verify(payments,never()).save(any());
    }
    @Test void existingPendingPaymentIsApprovedOnceWithoutChangingPolicyOrCreatingAnotherRecord() throws Exception {
        mvc.perform(pay(501,"{\"valor\":39.90,\"clienteId\":11,\"status\":\"CANCELADA\",\"formaPagamento\":\"CARTÃO\"}",owner)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APROVADA")).andExpect(jsonPath("$.formaPagamento").value("FICTICIO")).andExpect(jsonPath("$.ficticio").value(true)).andExpect(jsonPath("$.podePagar").value(false)).andExpect(jsonPath("$.dataPagamento").value(LocalDate.now().toString()));
        assertEquals(501,payment.getId());assertEquals(1,payment.getIdApolice());assertEquals(StatusContratacao.APOLICE_EMITIDA,contract.getStatus());verify(payments,times(1)).save(same(payment));verify(contracts,never()).save(any());
    }
    @Test void scalarLookupPrecedesLocksAndNoEntityReadHappensBeforeContractLock() {
        service.pagar(10,501,new Confirmacao(new BigDecimal("39.90")));var order=inOrder(payments,contracts);
        order.verify(payments).findContratacaoForCliente(501,10);order.verify(contracts).findForPagamento(101,10);order.verify(payments).findForMobileOperation(101);order.verify(payments).save(payment);
        verify(payments,never()).findByIdAndContratacao_Cliente_Id(anyInt(),anyInt());
    }
    @Test void repeatIsIdempotentAndPreservesOriginalPaymentDateEvenAfterPolicyCancellation() throws Exception {
        service.pagar(10,501,new Confirmacao(new BigDecimal("39.90")));LocalDate original=LocalDate.now().minusDays(10);payment.setDataPagamento(original);contract.setStatus(StatusContratacao.CANCELADA);
        mvc.perform(pay(501,"{\"valor\":39.9}",owner)).andExpect(status().isOk()).andExpect(jsonPath("$.dataPagamento").value(original.toString()));verify(payments,times(1)).save(any());assertEquals(StatusContratacao.CANCELADA,contract.getStatus());
    }
    @Test void mismatchedConfirmedAndStoredPolicyPricesCannotBeApproved() throws Exception {
        mvc.perform(pay(501,"{\"valor\":49.90}",owner)).andExpect(status().isConflict());contract.setValorContratacao(new BigDecimal("49.90"));mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isConflict());verify(payments,never()).save(any());
    }
    @Test void onlyIssuedPoliciesCanPayWithoutAutomaticExpiry() throws Exception {
        for(var status:StatusContratacao.values())if(status!=StatusContratacao.APOLICE_EMITIDA){contract.setStatus(status);mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isConflict());}
        contract.setStatus(StatusContratacao.APOLICE_EMITIDA);mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isOk());assertTrue(payment.getDataVencimento().isBefore(LocalDate.now()));
    }
    @Test void rejectedOrAlreadyPaidThroughAnotherFlowCannotBeConvertedToFictional() throws Exception {
        payment.setStatus(StatusPagamento.REPROVADA);mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isConflict());payment.setStatus(StatusPagamento.APROVADA);payment.setFormaPagamento("CARTÃO");payment.setDataPagamento(LocalDate.now().minusDays(1));
        mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isConflict());assertEquals("CARTÃO",payment.getFormaPagamento());verify(payments,never()).save(any());
    }
    @Test void duplicatePaymentRecordsBlockNewApprovalWithoutDeletingAnyHistory() throws Exception {
        var second=new Pagamento();second.setId(502);second.setContratacao(contract);second.setValor(new BigDecimal("39.90"));second.setStatus(StatusPagamento.PENDENTE);
        when(payments.findForMobileOperation(101)).thenReturn(List.of(payment,second));when(payments.findByContratacao_IdOrderByIdAsc(101)).thenReturn(List.of(payment,second));
        mvc.perform(get("/api/cliente/pagamentos/501").session(session(owner))).andExpect(jsonPath("$.podePagar").value(false));mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isConflict());
        second.setStatus(StatusPagamento.APROVADA);mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isConflict());verify(payments,never()).save(any());verify(payments,never()).delete(any());
    }
    @Test void missingOrInconsistentPaymentMetadataCannotBeApproved() throws Exception {
        payment.setDataPagamento(LocalDate.now());mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isConflict());payment.setDataPagamento(null);payment.setIdApolice(999);
        mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isConflict());payment.setIdApolice(null);contract.setApolice(null);mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isConflict());verify(payments,never()).save(any());
    }
    @Test void invalidAmountsAndMalformedBodiesNeverReachPersistence() throws Exception {
        for(String body:List.of("{}","{\"valor\":0}","{\"valor\":-1}","{\"valor\":1.001}","{\"valor\":100000000}","{\"valor\":\"abc\"}"))mvc.perform(pay(501,body,owner)).andExpect(status().isBadRequest());verifyNoInteractions(payments,contracts);
    }
    @Test void infrastructureErrorsAreSafeAndNotSuccessfulPayments() throws Exception {
        when(payments.findForMobileOperation(101)).thenThrow(new RuntimeException("PRIVATE_DATABASE_PASSWORD"));String body=mvc.perform(pay(501,"{\"valor\":39.90}",owner)).andExpect(status().isServiceUnavailable()).andExpect(header().string("Cache-Control","no-store")).andReturn().getResponse().getContentAsString();assertFalse(body.contains("PRIVATE_"));verify(payments,never()).save(any());
    }
    @Test void approvedPaymentFeedsExistingFinanceCalculationExactlyOnce() {
        when(payments.findByStatus(StatusPagamento.APROVADA)).thenAnswer(i -> payment.getStatus()==StatusPagamento.APROVADA?List.of(payment):List.of());
        var paymentService=new PagamentoService();ReflectionTestUtils.setField(paymentService,"pagamentoRepository",payments);var receipts=new ReceitaDashboardService(paymentService);
        assertEquals(BigDecimal.ZERO,receipts.resumo().totalRecebido());service.pagar(10,501,new Confirmacao(new BigDecimal("39.90")));service.pagar(10,501,new Confirmacao(new BigDecimal("39.90")));
        assertEquals(new BigDecimal("39.90"),receipts.resumo().totalRecebido());assertEquals(new BigDecimal("39.90"),receipts.resumo().meses().getLast().valor());verify(payments,times(1)).save(any());
    }
}

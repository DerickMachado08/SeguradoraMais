package com.mais.seguradora.controllers;

import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.enummerated.*;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Fixtures explícitas, exclusivas de teste. Não são carregadas pelo aplicativo nem pelo Spring Boot. */
final class ClienteUiFixtures {
    static Object[] controllers(ClienteRepository clients, Map<String, Cliente> identities, Corretor corretor, Corretor vazio, Admin admin, CorretorService registration) {
        ApoliceRepository products = mock(ApoliceRepository.class);
        ContratacaoRepository requests = mock(ContratacaoRepository.class);
        BemPatrimonialRepository assets = mock(BemPatrimonialRepository.class);
        CorretorRepository brokers = mock(CorretorRepository.class);
        PagamentoRepository payments = mock(PagamentoRepository.class);
        Map<Integer, Pagamento> pendingPayments = new ConcurrentHashMap<>();
        List<Apolice> catalog = new ArrayList<>();
        String[] categories = {"Celular", "Residencial", "Veículos", "Eletrônicos", "Bicicleta"};
        for (int n = 0; n < categories.length; n++) {
            Apolice p = new Apolice(); p.setId(n+1); p.setTipoApolice(categories[n]); p.setNumeroApolice("AP-FIXTURE-"+(n+1)); p.setStatus(StatusApolice.ATIVA);
            p.setDescricao("Seguro acadêmico de "+categories[n].toLowerCase(Locale.ROOT)+". Dados sintéticos para validar a interface.");
            Cobertura cobertura = new Cobertura(); cobertura.setNome("Danos acidentais"); cobertura.setDescricao("Cobertura de teste, conforme as condições cadastradas."); p.setCoberturas(List.of(cobertura)); catalog.add(p);
            p.setCorretores(new ArrayList<>(n == 2 ? List.of() : List.of(corretor)));
            if(n!=2) corretor.getApolices().add(p);
        }
        Map<Integer, Contratacao> rows = new ConcurrentHashMap<>();
        Cliente portfolio = identities.get("carteira@example.test");
        for (int n = 0; n < 3; n++) {
            Contratacao c = new Contratacao(); c.setId(103+n); c.setCliente(portfolio); c.setApolice(catalog.get(n == 0 ? 4 : n == 1 ? 3 : 2));
            c.setDataContratacao(LocalDate.of(2026,10,3+n)); c.setStatus(StatusContratacao.PENDENTE);
            BemPatrimonial b = new BemPatrimonial(); b.setId(13+n); b.setCliente(portfolio); b.setContratacao(c); b.setTipoBem(c.getApolice().getTipoApolice());
            b.setDescricao(n == 0 ? "Bicicleta da carteira de teste" : n == 1 ? "Notebook da carteira de teste" : "Veículo fora da carteira");
            b.setValorEstimado(new BigDecimal("2500.50")); b.setDataAquisicao(LocalDate.of(2024,1,15));
            c.getBensPatriomiais().add(b); rows.put(c.getId(), c);
        }
        Map<Integer,Corretor> brokerRows=new ConcurrentHashMap<>(); brokerRows.put(20,corretor); brokerRows.put(21,vazio);
        var encoder=new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        corretor.setSenha(encoder.encode("Teste@123")); vazio.setSenha(encoder.encode("Teste@123"));
        corretor.setCpf("11144477735"); vazio.setCpf("12345678909"); corretor.setTelefone("11999999999"); vazio.setTelefone("11888888888");
        ReflectionTestUtils.setField(registration,"corretorRepository",brokers); ReflectionTestUtils.setField(registration,"apoliceRepository",products); ReflectionTestUtils.setField(registration,"passwordEncoder",encoder);
        when(brokers.existsById(anyInt())).thenAnswer(i -> brokerRows.containsKey(i.getArgument(0)));
        when(brokers.findByEmail(anyString())).thenAnswer(i -> brokerRows.values().stream().filter(c -> c.getEmail().equals(i.getArgument(0))).findFirst());
        when(brokers.findAll()).thenAnswer(i -> new ArrayList<>(brokerRows.values())); when(brokers.count()).thenAnswer(i -> (long)brokerRows.size());
        when(brokers.existsByEmailIgnoreCase(anyString())).thenAnswer(i -> brokerRows.values().stream().anyMatch(c -> c.getEmail().equalsIgnoreCase(i.getArgument(0))));
        when(brokers.findForAdminOperation(anyInt())).thenAnswer(i -> Optional.ofNullable(brokerRows.get(i.getArgument(0))));
        AtomicInteger brokerSequence=new AtomicInteger(60);
        when(brokers.save(any())).thenAnswer(i -> { Corretor c=i.getArgument(0); if(c.getId()==null)c.setId(brokerSequence.incrementAndGet()); brokerRows.put(c.getId(),c); return c; });
        when(clients.findAll()).thenAnswer(i -> new ArrayList<>(identities.values())); when(clients.count()).thenAnswer(i -> (long)identities.size());
        when(clients.existsByEmailIgnoreCase(anyString())).thenAnswer(i -> identities.values().stream().anyMatch(c -> c.getEmail().equalsIgnoreCase(i.getArgument(0))));
        when(products.findAllForAdminOperation()).thenReturn(catalog);
        when(requests.findAll()).thenAnswer(i -> new ArrayList<>(rows.values()));
        AdminRepository admins=mock(AdminRepository.class); when(admins.existsById(30)).thenReturn(true);
        when(admins.existsByEmailIgnoreCase(anyString())).thenAnswer(i -> admin.getEmail().equalsIgnoreCase(i.getArgument(0)));
        List<Pagamento> approved=new ArrayList<>();
        String[] monthly={"110.10","0.00","220.20","330.30","0.00","440.40"};
        for(int i=0;i<6;i++) { var pay=new Pagamento(); pay.setValor(new BigDecimal(monthly[i])); pay.setStatus(StatusPagamento.APROVADA); pay.setDataPagamento(java.time.YearMonth.now().minusMonths(5-i).atDay(1)); approved.add(pay); }
        var old=new Pagamento(); old.setValor(new BigDecimal("500.50")); old.setDataPagamento(java.time.YearMonth.now().minusMonths(6).atDay(1)); old.setStatus(StatusPagamento.APROVADA); approved.add(old);
        var undated=new Pagamento(); undated.setValor(new BigDecimal("50.05")); undated.setStatus(StatusPagamento.APROVADA); approved.add(undated);
        when(payments.findByStatus(StatusPagamento.APROVADA)).thenReturn(approved);
        PagamentoService paymentService=new PagamentoService(); ReflectionTestUtils.setField(paymentService,"pagamentoRepository",payments);
        AdminMobileController adminController=new AdminMobileController(new AdminAreaService(admins,clients,brokers,products,requests,registration,new ReceitaDashboardService(paymentService)));
        when(requests.findDistinctByApolice_Corretores_Id(anyInt())).thenAnswer(i -> rows.values().stream().filter(c -> c.getApolice().getCorretores().stream().anyMatch(r -> r.getId().equals(i.getArgument(0)))).toList());
        when(requests.findByIdAndApolice_Corretores_Id(anyInt(),anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))).filter(c -> c.getApolice().getCorretores().stream().anyMatch(r -> r.getId().equals(i.getArgument(1)))));
        when(requests.findForCorretorOperation(anyInt(),anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))).filter(c -> c.getApolice().getCorretores().stream().anyMatch(r -> r.getId().equals(i.getArgument(1)))));
        when(payments.existsByContratacao_Id(anyInt())).thenAnswer(i -> pendingPayments.containsKey(i.getArgument(0)));
        when(payments.save(any())).thenAnswer(i -> { Pagamento p = i.getArgument(0); pendingPayments.put(p.getContratacao().getId(),p); return p; });
        Cliente owner = identities.get("cliente@example.test");
        for (int n = 0; n < 2; n++) {
            Contratacao c = new Contratacao(); c.setId(101+n); c.setCliente(owner); c.setApolice(catalog.get(n)); c.setDataContratacao(LocalDate.of(2026,10,1+n));
            c.setStatus(n == 0 ? StatusContratacao.APOLICE_EMITIDA : StatusContratacao.ANALISANDO); if (n==0) c.setValorContratacao(new BigDecimal("39.90"));
            BemPatrimonial b = new BemPatrimonial(); b.setId(10+n); b.setCliente(owner); b.setContratacao(c); b.setTipoBem(catalog.get(n).getTipoApolice());
            b.setDescricao(n == 0 ? "Celular de teste, modelo acadêmico." : "Residência de teste para análise."); b.setValorEstimado(new BigDecimal(n == 0 ? "1500.00" : "250000.00")); b.setDataAquisicao(LocalDate.of(2024,1,15));
            c.getBensPatriomiais().add(b); rows.put(c.getId(), c);
        }
        when(clients.findForSolicitacao(anyInt())).thenAnswer(i -> identities.values().stream().filter(c -> c.getId().equals(i.getArgument(0))).findFirst());
        when(products.findAll()).thenReturn(catalog); when(products.findById(anyInt())).thenAnswer(i -> catalog.stream().filter(p -> p.getId().equals(i.getArgument(0))).findFirst());
        when(requests.findByClienteId(anyInt())).thenAnswer(i -> rows.values().stream().filter(c -> c.getCliente().getId().equals(i.getArgument(0))).toList());
        when(requests.findById(anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))));
        when(requests.findByIdAndClienteId(anyInt(),anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))).filter(c -> c.getCliente().getId().equals(i.getArgument(1))));
        when(requests.findByClienteAndApolice_TipoApoliceAndStatusNot(any(),anyString(),any())).thenAnswer(i -> rows.values().stream().filter(c -> c.getCliente().getId().equals(((Cliente)i.getArgument(0)).getId()) && c.getApolice().getTipoApolice().equals(i.getArgument(1)) && c.getStatus()!=i.getArgument(2)).toList());
        when(requests.findFirstByClienteAndApoliceAndStatus(any(),any(),any())).thenAnswer(i -> rows.values().stream().filter(c -> c.getCliente().getId().equals(((Cliente)i.getArgument(0)).getId()) && c.getApolice().getId().equals(((Apolice)i.getArgument(1)).getId()) && c.getStatus()==i.getArgument(2)).findFirst());
        AtomicInteger sequence = new AtomicInteger(200);
        when(requests.save(any())).thenAnswer(i -> { Contratacao c=i.getArgument(0); if(c.getId()==null)c.setId(sequence.incrementAndGet());rows.put(c.getId(),c);return c; });
        when(assets.findByContratacaoId(anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))).flatMap(c->c.getBensPatriomiais().stream().findFirst()));
        when(assets.save(any())).thenAnswer(i -> { BemPatrimonial b=i.getArgument(0);b.setId(sequence.incrementAndGet());b.getContratacao().getBensPatriomiais().add(b);return b; });
        ContratacaoService flow = new ContratacaoService(); ReflectionTestUtils.setField(flow,"contratacaoRepository",requests); ReflectionTestUtils.setField(flow,"bemPatrimonialRepository",assets); ReflectionTestUtils.setField(flow,"emailService",mock(EmailService.class));
        return new Object[] { new ClienteMobileController(new ClienteAreaService(clients,products,requests,flow)),
                new CorretorMobileController(new CorretorAreaService(brokers,requests,assets,payments)), adminController };
    }
}

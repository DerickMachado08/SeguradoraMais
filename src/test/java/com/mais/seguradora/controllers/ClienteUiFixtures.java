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
    static Object[] controllers(ClienteRepository clients, Map<String, Cliente> identities, Corretor corretor, Corretor vazio, Admin admin, CorretorService registration, boolean paymentFixtures, boolean stage7) {
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
        when(clients.existsById(anyInt())).thenAnswer(i -> identities.values().stream().anyMatch(c -> c.getId().equals(i.getArgument(0))));
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
        when(payments.findByStatus(StatusPagamento.APROVADA)).thenAnswer(i -> paymentFixtures ? pendingPayments.values().stream().filter(p -> p.getStatus()==StatusPagamento.APROVADA).toList() : approved);
        PagamentoService paymentService=new PagamentoService(); ReflectionTestUtils.setField(paymentService,"pagamentoRepository",payments);
        AdminMobileController adminController=new AdminMobileController(new AdminAreaService(admins,clients,brokers,products,requests,registration,new ReceitaDashboardService(paymentService)));
        when(requests.findDistinctByApolice_Corretores_Id(anyInt())).thenAnswer(i -> rows.values().stream().filter(c -> c.getApolice().getCorretores().stream().anyMatch(r -> r.getId().equals(i.getArgument(0)))).toList());
        when(requests.findByIdAndApolice_Corretores_Id(anyInt(),anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))).filter(c -> c.getApolice().getCorretores().stream().anyMatch(r -> r.getId().equals(i.getArgument(1)))));
        when(requests.findForCorretorOperation(anyInt(),anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))).filter(c -> c.getApolice().getCorretores().stream().anyMatch(r -> r.getId().equals(i.getArgument(1)))));
        when(payments.existsByContratacao_Id(anyInt())).thenAnswer(i -> pendingPayments.values().stream().anyMatch(p -> p.getContratacao().getId().equals(i.getArgument(0))));
        AtomicInteger paymentSequence=new AtomicInteger(600);
        when(payments.save(any())).thenAnswer(i -> { Pagamento p = i.getArgument(0); if(p.getId()==null)p.setId(paymentSequence.incrementAndGet()); pendingPayments.put(p.getId(),p); return p; });
        Cliente owner = identities.get("cliente@example.test");
        for (int n = 0; n < 2; n++) {
            Contratacao c = new Contratacao(); c.setId(101+n); c.setCliente(owner); c.setApolice(catalog.get(n)); c.setDataContratacao(LocalDate.of(2026,10,1+n));
            c.setStatus(n == 0 ? StatusContratacao.APOLICE_EMITIDA : StatusContratacao.ANALISANDO); if (n==0) c.setValorContratacao(new BigDecimal("39.90"));
            BemPatrimonial b = new BemPatrimonial(); b.setId(10+n); b.setCliente(owner); b.setContratacao(c); b.setTipoBem(catalog.get(n).getTipoApolice());
            b.setDescricao(n == 0 ? "Celular de teste, modelo acadêmico." : "Residência de teste para análise."); b.setValorEstimado(new BigDecimal(n == 0 ? "1500.00" : "250000.00")); b.setDataAquisicao(LocalDate.of(2024,1,15));
            c.getBensPatriomiais().add(b); rows.put(c.getId(), c);
        }
        if(paymentFixtures) {
            // Registros exclusivos do modo --payments. Mantém as fixtures antigas sem esse argumento.
            seedPayment(pendingPayments,501,rows.get(101),"39.90",StatusPagamento.PENDENTE,"PENDENTE",null);
            for(int n=0;n<3;n++) {
                Contratacao c=new Contratacao();c.setId(106+n);c.setCliente(owner);c.setApolice(catalog.get(n==1?4:0));c.setDataContratacao(LocalDate.now().minusDays(20+n));
                c.setStatus(n==0?StatusContratacao.CANCELADA:StatusContratacao.APOLICE_EMITIDA);
                String value=n==0?"19.90":n==1?"25.00":"49.90";c.setValorContratacao(new BigDecimal(value));rows.put(c.getId(),c);
                seedPayment(pendingPayments,n==0?502:n==1?504:505,c,value,n==1?StatusPagamento.REPROVADA:StatusPagamento.APROVADA,
                        n==0?"FICTICIO":n==1?"PENDENTE":"CARTÃO",n==0?java.time.YearMonth.now().minusMonths(1).atDay(3):n==1?null:LocalDate.now().minusDays(1));
            }
            rows.get(102).setValorContratacao(new BigDecimal("89.90"));seedPayment(pendingPayments,503,rows.get(102),"89.90",StatusPagamento.PENDENTE,"PENDENTE",null);
            rows.get(105).setValorContratacao(new BigDecimal("75.00"));seedPayment(pendingPayments,506,rows.get(105),"75.00",StatusPagamento.PENDENTE,"PENDENTE",null);
        }
        when(payments.findAll()).thenAnswer(i -> new ArrayList<>(pendingPayments.values()));
        when(payments.findByContratacao_Cliente_IdOrderByIdDesc(anyInt())).thenAnswer(i -> pendingPayments.values().stream().filter(p -> p.getContratacao().getCliente().getId().equals(i.getArgument(0))).toList());
        when(payments.findByIdAndContratacao_Cliente_Id(anyInt(),anyInt())).thenAnswer(i -> Optional.ofNullable(pendingPayments.get(i.getArgument(0))).filter(p -> p.getContratacao().getCliente().getId().equals(i.getArgument(1))));
        when(payments.findContratacaoForCliente(anyInt(),anyInt())).thenAnswer(i -> Optional.ofNullable(pendingPayments.get(i.getArgument(0))).filter(p -> p.getContratacao().getCliente().getId().equals(i.getArgument(1))).map(p -> p.getContratacao().getId()));
        when(payments.findByContratacao_IdOrderByIdAsc(anyInt())).thenAnswer(i -> pendingPayments.values().stream().filter(p -> p.getContratacao().getId().equals(i.getArgument(0))).toList());
        when(payments.findForMobileOperation(anyInt())).thenAnswer(i -> pendingPayments.values().stream().filter(p -> p.getContratacao().getId().equals(i.getArgument(0))).toList());
        when(payments.findDistinctByContratacao_Apolice_Corretores_IdOrderByIdDesc(anyInt())).thenAnswer(i -> pendingPayments.values().stream().filter(p -> p.getContratacao().getApolice().getCorretores().stream().anyMatch(b -> b.getId().equals(i.getArgument(0)))).toList());
        when(requests.findForPagamento(anyInt(),anyInt())).thenAnswer(i -> Optional.ofNullable(rows.get(i.getArgument(0))).filter(c -> c.getCliente().getId().equals(i.getArgument(1))));
        PagamentoMobileController paymentController=new PagamentoMobileController(new PagamentoMobileService(payments,requests,clients,brokers,admins));
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
        List<Object> controllers=new ArrayList<>(List.of(new ClienteMobileController(new ClienteAreaService(clients,products,requests,flow)),
                new CorretorMobileController(new CorretorAreaService(brokers,requests,assets,payments)), adminController, paymentController));
        if(stage7) {
            identities.values().forEach(c -> c.setTelefone("11999999999"));
            when(clients.findById(anyInt())).thenAnswer(i -> identities.values().stream().filter(c -> c.getId().equals(i.getArgument(0))).findFirst());
            when(brokers.findById(anyInt())).thenAnswer(i -> Optional.ofNullable(brokerRows.get(i.getArgument(0))));
            when(admins.findById(30)).thenReturn(Optional.of(admin));when(admins.findForPerfil(30)).thenReturn(Optional.of(admin));when(admins.save(any())).thenAnswer(i -> i.getArgument(0));
            when(requests.findForSinistro(anyInt(),anyInt())).thenAnswer(i -> rows.values().stream().filter(c -> c.getCliente().getId().equals(i.getArgument(0))&&c.getApolice().getId().equals(i.getArgument(1))).sorted(Comparator.comparing(Contratacao::getId)).toList());
            Contratacao eligible=new Contratacao();eligible.setId(109);eligible.setCliente(owner);eligible.setApolice(catalog.get(3));eligible.setStatus(StatusContratacao.APOLICE_EMITIDA);eligible.setDataContratacao(LocalDate.now().minusDays(5));eligible.setValorContratacao(new BigDecimal("65.50"));rows.put(109,eligible);
            seedPayment(pendingPayments,507,eligible,"65.50",StatusPagamento.APROVADA,"FICTICIO",LocalDate.now().minusDays(2));
            SinistroRepository claims=mock(SinistroRepository.class);Map<Integer,Sinistro> history=new ConcurrentHashMap<>();
            for(int n=0;n<2;n++) {Sinistro s=new Sinistro();s.setId(701+n);s.setCliente(n==0?owner:portfolio);s.setApolice(catalog.get(n==0?0:4));s.setDescricao("Histórico sintético para consulta do aplicativo.");s.setDataSinsitro(LocalDate.now().minusDays(10+n));s.setValorPrejuizo(new BigDecimal(n==0?"120.50":"50.00"));s.setStatus(n==0?StatusSinistro.NEGADO:StatusSinistro.ANALISANDO);history.put(s.getId(),s);}
            when(claims.findAll()).thenAnswer(i -> new ArrayList<>(history.values()));when(claims.findById(anyInt())).thenAnswer(i -> Optional.ofNullable(history.get(i.getArgument(0))));
            when(claims.findByCliente_IdOrderByIdDesc(anyInt())).thenAnswer(i -> history.values().stream().filter(s -> s.getCliente().getId().equals(i.getArgument(0))).toList());
            when(claims.findByIdAndCliente_Id(anyInt(),anyInt())).thenAnswer(i -> Optional.ofNullable(history.get(i.getArgument(0))).filter(s -> s.getCliente().getId().equals(i.getArgument(1))));
            when(claims.existsByCliente_IdAndApolice_IdAndStatus(anyInt(),anyInt(),any())).thenAnswer(i -> history.values().stream().anyMatch(s -> s.getCliente().getId().equals(i.getArgument(0))&&s.getApolice().getId().equals(i.getArgument(1))&&s.getStatus()==i.getArgument(2)));
            AtomicInteger claimSequence=new AtomicInteger(702);when(claims.save(any())).thenAnswer(i -> {Sinistro s=i.getArgument(0);s.setId(claimSequence.incrementAndGet());history.put(s.getId(),s);return s;});
            controllers.add(new SinistroMobileController(new SinistroMobileService(claims,requests,payments,clients,admins)));
            controllers.add(new PerfilMobileController(new PerfilMobileService(clients,brokers,admins)));
        }
        return controllers.toArray();
    }
    private static void seedPayment(Map<Integer,Pagamento> target,int id,Contratacao c,String value,StatusPagamento status,String method,LocalDate date) {
        Pagamento p=new Pagamento();p.setId(id);p.setContratacao(c);p.setValor(new BigDecimal(value));p.setStatus(status);p.setFormaPagamento(method);p.setDataPagamento(date);p.setDataVencimento(LocalDate.now().minusDays(2));
        if(status==StatusPagamento.APROVADA)p.setIdApolice(c.getApolice().getId());target.put(id,p);
    }

}

const dadosDashboard = window.adminDashboardData || {};

function alternarTema() {
    const raiz = document.documentElement;
    const temaAtual = raiz.dataset.theme === 'dark' ? 'dark' : 'light';
    const novoTema = temaAtual === 'dark' ? 'light' : 'dark';
    raiz.dataset.theme = novoTema;

    try {
        localStorage.setItem('admin-theme', novoTema);
    } catch (erro) {
        // O tema continua funcionando mesmo quando o armazenamento está indisponível.
    }

    atualizarBotaoTema();
}

function atualizarBotaoTema() {
    const botao = document.getElementById('theme-toggle');
    if (!botao) return;

    const temaEscuro = document.documentElement.dataset.theme === 'dark';
    const icone = botao.querySelector('i');
    botao.setAttribute('aria-pressed', String(temaEscuro));
    botao.setAttribute('aria-label', temaEscuro ? 'Ativar modo claro' : 'Ativar modo escuro');

    if (icone) {
        icone.className = temaEscuro ? 'fa-regular fa-sun' : 'fa-regular fa-moon';
    }
}

function criarElementoSvg(nome, atributos = {}) {
    const elemento = document.createElementNS('http://www.w3.org/2000/svg', nome);
    Object.entries(atributos).forEach(([atributo, valor]) => elemento.setAttribute(atributo, String(valor)));
    return elemento;
}

function formatarValorCurto(valor) {
    const numero = Number(valor) || 0;
    if (numero >= 1_000_000) return `R$ ${(numero / 1_000_000).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} mi`;
    if (numero >= 1_000) return `R$ ${(numero / 1_000).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} mil`;
    return `R$ ${numero.toLocaleString('pt-BR', { maximumFractionDigits: 0 })}`;
}

function renderizarGraficoReceita() {
    const container = document.getElementById('revenue-chart');
    if (!container) return;

    const labels = Array.isArray(dadosDashboard.receitaLabels) ? dadosDashboard.receitaLabels : [];
    const valores = Array.isArray(dadosDashboard.receitaValores)
        ? dadosDashboard.receitaValores.map((valor) => Number(valor) || 0)
        : [];

    container.replaceChildren();
    if (!labels.length || labels.length !== valores.length) {
        const vazio = document.createElement('p');
        vazio.className = 'chart-empty';
        vazio.textContent = 'Ainda não há dados de receita para exibir.';
        container.appendChild(vazio);
        return;
    }

    const largura = Math.max(620, Math.round(container.clientWidth || 760));
    const altura = 270;
    const margem = { topo: 18, direita: 18, base: 38, esquerda: 70 };
    const larguraUtil = largura - margem.esquerda - margem.direita;
    const alturaUtil = altura - margem.topo - margem.base;
    const maiorValor = Math.max(...valores, 1);
    const teto = maiorValor * 1.15;
    const passoX = labels.length > 1 ? larguraUtil / (labels.length - 1) : larguraUtil;

    const svg = criarElementoSvg('svg', {
        viewBox: `0 0 ${largura} ${altura}`,
        preserveAspectRatio: 'none',
        'aria-hidden': 'true'
    });

    for (let linha = 0; linha <= 4; linha++) {
        const y = margem.topo + (alturaUtil / 4) * linha;
        const valorEixo = teto - (teto / 4) * linha;
        svg.appendChild(criarElementoSvg('line', {
            x1: margem.esquerda,
            y1: y,
            x2: largura - margem.direita,
            y2: y,
            class: 'chart-grid'
        }));

        const texto = criarElementoSvg('text', {
            x: margem.esquerda - 12,
            y: y + 4,
            'text-anchor': 'end',
            class: 'chart-axis-label'
        });
        texto.textContent = formatarValorCurto(valorEixo);
        svg.appendChild(texto);
    }

    const pontos = valores.map((valor, indice) => ({
        x: margem.esquerda + passoX * indice,
        y: margem.topo + alturaUtil - (valor / teto) * alturaUtil
    }));

    const caminhoLinha = pontos.map((ponto, indice) => `${indice === 0 ? 'M' : 'L'} ${ponto.x} ${ponto.y}`).join(' ');
    const caminhoArea = `${caminhoLinha} L ${pontos[pontos.length - 1].x} ${margem.topo + alturaUtil} L ${pontos[0].x} ${margem.topo + alturaUtil} Z`;
    svg.appendChild(criarElementoSvg('path', { d: caminhoArea, class: 'chart-area' }));
    svg.appendChild(criarElementoSvg('path', { d: caminhoLinha, class: 'chart-line' }));

    pontos.forEach((ponto, indice) => {
        const circulo = criarElementoSvg('circle', {
            cx: ponto.x,
            cy: ponto.y,
            r: 5,
            class: 'chart-point'
        });
        const titulo = criarElementoSvg('title');
        titulo.textContent = `${labels[indice]}: ${Number(valores[indice]).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}`;
        circulo.appendChild(titulo);
        svg.appendChild(circulo);

        const label = criarElementoSvg('text', {
            x: ponto.x,
            y: altura - 12,
            'text-anchor': indice === 0 ? 'start' : indice === labels.length - 1 ? 'end' : 'middle',
            class: 'chart-axis-label'
        });
        label.textContent = labels[indice];
        svg.appendChild(label);
    });

    container.appendChild(svg);
}

function renderizarDistribuicaoSeguros() {
    const donut = document.getElementById('insurance-donut');
    const legenda = document.getElementById('insurance-legend');
    if (!donut || !legenda) return;

    const labels = Array.isArray(dadosDashboard.segurosLabels) ? dadosDashboard.segurosLabels : [];
    const valores = Array.isArray(dadosDashboard.segurosValores)
        ? dadosDashboard.segurosValores.map((valor) => Number(valor) || 0)
        : [];
    const total = valores.reduce((soma, valor) => soma + valor, 0);
    const cores = ['#2563eb', '#60a5fa', '#06b6d4', '#8b5cf6', '#14b8a6', '#f59e0b', '#ec4899'];

    legenda.replaceChildren();
    const totalElemento = donut.querySelector('span');
    if (totalElemento) {
        totalElemento.replaceChildren(document.createTextNode(String(total)));
        const descricao = document.createElement('small');
        descricao.textContent = total === 1 ? 'contrato' : 'contratos';
        totalElemento.appendChild(descricao);
    }

    if (!total) {
        donut.style.background = 'conic-gradient(var(--admin-border) 0 100%)';
        const vazio = document.createElement('p');
        vazio.className = 'recent-client-empty';
        vazio.textContent = 'Nenhuma contratação aprovada ainda.';
        legenda.appendChild(vazio);
        return;
    }

    let acumulado = 0;
    const fatias = valores.map((valor, indice) => {
        const inicio = acumulado;
        acumulado += (valor / total) * 100;
        return `${cores[indice % cores.length]} ${inicio}% ${acumulado}%`;
    });
    donut.style.background = `conic-gradient(${fatias.join(', ')})`;

    labels.forEach((label, indice) => {
        const valor = valores[indice] || 0;
        const percentual = (valor / total) * 100;
        const item = document.createElement('div');
        item.className = 'insurance-legend-item';

        const cor = document.createElement('i');
        cor.style.background = cores[indice % cores.length];
        const nome = document.createElement('span');
        nome.textContent = label;
        const porcentagem = document.createElement('strong');
        porcentagem.textContent = `${percentual.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`;

        item.append(cor, nome, porcentagem);
        legenda.appendChild(item);
    });
}

document.getElementById('theme-toggle')?.addEventListener('click', alternarTema);
atualizarBotaoTema();
renderizarGraficoReceita();
renderizarDistribuicaoSeguros();

let redesenhoPendente = 0;
if ('ResizeObserver' in window) {
    const grafico = document.getElementById('revenue-chart');
    if (grafico) {
        new ResizeObserver(() => {
            cancelAnimationFrame(redesenhoPendente);
            redesenhoPendente = requestAnimationFrame(renderizarGraficoReceita);
        }).observe(grafico);
    }
}

document.querySelectorAll('a[href^="#"]').forEach((ancora) => {
    ancora.addEventListener('click', (evento) => {
        const destinoId = ancora.getAttribute('href');
        if (!destinoId || destinoId === '#') return;
        const destino = document.querySelector(destinoId);
        if (!destino) return;

        evento.preventDefault();
        destino.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
});

document.querySelectorAll('.item-menu a').forEach((link) => {
    link.addEventListener('click', () => {
        document.querySelectorAll('.item-menu').forEach((item) => item.classList.remove('active'));
        link.closest('.item-menu')?.classList.add('active');
    });
});

async function deletarCorretor(id) {
    const confirmar = confirm('Tem certeza que deseja excluir este corretor?');
    if (!confirmar) return;

    try {
        const response = await fetch(`/corretor/${id}`, { method: 'DELETE' });

        if (response.ok) {
            alert('Corretor excluído com sucesso!');
            location.reload();
        } else if (response.status === 401) {
            alert('Você precisa estar logado como administrador.');
            window.location.href = '/login.html';
        } else {
            alert('Erro ao excluir o corretor.');
        }
    } catch (erro) {
        console.error(erro);
        alert('Erro ao conectar com o servidor.');
    }
}

window.deletarCorretor = deletarCorretor;

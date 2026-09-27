function irLogin(perfil) {
    localStorage.setItem("perfil", perfil);
    window.location.href = "/login.html";
}

/* ====== HERO CINEMATOGRAFICO COM SCROLL SCRUB ====== */
(function heroScrub() {
    const section = document.querySelector('.hero-scrub');
    const canvas = section?.querySelector('.hero-scrub__canvas');
    if (!section || !canvas || !window.gsap || !window.ScrollTrigger) return;

    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reducedMotion) return;

    gsap.registerPlugin(ScrollTrigger);

    const card = section.querySelector('.hero-scrub__card');
    const titleTop = section.querySelector('.hero-scrub__title--top');
    const titleBottom = section.querySelector('.hero-scrub__title--bottom');
    const intro = section.querySelector('.hero-scrub__intro');
    const eyebrow = section.querySelector('.hero-scrub__eyebrow');
    const scrollHint = section.querySelector('.hero-scrub__scroll');
    const captionIndex = section.querySelector('.hero-scrub__caption-index');
    const captionName = section.querySelector('.hero-scrub__caption-name');
    const context = canvas.getContext('2d');

    const frames = [
        { src: 'css/public/Inicial/imagesInicial/vida.jpg', label: 'Seguro Vida' },
        { src: 'css/public/Inicial/imagesInicial/seguroResidencial.jpeg', label: 'Seguro Residencial' },
        { src: 'css/public/Inicial/imagesInicial/seguroAuto.jpg', label: 'Seguro Auto' },
        { src: 'css/public/Inicial/imagesInicial/viagemm.jpg', label: 'Seguro Viagem' },
        { src: 'css/public/Inicial/imagesInicial/segurosEletronicos.jpg', label: 'Seguro Eletrônicos' },
        { src: 'css/public/Inicial/imagesInicial/seguroBike.jpe', label: 'Seguro Bicicleta' },
        { src: 'css/public/Inicial/imagesInicial/seguroCell.jpg', label: 'Seguro Celular' }
    ];

    let loaded = 0;
    let lastPosition = -1;
    const images = frames.map((frame) => {
        const image = new Image();
        image.decoding = 'async';
        image.onload = () => {
            loaded += 1;
            if (loaded === 1) drawAt(0, true);
        };
        image.src = frame.src;
        return image;
    });

    function drawCover(image, alpha) {
        if (!image?.complete || !image.naturalWidth) return;
        const scale = Math.max(canvas.width / image.naturalWidth, canvas.height / image.naturalHeight);
        const width = image.naturalWidth * scale;
        const height = image.naturalHeight * scale;
        context.globalAlpha = alpha;
        context.drawImage(image, (canvas.width - width) / 2, (canvas.height - height) / 2, width, height);
    }

    function drawAt(position, force) {
        if (!context || (!force && Math.abs(position - lastPosition) < .015)) return;
        lastPosition = position;
        const current = Math.min(frames.length - 1, Math.floor(position));
        const next = Math.min(frames.length - 1, current + 1);
        const blend = position - current;
        context.clearRect(0, 0, canvas.width, canvas.height);
        context.globalAlpha = 1;
        context.fillStyle = '#1a110e';
        context.fillRect(0, 0, canvas.width, canvas.height);
        drawCover(images[current], 1);
        if (next !== current) drawCover(images[next], blend);
        context.globalAlpha = 1;

        const active = blend > .55 ? next : current;
        const number = String(active + 1).padStart(2, '0');
        if (captionIndex.textContent !== number) {
            captionIndex.textContent = number;
            captionName.textContent = frames[active].label;
        }
    }

    const startScale = () => window.innerWidth < 768 ? .82 : .64;
    const immerseScale = () => {
        const rect = card.getBoundingClientRect();
        if (!rect.width || !rect.height) return 2;
        return Math.max(window.innerWidth / rect.width, window.innerHeight / rect.height) * 1.06;
    };

    const gsapContext = gsap.context(() => {
        gsap.timeline({ defaults: { ease: 'power3.out' }, delay: .15 })
            .from('.hero-scrub__wash', { opacity: 0, duration: 1.2 })
            .from(card, { opacity: 0, duration: 1 }, .2)
            .from(titleTop, { opacity: 0, y: 26, duration: .9 }, .35)
            .from(titleBottom, { opacity: 0, y: -22, duration: .9 }, .45)
            .from([eyebrow, intro, scrollHint], { opacity: 0, y: 12, duration: .7, stagger: .08 }, .55);

        gsap.set(card, { scale: startScale(), transformOrigin: '50% 50%' });

        const timeline = gsap.timeline({
            scrollTrigger: {
                trigger: section,
                start: 'top top',
                end: 'bottom bottom',
                scrub: .45,
                invalidateOnRefresh: true,
                onUpdate: (self) => {
                    const mapped = gsap.utils.clamp(0, 1, (self.progress - .12) / .68);
                    drawAt(mapped * (frames.length - 1));
                }
            }
        });

        timeline.to(card, { scale: 1, ease: 'power2.out', duration: .14 }, 0)
            .to(titleTop, { x: () => window.innerWidth < 768 ? '-78vw' : '-62vw', ease: 'power2.inOut', duration: .16 }, 0)
            .to(titleBottom, { x: () => window.innerWidth < 768 ? '78vw' : '62vw', ease: 'power2.inOut', duration: .16 }, 0)
            .to([intro, eyebrow, scrollHint], { opacity: 0, y: -10, ease: 'power1.in', duration: .1 }, 0)
            .to(card, { scale: immerseScale, borderRadius: 0, ease: 'power2.in', duration: .64 }, .14)
            .to([titleTop, titleBottom], { opacity: 0, ease: 'power1.in', duration: .18 }, .14)
            .to(card, { scale: startScale, borderRadius: () => window.innerWidth < 768 ? 16 : 22, ease: 'power3.inOut', duration: .22 }, .78)
            .to(titleTop, { x: 0, opacity: 1, ease: 'power2.inOut', duration: .22 }, .78)
            .to(titleBottom, { x: 0, opacity: 1, ease: 'power2.inOut', duration: .22 }, .78)
            .to([eyebrow, intro, scrollHint], { opacity: 1, y: 0, ease: 'power2.out', duration: .16 }, .84);
    }, section);

    window.addEventListener('load', () => ScrollTrigger.refresh(), { once: true });
    window.addEventListener('resize', () => drawAt(lastPosition < 0 ? 0 : lastPosition, true));

    return () => gsapContext.revert();
})();

/* ====== HEADER SCROLL ====== */
window.addEventListener('scroll', () => {
    document.querySelector('.topo')?.classList.toggle('scrolled', window.scrollY > 20);
});

/* ====== ANIMACAO DE SECTIONS (DO FUNDO) ====== */
const sections = document.querySelectorAll('.section');
const sectionObs = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('show');
            // Disparar animacoes dos filhos
            triggerChildAnimations(entry.target);
        }
    });
}, { threshold: 0.12, rootMargin: '0px 0px -50px 0px' });
sections.forEach(sec => sectionObs.observe(sec));

function triggerChildAnimations(section) {
    // Cards de beneficios
    const cards = section.querySelectorAll('.cardB');
    cards.forEach((card, i) => {
        setTimeout(() => card.classList.add('show'), i * 120);
    });

    // Cards de seguros
    const seguros = section.querySelectorAll('.seguro-card');
    seguros.forEach((card, i) => {
        setTimeout(() => card.classList.add('show'), i * 150);
    });

    // Cards de contato
    const contactCards = section.querySelectorAll('.contact-card');
    contactCards.forEach((card, i) => {
        setTimeout(() => card.classList.add('show'), 300 + i * 200);
    });
}

/* ====== REVEAL (hero + sobre) ====== */
const revealEls = document.querySelectorAll('.reveal, .reveal-img, .sobre-item');
const revealObs = new IntersectionObserver((entries) => {
    entries.forEach(e => {
        if (e.isIntersecting) {
            const delay = parseInt(e.target.dataset.delay || 0, 10);
            setTimeout(() => e.target.classList.add('is-in'), delay);
            revealObs.unobserve(e.target);
        }
    });
}, { threshold: 0.12, rootMargin: '0px 0px -30px 0px' });
revealEls.forEach(el => revealObs.observe(el));

window.addEventListener('load', () => {
    document.querySelectorAll('#hero .reveal, #hero .reveal-img').forEach(el => {
        const delay = parseInt(el.dataset.delay || 0, 10);
        setTimeout(() => el.classList.add('is-in'), delay);
    });
});

(function beneficiosWallParallax() {

    const section = document.querySelector('.beneficios');
    const plane = document.querySelector('.wall-plane');

    if (!section || !plane) return;

    if (
        window.matchMedia('(prefers-reduced-motion: reduce)').matches
    ) {
        return;
    }

    let targetX = 0;
    let targetY = 0;

    let currentX = 0;
    let currentY = 0;

    section.addEventListener('mousemove', (e) => {

        const rect = section.getBoundingClientRect();

        const x =
            (e.clientX - rect.left) /
            rect.width -
            0.5;

        const y =
            (e.clientY - rect.top) /
            rect.height -
            0.5;

        targetX = x * 5;
        targetY = -y * 4;

    });

    section.addEventListener('mouseleave', () => {

        targetX = 0;
        targetY = 0;

    });

    function animate() {

        currentX +=
            (targetX - currentX) * 0.05;

        currentY +=
            (targetY - currentY) * 0.05;

        plane.style.transform = `
            translate(-50%, -50%)
            scale(1.15)
            rotateX(${16 + currentY}deg)
            rotateY(${-14 + currentX}deg)
            translateZ(-120px)
        `;

        requestAnimationFrame(animate);

    }

    animate();

})();

const insurancePanels =
    document.querySelectorAll('.insurance-panel');

insurancePanels.forEach((panel, index) => {

    panel.addEventListener('mouseenter', () => {

        insurancePanels.forEach(p => {
            p.classList.remove('active');
        });

        panel.classList.add('active');

    });


    panel.addEventListener('focus', () => {

        insurancePanels.forEach(p => {
            p.classList.remove('active');
        });

        panel.classList.add('active');

    });

});

/* ====== PARTICULAS HERO ====== */
(function heroParticles() {
    const canvas = document.getElementById('hero-particles');
    if (!canvas) return;
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

    const ctx = canvas.getContext('2d');
    let w, h;

    const LINES = [];
    const LINE_COUNT = 18;

    const DOTS = [];
    const DOT_COUNT = 38;

    function rand(a, b) { return a + Math.random() * (b - a); }

    function resize() {
        const dpr = Math.min(devicePixelRatio || 1, 2);
        const rect = canvas.getBoundingClientRect();
        w = rect.width;
        h = rect.height;
        canvas.width  = w * dpr;
        canvas.height = h * dpr;
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }

    function makeLine(reset) {
        return {
            x:         reset ? -rand(60, w * .5) : rand(-w * .25, w),
            y:         rand(-20, h + 20),
            length:    rand(240, 500),
            amplitude: rand(25, 80),
            frequency: rand(0.005, 0.013),
            phase:     rand(0, Math.PI * 2),
            speed:     rand(0.2, 0.65),
            thickness: rand(0.5, 1.3),
            opacity:   rand(0.05, 0.16),
        };
    }

    function makeDot() {
        return {
            x:       rand(0, w),
            y:       rand(0, h),
            r:       rand(1.2, 3.6),
            vx:      rand(-0.25, 0.25),
            vy:      rand(-0.22, 0.22),
            opacity: rand(0.06, 0.22),
            pulse:   rand(0, Math.PI * 2),
        };
    }

    function init() {
        resize();
        LINES.length = 0;
        DOTS.length  = 0;
        for (let i = 0; i < LINE_COUNT; i++) LINES.push(makeLine(false));
        for (let i = 0; i < DOT_COUNT;  i++) DOTS.push(makeDot());
    }

    function drawLine(l) {
        ctx.beginPath();
        const steps = 44;
        for (let i = 0; i <= steps; i++) {
            const t = i / steps;
            const x = l.x + t * l.length;
            const y = l.y + Math.sin(x * l.frequency + l.phase) * l.amplitude;
            i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
        }
        const g = ctx.createLinearGradient(l.x, 0, l.x + l.length, 0);
        g.addColorStop(0,   `rgba(110,75,58,0)`);
        g.addColorStop(.45, `rgba(110,75,58,${l.opacity})`);
        g.addColorStop(.55, `rgba(110,75,58,${l.opacity})`);
        g.addColorStop(1,   `rgba(110,75,58,0)`);
        ctx.strokeStyle = g;
        ctx.lineWidth   = l.thickness;
        ctx.lineCap     = 'round';
        ctx.stroke();
    }

    function drawDot(d) {
        const pulse  = Math.sin(d.pulse) * 0.4 + 0.6;
        const radius = d.r * pulse;
        const alpha  = d.opacity * pulse;
        ctx.beginPath();
        ctx.arc(d.x, d.y, radius, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(110,75,58,${alpha})`;
        ctx.fill();
    }

    let raf;
    function tick() {
        ctx.clearRect(0, 0, w, h);

        for (const l of LINES) {
            l.x     += l.speed;
            l.phase += 0.003;
            if (l.x - 60 > w) Object.assign(l, makeLine(true));
            drawLine(l);
        }

        for (const d of DOTS) {
            d.x     += d.vx;
            d.y     += d.vy;
            d.pulse += 0.018;
            if (d.x < -10) d.x = w + 10;
            if (d.x > w + 10) d.x = -10;
            if (d.y < -10) d.y = h + 10;
            if (d.y > h + 10) d.y = -10;
            drawDot(d);
        }

        raf = requestAnimationFrame(tick);
    }

    init();
    tick();

    let resizeTimer;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(init, 150);
    });

    const heroEl = document.getElementById('hero');
    if (heroEl && 'IntersectionObserver' in window) {
        new IntersectionObserver(([e]) => {
            if (e.isIntersecting && !raf) tick();
            else if (!e.isIntersecting && raf) { cancelAnimationFrame(raf); raf = null; }
        }, { threshold: 0 }).observe(heroEl);
    }
})();

/* ====== CONTATO  ====== */
(function contactForm() {
    const form     = document.getElementById('formContato');
    const botao    = document.getElementById('enviar');
    const feedback = document.getElementById('formFeedback');
    if (!form || !botao) return;

    const SPAM_KEY      = 'contato_timestamps';
    const MAX_PER_HOUR  = 3;
    const COOLDOWN_MS   = 60_000;
    const WINDOW_MS     = 3_600_000;

    function getStamps() {
        try { return JSON.parse(localStorage.getItem(SPAM_KEY) || '[]'); }
        catch { return []; }
    }

    function saveStamps(arr) {
        localStorage.setItem(SPAM_KEY, JSON.stringify(arr));
    }

    function canSend() {
        const now    = Date.now();
        const stamps = getStamps().filter(t => now - t < WINDOW_MS);
        saveStamps(stamps);
        if (stamps.length >= MAX_PER_HOUR) return { ok: false, reason: 'hourly' };
        if (stamps.length > 0 && now - stamps[stamps.length - 1] < COOLDOWN_MS) {
            const wait = Math.ceil((COOLDOWN_MS - (now - stamps[stamps.length - 1])) / 1000);
            return { ok: false, reason: 'cooldown', wait };
        }
        return { ok: true };
    }

    function showFeedback(msg, type) {
        if (!feedback) return;
        feedback.textContent = msg;
        feedback.className   = `spam-feedback show ${type}`;
        setTimeout(() => feedback.classList.remove('show'), 6000);
    }

    let countdown;
    function startCooldownUI(seconds) {
        let rem = seconds;
        botao.disabled = true;
        clearInterval(countdown);
        countdown = setInterval(() => {
            botao.querySelector('.btn-label').textContent = `Aguarde ${rem}s...`;
            rem--;
            if (rem < 0) {
                clearInterval(countdown);
                botao.disabled = false;
                botao.querySelector('.btn-label').textContent = 'Solicitar contato';
            }
        }, 1000);
    }

    function validate(dados) {
        if (!dados.name)     return 'Por favor, informe seu nome.';
        if (!dados.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(dados.email))
            return 'Por favor, informe um e-mail valido.';
        if (!dados.assunto)  return 'Por favor, escreva sua mensagem.';
        return null;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const dados = {
            name:     document.getElementById('name')?.value.trim(),
            email:    document.getElementById('email')?.value.trim(),
            telefone: document.getElementById('telefone')?.value.trim(),
            assunto:  document.getElementById('assunto')?.value.trim(),
        };

        const validErr = validate(dados);
        if (validErr) { showFeedback(validErr, 'error'); return; }

        const check = canSend();
        if (!check.ok) {
            if (check.reason === 'hourly') {
                showFeedback('Limite de 3 mensagens por hora atingido. Tente novamente mais tarde.', 'error');
            } else {
                showFeedback(`Aguarde ${check.wait} segundo(s) antes de enviar outra mensagem.`, 'error');
                startCooldownUI(check.wait);
            }
            return;
        }

        botao.disabled = true;
        const label = botao.querySelector('.btn-label');
        const orig  = label?.textContent;
        if (label) label.textContent = 'Enviando...';

        try {
            const res = await fetch('http://localhost:8080/contato', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dados),
            });

            const stamps = getStamps();
            stamps.push(Date.now());
            saveStamps(stamps);

            if (res.ok) {
                showFeedback('Mensagem enviada com sucesso! Em breve entraremos em contato.', 'success');
                form.reset();
                startCooldownUI(60);
            } else {
                showFeedback('Erro ao enviar mensagem. Tente novamente.', 'error');
                botao.disabled = false;
                if (label) label.textContent = orig;
            }
        } catch {
            showFeedback('Erro de conexao com o servidor. Verifique sua internet.', 'error');
            botao.disabled = false;
            if (label) label.textContent = orig;
        }
    });
})();

/* ====== MASCARA TELEFONE ====== */
document.getElementById('telefone')?.addEventListener('input', function () {
    let v = this.value.replace(/\D/g, '').slice(0, 11);
    v = v.replace(/^(\d{2})(\d)/, '($1) $2');
    v = v.replace(/(\d{5})(\d{1,4})$/, '$1-$2');
    this.value = v;
});

/* ====== CARROSSEL DA EQUIPE ====== */
(() => {
    const trilha = document.getElementById('equipe-trilha');
    const anterior = document.getElementById('equipe-anterior');
    const proximo = document.getElementById('equipe-proximo');
    const posicao = document.getElementById('equipe-posicao');
    if (!trilha || !anterior || !proximo || !posicao) return;

    const cartoes = [...trilha.querySelectorAll('.equipe-cartao')];
    if (!cartoes.length) return;
    let atual = 0;

    function atualizar() {
        posicao.textContent = `${atual + 1} / ${cartoes.length}`;
        anterior.disabled = atual === 0;
        proximo.disabled = atual === cartoes.length - 1;
    }

    function irPara(indice) {
        atual = Math.max(0, Math.min(indice, cartoes.length - 1));
        trilha.scrollTo({ left: cartoes[atual].offsetLeft - cartoes[0].offsetLeft, behavior: 'smooth' });
        atualizar();
    }

    anterior.addEventListener('click', () => irPara(atual - 1));
    proximo.addEventListener('click', () => irPara(atual + 1));
    trilha.addEventListener('keydown', (evento) => {
        if (evento.key === 'ArrowLeft' || evento.key === 'ArrowRight') {
            evento.preventDefault();
            irPara(atual + (evento.key === 'ArrowRight' ? 1 : -1));
        }
    });

    let atualizacaoPendente = false;
    trilha.addEventListener('scroll', () => {
        if (atualizacaoPendente) return;
        atualizacaoPendente = true;
        requestAnimationFrame(() => {
            atual = cartoes.reduce((melhor, cartao, indice) =>
                Math.abs(cartao.offsetLeft - cartoes[0].offsetLeft - trilha.scrollLeft) <
                Math.abs(cartoes[melhor].offsetLeft - cartoes[0].offsetLeft - trilha.scrollLeft)
                    ? indice : melhor, 0);
            atualizar();
            atualizacaoPendente = false;
        });
    }, { passive: true });

    atualizar();
})();

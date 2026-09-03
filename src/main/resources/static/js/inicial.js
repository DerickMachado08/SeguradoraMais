function irLogin(perfil) {
    localStorage.setItem("perfil", perfil);
    window.location.href = "/login.html";
}

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

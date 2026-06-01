function irLogin(perfil) {
    localStorage.setItem("perfil", perfil);
    window.location.href = "/login.html";
}

/* ====== ANIMAÇÃO DE SEÇÕES (scroll) ====== */
const sections = document.querySelectorAll('.section');
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if(entry.isIntersecting) {
            entry.target.classList.add('show');
        }
    });
}, { threshold: 0.2 });
sections.forEach(sec => observer.observe(sec));

/* ====== REVEAL HERO + SOBRE ====== */
const revealEls = document.querySelectorAll('.reveal, .reveal-img, .sobre-item');
const revealObs = new IntersectionObserver((entries) => {
    entries.forEach(e => {
        if(e.isIntersecting) {
            const delay = parseInt(e.target.dataset.delay || 0, 10);
            setTimeout(() => e.target.classList.add('is-in'), delay);
            revealObs.unobserve(e.target);
        }
    });
}, { threshold: 0.15 });
revealEls.forEach(el => revealObs.observe(el));

// Dispara hero imediatamente no load
window.addEventListener('load', () => {
    document.querySelectorAll('#hero .reveal, #hero .reveal-img').forEach(el => {
        const delay = parseInt(el.dataset.delay || 0, 10);
        setTimeout(() => el.classList.add('is-in'), delay);
    });
});

/* ====== CARROSSEL ====== */
const carousel = document.querySelector(".carousel");
const nextBtn = document.querySelector(".next");
const prevBtn = document.querySelector(".prev");
if (nextBtn && carousel) {
    nextBtn.addEventListener("click", () => carousel.scrollBy({ left: 320, behavior: "smooth" }));
}
if (prevBtn && carousel) {
    prevBtn.addEventListener("click", () => carousel.scrollBy({ left: -320, behavior: "smooth" }));
}

/* PARTÍCULAS CURVADAS NO FUNDO DO HERO */
(function heroParticles() {
    const canvas = document.getElementById('hero-particles');
    if (!canvas) return;

    const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefersReduced) return;

    const ctx = canvas.getContext('2d');
    let w, h, dpr;
    const lines = [];
    const LINE_COUNT = 22;

    function resize() {
        dpr = Math.min(window.devicePixelRatio || 1, 2);
        const rect = canvas.getBoundingClientRect();
        w = rect.width;
        h = rect.height;
        canvas.width = w * dpr;
        canvas.height = h * dpr;
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }

    function rand(a, b) { return a + Math.random() * (b - a); }

    function createLine(reset = false) {
        const y = rand(-20, h + 20);
        return {
            x: reset ? -rand(50, w * 0.6) : rand(-w * 0.3, w),
            y,
            length: rand(220, 480),
            amplitude: rand(30, 90),
            frequency: rand(0.006, 0.014),
            phase: rand(0, Math.PI * 2),
            speed: rand(0.25, 0.7),
            thickness: rand(0.6, 1.4),
            opacity: rand(0.06, 0.18),
            hueShift: rand(-10, 10)
        };
    }

    function init() {
        resize();
        lines.length = 0;
        for (let i = 0; i < LINE_COUNT; i++) lines.push(createLine());
    }

    function drawLine(l) {
        ctx.beginPath();
        const steps = 40;
        for (let i = 0; i <= steps; i++) {
            const t = i / steps;
            const x = l.x + t * l.length;
            const y = l.y + Math.sin((x * l.frequency) + l.phase) * l.amplitude;
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        const grad = ctx.createLinearGradient(l.x, 0, l.x + l.length, 0);
        grad.addColorStop(0, `rgba(110,75,58,0)`);
        grad.addColorStop(0.5, `rgba(110,75,58,${l.opacity})`);
        grad.addColorStop(1, `rgba(110,75,58,0)`);
        ctx.strokeStyle = grad;
        ctx.lineWidth = l.thickness;
        ctx.lineCap = 'round';
        ctx.stroke();
    }

    let rafId;
    function tick() {
        ctx.clearRect(0, 0, w, h);
        for (const l of lines) {
            l.x += l.speed;
            l.phase += 0.004;
            if (l.x - 50 > w) Object.assign(l, createLine(true));
            drawLine(l);
        }
        rafId = requestAnimationFrame(tick);
    }

    init();
    tick();

    let resizeTimer;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => { init(); }, 150);
    });

    // pausa quando o hero sai da tela
    const heroEl = document.getElementById('hero');
    if (heroEl && 'IntersectionObserver' in window) {
        const io = new IntersectionObserver(([entry]) => {
            if (entry.isIntersecting && !rafId) tick();
            else if (!entry.isIntersecting && rafId) {
                cancelAnimationFrame(rafId);
                rafId = null;
            }
        }, { threshold: 0 });
        io.observe(heroEl);
    }
})();

const botaoContato = document.getElementById("enviar");

botaoContato.addEventListener("click", async () =>{
    const dados = {
        name: document.getElementById("name").value,
        email: document.getElementById("email").value,
        telefone: document.getElementById("telefone").value,
        assunto: document.getElementById("assunto").value
    };

    const response = await fetch("http://localhost:8080/contato",  {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(dados)
    });

    if (response.ok){
        alert("Mensagem enviada com sucesso!");
    }
    else{
        alert("Erro ao enviar mensagem.");
    }

});
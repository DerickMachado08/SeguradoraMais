
// ===== Login =====
function logar() {
    fetch("/auten/login", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
            email: document.getElementById("email").value,
            senha: document.getElementById("senha").value
        })
    })
        .then(r => { if (!r.ok) throw new Error("Login inválido"); return r.json(); })
        .then(data => { window.location.href = data.redirect; })
        .catch(err => { console.error(err); alert("Email ou senha inválidos"); });
}

// ===== Eye / password toggle (diferencial) =====
function olharSenha() {
    const campo = document.getElementById("senha");
    const btn = document.getElementById("eyeBtn");
    if (campo.type === "password") {
        campo.type = "text";
        btn.classList.remove("is-closed");
    } else {
        campo.type = "password";
        btn.classList.add("is-closed");
    }
}
// começa fechado (senha oculta)
document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("eyeBtn")?.classList.add("is-closed");

    // pupila acompanha o mouse
    const pupil = document.querySelector(".eye-pupil");
    const wrap = document.querySelector(".eye-wrap");
    if (pupil && wrap) {
        document.addEventListener("mousemove", (e) => {
            const r = wrap.getBoundingClientRect();
            const cx = r.left + r.width / 2;
            const cy = r.top + r.height / 2;
            const dx = Math.max(-3, Math.min(3, (e.clientX - cx) / 40));
            const dy = Math.max(-2, Math.min(2, (e.clientY - cy) / 40));
            if (!document.getElementById("eyeBtn").classList.contains("is-closed")) {
                pupil.style.transform = `translate(${dx}px, ${dy}px)`;
            }
        });
    }
});

// ===== Theme =====
(function initTheme() {
    const saved = localStorage.getItem("theme") || "dark";
    document.documentElement.setAttribute("data-theme", saved);
})();
function toggleTheme() {
    const cur = document.documentElement.getAttribute("data-theme");
    const next = cur === "dark" ? "light" : "dark";
    document.documentElement.setAttribute("data-theme", next);
    localStorage.setItem("theme", next);
}

// ===== Particles =====
(function particles() {
    const canvas = document.getElementById("particles");
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    let w, h, mouse = { x: -9999, y: -9999 };
    const COUNT = 100;
    const parts = [];

    function resize() {
        w = canvas.width = window.innerWidth;
        h = canvas.height = window.innerHeight;
    }
    window.addEventListener("resize", resize);
    window.addEventListener("mousemove", e => { mouse.x = e.clientX; mouse.y = e.clientY; });
    resize();

    for (let i = 0; i < COUNT; i++) {
        parts.push({
            x: Math.random() * w,
            y: Math.random() * h,
            vx: (Math.random() - 0.5) * 0.5,
            vy: (Math.random() - 0.5) * 0.5,
            r: Math.random() * 2 + 0.6
        });
    }

    const getColor = () => getComputedStyle(document.documentElement)
        .getPropertyValue("--particle-color").trim() || "124, 92, 255";

    function draw() {
        ctx.clearRect(0, 0, w, h);
        const color = getColor();

        for (let i = 0; i < parts.length; i++) {
            const p = parts[i];
            p.x += p.vx; p.y += p.vy;
            if (p.x < 0 || p.x > w) p.vx *= -1;
            if (p.y < 0 || p.y > h) p.vy *= -1;

            // empurra suavemente partículas perto do mouse
            const mdx = p.x - mouse.x, mdy = p.y - mouse.y;
            const md = Math.sqrt(mdx * mdx + mdy * mdy);
            if (md < 120) {
                p.x += mdx / md * 0.8;
                p.y += mdy / md * 0.8;
            }

            ctx.beginPath();
            ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(${color}, 0.85)`;
            ctx.fill();

            for (let j = i + 1; j < parts.length; j++) {
                const p2 = parts[j];
                const dx = p.x - p2.x, dy = p.y - p2.y;
                const d = Math.sqrt(dx * dx + dy * dy);
                if (d < 140) {
                    ctx.beginPath();
                    ctx.moveTo(p.x, p.y);
                    ctx.lineTo(p2.x, p2.y);
                    ctx.strokeStyle = `rgba(${color}, ${0.22 * (1 - d / 140)})`;
                    ctx.lineWidth = 1;
                    ctx.stroke();
                }
            }
        }
        requestAnimationFrame(draw);
    }
    draw();
})();


const form = document.getElementById("login-form");

if (form) {
    form.addEventListener("submit", (event) => {
        event.preventDefault();
        logar();
    });
}
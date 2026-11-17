(function () {
  const body = document.body;
  if (!body) return;
  let standardClientMode = false;
  try { standardClientMode = localStorage.getItem('seguradora-client-visual-mode') !== 'custom'; } catch (_) { standardClientMode = true; }
  if (!standardClientMode) body.classList.add('motion-dashboard');
  const isAdminDashboard = Boolean(body.querySelector('.admin-overview'));
  if (isAdminDashboard) body.classList.add('admin-motion-dashboard');

  const themeButton = body.querySelector('#theme-toggle');
  let storedTheme = null;
  try { storedTheme = localStorage.getItem('admin-theme') || localStorage.getItem('seguradora-theme'); } catch (_) { /* Theme switching still works for this page. */ }
  if (storedTheme) document.documentElement.dataset.theme = storedTheme;
  else if (!document.documentElement.dataset.theme && window.matchMedia('(prefers-color-scheme: dark)').matches) document.documentElement.dataset.theme = 'dark';

  if (!themeButton) {
    const headerActions = body.querySelector('.header-actions, .header-user');
    if (headerActions) {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'motion-theme-toggle';
      button.setAttribute('aria-label', 'Ativar modo escuro');
      button.setAttribute('aria-pressed', 'false');
      button.innerHTML = '<i class="fa-regular fa-moon" aria-hidden="true"></i>';
      button.addEventListener('click', () => {
        const dark = document.documentElement.dataset.theme !== 'dark';
        document.documentElement.dataset.theme = dark ? 'dark' : 'light';
        button.setAttribute('aria-pressed', String(dark));
        button.setAttribute('aria-label', dark ? 'Ativar modo claro' : 'Ativar modo escuro');
        const icon = button.querySelector('i');
        if (icon) icon.className = dark ? 'fa-regular fa-sun' : 'fa-regular fa-moon';
        try {
          localStorage.setItem('seguradora-theme', dark ? 'dark' : 'light');
          localStorage.setItem('admin-theme', dark ? 'dark' : 'light');
        } catch (_) { /* Keep the selection in memory. */ }
      });
      headerActions.prepend(button);
      const dark = document.documentElement.dataset.theme === 'dark';
      button.setAttribute('aria-pressed', String(dark));
      button.setAttribute('aria-label', dark ? 'Ativar modo claro' : 'Ativar modo escuro');
      if (dark) button.querySelector('i').className = 'fa-regular fa-sun';
    }
  }

  const gsap = window.gsap;
  if (!gsap || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  const ScrollTrigger = window.ScrollTrigger;
  if (ScrollTrigger) gsap.registerPlugin(ScrollTrigger);

  const pointerCleanup = [];
  const context = gsap.context(() => {
    const header = body.querySelector('.dashboard-header') || body.querySelector('header');
    const sidebar = body.querySelector('.sidebar, .menu-lateral');
    const hero = body.querySelector('.dashboard-hero, .admin-page-heading, .role-quick, .welcome-banner');
    const metrics = gsap.utils.toArray('.metric, .admin-stat-card');
    const opening = gsap.timeline({ defaults: { ease: 'power3.out' } });

    if (sidebar) opening.from(sidebar, { x: -14, duration: .5 }, 0);
    if (header) opening.from(header, { y: -9, duration: .42 }, 0);
    if (hero) opening.from(hero, { y: 15, duration: .56 }, .08);
    if (metrics.length) opening.from(metrics, { y: 13, stagger: .055, duration: .42 }, .18);

    if (isAdminDashboard) {
      metrics.forEach((card) => {
        const value = card.querySelector(':scope > strong');
        if (!value) return;
        const original = value.textContent.trim();
        const amount = original.replace(/\s/g, '').match(/-?[\d.,]+/);
        if (!amount) return;
        const target = Number(amount[0].replace(/\./g, '').replace(',', '.'));
        if (!Number.isFinite(target) || target <= 0) return;
        const isCurrency = original.includes('R$');
        const formatter = isCurrency
          ? new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
          : new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 0 });
        const counter = { value: 0 };
        gsap.to(counter, {
          value: target,
          duration: .9,
          delay: .2,
          ease: 'power2.out',
          onUpdate: () => {
            value.textContent = formatter.format(counter.value);
          },
          onComplete: () => { value.textContent = original; },
        });
      });

      const revealGroup = (selector, triggerSelector, stagger, max = Infinity) => {
        if (!ScrollTrigger) return;
        const items = gsap.utils.toArray(selector).slice(0, max);
        if (!items.length) return;
        gsap.from(items, {
          y: 9,
          opacity: .82,
          duration: .42,
          stagger,
          ease: 'power2.out',
          scrollTrigger: { trigger: body.querySelector(triggerSelector) || items[0], start: 'top 88%', once: true },
        });
      };
      revealGroup('.recent-client-list li:not(.recent-client-empty)', '.recent-clients', .045, 8);
      revealGroup('.relatorio-card', '#relatorios', .07, 6);
      revealGroup('#clientes .profile-card tbody tr', '#clientes', .018, 12);

      if (window.matchMedia('(pointer: fine)').matches) {
        body.querySelectorAll('.admin-stat-card, .admin-panel, .relatorio-card, .profile-card').forEach((surface) => {
          const trackPointer = (event) => {
            const bounds = surface.getBoundingClientRect();
            surface.style.setProperty('--pointer-x', `${event.clientX - bounds.left}px`);
            surface.style.setProperty('--pointer-y', `${event.clientY - bounds.top}px`);
          };
          const clearPointer = () => {
            surface.style.setProperty('--pointer-x', '50%');
            surface.style.setProperty('--pointer-y', '50%');
          };
          surface.addEventListener('pointermove', trackPointer, { passive: true });
          surface.addEventListener('pointerleave', clearPointer, { passive: true });
          pointerCleanup.push(() => {
            surface.removeEventListener('pointermove', trackPointer);
            surface.removeEventListener('pointerleave', clearPointer);
          });
        });
      }
    }

    const sections = gsap.utils.toArray(
      '.dashboard-main > .dashboard-section:not(.dashboard-hero), .admin-panel, main > .relatorios, main > .apolice, main > .corretor, main > .sinistros, .area-emissao'
    ).filter((element) => element !== hero);

    if (ScrollTrigger) {
      sections.forEach((section) => {
        gsap.from(section, {
          y: 15,
          opacity: .86,
          duration: .55,
          ease: 'power2.out',
          scrollTrigger: { trigger: section, start: 'top 88%', once: true },
        });
      });
    }

    const chartLine = body.querySelector('.revenue-chart .chart-line');
    const chartArea = body.querySelector('.revenue-chart .chart-area');
    if (chartLine && typeof chartLine.getTotalLength === 'function') {
      const length = chartLine.getTotalLength();
      gsap.set(chartLine, { strokeDasharray: length, strokeDashoffset: length });
      gsap.to(chartLine, { strokeDashoffset: 0, duration: 1.05, delay: .32, ease: 'power2.out' });
      if (chartArea) gsap.from(chartArea, { opacity: 0, duration: .55, delay: .56, ease: 'power1.out' });
    }

    const donut = body.querySelector('.insurance-donut');
    if (donut) gsap.from(donut, { rotate: -24, scale: .94, duration: .7, delay: .28, ease: 'back.out(1.5)' });
  }, body);

  window.addEventListener('pagehide', () => {
    context.revert();
    pointerCleanup.forEach((cleanup) => cleanup());
  }, { once: true });
})();

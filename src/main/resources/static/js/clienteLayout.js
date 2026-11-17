// Sidebar expand/collapse toggle
(function () {
    const visualModeKey = 'seguradora-client-visual-mode';
    const visualModeButton = document.querySelector('.client-visual-mode');
    const isStandard = () => {
        try { return localStorage.getItem(visualModeKey) !== 'custom'; } catch (_) { return true; }
    };
    const updateVisualModeButton = () => {
        if (!visualModeButton) return;
        const standard = isStandard();
        visualModeButton.setAttribute('aria-pressed', String(standard));
        visualModeButton.innerHTML = standard
            ? '<span class="icon"><i class="fa-solid fa-wand-magic-sparkles" aria-hidden="true"></i></span><span class="txt-link">Visual personalizado</span>'
            : '<span class="icon"><i class="fa-solid fa-sun" aria-hidden="true"></i></span><span class="txt-link">Modo padrão</span>';
    };
    updateVisualModeButton();
    visualModeButton?.addEventListener('click', function () {
        const next = isStandard() ? 'custom' : 'standard';
        try { localStorage.setItem(visualModeKey, next); } catch (_) { /* Continue with a reload. */ }
        window.location.reload();
    });

    const sidebar = document.querySelector('.menu-lateral');
    const mainEl  = document.querySelector('main');
    const footerEl = document.querySelector('footer');
    const btn     = document.querySelector('.ButtonExpandir');

    if (!sidebar || !btn) return;

    btn.addEventListener('click', function () {
        const expanded = sidebar.classList.toggle('expanded');
        if (mainEl)   mainEl.classList.toggle('expanded', expanded);
        if (footerEl) footerEl.classList.toggle('expanded', expanded);
    });

    // Mark active menu item based on current URL
    const path = window.location.pathname;
    document.querySelectorAll('.item-menu a').forEach(link => {
        if (link.getAttribute('href') && path.includes(link.getAttribute('href').replace('#', ''))) {
            link.closest('.item-menu').classList.add('active');
        }
    });
})();

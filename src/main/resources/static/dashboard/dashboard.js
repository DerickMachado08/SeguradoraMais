(function () {
    const visualModeKey = 'seguradora-client-visual-mode';
    const visualModeButton = document.querySelector('.client-visual-mode');
    const useStandardMode = () => {
        try { return localStorage.getItem(visualModeKey) !== 'custom'; } catch (_) { return true; }
    };
    const updateVisualModeButton = () => {
        if (!visualModeButton) return;
        const standard = useStandardMode();
        visualModeButton.setAttribute('aria-pressed', String(standard));
        visualModeButton.innerHTML = standard
            ? '<i class="fa-solid fa-wand-magic-sparkles" aria-hidden="true"></i><span>Visual personalizado</span>'
            : '<i class="fa-solid fa-sun" aria-hidden="true"></i><span>Modo padrão</span>';
    };
    if (useStandardMode()) document.body.classList.remove('glass-dashboard');
    updateVisualModeButton();
    visualModeButton?.addEventListener('click', () => {
        const next = useStandardMode() ? 'custom' : 'standard';
        try { localStorage.setItem(visualModeKey, next); } catch (_) { /* Apply for this page even if storage is unavailable. */ }
        document.body.classList.toggle('glass-dashboard', next !== 'standard');
        window.location.reload();
    });

    const sidebar = document.querySelector('.sidebar');
    const toggle = document.querySelector('.menu-toggle');

    if (sidebar && toggle) {
        toggle.addEventListener('click', () => sidebar.classList.toggle('open'));
        sidebar.querySelectorAll('a').forEach(link => link.addEventListener('click', () => sidebar.classList.remove('open')));
    }

    const navigationLinks = [...document.querySelectorAll('.sidebar a[href^="#"]')];
    const sections = navigationLinks
        .map(link => document.querySelector(link.getAttribute('href')))
        .filter(Boolean);

    if ('IntersectionObserver' in window && sections.length) {
        const observer = new IntersectionObserver(entries => {
            const current = entries.find(entry => entry.isIntersecting);
            if (!current) return;
            navigationLinks.forEach(link => {
                link.classList.toggle('active', link.getAttribute('href') === `#${current.target.id}`);
            });
        }, { rootMargin: '-25% 0px -65% 0px' });

        sections.forEach(section => observer.observe(section));
    }

    document.querySelectorAll('[data-count]').forEach(element => {
        const target = Number(element.dataset.count);
        if (!Number.isFinite(target) || target <= 0 || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
        const start = performance.now();
        const duration = 450;
        const update = now => {
            const progress = Math.min((now - start) / duration, 1);
            element.textContent = Math.round(target * progress);
            if (progress < 1) requestAnimationFrame(update);
        };
        requestAnimationFrame(update);
    });

    /* Painel de aparência: equivalente em JavaScript puro ao controle de
       material do componente glass. As escolhas ficam somente neste navegador. */
    const glassRoot = document.querySelector('.glass-dashboard');
    const glassPanel = document.querySelector('#glass-material');
    const glassTrigger = document.querySelector('.glass-adjust-trigger');
    const notificationPanel = document.querySelector('#glass-notifications');
    const notificationTrigger = document.querySelector('.glass-notification-trigger');

    if (glassRoot && glassPanel && glassTrigger) {
        const storageKey = 'seguradora-glass-settings';
        const defaultGlass = { blur: 22, opacity: 30, saturation: 120, edge: 62, tint: '#e8f2ff' };
        const fields = {
            blur: glassPanel.querySelector('#glass-blur'),
            opacity: glassPanel.querySelector('#glass-opacity'),
            saturation: glassPanel.querySelector('#glass-saturation'),
            edge: glassPanel.querySelector('#glass-edge'),
            tint: glassPanel.querySelector('#glass-tint')
        };

        const numberInRange = (value, min, max, fallback) => {
            const number = Number(value);
            return Number.isFinite(number) ? Math.min(max, Math.max(min, number)) : fallback;
        };

        const normalize = (settings = {}) => ({
            blur: numberInRange(settings.blur, 0, 40, defaultGlass.blur),
            opacity: numberInRange(settings.opacity, 8, 85, defaultGlass.opacity),
            saturation: numberInRange(settings.saturation, 50, 180, defaultGlass.saturation),
            edge: numberInRange(settings.edge, 0, 100, defaultGlass.edge),
            tint: /^#[0-9a-f]{6}$/i.test(settings.tint || '') ? settings.tint : defaultGlass.tint
        });

        const updateOutput = (field) => {
            const output = fields[field]?.closest('label')?.querySelector('output');
            if (!output) return;
            const suffix = field === 'blur' ? ' px' : '%';
            output.textContent = `${fields[field].value}${suffix}`;
        };

        const applyGlass = (settings, persist = true) => {
            const next = normalize(settings);
            fields.blur.value = next.blur;
            fields.opacity.value = next.opacity;
            fields.saturation.value = next.saturation;
            fields.edge.value = next.edge;
            fields.tint.value = next.tint;

            glassRoot.style.setProperty('--glass-blur', `${next.blur}px`);
            glassRoot.style.setProperty('--glass-opacity', `${next.opacity / 100}`);
            glassRoot.style.setProperty('--glass-saturation', `${next.saturation / 100}`);
            glassRoot.style.setProperty('--glass-edge', `${next.edge / 100}`);
            glassRoot.style.setProperty('--glass-tint', next.tint);
            ['blur', 'opacity', 'saturation', 'edge'].forEach(updateOutput);

            if (persist) {
                try { localStorage.setItem(storageKey, JSON.stringify(next)); } catch (_) { /* Storage may be unavailable. */ }
            }
        };

        let savedGlass = null;
        try { savedGlass = JSON.parse(localStorage.getItem(storageKey) || 'null'); } catch (_) { /* Use defaults. */ }
        applyGlass(savedGlass || defaultGlass, false);

        Object.entries(fields).forEach(([name, field]) => {
            field?.addEventListener('input', () => {
                applyGlass({
                    blur: fields.blur.value,
                    opacity: fields.opacity.value,
                    saturation: fields.saturation.value,
                    edge: fields.edge.value,
                    tint: fields.tint.value
                });
            });
        });

        const presets = {
            cristal: { blur: 29, opacity: 23, saturation: 110, edge: 78, tint: '#ffffff' },
            azul: defaultGlass,
            noite: { blur: 34, opacity: 42, saturation: 105, edge: 50, tint: '#25496d' }
        };
        glassPanel.querySelectorAll('[data-glass-preset]').forEach(button => {
            button.addEventListener('click', () => applyGlass(presets[button.dataset.glassPreset] || defaultGlass));
        });
        glassPanel.querySelector('.glass-reset')?.addEventListener('click', () => applyGlass(defaultGlass));

        const closeGlass = () => {
            glassPanel.hidden = true;
            glassTrigger.setAttribute('aria-expanded', 'false');
        };
        const openGlass = () => {
            if (notificationPanel) notificationPanel.hidden = true;
            if (notificationTrigger) notificationTrigger.setAttribute('aria-expanded', 'false');
            glassPanel.hidden = false;
            glassTrigger.setAttribute('aria-expanded', 'true');
            fields.blur?.focus();
        };
        glassTrigger.addEventListener('click', () => glassPanel.hidden ? openGlass() : closeGlass());
        glassPanel.querySelector('.glass-close')?.addEventListener('click', closeGlass);

        const closeNotification = () => {
            if (!notificationPanel) return;
            notificationPanel.hidden = true;
            notificationTrigger?.setAttribute('aria-expanded', 'false');
        };
        const openNotification = () => {
            if (!notificationPanel) return;
            closeGlass();
            notificationPanel.hidden = false;
            notificationTrigger?.setAttribute('aria-expanded', 'true');
            notificationPanel.querySelector('.glass-close')?.focus();
        };
        notificationTrigger?.addEventListener('click', () => notificationPanel?.hidden ? openNotification() : closeNotification());
        notificationPanel?.querySelector('.glass-close')?.addEventListener('click', closeNotification);

        document.addEventListener('keydown', event => {
            if (event.key === 'Escape') {
                closeGlass();
                closeNotification();
            }
        });
        document.addEventListener('click', event => {
            const target = event.target;
            if (!glassPanel.hidden && !glassPanel.contains(target) && !glassTrigger.contains(target)) closeGlass();
            if (notificationPanel && !notificationPanel.hidden && !notificationPanel.contains(target) && !notificationTrigger?.contains(target)) closeNotification();
        });
    }
})();

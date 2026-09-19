(function () {
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
})();

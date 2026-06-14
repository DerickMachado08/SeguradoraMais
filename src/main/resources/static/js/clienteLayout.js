// Sidebar expand/collapse toggle
(function () {
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

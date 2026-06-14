// Normalize status string to lowercase class
function statusClass(status) {
    if (!status) return 'default';
    var s = status.toString().toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    if (s === 'ativa') return 'ativa';
    if (s === 'pendente') return 'pendente';
    if (s === 'inativa' || s === 'cancelada') return 'inativa';
    return 'default';
}

// Apply dynamic status classes after Thymeleaf renders
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-status]').forEach(function (el) {
        var cls = statusClass(el.dataset.status);
        el.classList.add(cls);
    });
});

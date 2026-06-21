function statusClass(status) {
    if (!status) return 'default';
    var s = status.toString().toUpperCase().trim();

    switch (s) {
        case 'PENDENTE':        return 'pendente';
        case 'ANALISANDO':      return 'analisando';
        case 'APROVADA':        return 'aprovada';
        case 'APOLICE_EMITIDA': return 'emitida';
        case 'REJEITADA':       return 'rejeitada';
        default:                return 'default';
    }
}

// Texto amigável exibido no badge
function statusLabel(status) {
    if (!status) return '';
    var s = status.toString().toUpperCase().trim();

    switch (s) {
        case 'PENDENTE':        return 'Pendente';
        case 'ANALISANDO':      return 'Analisando';
        case 'APROVADA':        return 'Aprovada';
        case 'APOLICE_EMITIDA': return 'Apólice Emitida';
        case 'REJEITADA':       return 'Rejeitada';
        default:                return status;
    }
}

document.addEventListener('DOMContentLoaded', function () {
    // Aplica classe dinâmica em qualquer elemento com data-status
    document.querySelectorAll('[data-status]').forEach(function (el) {
        var cls = statusClass(el.dataset.status);
        el.classList.add(cls);

        // Se o elemento for um badge, troca o texto pelo label amigável
        if (el.classList.contains('status-badge')) {
            el.textContent = statusLabel(el.dataset.status);
        }
    });
});
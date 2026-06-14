// Loading state on form submit
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('form.form-contratar').forEach(function (form) {
        form.addEventListener('submit', function () {
            var btn = form.querySelector('.btn-contratar');
            if (!btn) return;
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-circle-notch fa-spin-custom"></i> Processando...';
        });
    });
});

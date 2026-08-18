document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('form.form-contratar').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            var btn = form.querySelector('.btn-contratar');

            if (!btn) return;

            if (btn.disabled) {
                event.preventDefault();
                return;
            }

            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-circle-notch fa-spin-custom"></i> Processando...';
        });
    });
});

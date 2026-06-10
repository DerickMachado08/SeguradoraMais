function logar(){
        fetch("/auten/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                email: document.getElementById("email").value,
                senha: document.getElementById("senha").value
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Login inválido");
                }
                return response.json();
            })
            .then(data => {
                window.location.href = data.redirect;
            })
            .catch(error => {
                console.error(error);
                alert("Email ou senha inválidos");
            });
}

function olharSenha() {
    const campo = document.getElementById("senha");

    if (campo.type === "password") {
        campo.type = "text";
    } else {
        campo.type = "password";
    }
}




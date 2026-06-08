function logar(){

    fetch("http://localhost:8080/cliente/login", {
        method: "POST",
        headers:{
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: document.getElementById("email").value,
            senha: document.getElementById("senha").value
        })
    })
        .then(res => res.json())
        .then(data => {
            console.log(data);

            if (data.status === "ok") {
                window.location.href = "/cliente/dashboard";
            } else {
                alert("Email ou senha inválidos");
            }

        })
        .catch(error => {
            console.error("Erro:", error);
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




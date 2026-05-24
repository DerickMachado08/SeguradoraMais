// Validação do cadastro
function cadastrar() {

    limparErros();

    fetch("http://localhost:8080/cliente", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify({
            nome: document.getElementById("nome").value,
            email: document.getElementById("email").value,
            cpf: document.getElementById("cpf").value,
            senha: document.getElementById("senha").value,
            dataNascimento: document.getElementById("dataNascimento").value,
            telefone: document.getElementById("telefone").value,
            logradouro: document.getElementById("logradouro").value,
            numero: document.getElementById("numero").value,
            bairro: document.getElementById("bairro").value,
            cidade: document.getElementById("cidade").value,
            estado: document.getElementById("estado").value,
            cep: document.getElementById("cep").value
        })
    })

        .then(async response => {

            if (!response.ok) {

                const erros = await response.json();

                mostrarErros(erros);

                throw new Error("Erro de validação");
            }

            return response.json();
        })

        .then(() => {

            alert("Cadastro realizado com sucesso!");

            window.location.href = "/login.html";
        })

        .catch(error => {
            console.log(error);
        });
}

function limparErros() {

    document.getElementById("nomeError").innerText = "";
    document.getElementById("cpfError").innerText = "";
    document.getElementById("emailError").innerText = "";
    document.getElementById("senhaError").innerText = "";
    document.getElementById("idadeError").innerText = "";
}

function mostrarErros(erros) {

    if (erros.nome) {
        document.getElementById("nomeError").innerText = erros.nome;
    }
    if (erros.cpf) {
        document.getElementById("cpfError").innerText = erros.cpf;
    }
    if (erros.email) {
        document.getElementById("emailError").innerText = erros.email;
    }
    if (erros.senha) {
        document.getElementById("senhaError").innerText = erros.senha;
    }
    if (erros.dataNascimento) {
        document.getElementById("idadeError").innerText =
            erros.dataNascimento;
    }
}

// CPF
const cpfInput = document.getElementById("cpf");
cpfInput.addEventListener("input", () => {
    let value = cpfInput.value.replace(/\D/g, "");

    value = value.slice(0, 11);

    value = value.replace(/(\d{3})(\d)/, "$1.$2");
    value = value.replace(/(\d{3})(\d)/, "$1.$2");
    value = value.replace(/(\d{3})(\d{1,2})$/, "$1-$2");

    cpfInput.value = value;
});

// TELEFONE
const telefoneInput = document.getElementById("telefone");
telefoneInput.addEventListener("input", () => {
    let value = telefoneInput.value.replace(/\D/g, "");

    value = value.slice(0, 11);

    value = value.replace(/^(\d{2})(\d)/g, "($1) $2");
    value = value.replace(/(\d{5})(\d{4})$/, "$1-$2");

    telefoneInput.value = value;
});
function validarFormulario() {

    limparErros();

    let possuiErro = false;

    if(document.getElementById("nome").value.trim() === ""){
        document.getElementById("nomeError").innerText = "Nome obrigatório";
        possuiErro = true;
    }

    if(document.getElementById("email").value.trim() === ""){
        document.getElementById("emailError").innerText = "Email obrigatório";
        possuiErro = true;
    }

    if(document.getElementById("telefone").value.trim() === ""){
        document.getElementById("telefoneError").innerText = "Telefone obrigatório";
        possuiErro = true;
    }

    if(document.getElementById("cpf").value.trim() === ""){
        document.getElementById("cpfError").innerText = "CPF obrigatório";
        possuiErro = true;
    }

    if(document.getElementById("dataNascimento").value.trim() === ""){
        document.getElementById("idadeError").innerText = "Data de nascimento obrigatória";
        possuiErro = true;
    }

    if(document.getElementById("senha").value.trim() === ""){
        document.getElementById("senhaError").innerText = "Senha obrigatória";
        possuiErro = true;
    }

    if(document.getElementById("logradouro").value.trim() === ""){
        document.getElementById("logradouroError").innerText = "Logradouro obrigatório";
        possuiErro = true;
    }

    if(document.getElementById("numero").value.trim() === ""){
        document.getElementById("numeroError").innerText = "Número obrigatório";
        possuiErro = true;
    }

    if(document.getElementById("bairro").value.trim() === ""){
        document.getElementById("bairroError").innerText = "Bairro obrigatório";
        possuiErro = true;
    }

    if(document.getElementById("cidade").value.trim() === ""){
        document.getElementById("cidadeError").innerText = "Cidade obrigatória";
        possuiErro = true;
    }

    if(document.getElementById("estado").value.trim() === ""){
        document.getElementById("estadoError").innerText = "Estado obrigatório";
        possuiErro = true;
    }

    if(document.getElementById("cep").value.trim() === ""){
        document.getElementById("cepError").innerText = "CEP obrigatório";
        possuiErro = true;
    }

    return !possuiErro;
}

// Validação do cadastro
function cadastrar() {

    limparErros();
    if(!validarFormulario()){
        return;
    }
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
    document.getElementById("telefoneError").innerText = "";
    document.getElementById("logradouroError").innerText = "";
    document.getElementById("numeroError").innerText = "";
    document.getElementById("bairroError").innerText = "";
    document.getElementById("cidadeError").innerText = "";
    document.getElementById("estadoError").innerText = "";
    document.getElementById("cepError").innerText = "";
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

    if (value.length > 10) {
        value = value.replace(/^(\d{2})(\d{5})(\d{4})$/, "($1) $2-$3");
    } else {
        value = value.replace(/^(\d{2})(\d{4})(\d{0,4})$/, "($1) $2-$3");
    }

    telefoneInput.value = value;
});

// CEP

const cepInput = document.getElementById("cep");
cepInput.addEventListener("input", () => {
    let value = cepInput.value.replace(/\D/g, "");

    value = value.slice(0, 8);

    if(value.length > 5){
        value = value.replace(/(\d{5})(\d+)/, "$1-$2");
    }

    cepInput.value = value;

});

cepInput.addEventListener("blur", async () => {
    const cep = cepInput.value.replace(/\D/g, "");
    if(cep.length !== 8){
        alert("CEP inválido");
        return;
    }

    try{

        const response = await fetch(
            `https://viacep.com.br/ws/${cep}/json/`
        );

        const data = await response.json();

        console.log(data);

        if(data.erro){
            alert("CEP não encontrado");
            return;
        }

        document.getElementById("logradouro").value = data.logradouro;
        document.getElementById("bairro").value = data.bairro;
        document.getElementById("cidade").value = data.localidade;
        document.getElementById("estado").value = data.uf;

    }catch(error){
        console.log(error);
        alert("Erro ao buscar CEP");
    }

});

function olharSenha(){
    const campo = document.getElementById("senha");

    if(campo.type === "password") {
        campo.type = "text";
    }else{
          campo.type = "password"
        }

        const campoNascimento = document.getElementById("dataNascimento");
        const hoje = new Date();

        const dataMaxima = new Date(
        hoje.getFullYear() - 18,
        hoje.getMonth(),
        hoje.getDate()
        );

        const ano = dataMaxima.getFullYear();
        const mes = String(dataMaxima.getMonth() + 1).padStart(2, "0");
        const dia = String(dataMaxima.getDate()).padStart(2, "0");

        campoNascimento.max = `${ano}-${mes}-${dia}`;

}

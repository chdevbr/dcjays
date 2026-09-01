const elementos = {
    seletor: document.querySelector("#seletor-musicas"),
    carregar: document.querySelector("#carregar-faixa"),
    musicaAtual: document.querySelector("#musica-atual"),
    agora: document.querySelector(".now-playing"),
    progresso: document.querySelector(".playback-progress"),
    barraProgresso: document.querySelector(".playback-progress span"),
    tempos: document.querySelectorAll(".playback-times span"),
    lista: document.querySelector("#lista-instrumentos"),
    tocarTodos: document.querySelector("#tocar-todos"),
    pausarTodos: document.querySelector("#pausar-todos"),
    encerrar: document.querySelector("#encerrar-mesa"),
    mensagem: document.querySelector("#mensagem")
};

const nomes = {
    bateria: "Bateria",
    baixo: "Baixo",
    beat: "Beat",
    vocal: "Vocal"
};

const acentos = {
    bateria: "#e3ae41",
    baixo: "#5ea64b",
    beat: "#e68732",
    vocal: "#ece2c7"
};

const icones = {
    bateria: "&#129345;",
    baixo: "&#127928;",
    beat: "&#9638;",
    vocal: "&#127897;"
};

let atualizando = false;

async function api(caminho, opcoes = {}) {
    const resposta = await fetch(caminho, {
        headers: {
            "Content-Type": "application/json"
        },
        ...opcoes
    });

    const dados = await resposta.json();

    if (!resposta.ok) {
        throw new Error(dados.erro || "Nao foi possivel concluir a acao.");
    }

    return dados;
}

async function carregarMusicas() {
    const dados = await api("/api/musicas");

    elementos.seletor.innerHTML = dados.musicas
        .map((musica) => `<option value="${escaparHtml(musica.nome)}">${escaparHtml(musica.nome)}</option>`)
        .join("");
}

async function carregarFaixa() {
    await executarAcao("Carregando faixa...", async () => {
        const musica = elementos.seletor.value;
        const status = await api("/api/mesa/carregar", {
            method: "POST",
            body: JSON.stringify({ musica })
        });
        renderizarStatus(status);
        mostrarMensagem(`${musica} carregada.`);
    });
}

async function alternarInstrumento(instrumento, tocando) {
    const acao = tocando ? "pausar" : "retomar";
    await executarAcao("Atualizando faixa...", async () => {
        const status = await api(`/api/mesa/instrumentos/${encodeURIComponent(instrumento)}/${acao}`, {
            method: "POST"
        });
        renderizarStatus(status);
    });
}

async function solarInstrumento(instrumento) {
    await executarAcao("Isolando faixa...", async () => {
        let status = await api("/api/mesa/status");

        for (const faixa of status.instrumentos || []) {
            const deveTocar = faixa.nome === instrumento;
            const precisaAlterar = deveTocar !== faixa.tocando;

            if (precisaAlterar) {
                const acao = deveTocar ? "retomar" : "pausar";
                status = await api(`/api/mesa/instrumentos/${encodeURIComponent(faixa.nome)}/${acao}`, {
                    method: "POST"
                });
            }
        }

        renderizarStatus(status);
        mostrarMensagem(`${nomes[instrumento] || capitalizar(instrumento)} em solo.`);
    });
}

async function alterarTodas(tocar) {
    const caminho = tocar ? "/api/mesa/tocar-todos" : "/api/mesa/pausar-todos";
    await executarAcao("Atualizando mesa...", async () => {
        const status = await api(caminho, { method: "POST" });
        renderizarStatus(status);
    });
}

async function encerrarMesa() {
    await executarAcao("Encerrando mesa...", async () => {
        const status = await api("/api/mesa/encerrar", { method: "POST" });
        renderizarStatus(status);
        mostrarMensagem("Mesa encerrada.");
    });
}

async function buscarStatusSilencioso() {
    if (atualizando) {
        return;
    }

    try {
        const status = await api("/api/mesa/status");
        renderizarStatus(status);
    } catch (erro) {
        mostrarMensagem(erro.message);
    }
}

async function executarAcao(mensagem, acao) {
    atualizando = true;
    bloquearControles(true);
    mostrarMensagem(mensagem);

    try {
        await acao();
    } catch (erro) {
        mostrarMensagem(erro.message);
    } finally {
        bloquearControles(false);
        atualizando = false;
    }
}

function renderizarStatus(status) {
    const instrumentos = status.instrumentos || [];
    const temMusica = Boolean(status.musicaAtual);

    elementos.musicaAtual.textContent = temMusica
        ? status.musicaAtual.nome
        : "Nenhuma faixa carregada";
    elementos.agora.classList.toggle("is-empty", !temMusica);
    elementos.progresso.setAttribute("aria-valuenow", temMusica ? "78" : "0");
    elementos.barraProgresso.style.width = temMusica ? "78%" : "0%";
    elementos.tempos[0].textContent = temMusica ? "3:20" : "0:00";
    elementos.tempos[1].textContent = temMusica ? "4:15" : "--:--";

    elementos.tocarTodos.disabled = !temMusica;
    elementos.pausarTodos.disabled = !temMusica;
    elementos.encerrar.disabled = !temMusica;

    if (!instrumentos.length) {
        elementos.lista.innerHTML = `
            <article class="empty-state">
                <span>Pronto para carregar</span>
                <strong>Escolha uma musica para abrir os controles.</strong>
            </article>
        `;
        return;
    }

    const soloAtivo = instrumentos.filter((instrumento) => instrumento.tocando).length === 1;

    elementos.lista.innerHTML = instrumentos
        .map((instrumento, indice) => criarCardInstrumento(instrumento, indice, soloAtivo && instrumento.tocando))
        .join("");

    document.querySelectorAll("[data-instrumento]").forEach((botao) => {
        botao.addEventListener("click", () => {
            alternarInstrumento(botao.dataset.instrumento, botao.dataset.tocando === "true");
        });
    });

    document.querySelectorAll("[data-solo]").forEach((botao) => {
        botao.addEventListener("click", () => solarInstrumento(botao.dataset.solo));
    });

    if (status.mensagem) {
        mostrarMensagem(status.mensagem);
    } else if (!status.carregando) {
        mostrarMensagem("Mesa sincronizada.");
    }
}

function criarCardInstrumento(instrumento, indice, soloAtivo) {
    const nome = nomes[instrumento.nome] || capitalizar(instrumento.nome);
    const tocando = instrumento.tocando;
    const classe = tocando ? "" : " is-muted";
    const accent = acentos[instrumento.nome] || "#e3ae41";
    const icone = icones[instrumento.nome] || "&#9835;";

    return `
        <article class="track-card${classe}" style="--accent: ${accent}">
            <div class="track-top">
                <span class="track-number">FAIXA ${String(indice + 1).padStart(2, "0")}</span>
                <div class="track-heading">
                    <span class="track-icon" aria-hidden="true">${icone}</span>
                    <h2 class="track-name">${escaparHtml(nome)}</h2>
                </div>
            </div>
            <div class="track-console" aria-hidden="true">
                <div class="meter">
                    <span></span><span></span><span></span><span></span><span></span><span></span>
                    <span></span><span></span><span></span><span></span><span></span>
                </div>
                <div class="gain-control">
                    <span class="gain-left">+dB</span><span class="gain-right">+6dB</span>
                    <span class="gain-left">+3</span><span class="gain-right">+3</span>
                    <span class="gain-left">-4</span><span class="gain-right">0</span>
                    <span class="gain-left">-inf</span><span class="gain-right">-inf</span>
                    <i class="fader"></i>
                </div>
            </div>
            <div class="track-actions">
                <button
                    class="track-toggle"
                    type="button"
                    aria-pressed="${!tocando}"
                    data-instrumento="${escaparHtml(instrumento.nome)}"
                    data-tocando="${tocando}">
                    Mudo
                </button>
                <button
                    class="track-solo${soloAtivo ? " is-active" : ""}"
                    type="button"
                    aria-pressed="${soloAtivo}"
                    data-solo="${escaparHtml(instrumento.nome)}">
                    Solo
                </button>
            </div>
        </article>
    `;
}

function bloquearControles(bloquear) {
    elementos.carregar.disabled = bloquear;
    elementos.seletor.disabled = bloquear;
}

function mostrarMensagem(texto) {
    elementos.mensagem.textContent = texto;
}

function capitalizar(texto) {
    return texto.charAt(0).toUpperCase() + texto.slice(1);
}

function escaparHtml(texto) {
    return String(texto)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

elementos.carregar.addEventListener("click", carregarFaixa);
elementos.tocarTodos.addEventListener("click", () => alterarTodas(true));
elementos.pausarTodos.addEventListener("click", () => alterarTodas(false));
elementos.encerrar.addEventListener("click", encerrarMesa);

carregarMusicas()
    .then(buscarStatusSilencioso)
    .catch((erro) => mostrarMensagem(erro.message));

setInterval(buscarStatusSilencioso, 1000);

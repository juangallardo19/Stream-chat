"use strict";

const ESPERA_RECONEXION_MS = 3000;

const estado = {
    usuario: "",
    grupo: "",
    socketChat: null,
    reconexion: null,
    colores: new Map()
};

const PALETA = ["#e11d48", "#0891b2", "#16a34a", "#d97706", "#9333ea", "#2563eb", "#db2777", "#0d9488"];

const elementos = {
    formularioEntrada: document.getElementById("formulario-entrada"),
    usuario: document.getElementById("usuario"),
    grupo: document.getElementById("grupo"),
    sala: document.getElementById("sala"),
    tituloSala: document.getElementById("titulo-sala"),
    subtituloSala: document.getElementById("subtitulo-sala"),
    mensajes: document.getElementById("mensajes"),
    formularioMensaje: document.getElementById("formulario-mensaje"),
    texto: document.getElementById("texto"),
    error: document.getElementById("error"),
    estadoChat: document.getElementById("estado-chat")
};

function direccionWebSocket(ruta) {
    const protocolo = location.protocol === "https:" ? "wss:" : "ws:";
    const consulta = new URLSearchParams({ user: estado.usuario, group: estado.grupo });
    return `${protocolo}//${location.host}${ruta}?${consulta}`;
}

function marcarConexion(conectado) {
    elementos.estadoChat.textContent = conectado ? "Mensajes: conectado" : "Mensajes: desconectado";
    elementos.estadoChat.classList.toggle("conectado", conectado);
    elementos.estadoChat.classList.toggle("desconectado", !conectado);
}

function horaLocal(instante) {
    return new Date(instante).toLocaleTimeString("es-CO", { hour: "2-digit", minute: "2-digit", second: "2-digit" });
}

function colorDe(usuario) {
    if (!estado.colores.has(usuario)) {
        estado.colores.set(usuario, PALETA[estado.colores.size % PALETA.length]);
    }
    return estado.colores.get(usuario);
}

function crear(etiqueta, clase, texto) {
    const elemento = document.createElement(etiqueta);
    elemento.className = clase;
    elemento.textContent = texto;
    return elemento;
}

function mostrarMensaje(mensaje) {
    const item = document.createElement("li");
    item.className = mensaje.user === estado.usuario ? "mensaje propio" : "mensaje";
    item.style.setProperty("--color-usuario", colorDe(mensaje.user));

    const meta = crear("div", "meta", "");
    meta.append(crear("span", "autor", mensaje.user), crear("span", "hora", horaLocal(mensaje.sentAt)));

    const burbuja = crear("div", "burbuja", "");
    burbuja.append(meta, crear("div", "texto", mensaje.text));

    item.append(crear("span", "avatar", mensaje.user.charAt(0).toUpperCase()), burbuja);
    elementos.mensajes.append(item);
    elementos.mensajes.scrollTop = elementos.mensajes.scrollHeight;
}

function conectarChat() {
    clearTimeout(estado.reconexion);
    const socket = new WebSocket(direccionWebSocket("/ws/chat"));
    estado.socketChat = socket;

    socket.onopen = () => marcarConexion(true);
    socket.onmessage = (evento) => mostrarMensaje(JSON.parse(evento.data));
    socket.onclose = (evento) => {
        marcarConexion(false);
        if (evento.code === 1008) {
            elementos.error.textContent = evento.reason || "El grupo no es válido.";
            return;
        }
        estado.reconexion = setTimeout(conectarChat, ESPERA_RECONEXION_MS);
    };
}

async function enviarMensaje(texto) {
    const respuesta = await fetch("/message", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ user: estado.usuario, group: estado.grupo, text: texto })
    });
    if (respuesta.status === 202) {
        elementos.error.textContent = "";
        return;
    }
    const cuerpo = await respuesta.json().catch(() => ({}));
    const detalles = Object.values(cuerpo.detallesValidacion || {}).join(" ");
    elementos.error.textContent = detalles || cuerpo.mensaje || `Error ${respuesta.status}`;
}

elementos.formularioEntrada.addEventListener("submit", (evento) => {
    evento.preventDefault();
    estado.usuario = elementos.usuario.value.trim();
    estado.grupo = elementos.grupo.value;
    elementos.formularioEntrada.classList.add("oculto");
    elementos.sala.classList.remove("oculto");
    elementos.tituloSala.textContent = estado.grupo;
    elementos.subtituloSala.textContent = `Conectado como ${estado.usuario}`;
    colorDe(estado.usuario);
    conectarChat();
    elementos.texto.focus();
});

elementos.formularioMensaje.addEventListener("submit", (evento) => {
    evento.preventDefault();
    const texto = elementos.texto.value.trim();
    if (texto === "") {
        return;
    }
    elementos.texto.value = "";
    enviarMensaje(texto).catch(() => {
        elementos.error.textContent = "No se pudo enviar el mensaje. Revisa la conexión.";
    });
});

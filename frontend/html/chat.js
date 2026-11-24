"use strict";

const ESPERA_RECONEXION_MS = 3000;

const estado = {
    usuario: "",
    grupo: "",
    sockets: new Map(),
    reconexiones: new Map(),
    colores: new Map(),
    escribiendo: new Set()
};

const evidencia = {
    tecleosEnviados: 0,
    tecleosFallidos: 0,
    mensajesEnviados: 0,
    transicionesGrupo: 0,
    transiciones: { TYPING: 0, STOPPED: 0 },
    emisores: new Set()
};

const bots = {
    control: null,
    activos: new Map()
};

const PALETA = ["#e11d48", "#0891b2", "#16a34a", "#d97706", "#9333ea", "#2563eb", "#db2777", "#0d9488"];

const NOMBRES_BOT = ["Bot-Marta", "Bot-Pedro", "Bot-Lucia", "Bot-Andres", "Bot-Sofia", "Bot-Tomas"];

const FRASES_BOT = [
    "Hola a todos, ¿cómo van con el proyecto?",
    "¿Alguien ya revisó la cola de RabbitMQ?",
    "Kafka guarda todo en el log, se puede releer cuando queramos",
    "Yo me encargo del docker compose esta noche",
    "¿A qué hora es la sustentación?",
    "Las pruebas de mi servicio ya están en verde",
    "Voy por un café y vuelvo en un rato",
    "¿Quién revisa el pull request de develop?",
    "El debounce convierte muchas teclas en pocas transiciones",
    "Ya subí los cambios a mi rama",
    "Mañana repasamos la arquitectura completa",
    "¿A ustedes les funciona el WebSocket de estado?",
    "Perfecto, nos vemos en la reunión",
    "Acabo de matar una réplica y el chat siguió funcionando",
    "Revisen docker stats, todo está dentro de los límites"
];

const RITMO_BOT = {
    teclaMinMs: 70,
    teclaMaxMs: 260,
    probabilidadPausa: 0.04,
    pausaMinMs: 1500,
    pausaMaxMs: 6000,
    dudaMinMs: 300,
    dudaMaxMs: 1500,
    descansoMinMs: 2500,
    descansoMaxMs: 9000
};

const elementos = {
    formularioEntrada: document.getElementById("formulario-entrada"),
    usuario: document.getElementById("usuario"),
    grupo: document.getElementById("grupo"),
    contenido: document.getElementById("contenido"),
    tituloSala: document.getElementById("titulo-sala"),
    subtituloSala: document.getElementById("subtitulo-sala"),
    mensajes: document.getElementById("mensajes"),
    formularioMensaje: document.getElementById("formulario-mensaje"),
    texto: document.getElementById("texto"),
    error: document.getElementById("error"),
    estadoChat: document.getElementById("estado-chat"),
    estadoTecleo: document.getElementById("estado-tecleo"),
    indicador: document.getElementById("indicador"),
    textoIndicador: document.getElementById("texto-indicador"),
    evTecleos: document.getElementById("ev-tecleos"),
    evFallidos: document.getElementById("ev-fallidos"),
    evTransiciones: document.getElementById("ev-transiciones"),
    evDetalle: document.getElementById("ev-detalle"),
    evProporcion: document.getElementById("ev-proporcion"),
    evMensajes: document.getElementById("ev-mensajes"),
    evGrupo: document.getElementById("ev-grupo"),
    reiniciar: document.getElementById("reiniciar"),
    cantidadBots: document.getElementById("cantidad-bots"),
    botonBots: document.getElementById("boton-bots"),
    estadoBots: document.getElementById("estado-bots"),
    listaBots: document.getElementById("lista-bots")
};

const CANALES = {
    chat: {
        ruta: "/ws/chat",
        etiqueta: "Mensajes",
        indicador: elementos.estadoChat,
        alRecibir: (datos) => {
            mostrarMensaje(datos);
            quitarDeTecleo(datos.user);
        }
    },
    estado: {
        ruta: "/ws/status",
        etiqueta: "Estado",
        indicador: elementos.estadoTecleo,
        alRecibir: (datos) => {
            contarTransicion(datos);
            actualizarTecleo(datos);
        }
    }
};

function direccionWebSocket(ruta) {
    const protocolo = location.protocol === "https:" ? "wss:" : "ws:";
    const consulta = new URLSearchParams({ user: estado.usuario, group: estado.grupo });
    return `${protocolo}//${location.host}${ruta}?${consulta}`;
}

function marcarConexion(canal, conectado) {
    canal.indicador.textContent = `${canal.etiqueta}: ${conectado ? "conectado" : "desconectado"}`;
    canal.indicador.classList.toggle("conectado", conectado);
    canal.indicador.classList.toggle("desconectado", !conectado);
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

function actualizarTecleo(cambio) {
    if (cambio.user === estado.usuario) {
        return;
    }
    if (cambio.status === "TYPING") {
        estado.escribiendo.add(cambio.user);
    } else {
        estado.escribiendo.delete(cambio.user);
    }
    mostrarIndicador();
}

function quitarDeTecleo(usuario) {
    if (estado.escribiendo.delete(usuario)) {
        mostrarIndicador();
    }
}

function fraseDeTecleo(nombres) {
    if (nombres.length === 1) {
        return `${nombres[0]} está escribiendo…`;
    }
    if (nombres.length === 2) {
        return `${nombres[0]} y ${nombres[1]} están escribiendo…`;
    }
    return "Varias personas están escribiendo…";
}

function mostrarIndicador() {
    const nombres = [...estado.escribiendo];
    elementos.indicador.classList.toggle("oculto", nombres.length === 0);
    elementos.textoIndicador.textContent = nombres.length === 0 ? "" : fraseDeTecleo(nombres);
}

function conectar(nombre) {
    const canal = CANALES[nombre];
    clearTimeout(estado.reconexiones.get(nombre));
    const socket = new WebSocket(direccionWebSocket(canal.ruta));
    estado.sockets.set(nombre, socket);

    socket.onopen = () => marcarConexion(canal, true);
    socket.onmessage = (evento) => canal.alRecibir(JSON.parse(evento.data));
    socket.onclose = (evento) => {
        marcarConexion(canal, false);
        if (evento.code === 1008) {
            elementos.error.textContent = evento.reason || "El grupo no es válido.";
            return;
        }
        estado.reconexiones.set(nombre, setTimeout(() => conectar(nombre), ESPERA_RECONEXION_MS));
    };
}

function publicar(ruta, cuerpo) {
    return fetch(ruta, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(cuerpo)
    });
}

function avisarTecleo(usuario) {
    evidencia.emisores.add(usuario);
    publicar("/typing", { user: usuario, group: estado.grupo })
        .then((respuesta) => contarTecleo(respuesta.status === 202))
        .catch(() => contarTecleo(false));
}

function contarTecleo(aceptado) {
    if (aceptado) {
        evidencia.tecleosEnviados += 1;
    } else {
        evidencia.tecleosFallidos += 1;
    }
    pintarEvidencia();
}

function contarTransicion(cambio) {
    evidencia.transicionesGrupo += 1;
    if (evidencia.emisores.has(cambio.user) && cambio.status in evidencia.transiciones) {
        evidencia.transiciones[cambio.status] += 1;
    }
    pintarEvidencia();
}

function contarMensaje() {
    evidencia.mensajesEnviados += 1;
    pintarEvidencia();
}

function totalTransiciones() {
    return evidencia.transiciones.TYPING + evidencia.transiciones.STOPPED;
}

function proporcion() {
    const transiciones = totalTransiciones();
    if (transiciones === 0) {
        return "—";
    }
    return (evidencia.tecleosEnviados / transiciones).toFixed(1);
}

function formatear(numero) {
    return numero.toLocaleString("es-CO");
}

function pintarEvidencia() {
    elementos.evTecleos.textContent = formatear(evidencia.tecleosEnviados);
    elementos.evFallidos.textContent = evidencia.tecleosFallidos === 0
        ? "POST /typing"
        : `POST /typing · ${formatear(evidencia.tecleosFallidos)} fallidos`;
    elementos.evTransiciones.textContent = formatear(totalTransiciones());
    elementos.evDetalle.textContent =
        `TYPING ${formatear(evidencia.transiciones.TYPING)} · STOPPED ${formatear(evidencia.transiciones.STOPPED)}`;
    elementos.evProporcion.textContent = proporcion();
    elementos.evMensajes.textContent = formatear(evidencia.mensajesEnviados);
    elementos.evGrupo.textContent =
        `Cuenta lo que envía esta ventana y las transiciones de sus usuarios por /ws/status. ` +
        `Transiciones de todo ${estado.grupo}: ${formatear(evidencia.transicionesGrupo)}.`;
}

function reiniciarEvidencia() {
    evidencia.tecleosEnviados = 0;
    evidencia.tecleosFallidos = 0;
    evidencia.mensajesEnviados = 0;
    evidencia.transicionesGrupo = 0;
    evidencia.transiciones.TYPING = 0;
    evidencia.transiciones.STOPPED = 0;
    pintarEvidencia();
}

async function enviarComo(usuario, texto) {
    const respuesta = await publicar("/message", { user: usuario, group: estado.grupo, text: texto });
    if (respuesta.status === 202) {
        contarMensaje();
    }
    return respuesta;
}

async function enviarMensaje(texto) {
    const respuesta = await enviarComo(estado.usuario, texto);
    if (respuesta.status === 202) {
        elementos.error.textContent = "";
        return;
    }
    const cuerpo = await respuesta.json().catch(() => ({}));
    const detalles = Object.values(cuerpo.detallesValidacion || {}).join(" ");
    elementos.error.textContent = detalles || cuerpo.mensaje || `Error ${respuesta.status}`;
}

function azar(minimo, maximo) {
    return minimo + Math.floor(Math.random() * (maximo - minimo + 1));
}

function elegir(lista) {
    return lista[Math.floor(Math.random() * lista.length)];
}

function esperar(milisegundos, senal) {
    return new Promise((resolver, rechazar) => {
        if (senal.aborted) {
            rechazar(senal.reason);
            return;
        }
        const alAbortar = () => {
            clearTimeout(temporizador);
            rechazar(senal.reason);
        };
        const temporizador = setTimeout(() => {
            senal.removeEventListener("abort", alAbortar);
            resolver();
        }, milisegundos);
        senal.addEventListener("abort", alAbortar, { once: true });
    });
}

function pausaEntreTeclas() {
    if (Math.random() < RITMO_BOT.probabilidadPausa) {
        return azar(RITMO_BOT.pausaMinMs, RITMO_BOT.pausaMaxMs);
    }
    return azar(RITMO_BOT.teclaMinMs, RITMO_BOT.teclaMaxMs);
}

function marcarBot(nombre, actividad) {
    if (!bots.activos.has(nombre)) {
        return;
    }
    bots.activos.set(nombre, actividad);
    pintarBots();
}

async function teclearFrase(nombre, frase, senal) {
    marcarBot(nombre, "escribiendo");
    for (let posicion = 0; posicion < frase.length; posicion += 1) {
        avisarTecleo(nombre);
        await esperar(pausaEntreTeclas(), senal);
    }
}

async function enviarFraseDeBot(nombre, frase) {
    marcarBot(nombre, "enviando");
    await enviarComo(nombre, frase).catch(() => undefined);
}

async function vivirBot(nombre, senal) {
    await esperar(azar(0, RITMO_BOT.descansoMinMs), senal);
    while (!senal.aborted) {
        const frase = elegir(FRASES_BOT);
        await teclearFrase(nombre, frase, senal);
        await esperar(azar(RITMO_BOT.dudaMinMs, RITMO_BOT.dudaMaxMs), senal);
        await enviarFraseDeBot(nombre, frase);
        marcarBot(nombre, "descansando");
        await esperar(azar(RITMO_BOT.descansoMinMs, RITMO_BOT.descansoMaxMs), senal);
    }
}

function nombresDeBots(cantidad) {
    return NOMBRES_BOT.filter((nombre) => nombre !== estado.usuario).slice(0, cantidad);
}

function activarBots() {
    bots.control = new AbortController();
    const senal = bots.control.signal;
    nombresDeBots(Number(elementos.cantidadBots.value)).forEach((nombre) => {
        bots.activos.set(nombre, "esperando");
        vivirBot(nombre, senal).catch(() => undefined);
    });
    pintarBots();
}

function detenerBots() {
    bots.control.abort();
    bots.control = null;
    bots.activos.clear();
    pintarBots();
}

function alternarBots() {
    if (bots.control) {
        detenerBots();
        return;
    }
    activarBots();
}

function pintarBots() {
    const activos = bots.control !== null;
    elementos.botonBots.textContent = activos ? "Detener bots" : "Activar bots";
    elementos.botonBots.classList.toggle("peligro", activos);
    elementos.cantidadBots.disabled = activos;
    elementos.estadoBots.textContent = activos ? `${bots.activos.size} activos` : "Detenidos";
    elementos.estadoBots.classList.toggle("activos", activos);
    elementos.listaBots.replaceChildren(...[...bots.activos].map(([nombre, actividad]) => filaDeBot(nombre, actividad)));
}

function filaDeBot(nombre, actividad) {
    const fila = crear("li", "bot", "");
    fila.style.setProperty("--color-usuario", colorDe(nombre));
    const inicial = nombre.split("-").pop().charAt(0).toUpperCase();
    fila.append(crear("span", "avatar", inicial), crear("span", "nombre-bot", nombre), crear("span", "actividad", actividad));
    return fila;
}

elementos.formularioEntrada.addEventListener("submit", (evento) => {
    evento.preventDefault();
    estado.usuario = elementos.usuario.value.trim();
    estado.grupo = elementos.grupo.value;
    elementos.formularioEntrada.classList.add("oculto");
    elementos.contenido.classList.remove("oculto");
    elementos.tituloSala.textContent = estado.grupo;
    elementos.subtituloSala.textContent = `Conectado como ${estado.usuario}`;
    colorDe(estado.usuario);
    pintarEvidencia();
    pintarBots();
    conectar("chat");
    conectar("estado");
    elementos.texto.focus();
});

elementos.texto.addEventListener("input", () => avisarTecleo(estado.usuario));

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

elementos.reiniciar.addEventListener("click", reiniciarEvidencia);

elementos.botonBots.addEventListener("click", alternarBots);

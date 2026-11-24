package com.streamchat.chat_backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GroupSessions")
class GroupSessionsTest {

    private static final GroupId GRUPO_A = new GroupId("GrupoA");
    private static final GroupId GRUPO_B = new GroupId("GrupoB");

    private final GroupSessions sesiones = new GroupSessions();

    private static ChatMessage mensajePara(GroupId grupo, String texto) {
        return new ChatMessage(UUID.randomUUID(), new UserId("Ana"), grupo, new MessageText(texto),
                Instant.parse("2026-11-13T16:30:10.123Z"));
    }

    private static final class SuscriptorEnMemoria implements ChatSubscriber {

        private final List<ChatMessage> recibidos = new ArrayList<>();

        @Override
        public void recibir(ChatMessage mensaje) {
            recibidos.add(mensaje);
        }

        List<ChatMessage> recibidos() {
            return List.copyOf(recibidos);
        }
    }

    @Nested
    @DisplayName("difundir()")
    class Difundir {

        @Test
        @DisplayName("solo reciben los suscriptores del grupo destino")
        void soloRecibeElGrupoDestino() {
            SuscriptorEnMemoria anaEnA = new SuscriptorEnMemoria();
            SuscriptorEnMemoria luisEnA = new SuscriptorEnMemoria();
            SuscriptorEnMemoria martaEnB = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, anaEnA);
            sesiones.suscribir(GRUPO_A, luisEnA);
            sesiones.suscribir(GRUPO_B, martaEnB);
            ChatMessage mensaje = mensajePara(GRUPO_A, "¡Hola GrupoA!");

            sesiones.difundir(mensaje);

            assertEquals(List.of(mensaje), anaEnA.recibidos());
            assertEquals(List.of(mensaje), luisEnA.recibidos());
            assertTrue(martaEnB.recibidos().isEmpty());
        }

        @Test
        @DisplayName("devuelve a cuántos suscriptores entregó el mensaje")
        void devuelveCantidadDeEntregas() {
            sesiones.suscribir(GRUPO_A, new SuscriptorEnMemoria());
            sesiones.suscribir(GRUPO_A, new SuscriptorEnMemoria());
            sesiones.suscribir(GRUPO_B, new SuscriptorEnMemoria());

            int entregas = sesiones.difundir(mensajePara(GRUPO_A, "Hola"));

            assertEquals(2, entregas);
        }

        @Test
        @DisplayName("un grupo sin suscriptores no falla y devuelve cero")
        void grupoSinSuscriptoresNoFalla() {
            ChatMessage mensaje = mensajePara(GRUPO_B, "Nadie conectado");

            int entregas = sesiones.difundir(mensaje);

            assertEquals(0, entregas);
        }

        @Test
        @DisplayName("entrega los mensajes en el mismo orden en que se difunden")
        void conservaElOrden() {
            SuscriptorEnMemoria anaEnA = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, anaEnA);
            ChatMessage primero = mensajePara(GRUPO_A, "primero");
            ChatMessage segundo = mensajePara(GRUPO_A, "segundo");
            ChatMessage tercero = mensajePara(GRUPO_A, "tercero");

            sesiones.difundir(primero);
            sesiones.difundir(segundo);
            sesiones.difundir(tercero);

            assertEquals(List.of(primero, segundo, tercero), anaEnA.recibidos());
        }

        @Test
        @DisplayName("rechaza un mensaje nulo")
        void rechazaMensajeNulo() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> sesiones.difundir(null));

            assertEquals("El mensaje es obligatorio", error.getMessage());
        }
    }

    @Nested
    @DisplayName("suscribir() y retirar()")
    class SuscribirYRetirar {

        @Test
        @DisplayName("suscribir dos veces la misma sesión no duplica la entrega")
        void suscribirDosVecesNoDuplica() {
            SuscriptorEnMemoria anaEnA = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, anaEnA);
            sesiones.suscribir(GRUPO_A, anaEnA);

            sesiones.difundir(mensajePara(GRUPO_A, "Hola"));

            assertEquals(1, anaEnA.recibidos().size());
        }

        @Test
        @DisplayName("una sesión retirada deja de recibir mensajes")
        void retiradoNoRecibe() {
            SuscriptorEnMemoria anaEnA = new SuscriptorEnMemoria();
            SuscriptorEnMemoria luisEnA = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, anaEnA);
            sesiones.suscribir(GRUPO_A, luisEnA);

            sesiones.retirar(GRUPO_A, anaEnA);
            sesiones.difundir(mensajePara(GRUPO_A, "Hola"));

            assertTrue(anaEnA.recibidos().isEmpty());
            assertEquals(1, luisEnA.recibidos().size());
        }

        @Test
        @DisplayName("al retirar la última sesión, el grupo deja de ocupar memoria")
        void grupoVacioSeLibera() {
            SuscriptorEnMemoria anaEnA = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, anaEnA);
            sesiones.suscribir(GRUPO_B, new SuscriptorEnMemoria());

            sesiones.retirar(GRUPO_A, anaEnA);

            assertEquals(Set.of(GRUPO_B), sesiones.gruposActivos());
        }

        @Test
        @DisplayName("retirar una sesión que no estaba suscrita no falla")
        void retirarDesconocidoNoFalla() {
            SuscriptorEnMemoria desconocido = new SuscriptorEnMemoria();

            sesiones.retirar(GRUPO_A, desconocido);

            assertTrue(sesiones.gruposActivos().isEmpty());
        }

        @Test
        @DisplayName("gruposActivos() devuelve una copia que no se puede modificar")
        void gruposActivosEsCopia() {
            sesiones.suscribir(GRUPO_A, new SuscriptorEnMemoria());

            Set<GroupId> activos = sesiones.gruposActivos();

            assertThrows(UnsupportedOperationException.class, () -> activos.add(GRUPO_B));
        }

        @Test
        @DisplayName("rechaza suscribir sin grupo")
        void rechazaSinGrupo() {
            SuscriptorEnMemoria anaEnA = new SuscriptorEnMemoria();

            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> sesiones.suscribir(null, anaEnA));

            assertEquals("El grupo es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza suscribir sin sesión")
        void rechazaSinSuscriptor() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> sesiones.suscribir(GRUPO_A, null));

            assertEquals("La sesión suscriptora es obligatoria", error.getMessage());
        }
    }
}

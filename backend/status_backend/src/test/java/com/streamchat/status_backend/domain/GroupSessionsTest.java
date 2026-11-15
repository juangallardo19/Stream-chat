package com.streamchat.status_backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GroupSessions (sesiones de estado por grupo)")
class GroupSessionsTest {

    private static final GroupId GRUPO_A = new GroupId("GrupoA");
    private static final GroupId GRUPO_B = new GroupId("GrupoB");
    private static final StatusUpdate ANA_TYPING_EN_A =
            new StatusUpdate(new UserId("Ana"), GRUPO_A, TypingStatus.TYPING);
    private static final StatusUpdate ANA_STOPPED_EN_A =
            new StatusUpdate(new UserId("Ana"), GRUPO_A, TypingStatus.STOPPED);

    private final GroupSessions sesiones = new GroupSessions();

    private static final class SuscriptorEnMemoria implements StatusSubscriber {

        private final List<StatusUpdate> recibidos = new ArrayList<>();

        @Override
        public void recibir(StatusUpdate actualizacion) {
            recibidos.add(actualizacion);
        }

        List<StatusUpdate> recibidos() {
            return List.copyOf(recibidos);
        }
    }

    @Nested
    @DisplayName("difundir()")
    class Difundir {

        @Test
        @DisplayName("solo reciben las sesiones del grupo de la actualización (filtra por grupo)")
        void filtraPorGrupo() {
            SuscriptorEnMemoria luisEnA = new SuscriptorEnMemoria();
            SuscriptorEnMemoria martaEnB = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, luisEnA);
            sesiones.suscribir(GRUPO_B, martaEnB);

            sesiones.difundir(ANA_TYPING_EN_A);

            assertEquals(List.of(ANA_TYPING_EN_A), luisEnA.recibidos());
            assertTrue(martaEnB.recibidos().isEmpty());
        }

        @Test
        @DisplayName("devuelve a cuántas sesiones entregó")
        void devuelveEntregas() {
            sesiones.suscribir(GRUPO_A, new SuscriptorEnMemoria());
            sesiones.suscribir(GRUPO_A, new SuscriptorEnMemoria());
            sesiones.suscribir(GRUPO_B, new SuscriptorEnMemoria());

            int entregas = sesiones.difundir(ANA_TYPING_EN_A);

            assertEquals(2, entregas);
        }

        @Test
        @DisplayName("un grupo sin sesiones no falla y devuelve cero")
        void grupoSinSesiones() {
            int entregas = sesiones.difundir(ANA_TYPING_EN_A);

            assertEquals(0, entregas);
        }

        @Test
        @DisplayName("entrega TYPING y STOPPED en el mismo orden en que llegan")
        void conservaElOrden() {
            SuscriptorEnMemoria luisEnA = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, luisEnA);

            sesiones.difundir(ANA_TYPING_EN_A);
            sesiones.difundir(ANA_STOPPED_EN_A);

            assertEquals(List.of(ANA_TYPING_EN_A, ANA_STOPPED_EN_A), luisEnA.recibidos());
        }

        @Test
        @DisplayName("rechaza una actualización nula")
        void rechazaNula() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> sesiones.difundir(null));

            assertEquals("La actualización de estado es obligatoria", error.getMessage());
        }
    }

    @Nested
    @DisplayName("suscribir() y retirar()")
    class SuscribirYRetirar {

        @Test
        @DisplayName("suscribir dos veces la misma sesión no duplica la entrega")
        void noDuplica() {
            SuscriptorEnMemoria luisEnA = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, luisEnA);
            sesiones.suscribir(GRUPO_A, luisEnA);

            sesiones.difundir(ANA_TYPING_EN_A);

            assertEquals(1, luisEnA.recibidos().size());
        }

        @Test
        @DisplayName("una sesión retirada deja de recibir")
        void retiradaNoRecibe() {
            SuscriptorEnMemoria luisEnA = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, luisEnA);

            sesiones.retirar(GRUPO_A, luisEnA);
            sesiones.difundir(ANA_TYPING_EN_A);

            assertTrue(luisEnA.recibidos().isEmpty());
        }

        @Test
        @DisplayName("al retirar la última sesión, el grupo deja de ocupar memoria")
        void liberaGrupoVacio() {
            SuscriptorEnMemoria luisEnA = new SuscriptorEnMemoria();
            sesiones.suscribir(GRUPO_A, luisEnA);
            sesiones.suscribir(GRUPO_B, new SuscriptorEnMemoria());

            sesiones.retirar(GRUPO_A, luisEnA);

            assertEquals(Set.of(GRUPO_B), sesiones.gruposActivos());
        }

        @Test
        @DisplayName("retirar una sesión desconocida no falla")
        void retirarDesconocida() {
            SuscriptorEnMemoria desconocida = new SuscriptorEnMemoria();

            sesiones.retirar(GRUPO_A, desconocida);

            assertTrue(sesiones.gruposActivos().isEmpty());
        }

        @Test
        @DisplayName("rechaza suscribir sin grupo o sin sesión")
        void rechazaNulos() {
            SuscriptorEnMemoria luisEnA = new SuscriptorEnMemoria();

            NullPointerException sinGrupo = assertThrows(NullPointerException.class,
                    () -> sesiones.suscribir(null, luisEnA));
            NullPointerException sinSesion = assertThrows(NullPointerException.class,
                    () -> sesiones.suscribir(GRUPO_A, null));

            assertEquals("El grupo es obligatorio", sinGrupo.getMessage());
            assertEquals("La sesión suscriptora es obligatoria", sinSesion.getMessage());
        }
    }
}

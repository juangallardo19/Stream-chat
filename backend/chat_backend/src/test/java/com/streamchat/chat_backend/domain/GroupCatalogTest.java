package com.streamchat.chat_backend.domain;

import com.streamchat.chat_backend.exception.GrupoNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GroupCatalog")
class GroupCatalogTest {

    private final GroupCatalog catalogo = GroupCatalog.desde(List.of("GrupoA", "GrupoB", "GrupoC"));

    @Nested
    @DisplayName("resolver()")
    class Resolver {

        @ParameterizedTest(name = "{0} se resuelve como GrupoA")
        @ValueSource(strings = {"GrupoA", "grupoa", "GRUPOA", "gRuPoA"})
        @DisplayName("busca sin distinguir mayúsculas y devuelve el nombre canónico del catálogo")
        void resuelveSinDistinguirMayusculas(String nombre) {
            GroupId grupo = catalogo.resolver(nombre);

            assertEquals(new GroupId("GrupoA"), grupo);
        }

        @Test
        @DisplayName("lanza GrupoNoEncontradoException si el grupo no está en el catálogo")
        void rechazaGrupoDesconocido() {
            String nombre = "GrupoZ";

            GrupoNoEncontradoException error = assertThrows(GrupoNoEncontradoException.class,
                    () -> catalogo.resolver(nombre));

            assertEquals("El grupo GrupoZ no existe", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un nombre con formato inválido antes de buscarlo")
        void rechazaFormatoInvalido() {
            String nombre = "Grupo.A";

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> catalogo.resolver(nombre));

            assertEquals("El grupo solo admite letras, números, guion y guion bajo", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un nombre vacío antes de buscarlo")
        void rechazaNombreVacio() {
            String nombre = " ";

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> catalogo.resolver(nombre));

            assertEquals("El grupo es obligatorio", error.getMessage());
        }
    }

    @Nested
    @DisplayName("grupos()")
    class Grupos {

        @Test
        @DisplayName("devuelve los grupos canónicos en el mismo orden de CHAT_GROUPS")
        void devuelveGruposEnOrden() {
            List<GroupId> grupos = catalogo.grupos();

            assertEquals(List.of(new GroupId("GrupoA"), new GroupId("GrupoB"), new GroupId("GrupoC")), grupos);
        }

        @Test
        @DisplayName("devuelve una lista que no se puede modificar desde fuera")
        void listaNoModificable() {
            List<GroupId> grupos = catalogo.grupos();

            assertThrows(UnsupportedOperationException.class, () -> grupos.add(new GroupId("GrupoZ")));
        }
    }

    @Nested
    @DisplayName("Al construirlo con CHAT_GROUPS")
    class Construccion {

        @Test
        @DisplayName("no se deja construir con una lista vacía")
        void rechazaListaVacia() {
            List<String> nombres = List.of();

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> GroupCatalog.desde(nombres));

            assertEquals("El catálogo de grupos no puede estar vacío", error.getMessage());
        }

        @Test
        @DisplayName("no se deja construir sin lista")
        void rechazaListaNula() {
            NullPointerException error = assertThrows(NullPointerException.class,
                    () -> GroupCatalog.desde(null));

            assertEquals("La lista de grupos es obligatoria", error.getMessage());
        }

        @Test
        @DisplayName("no se deja construir con dos nombres que solo difieren en mayúsculas")
        void rechazaDuplicadosPorMayusculas() {
            List<String> nombres = List.of("GrupoA", "grupoa");

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> GroupCatalog.desde(nombres));

            assertEquals("El catálogo de grupos tiene nombres repetidos: grupoa", error.getMessage());
        }

        @Test
        @DisplayName("no se deja construir con un nombre repetido exacto")
        void rechazaDuplicadoExacto() {
            List<String> nombres = List.of("GrupoA", "GrupoB", "GrupoA");

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> GroupCatalog.desde(nombres));

            assertEquals("El catálogo de grupos tiene nombres repetidos: GrupoA", error.getMessage());
        }

        @Test
        @DisplayName("no se deja construir con un nombre de grupo inválido")
        void rechazaNombreInvalido() {
            List<String> nombres = List.of("GrupoA", "Grupo.B");

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> GroupCatalog.desde(nombres));

            assertEquals("El grupo solo admite letras, números, guion y guion bajo", error.getMessage());
        }

        @Test
        @DisplayName("guarda una copia: cambiar la lista original después no altera el catálogo")
        void haceCopiaDefensiva() {
            List<String> nombres = new ArrayList<>(List.of("GrupoA"));
            GroupCatalog catalogoCopiado = GroupCatalog.desde(nombres);

            nombres.add("GrupoB");

            assertThrows(GrupoNoEncontradoException.class, () -> catalogoCopiado.resolver("GrupoB"));
        }
    }
}

package com.streamchat.chat_backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GroupId")
class GroupIdTest {

    @Nested
    @DisplayName("Al crearlo")
    class Creacion {

        @Test
        @DisplayName("acepta letras, números, guion y guion bajo")
        void aceptaNombreValido() {
            String valor = "Grupo_A-1";

            GroupId grupo = new GroupId(valor);

            assertEquals(valor, grupo.value());
        }

        @Test
        @DisplayName("acepta un nombre de exactamente 40 caracteres")
        void aceptaCuarentaCaracteres() {
            String valor = "g".repeat(40);

            GroupId grupo = new GroupId(valor);

            assertEquals(40, grupo.value().length());
        }

        @ParameterizedTest(name = "rechaza [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("rechaza un grupo nulo, vacío o en blanco")
        void rechazaGrupoVacio(String valor) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new GroupId(valor));

            assertEquals("El grupo es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un nombre de más de 40 caracteres")
        void rechazaMasDeCuarentaCaracteres() {
            String valor = "g".repeat(41);

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new GroupId(valor));

            assertEquals("El grupo admite como máximo 40 caracteres", error.getMessage());
        }

        @ParameterizedTest(name = "rechaza [{0}]")
        @ValueSource(strings = {"Grupo.A", "chat#", "grupo*", "Grupo A", "grupo/1", "grupo!"})
        @DisplayName("rechaza caracteres que romperían la routing key")
        void rechazaCaracteresInvalidos(String valor) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new GroupId(valor));

            assertEquals("El grupo solo admite letras, números, guion y guion bajo", error.getMessage());
        }
    }

    @Nested
    @DisplayName("routingKey()")
    class RoutingKey {

        @ParameterizedTest(name = "{0} produce {1}")
        @CsvSource({"GrupoA, chat.grupoa", "Grupo_B-2, chat.grupo_b-2", "GRUPOC, chat.grupoc"})
        @DisplayName("antepone chat. y pasa el grupo a minúsculas")
        void produceRoutingKeyEnMinusculas(String valor, String esperada) {
            GroupId grupo = new GroupId(valor);

            String routingKey = grupo.routingKey();

            assertEquals(esperada, routingKey);
        }
    }

    @Nested
    @DisplayName("Como Value Object")
    class ValueObject {

        @Test
        @DisplayName("dos grupos con el mismo nombre son iguales")
        void igualdadPorValor() {
            GroupId primero = new GroupId("GrupoA");
            GroupId segundo = new GroupId("GrupoA");

            boolean iguales = primero.equals(segundo);

            assertTrue(iguales);
            assertEquals(primero.hashCode(), segundo.hashCode());
        }
    }
}

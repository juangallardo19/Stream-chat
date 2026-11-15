package com.streamchat.typing_api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UserId")
class UserIdTest {

    @Nested
    @DisplayName("Al crearlo")
    class Creacion {

        @Test
        @DisplayName("acepta letras, números, guion y guion bajo")
        void aceptaNombreValido() {
            String valor = "Ana_Maria-2";

            UserId usuario = new UserId(valor);

            assertEquals(valor, usuario.value());
        }

        @Test
        @DisplayName("acepta un nombre de exactamente 40 caracteres")
        void aceptaCuarentaCaracteres() {
            String valor = "u".repeat(40);

            UserId usuario = new UserId(valor);

            assertEquals(40, usuario.value().length());
        }

        @ParameterizedTest(name = "rechaza [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("rechaza un usuario nulo, vacío o en blanco")
        void rechazaUsuarioVacio(String valor) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new UserId(valor));

            assertEquals("El usuario es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un nombre de más de 40 caracteres")
        void rechazaMasDeCuarentaCaracteres() {
            String valor = "u".repeat(41);

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new UserId(valor));

            assertEquals("El usuario admite como máximo 40 caracteres", error.getMessage());
        }

        @ParameterizedTest(name = "rechaza [{0}]")
        @ValueSource(strings = {"Ana Maria", "ana.maria", "ana@correo", "<script>"})
        @DisplayName("rechaza espacios y caracteres especiales")
        void rechazaCaracteresInvalidos(String valor) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new UserId(valor));

            assertEquals("El usuario solo admite letras, números, guion y guion bajo", error.getMessage());
        }
    }
}

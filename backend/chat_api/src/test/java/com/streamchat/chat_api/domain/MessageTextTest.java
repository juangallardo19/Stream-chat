package com.streamchat.chat_api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MessageText")
class MessageTextTest {

    @Nested
    @DisplayName("Al crearlo")
    class Creacion {

        @Test
        @DisplayName("conserva el texto tal como lo escribió el usuario, con tildes y signos")
        void conservaTextoOriginal() {
            String valor = "¡Hola a todos! ¿Cómo están?";

            MessageText texto = new MessageText(valor);

            assertEquals(valor, texto.value());
        }

        @Test
        @DisplayName("acepta un texto de exactamente 1000 caracteres")
        void aceptaMilCaracteres() {
            String valor = "t".repeat(1000);

            MessageText texto = new MessageText(valor);

            assertEquals(1000, texto.value().length());
        }

        @ParameterizedTest(name = "rechaza [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\n"})
        @DisplayName("rechaza un texto nulo, vacío o en blanco")
        void rechazaTextoVacio(String valor) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new MessageText(valor));

            assertEquals("El texto del mensaje es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("rechaza un texto de más de 1000 caracteres")
        void rechazaMasDeMilCaracteres() {
            String valor = "t".repeat(1001);

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new MessageText(valor));

            assertEquals("El texto del mensaje admite como máximo 1000 caracteres", error.getMessage());
        }
    }
}

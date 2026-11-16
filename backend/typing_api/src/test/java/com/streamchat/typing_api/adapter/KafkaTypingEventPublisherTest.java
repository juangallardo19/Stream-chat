package com.streamchat.typing_api.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamchat.typing_api.domain.GroupId;
import com.streamchat.typing_api.domain.TypingEvent;
import com.streamchat.typing_api.domain.UserId;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaTypingEventPublisher")
class KafkaTypingEventPublisherTest {

    private static final String JSON_ESPERADO =
            "{\"user\":\"Ana\",\"group\":\"GrupoA\",\"occurredAt\":\"2026-11-13T16:30:01.050Z\"}";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @InjectMocks
    private KafkaTypingEventPublisher publicador;

    private final TypingEvent evento = new TypingEvent(
            new UserId("Ana"), new GroupId("GrupoA"), Instant.parse("2026-11-13T16:30:01.050Z"));

    @Nested
    @DisplayName("Al publicar un evento")
    class Publicar {

        @Test
        @DisplayName("lo envía a typing_events con la clave group y el JSON del contrato")
        void publicaConTopicClaveYJson() {
            when(kafkaTemplate.send("typing_events", "GrupoA", JSON_ESPERADO)).thenReturn(new CompletableFuture<>());

            publicador.publicar(evento);

            verify(kafkaTemplate, times(1)).send("typing_events", "GrupoA", JSON_ESPERADO);
        }

        @Test
        @DisplayName("no se queda esperando la confirmación de Kafka")
        void noEsperaConfirmacion() {
            CompletableFuture<SendResult<String, String>> sinRespuesta = new CompletableFuture<>();
            when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(sinRespuesta);

            assertDoesNotThrow(() -> publicador.publicar(evento));

            verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Al arrancar el servicio")
    class Calentamiento {

        @Test
        @DisplayName("pide una sola vez las particiones de typing_events para dejar listo el productor")
        void pideLasParticionesUnaVez() {
            when(kafkaTemplate.partitionsFor("typing_events")).thenReturn(List.of());

            publicador.afterSingletonsInstantiated();

            verify(kafkaTemplate, times(1)).partitionsFor("typing_events");
        }

        @Test
        @DisplayName("no impide el arranque si Kafka no responde a tiempo")
        void arrancaSiKafkaNoResponde() {
            when(kafkaTemplate.partitionsFor("typing_events"))
                    .thenThrow(new TimeoutException("Topic typing_events not present in metadata after 2000 ms."));

            assertDoesNotThrow(() -> publicador.afterSingletonsInstantiated());

            verify(kafkaTemplate, times(1)).partitionsFor("typing_events");
        }

        @Test
        @DisplayName("no impide el arranque si el productor no se puede crear")
        void arrancaSinProductor() {
            when(kafkaTemplate.partitionsFor("typing_events"))
                    .thenThrow(new org.apache.kafka.common.KafkaException("Failed to construct kafka producer",
                            new ConfigException("No resolvable bootstrap urls given in bootstrap.servers")));

            assertDoesNotThrow(() -> publicador.afterSingletonsInstantiated());

            verify(kafkaTemplate, times(1)).partitionsFor("typing_events");
        }
    }

    @Nested
    @DisplayName("Cuando Kafka falla")
    class KafkaFalla {

        @Test
        @DisplayName("no lanza error: en el flujo de estado se acepta perder un evento")
        void aceptaLaPerdida() {
            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker caído")));

            assertDoesNotThrow(() -> publicador.publicar(evento));

            verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("descarta el evento sin propagar el error cuando send falla de forma síncrona")
        void descartaFalloSincrono() {
            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                    .thenThrow(new KafkaException("Send failed",
                            new TimeoutException("Topic typing_events not present in metadata after 2000 ms.")));

            assertDoesNotThrow(() -> publicador.publicar(evento));

            verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("descarta el evento sin propagar el error cuando el productor no se puede crear")
        void descartaProductorSinCrear() {
            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                    .thenThrow(new org.apache.kafka.common.KafkaException("Failed to construct kafka producer",
                            new ConfigException("No resolvable bootstrap urls given in bootstrap.servers")));

            assertDoesNotThrow(() -> publicador.publicar(evento));

            verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("rechaza un evento nulo sin tocar Kafka")
        void rechazaNulo() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> publicador.publicar(null));

            assertEquals("El evento de tecleo es obligatorio", error.getMessage());
            verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
        }
    }
}

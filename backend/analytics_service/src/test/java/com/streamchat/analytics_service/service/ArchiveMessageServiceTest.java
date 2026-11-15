package com.streamchat.analytics_service.service;

import com.streamchat.analytics_service.domain.ArchivedMessage;
import com.streamchat.analytics_service.domain.AuditedMessage;
import com.streamchat.analytics_service.domain.ChatGroup;
import com.streamchat.analytics_service.port.ChatGroupRepository;
import com.streamchat.analytics_service.port.ChatMessageRepository;
import com.streamchat.analytics_service.port.MetricsReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArchiveMessageService")
class ArchiveMessageServiceTest {

    private static final Instant AHORA = Instant.parse("2026-11-13T16:31:00.000Z");
    private static final UUID ID = UUID.fromString("3f0c1c8e-7a2b-4d4e-9b1a-2c3d4e5f6a7b");

    @Mock
    private ChatMessageRepository mensajes;

    @Mock
    private ChatGroupRepository grupos;

    @Mock
    private MetricsReporter metricas;

    private ArchiveMessageService servicio;

    private final AuditedMessage auditado = new AuditedMessage(ID, "Ana", "GrupoA", "¡Hola!",
            Instant.parse("2026-11-13T16:30:10.123Z"));

    @BeforeEach
    void setUp() {
        servicio = new ArchiveMessageService(mensajes, grupos, metricas, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Nested
    @DisplayName("Con un mensaje nuevo")
    class MensajeNuevo {

        @Test
        @DisplayName("en un grupo que no existe, crea el grupo con total 1 y lo guarda")
        void creaElGrupo() {
            when(mensajes.existsById(ID)).thenReturn(false);
            when(grupos.findById("GrupoA")).thenReturn(Optional.empty());
            ArgumentCaptor<ChatGroup> grupoGuardado = ArgumentCaptor.forClass(ChatGroup.class);

            servicio.archivar(auditado);

            verify(grupos, times(1)).save(grupoGuardado.capture());
            assertEquals("GrupoA", grupoGuardado.getValue().getGroupId());
            assertEquals(1, grupoGuardado.getValue().getTotalMessages());
        }

        @Test
        @DisplayName("en un grupo existente, incrementa su total")
        void incrementaElTotal() {
            ChatGroup existente = ChatGroup.nuevo("GrupoA");
            existente.registrarMensaje(ArchivedMessage.archivar(
                    new AuditedMessage(UUID.randomUUID(), "Luis", "GrupoA", "Antes", Instant.parse("2026-11-13T16:00:00Z")), AHORA));
            when(mensajes.existsById(ID)).thenReturn(false);
            when(grupos.findById("GrupoA")).thenReturn(Optional.of(existente));

            servicio.archivar(auditado);

            assertEquals(2, existente.getTotalMessages());
            verify(grupos, times(1)).save(existente);
        }

        @Test
        @DisplayName("guarda el mensaje una vez, con la hora de archivo del reloj")
        void guardaElMensaje() {
            when(mensajes.existsById(ID)).thenReturn(false);
            when(grupos.findById("GrupoA")).thenReturn(Optional.empty());
            ArgumentCaptor<ArchivedMessage> guardado = ArgumentCaptor.forClass(ArchivedMessage.class);

            servicio.archivar(auditado);

            verify(mensajes, times(1)).save(guardado.capture());
            assertEquals(ID, guardado.getValue().getMessageId());
            assertEquals(AHORA, guardado.getValue().getArchivedAt());
        }

        @Test
        @DisplayName("reporta la métrica del grupo una vez")
        void reportaLaMetrica() {
            ChatGroup existente = ChatGroup.nuevo("GrupoA");
            when(mensajes.existsById(ID)).thenReturn(false);
            when(grupos.findById("GrupoA")).thenReturn(Optional.of(existente));
            ArgumentCaptor<ChatGroup> reportado = ArgumentCaptor.forClass(ChatGroup.class);

            servicio.archivar(auditado);

            verify(metricas, times(1)).reportar(reportado.capture());
            assertSame(existente, reportado.getValue());
        }
    }

    @Nested
    @DisplayName("Con un mensaje duplicado")
    class MensajeDuplicado {

        @Test
        @DisplayName("no guarda nada, no incrementa y no reporta")
        void ignoraElDuplicado() {
            when(mensajes.existsById(ID)).thenReturn(true);

            servicio.archivar(auditado);

            verify(mensajes, never()).save(any());
            verify(grupos, never()).save(any());
            verify(metricas, never()).reportar(any());
        }
    }

    @Nested
    @DisplayName("Con errores")
    class Errores {

        @Test
        @DisplayName("rechaza un mensaje nulo sin tocar la base de datos")
        void rechazaNulo() {
            NullPointerException error = assertThrows(NullPointerException.class, () -> servicio.archivar(null));

            assertEquals("El mensaje auditado es obligatorio", error.getMessage());
            verify(mensajes, never()).save(any());
            verify(grupos, never()).save(any());
        }
    }
}

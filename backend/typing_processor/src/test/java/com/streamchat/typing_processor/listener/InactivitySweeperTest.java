package com.streamchat.typing_processor.listener;

import com.streamchat.typing_processor.service.TypingProcessorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("InactivitySweeper")
class InactivitySweeperTest {

    @Mock
    private TypingProcessorService servicio;

    @InjectMocks
    private InactivitySweeper barredor;

    @Test
    @DisplayName("cada barrido le pide al servicio que detecte la inactividad una vez")
    void barreUnaVez() {
        barredor.barrer();

        verify(servicio, times(1)).detectarInactividad();
    }
}

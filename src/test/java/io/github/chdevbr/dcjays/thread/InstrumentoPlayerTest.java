package io.github.chdevbr.dcjays.thread;

import io.github.chdevbr.dcjays.model.Instrumento;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstrumentoPlayerTest {

    @Test
    void devePausarESerRetomadoSemPararAThread() {
        Instrumento instrumento = new Instrumento("baixo", "audio/baixo.wav");

        assertFalse(instrumento.isPausado());

        instrumento.pausar();
        assertTrue(instrumento.isPausado());

        instrumento.retomar();
        assertFalse(instrumento.isPausado());
    }
}

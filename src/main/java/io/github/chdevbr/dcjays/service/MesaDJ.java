package io.github.chdevbr.dcjays.service;

import io.github.chdevbr.dcjays.model.Instrumento;
import io.github.chdevbr.dcjays.thread.InstrumentoPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MesaDJ {

    private final List<Instrumento> instrumentos = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();

    private CountDownLatch inicioSincronizado;

    public void adicionarInstrumento(String nome, String caminhoAudio) {
        Instrumento instrumento = new Instrumento(nome, caminhoAudio);
        instrumentos.add(instrumento);
    }

    public void iniciar() {
        inicioSincronizado = new CountDownLatch(instrumentos.size());

        for (Instrumento instrumento : instrumentos) {
            InstrumentoPlayer player = new InstrumentoPlayer(instrumento, inicioSincronizado);
            Thread thread = new Thread(player, "audio-" + instrumento.getNome());

            threads.add(thread);
            thread.start();
        }
    }

    public boolean pausar(String nome) {
        Instrumento instrumento = encontrar(nome);
        if (instrumento == null) {
            return false;
        }

        instrumento.pausar();
        return true;
    }

    public boolean retomar(String nome) {
        Instrumento instrumento = encontrar(nome);
        if (instrumento == null) {
            return false;
        }

        instrumento.retomar();
        return true;
    }

    public List<Instrumento> listarInstrumentos() {
        return Collections.unmodifiableList(instrumentos);
    }

    private Instrumento encontrar(String nome) {
        for (Instrumento instrumento : instrumentos) {
            if (instrumento.getNome().equalsIgnoreCase(nome)) {
                return instrumento;
            }
        }
        return null;
    }

    public void encerrarTudo() throws InterruptedException {
        for (Instrumento instrumento : instrumentos) {
            instrumento.encerrar();
        }

        for (Thread thread : threads) {
            thread.interrupt();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
}
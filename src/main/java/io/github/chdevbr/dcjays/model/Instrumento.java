package io.github.chdevbr.dcjays.model;

public class Instrumento {

    private final String nome;
    private final String caminhoAudio;

    private boolean pausado;
    private boolean encerrado;

    public Instrumento(String nome, String caminhoAudio) {
        this.nome = nome;
        this.caminhoAudio = caminhoAudio;
    }

    public String getNome() {
        return nome;
    }

    public String getCaminhoAudio() {
        return caminhoAudio;
    }

    public synchronized boolean isPausado() {
        return pausado;
    }

    public synchronized boolean isEncerrado() {
        return encerrado;
    }

    public synchronized void pausar() {
        pausado = true;
    }

    public synchronized void retomar() {
        pausado = false;
    }

    public synchronized void encerrar() {
        encerrado = true;
    }
}

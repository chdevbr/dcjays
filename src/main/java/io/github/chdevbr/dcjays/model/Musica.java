package io.github.chdevbr.dcjays.model;

public class Musica {

    private final String nome;
    private final String pasta;

    public Musica(String nome, String pasta) {
        this.nome = nome;
        this.pasta = pasta;
    }

    public String getNome() {
        return nome;
    }

    public String getPasta() {
        return pasta;
    }

    @Override
    public String toString() {
        return nome;
    }
}
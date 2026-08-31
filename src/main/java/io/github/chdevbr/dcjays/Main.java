package io.github.chdevbr.dcjays;

import io.github.chdevbr.dcjays.controller.DJVisualController;
import io.github.chdevbr.dcjays.service.MesaDJ;

public class Main {
    private static final String AUDIO = "audio/it-was-a-good-day/";
    public static void main(String[] args) {
        MesaDJ mesaDJ = new MesaDJ();
        mesaDJ.adicionarInstrumento("bateria", AUDIO + "bateria.wav");
        mesaDJ.adicionarInstrumento("baixo", AUDIO + "baixo.wav");
        mesaDJ.adicionarInstrumento("beat", AUDIO + "outro.wav");
        mesaDJ.adicionarInstrumento("vocal", AUDIO + "vocal.wav");
        mesaDJ.iniciar();
        new DJVisualController(mesaDJ).iniciar();
    }
}
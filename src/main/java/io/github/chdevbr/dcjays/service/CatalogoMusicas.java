package io.github.chdevbr.dcjays.service;

import io.github.chdevbr.dcjays.model.Musica;

import java.util.List;

public class CatalogoMusicas {

    public List<Musica> listar() {
        return List.of(
            new Musica(
                "It Was a Good Day",
                "audio/it-was-a-good-day/"
            ),
            new Musica(
                "GTA San Andreas Theme",
                "audio/gta-san-andreas-theme/"
            )
        );
    }
}
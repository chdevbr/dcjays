package io.github.chdevbr.dcjays.web;

import io.github.chdevbr.dcjays.model.Instrumento;
import io.github.chdevbr.dcjays.model.Musica;
import io.github.chdevbr.dcjays.service.CatalogoMusicas;
import io.github.chdevbr.dcjays.service.MesaDJ;

import java.util.List;

public class MesaDJWebController {

    private final CatalogoMusicas catalogoMusicas = new CatalogoMusicas();
    private final List<Musica> musicas = catalogoMusicas.listar();

    private MesaDJ mesaDJ;
    private Musica musicaAtual;
    private boolean carregando;

    public synchronized List<Musica> listarMusicas() {
        return musicas;
    }

    public synchronized StatusMesa carregarMusica(String nomeMusica) throws InterruptedException {
        Musica musica = encontrarMusica(nomeMusica);
        if (musica == null) {
            return StatusMesa.erro("Musica nao encontrada");
        }

        carregando = true;

        try {
            if (mesaDJ != null) {
                mesaDJ.encerrarTudo();
            }

            MesaDJ novaMesa = criarMesa(musica);
            novaMesa.iniciar();

            mesaDJ = novaMesa;
            musicaAtual = musica;
        } finally {
            carregando = false;
        }

        return criarStatus(null);
    }

    public synchronized StatusMesa pausar(String instrumento) {
        if (mesaDJ == null) {
            return StatusMesa.erro("Nenhuma musica carregada");
        }

        if (!mesaDJ.pausar(instrumento)) {
            return StatusMesa.erro("Faixa nao encontrada");
        }

        return criarStatus(null);
    }

    public synchronized StatusMesa retomar(String instrumento) {
        if (mesaDJ == null) {
            return StatusMesa.erro("Nenhuma musica carregada");
        }

        if (!mesaDJ.retomar(instrumento)) {
            return StatusMesa.erro("Faixa nao encontrada");
        }

        return criarStatus(null);
    }

    public synchronized StatusMesa alterarTodas(boolean tocar) {
        if (mesaDJ == null) {
            return StatusMesa.erro("Nenhuma musica carregada");
        }

        for (Instrumento instrumento : mesaDJ.listarInstrumentos()) {
            if (tocar) {
                mesaDJ.retomar(instrumento.getNome());
            } else {
                mesaDJ.pausar(instrumento.getNome());
            }
        }

        return criarStatus(null);
    }

    public synchronized StatusMesa encerrar() throws InterruptedException {
        if (mesaDJ != null) {
            mesaDJ.encerrarTudo();
        }

        mesaDJ = null;
        musicaAtual = null;

        return criarStatus("Mesa encerrada");
    }

    public synchronized StatusMesa status() {
        return criarStatus(null);
    }

    private MesaDJ criarMesa(Musica musica) {
        MesaDJ novaMesa = new MesaDJ();
        String audio = musica.getPasta();

        novaMesa.adicionarInstrumento("bateria", audio + "bateria.wav");
        novaMesa.adicionarInstrumento("baixo", audio + "baixo.wav");
        novaMesa.adicionarInstrumento("beat", audio + "outro.wav");
        novaMesa.adicionarInstrumento("vocal", audio + "vocal.wav");

        return novaMesa;
    }

    private Musica encontrarMusica(String nomeMusica) {
        for (Musica musica : musicas) {
            if (musica.getNome().equalsIgnoreCase(nomeMusica)) {
                return musica;
            }
        }

        return null;
    }

    private StatusMesa criarStatus(String mensagem) {
        return new StatusMesa(musicaAtual, mesaDJ, carregando, mensagem, null);
    }

    public record StatusMesa(
            Musica musicaAtual,
            MesaDJ mesaDJ,
            boolean carregando,
            String mensagem,
            String erro
    ) {
        public static StatusMesa erro(String erro) {
            return new StatusMesa(null, null, false, null, erro);
        }
    }
}

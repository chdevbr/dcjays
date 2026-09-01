package io.github.chdevbr.dcjays.thread;

import io.github.chdevbr.dcjays.model.Instrumento;
import io.github.chdevbr.dcjays.web.RecursoAplicacao;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class InstrumentoPlayer implements Runnable {

    private static final long INTERVALO_VERIFICACAO_MS = 20;

    private final Instrumento instrumento;
    private final CountDownLatch inicioSincronizado;

    public InstrumentoPlayer(Instrumento instrumento, CountDownLatch inicioSincronizado) {
        this.instrumento = instrumento;
        this.inicioSincronizado = inicioSincronizado;
    }

    @Override
    public void run() {
        Clip clip = null;

        try {
            clip = carregarClip();
        } catch (Exception e) {
            System.err.printf("Não foi possível carregar '%s': %s%n",
                    instrumento.getNome(), e.getMessage());
        } finally {
            inicioSincronizado.countDown();
        }

        if (clip == null) {
            return;
        }

        try {
            inicioSincronizado.await();

            clip.loop(Clip.LOOP_CONTINUOUSLY);

            boolean somAtivo = true;

            while (!instrumento.isEncerrado()) {
                if (instrumento.isPausado() && somAtivo) {
                    silenciar(clip);
                    somAtivo = false;
                } else if (!instrumento.isPausado() && !somAtivo) {
                    ativarSom(clip);
                    somAtivo = true;
                }

                Thread.sleep(INTERVALO_VERIFICACAO_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            clip.stop();
            clip.close();
        }
    }

    private void silenciar(Clip clip) {
        if (clip.isControlSupported(BooleanControl.Type.MUTE)) {
            BooleanControl mute = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
            mute.setValue(true);
            return;
        }

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(volume.getMinimum());
        }
    }

    private void ativarSom(Clip clip) {
        if (clip.isControlSupported(BooleanControl.Type.MUTE)) {
            BooleanControl mute = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
            mute.setValue(false);
            return;
        }

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(0.0f);
        }
    }

    private Clip carregarClip() throws Exception {
        try (InputStream recurso = RecursoAplicacao.abrir(instrumento.getCaminhoAudio());
             InputStream buffer = new BufferedInputStream(recurso);
             AudioInputStream audio = AudioSystem.getAudioInputStream(buffer)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            return clip;
        }
    }
}

package io.github.chdevbr.dcjays;

import io.github.chdevbr.dcjays.controller.DJVisualController;
import io.github.chdevbr.dcjays.web.ServidorWeb;

import java.io.IOException;
import java.net.BindException;

public class Main {

    private static final int PORTA_INICIAL = 8080;
    private static final int PORTA_FINAL = 8090;

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && "--swing".equalsIgnoreCase(args[0])) {
            iniciarInterfaceLegada();
            return;
        }

        iniciarServidorWeb();
    }

    private static void iniciarInterfaceLegada() {
        new DJVisualController().iniciar();
    }

    private static void iniciarServidorWeb() throws IOException {
        IOException ultimoErro = null;

        for (int porta = PORTA_INICIAL; porta <= PORTA_FINAL; porta++) {
            try {
                new ServidorWeb(porta).iniciar();
                return;
            } catch (BindException e) {
                ultimoErro = e;
                System.out.println("Porta " + porta + " ocupada. Tentando a proxima...");
            }
        }

        throw new IOException(
                "Nao foi possivel iniciar o DCJAYS Web entre as portas "
                        + PORTA_INICIAL
                        + " e "
                        + PORTA_FINAL,
                ultimoErro
        );
    }
}

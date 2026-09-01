package io.github.chdevbr.dcjays.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RecursoAplicacao {

    private static final Path PASTA_RECURSOS =
            Path.of("src", "main", "resources");

    private RecursoAplicacao() {
    }

    public static InputStream abrir(String caminho) throws IOException {
        String caminhoNormalizado = normalizar(caminho);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream recurso = classLoader.getResourceAsStream(caminhoNormalizado);

        if (recurso != null) {
            return recurso;
        }

        Path arquivo = PASTA_RECURSOS.resolve(caminhoNormalizado).normalize();
        if (!arquivo.startsWith(PASTA_RECURSOS.normalize())) {
            throw new IOException("caminho de recurso invalido");
        }

        if (!Files.exists(arquivo) || Files.isDirectory(arquivo)) {
            throw new IOException("recurso nao encontrado");
        }

        return Files.newInputStream(arquivo);
    }

    public static boolean existe(String caminho) {
        try (InputStream ignored = abrir(caminho)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String normalizar(String caminho) {
        return caminho
                .replace('\\', '/')
                .replaceFirst("^/+", "");
    }
}

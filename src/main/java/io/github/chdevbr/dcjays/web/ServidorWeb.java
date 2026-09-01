package io.github.chdevbr.dcjays.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.chdevbr.dcjays.model.Instrumento;
import io.github.chdevbr.dcjays.model.Musica;
import io.github.chdevbr.dcjays.service.MesaDJ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

public class ServidorWeb {

    private final MesaDJWebController controller;
    private final HttpServer servidor;
    private final int porta;

    public ServidorWeb(int porta) throws IOException {
        this.porta = porta;
        controller = new MesaDJWebController();
        servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        servidor.createContext("/", this::rotear);
        servidor.setExecutor(Executors.newCachedThreadPool());
    }

    public void iniciar() {
        servidor.start();
        System.out.println("DCJAYS Web rodando em http://localhost:" + porta);
    }

    private void rotear(HttpExchange exchange) throws IOException {
        try {
            URI uri = exchange.getRequestURI();
            String caminho = uri.getPath();

            if (caminho.startsWith("/api/")) {
                rotearApi(exchange, caminho);
                return;
            }

            servirArquivoEstatico(exchange, caminho);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            enviarJson(exchange, 500, "{\"erro\":\"Operacao interrompida\"}");
        } catch (Exception e) {
            enviarJson(exchange, 500, "{\"erro\":\"Erro interno\"}");
        } finally {
            exchange.close();
        }
    }

    private void rotearApi(HttpExchange exchange, String caminho)
            throws IOException, InterruptedException {

        String metodo = exchange.getRequestMethod();

        if ("GET".equals(metodo) && "/api/musicas".equals(caminho)) {
            enviarJson(exchange, 200, musicasJson(controller.listarMusicas()));
            return;
        }

        if ("GET".equals(metodo) && "/api/mesa/status".equals(caminho)) {
            enviarJson(exchange, 200, statusJson(controller.status()));
            return;
        }

        if ("POST".equals(metodo) && "/api/mesa/carregar".equals(caminho)) {
            String corpo = lerCorpo(exchange);
            String musica = extrairValorJson(corpo, "musica");
            MesaDJWebController.StatusMesa status = controller.carregarMusica(musica);
            enviarStatus(exchange, status);
            return;
        }

        if ("POST".equals(metodo) && "/api/mesa/tocar-todos".equals(caminho)) {
            enviarStatus(exchange, controller.alterarTodas(true));
            return;
        }

        if ("POST".equals(metodo) && "/api/mesa/pausar-todos".equals(caminho)) {
            enviarStatus(exchange, controller.alterarTodas(false));
            return;
        }

        if ("POST".equals(metodo) && "/api/mesa/encerrar".equals(caminho)) {
            enviarStatus(exchange, controller.encerrar());
            return;
        }

        if ("POST".equals(metodo) && caminho.startsWith("/api/mesa/instrumentos/")) {
            rotearInstrumento(exchange, caminho);
            return;
        }

        enviarJson(exchange, 404, "{\"erro\":\"Rota nao encontrada\"}");
    }

    private void rotearInstrumento(HttpExchange exchange, String caminho) throws IOException {
        String prefixo = "/api/mesa/instrumentos/";
        String restante = caminho.substring(prefixo.length());
        String[] partes = restante.split("/");

        if (partes.length != 2) {
            enviarJson(exchange, 404, "{\"erro\":\"Rota nao encontrada\"}");
            return;
        }

        String instrumento = decodificar(partes[0]);
        String acao = partes[1];

        MesaDJWebController.StatusMesa status;
        if ("pausar".equals(acao)) {
            status = controller.pausar(instrumento);
        } else if ("retomar".equals(acao)) {
            status = controller.retomar(instrumento);
        } else {
            enviarJson(exchange, 404, "{\"erro\":\"Rota nao encontrada\"}");
            return;
        }

        enviarStatus(exchange, status);
    }

    private void servirArquivoEstatico(HttpExchange exchange, String caminho) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            enviarTexto(exchange, 405, "Metodo nao permitido", "text/plain; charset=utf-8");
            return;
        }

        String recurso = "/".equals(caminho)
                ? "web/index.html"
                : "web" + caminho;

        if (caminho.startsWith("/images/")) {
            recurso = caminho.substring(1);
        }

        try (InputStream input = RecursoAplicacao.abrir(recurso)) {
            byte[] bytes = input.readAllBytes();
            enviarBytes(exchange, 200, bytes, contentType(recurso));
        } catch (IOException e) {
            enviarTexto(exchange, 404, "Pagina nao encontrada", "text/plain; charset=utf-8");
        }
    }

    private void enviarStatus(HttpExchange exchange, MesaDJWebController.StatusMesa status) throws IOException {
        int codigo = status.erro() == null ? 200 : 400;
        enviarJson(exchange, codigo, statusJson(status));
    }

    private String musicasJson(List<Musica> musicas) {
        StringBuilder json = new StringBuilder();
        json.append("{\"musicas\":[");

        for (int i = 0; i < musicas.size(); i++) {
            Musica musica = musicas.get(i);
            if (i > 0) {
                json.append(',');
            }

            json.append("{\"nome\":\"")
                    .append(escaparJson(musica.getNome()))
                    .append("\"}");
        }

        json.append("]}");
        return json.toString();
    }

    private String statusJson(MesaDJWebController.StatusMesa status) {
        StringBuilder json = new StringBuilder();
        json.append('{');
        json.append("\"carregando\":").append(status.carregando());
        json.append(",\"musicaAtual\":");

        if (status.musicaAtual() == null) {
            json.append("null");
        } else {
            json.append("{\"nome\":\"")
                    .append(escaparJson(status.musicaAtual().getNome()))
                    .append("\"}");
        }

        json.append(",\"instrumentos\":[");

        MesaDJ mesaDJ = status.mesaDJ();
        if (mesaDJ != null) {
            List<Instrumento> instrumentos = mesaDJ.listarInstrumentos();
            for (int i = 0; i < instrumentos.size(); i++) {
                Instrumento instrumento = instrumentos.get(i);
                if (i > 0) {
                    json.append(',');
                }

                boolean tocando = !instrumento.isPausado() && !instrumento.isEncerrado();

                json.append("{\"nome\":\"")
                        .append(escaparJson(instrumento.getNome()))
                        .append("\",\"tocando\":")
                        .append(tocando)
                        .append(",\"status\":\"")
                        .append(tocando ? "tocando" : "mudo")
                        .append("\"}");
            }
        }

        json.append(']');

        if (status.mensagem() != null) {
            json.append(",\"mensagem\":\"")
                    .append(escaparJson(status.mensagem()))
                    .append("\"");
        }

        if (status.erro() != null) {
            json.append(",\"erro\":\"")
                    .append(escaparJson(status.erro()))
                    .append("\"");
        }

        json.append('}');
        return json.toString();
    }

    private String lerCorpo(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private String extrairValorJson(String json, String chave) {
        if (json == null || json.isBlank()) {
            return "";
        }

        String busca = "\"" + chave + "\"";
        int chaveInicio = json.indexOf(busca);
        if (chaveInicio < 0) {
            return "";
        }

        int doisPontos = json.indexOf(':', chaveInicio + busca.length());
        if (doisPontos < 0) {
            return "";
        }

        int aspasInicio = json.indexOf('"', doisPontos + 1);
        if (aspasInicio < 0) {
            return "";
        }

        StringBuilder valor = new StringBuilder();
        boolean escapado = false;

        for (int i = aspasInicio + 1; i < json.length(); i++) {
            char caractere = json.charAt(i);

            if (escapado) {
                valor.append(caractere);
                escapado = false;
            } else if (caractere == '\\') {
                escapado = true;
            } else if (caractere == '"') {
                return valor.toString();
            } else {
                valor.append(caractere);
            }
        }

        return "";
    }

    private String decodificar(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }

    private void enviarJson(HttpExchange exchange, int codigo, String json) throws IOException {
        enviarTexto(exchange, codigo, json, "application/json; charset=utf-8");
    }

    private void enviarTexto(HttpExchange exchange, int codigo, String texto, String contentType) throws IOException {
        enviarBytes(exchange, codigo, texto.getBytes(StandardCharsets.UTF_8), contentType);
    }

    private void enviarBytes(HttpExchange exchange, int codigo, byte[] bytes, String contentType) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(codigo, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String contentType(String recurso) {
        if (recurso.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (recurso.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (recurso.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (recurso.endsWith(".png")) {
            return "image/png";
        }

        return "application/octet-stream";
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}

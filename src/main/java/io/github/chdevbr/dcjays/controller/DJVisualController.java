package io.github.chdevbr.dcjays.controller;

import io.github.chdevbr.dcjays.model.Instrumento;
import io.github.chdevbr.dcjays.model.Musica;
import io.github.chdevbr.dcjays.service.CatalogoMusicas;
import io.github.chdevbr.dcjays.service.MesaDJ;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DJVisualController {

    private static final Color FUNDO = new Color(24, 24, 28);
    private static final Color PAINEL = new Color(36, 37, 43);
    private static final Color TEXTO = new Color(241, 241, 238);
    private static final Color TEXTO_FRACO = new Color(176, 179, 184);
    private static final Color VERDE = new Color(71, 201, 128);
    private static final Color VERMELHO = new Color(235, 103, 103);

    private MesaDJ mesaDJ;

    private JComboBox<Musica> seletorMusicas;

    private JPanel painelInstrumentos;

    private final Map<String, JCheckBox> caixas = new LinkedHashMap<>();
    private final Map<String, JLabel> status = new LinkedHashMap<>();

    private JFrame janela;

    private Timer timer;

    private final CatalogoMusicas catalogoMusicas =
            new CatalogoMusicas();

    private final List<Musica> musicas =
            catalogoMusicas.listar();

    public void iniciar() {
        SwingUtilities.invokeLater(this::criarJanela);
    }

    private void criarJanela() {
        janela = new JFrame("DCJAYS - GTA San Andreas Theme");
        janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        janela.setMinimumSize(new Dimension(430, 350));
        janela.setLocationByPlatform(true);

        janela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                encerrarAplicacao();
            }
        });

        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBackground(DJVisualController.FUNDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        raiz.add(criarCabecalho(), BorderLayout.NORTH);

        painelInstrumentos = new JPanel(new BorderLayout());
        painelInstrumentos.setOpaque(false);

        raiz.add(painelInstrumentos, BorderLayout.CENTER);

        raiz.add(criarBotoes(), BorderLayout.SOUTH);

        janela.setContentPane(raiz);
        janela.pack();
        janela.setVisible(true);

        timer = new Timer(250, event -> atualizarStatus());
        timer.start();
    }

    private void carregarMusica() {

        Musica musica =
            (Musica) seletorMusicas.getSelectedItem();

        if (musica == null) {
            return;
        }

        new Thread(() -> {

            try {

                if (mesaDJ != null) {
                    mesaDJ.encerrarTudo();
                }

                MesaDJ novaMesa = criarMesa(musica);

                novaMesa.iniciar();

                mesaDJ = novaMesa;

                SwingUtilities.invokeLater(
                        this::atualizarPainelInstrumentos
                );

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

            }

        }, "troca-musica").start();
    }

    private MesaDJ criarMesa(Musica musica) {

        MesaDJ novaMesa = new MesaDJ();

        String audio = musica.getPasta();

        novaMesa.adicionarInstrumento(
                "bateria",
                audio + "bateria.wav"
        );

        novaMesa.adicionarInstrumento(
                "baixo",
                audio + "baixo.wav"
        );

        novaMesa.adicionarInstrumento(
                "beat",
                audio + "outro.wav"
        );

        novaMesa.adicionarInstrumento(
                "vocal",
                audio + "vocal.wav"
        );

        return novaMesa;
    }

    private JPanel criarCabecalho() {

        JPanel painel = new JPanel(new GridBagLayout());
        painel.setOpaque(false);

        JLabel titulo = new JLabel("DCJAYS");
        titulo.setForeground(TEXTO);
        titulo.setFont(
            new Font(
                Font.SANS_SERIF,
                Font.BOLD,
                28
            )
        );

        JLabel labelMusica =
                new JLabel("Escolha uma música:");


        labelMusica.setForeground(TEXTO_FRACO);

        seletorMusicas =
                new JComboBox<>(musicas.toArray(new Musica[0])
        );

        JButton carregar =
                new JButton("Carregar música");

        carregar.addActionListener(
                event -> carregarMusica()
        );

        GridBagConstraints c =
                new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 10, 0);

        painel.add(titulo, c);

        c.gridy = 1;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 5, 10);

        painel.add(labelMusica, c);

        c.gridx = 1;

        painel.add(seletorMusicas, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;

        painel.add(carregar, c);

        return painel;
    }

    private JPanel criarListaInstrumentos() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(PAINEL);
        painel.setBorder(BorderFactory.createLineBorder(new Color(61, 63, 72)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 12, 12, 12);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        int linha = 0;
        for (Instrumento instrumento : mesaDJ.listarInstrumentos()) {
            JCheckBox caixa = new JCheckBox(capitalizar(instrumento.getNome()));
            caixa.setSelected(!instrumento.isPausado());
            caixa.setOpaque(false);
            caixa.setForeground(TEXTO);
            caixa.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            caixa.addActionListener(event -> alternarInstrumento(instrumento, caixa.isSelected()));

            JLabel textoStatus = new JLabel();
            textoStatus.setHorizontalAlignment(SwingConstants.RIGHT);
            textoStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

            caixas.put(instrumento.getNome(), caixa);
            status.put(instrumento.getNome(), textoStatus);

            c.gridy = linha;
            c.gridx = 0;
            painel.add(caixa, c);

            c.gridx = 1;
            c.weightx = 0;
            painel.add(textoStatus, c);

            linha++;
        }

        return painel;
    }

    private void atualizarPainelInstrumentos() {

        painelInstrumentos.removeAll();

        caixas.clear();
        status.clear();

        painelInstrumentos.add(
            criarListaInstrumentos(),
            BorderLayout.CENTER
        );

        painelInstrumentos.revalidate();
        painelInstrumentos.repaint();

        janela.pack();
    }

    private JPanel criarBotoes() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setOpaque(false);

        JButton tocarTodos = new JButton("Tocar todos");
        tocarTodos.addActionListener(event -> alterarTodas(true));

        JButton pausarTodos = new JButton("Pausar todos");
        pausarTodos.addActionListener(event -> alterarTodas(false));

        JButton sair = new JButton("Sair");
        sair.addActionListener(event -> encerrarAplicacao());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 4, 0, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.gridx = 0;
        painel.add(tocarTodos, c);
        c.gridx = 1;
        painel.add(pausarTodos, c);
        c.gridx = 2;
        painel.add(sair, c);

        return painel;
    }

    private void alternarInstrumento(Instrumento instrumento, boolean tocar) {
        if (tocar) {
            mesaDJ.retomar(instrumento.getNome());
        } else {
            mesaDJ.pausar(instrumento.getNome());
        }

        atualizarStatus();
    }

    private void alterarTodas(boolean tocar) {

        if (mesaDJ == null) {
            return;
        }

        for (Instrumento instrumento : mesaDJ.listarInstrumentos()) {
            if (tocar) {
                mesaDJ.retomar(instrumento.getNome());
            } else {
                mesaDJ.pausar(instrumento.getNome());
            }
        }

        atualizarStatus();
    }

    private void atualizarStatus() {

        if (mesaDJ == null) {
            return;
        }

        for (Instrumento instrumento : mesaDJ.listarInstrumentos()) {
            boolean tocando = !instrumento.isPausado() && !instrumento.isEncerrado();

            JCheckBox caixa = caixas.get(instrumento.getNome());
            JLabel textoStatus = status.get(instrumento.getNome());

            caixa.setSelected(tocando);
            textoStatus.setText(tocando ? "TOCANDO" : "MUDO");
            textoStatus.setForeground(tocando ? VERDE : VERMELHO);
        }
    }

    private String capitalizar(String nome) {
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }

    private void encerrarAplicacao() {
        if (timer != null) {
            timer.stop();
        }

        new Thread(() -> {
            try {

                if (mesaDJ != null) {
                    mesaDJ.encerrarTudo();
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

            } finally {

                SwingUtilities.invokeLater(() -> {
                    janela.dispose();
                    System.exit(0);

                });
            }
        }, "encerramento").start();
    }
}
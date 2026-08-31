package io.github.chdevbr.dcjays.controller;

import io.github.chdevbr.dcjays.model.Instrumento;
import io.github.chdevbr.dcjays.service.MesaDJ;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
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
import java.util.Map;

public class DJVisualController {

    private static final Color FUNDO = new Color(24, 24, 28);
    private static final Color PAINEL = new Color(36, 37, 43);
    private static final Color TEXTO = new Color(241, 241, 238);
    private static final Color TEXTO_FRACO = new Color(176, 179, 184);
    private static final Color VERDE = new Color(71, 201, 128);
    private static final Color VERMELHO = new Color(235, 103, 103);

    private final MesaDJ mesaDJ;
    private final Map<String, JCheckBox> caixas = new LinkedHashMap<>();
    private final Map<String, JLabel> status = new LinkedHashMap<>();

    private JFrame janela;
    private Timer timer;

    public DJVisualController(MesaDJ mesaDJ) {
        this.mesaDJ = mesaDJ;
    }

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
        raiz.setBackground(FUNDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(criarListaInstrumentos(), BorderLayout.CENTER);
        raiz.add(criarBotoes(), BorderLayout.SOUTH);

        janela.setContentPane(raiz);
        janela.pack();
        janela.setVisible(true);

        timer = new Timer(250, event -> atualizarStatus());
        timer.start();
    }

    private JPanel criarCabecalho() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setOpaque(false);

        JLabel titulo = new JLabel("DCJAYS");
        titulo.setForeground(TEXTO);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));

        JLabel subtitulo = new JLabel("Marque as faixas que você quer ouvir");
        subtitulo.setForeground(TEXTO_FRACO);

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(subtitulo, BorderLayout.CENTER);
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
                mesaDJ.encerrarTudo();
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
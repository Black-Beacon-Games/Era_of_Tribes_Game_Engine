package com.eraoftribes.engine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

public class DebugConsole {
    private JFrame frame;
    private JTextArea textArea;
    private JTextField inputField;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private boolean visible;

    public DebugConsole() {
        originalOut = System.out;
        originalErr = System.err;
    }

    public void show() {
        if (visible) return;
        visible = true;

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Debug Console");
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);

            textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 13));
            textArea.setBackground(new java.awt.Color(30, 30, 30));
            textArea.setForeground(new java.awt.Color(220, 220, 220));
            ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            inputField = new JTextField();
            inputField.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 13));
            inputField.setBackground(new java.awt.Color(20, 20, 20));
            inputField.setForeground(new java.awt.Color(0, 255, 0));
            inputField.setCaretColor(java.awt.Color.GREEN);
            inputField.addActionListener(e -> {
                String cmd = inputField.getText().trim();
                inputField.setText("");
                if (!cmd.isEmpty()) {
                    appendText("> " + cmd + "\n");
                    executeCommand(cmd);
                }
            });

            JScrollPane scrollPane = new JScrollPane(textArea);
            frame.add(scrollPane, java.awt.BorderLayout.CENTER);
            frame.add(inputField, java.awt.BorderLayout.SOUTH);

            frame.setVisible(true);

            PrintStream consoleStream = new PrintStream(new OutputStream() {
                private final StringBuilder buffer = new StringBuilder();

                public void write(int b) throws IOException {
                    buffer.append((char) b);
                    if (b == '\n') {
                        String line = buffer.toString();
                        buffer.setLength(0);
                        appendText(line);
                    }
                }

                public void write(byte[] b, int off, int len) {
                    String text = new String(b, off, len);
                    appendText(text);
                }
            });

            System.setOut(consoleStream);
            System.setErr(consoleStream);
        });
    }

    public void hide() {
        if (!visible) return;
        visible = false;
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispose();
                frame = null;
            }
            System.setOut(originalOut);
            System.setErr(originalErr);
        });
    }

    public void toggle() {
        if (visible) hide();
        else show();
    }

    public boolean isVisible() { return visible; }

    private void appendText(String text) {
        if (textArea != null) {
            SwingUtilities.invokeLater(() -> {
                textArea.append(text);
                int max = 10000;
                if (textArea.getLineCount() > max) {
                    try {
                        int end = textArea.getLineEndOffset(textArea.getLineCount() - max - 1);
                        textArea.replaceRange("", 0, end);
                    } catch (Exception ignored) {}
                }
            });
        }
        if (originalOut != null) {
            originalOut.print(text);
        }
    }

    private void executeCommand(String cmd) {
        String[] parts = cmd.split("\\s+");
        switch (parts[0].toLowerCase()) {
            case "help" -> appendText("Commands: help, clear, gc, mem, threads, exit\n");
            case "clear" -> {
                if (textArea != null) textArea.setText("");
            }
            case "gc" -> {
                System.gc();
                appendText("GC triggered.\n");
            }
            case "mem" -> {
                Runtime rt = Runtime.getRuntime();
                long used = rt.totalMemory() - rt.freeMemory();
                appendText("Used: " + (used / 1024 / 1024) + " MB / Total: "
                    + (rt.totalMemory() / 1024 / 1024) + " MB / Max: "
                    + (rt.maxMemory() / 1024 / 1024) + " MB\n");
            }
            case "threads" -> {
                int count = java.lang.management.ManagementFactory.getThreadMXBean().getThreadCount();
                appendText("Active threads: " + count + "\n");
            }
            case "exit" -> hide();
            default -> appendText("Unknown command: " + cmd + "\n");
        }
    }
}

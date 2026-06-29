package com.eraoftribes.engine.rendering;

import com.eraoftribes.engine.EngineConfig.RendererConfig;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Renderer {
    private RendererConfig config;
    private JFrame frame;
    private Canvas canvas;
    private BufferStrategy buffer;
    private Graphics2D g;
    private volatile boolean running;
    private int width, height;

    private volatile int mouseX, mouseY;
    private volatile boolean mousePressed;
    private volatile boolean mouseClicked;
    private volatile int clickX, clickY;
    private volatile int lastKey;

    private final Map<Integer, BufferedImage> textures = new HashMap<>();
    private int nextTexId = 1;

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 40);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public Renderer(RendererConfig config) {
        this.config = config;
        System.out.println("[Renderer] Initialized (backend=swing, vsync=" + config.vsync + ")");
    }

    private String windowTitle;
    private boolean pendingRebuild;

    public void createWindow(String title, int w, int h) {
        this.windowTitle = title;
        this.width = w;
        this.height = h;
        doBuildWindow();
        running = true;
        System.out.println("[Renderer] Window created: " + title + " (" + w + "x" + h + ")");
    }

    private void doBuildWindow() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }

        boolean undecorated = config.borderless || config.fullscreen;
        frame = new JFrame(windowTitle);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setUndecorated(undecorated);
        frame.setSize(width, height);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        canvas = new Canvas();
        canvas.setSize(width, height);
        canvas.setBackground(Color.BLACK);
        frame.add(canvas);

        canvas.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) { clickX = e.getX(); clickY = e.getY(); mouseClicked = true; }
            public void mousePressed(MouseEvent e) { mousePressed = true; mouseX = e.getX(); mouseY = e.getY(); }
            public void mouseReleased(MouseEvent e) { mousePressed = false; }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        canvas.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
            public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        });

        canvas.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) { lastKey = e.getKeyCode(); }
            public void keyReleased(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { running = false; }
        });

        frame.setVisible(true);
        canvas.requestFocus();

        while (!canvas.isDisplayable()) {
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }

        canvas.createBufferStrategy(2);
        buffer = canvas.getBufferStrategy();
    }

    public void applyConfig(RendererConfig cfg) {
        if (cfg.borderless != config.borderless || cfg.fullscreen != config.fullscreen) {
            config = cfg;
            width = cfg.resolution.width;
            height = cfg.resolution.height;
            pendingRebuild = true;
            System.out.println("[Renderer] Window rebuild pending (borderless=" + cfg.borderless + ", fullscreen=" + cfg.fullscreen + ")");
        } else {
            config = cfg;
            if (cfg.resolution.width != width || cfg.resolution.height != height) {
                width = cfg.resolution.width;
                height = cfg.resolution.height;
                frame.setSize(width, height);
                canvas.setSize(width, height);
                frame.setLocationRelativeTo(null);
                System.out.println("[Renderer] Resolution changed to " + width + "x" + height);
            }
        }
    }

    public void beginFrame() {
        if (pendingRebuild) {
            pendingRebuild = false;
            doBuildWindow();
        }
        if (buffer == null) return;
        g = (Graphics2D) buffer.getDrawGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
    }

    public void endFrame() {
        if (g != null) g.dispose();
        if (buffer != null) buffer.show();
    }

    public boolean shouldClose() { return !running; }

    public void requestClose() { running = false; }

    public void destroy() {
        running = false;
        if (frame != null) {
            try {
                SwingUtilities.invokeAndWait(frame::dispose);
            } catch (Exception e) {
                frame.dispose();
            }
        }
        System.out.println("[Renderer] Shutdown.");
    }

    public void clear(float r, float g, float b, float a) {
        if (this.g != null) {
            this.g.setColor(new Color(r, g, b, a));
            this.g.fillRect(0, 0, width, height);
        }
    }

    public void drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {
        if (this.g != null) {
            this.g.setColor(new Color(clamp(r), clamp(g), clamp(b), clamp(a)));
            this.g.fillRect((int) x, (int) y, (int) w, (int) h);
        }
    }

    public void drawText(String text, float x, float y, float r, float g, float b, float a) {
        drawText(text, x, y, r, g, b, a, BODY_FONT);
    }

    public void drawText(String text, float x, float y, float r, float g, float b, float a, Font font) {
        if (this.g != null) {
            this.g.setFont(font);
            this.g.setColor(new Color(clamp(r), clamp(g), clamp(b), clamp(a)));
            this.g.drawString(text, (int) x, (int) y);
        }
    }

    public int textWidth(String text) {
        return textWidth(text, BODY_FONT);
    }

    public int textWidth(String text, Font font) {
        if (g == null) return 0;
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        return fm.stringWidth(text);
    }

    public int textHeight() { return textHeight(BODY_FONT); }
    public int textHeight(Font font) {
        if (g == null) return 20;
        g.setFont(font);
        return g.getFontMetrics().getHeight();
    }

    public boolean drawButton(String text, float x, float y, float w, float h) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        boolean clicked = hover && mouseClicked;

        if (hover) drawRect(x, y, w, h, 0.3f, 0.5f, 0.8f, 1.0f);
        else drawRect(x, y, w, h, 0.15f, 0.15f, 0.15f, 1.0f);
        drawRect(x, y, w, h, 0.5f, 0.5f, 0.5f, 0.3f);

        drawTextCentered(text, x + w / 2, y + h / 2, 1, 1, 1, 1);

        if (clicked) {
            mouseClicked = false;
            return true;
        }
        return false;
    }

    public void drawTextCentered(String text, float cx, float cy, float r, float g, float b, float a) {
        drawTextCentered(text, cx, cy, r, g, b, a, BODY_FONT);
    }

    public void drawTextCentered(String text, float cx, float cy, float r, float g, float b, float a, Font font) {
        if (this.g != null) {
            this.g.setFont(font);
            FontMetrics fm = this.g.getFontMetrics();
            int tx = (int) cx - fm.stringWidth(text) / 2;
            int ty = (int) cy + fm.getAscent() / 2;
            this.g.setColor(new Color(clamp(r), clamp(g), clamp(b), clamp(a)));
            this.g.drawString(text, tx, ty);
        }
    }

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public boolean isMousePressed() { return mousePressed; }
    public boolean isMouseClicked() { return mouseClicked; }
    public void consumeClick() { mouseClicked = false; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getLastKey() { int k = lastKey; lastKey = 0; return k; }

    public Font getTitleFont() { return TITLE_FONT; }
    public Font getHeaderFont() { return HEADER_FONT; }
    public Font getBodyFont() { return BODY_FONT; }
    public Font getSmallFont() { return SMALL_FONT; }

    public void drawTexture(int textureId, float x, float y, float w, float h) {
        if (this.g != null) {
            BufferedImage img = textures.get(textureId);
            if (img != null) this.g.drawImage(img, (int) x, (int) y, (int) w, (int) h, null);
        }
    }

    public void drawTextureSub(int textureId, int srcX, int srcY, int srcW, int srcH, int dstX, int dstY, int dstW, int dstH) {
        if (this.g != null) {
            BufferedImage img = textures.get(textureId);
            if (img != null) {
                this.g.drawImage(img, dstX, dstY, dstX + dstW, dstY + dstH, srcX, srcY, srcX + srcW, srcY + srcH, null);
            }
        }
    }

    public void drawTextureFull(int textureId) {
        drawTexture(textureId, 0, 0, width, height);
    }

    public void drawTextureCover(int textureId) {
        BufferedImage img = textures.get(textureId);
        if (img == null || g == null) return;
        float imgAspect = (float) img.getWidth() / img.getHeight();
        float winAspect = (float) width / height;
        int drawW, drawH, drawX, drawY;
        if (imgAspect > winAspect) {
            drawH = height;
            drawW = (int) (height * imgAspect);
            drawX = (width - drawW) / 2;
            drawY = 0;
        } else {
            drawW = width;
            drawH = (int) (width / imgAspect);
            drawX = 0;
            drawY = (height - drawH) / 2;
        }
        g.drawImage(img, drawX, drawY, drawW, drawH, null);
    }

    public int loadTexture(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img != null) {
                int id = nextTexId++;
                textures.put(id, img);
                System.out.println("[Renderer] Loaded texture: " + path);
                return id;
            }
        } catch (IOException e) {
            System.err.println("[Renderer] Failed to load texture: " + path + " - " + e.getMessage());
        }
        return 0;
    }

    public int loadTexture(String path, int width, int height) {
        if (path.toLowerCase().endsWith(".svg")) {
            return loadSvg(path, width, height);
        }
        return loadTexture(path);
    }

    private int loadSvg(String path, int w, int h) {
        try (var fis = new java.io.FileInputStream(path)) {
            var input = new org.apache.batik.transcoder.TranscoderInput(fis);
            final BufferedImage[] result = new BufferedImage[1];
            var transcoder = new org.apache.batik.transcoder.image.ImageTranscoder() {
                public BufferedImage createImage(int width, int height) {
                    return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                }
                public void writeImage(BufferedImage img, org.apache.batik.transcoder.TranscoderOutput out) {
                    result[0] = img;
                }
            };
            transcoder.addTranscodingHint(
                org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH, (float) w);
            transcoder.addTranscodingHint(
                org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT, (float) h);
            transcoder.transcode(input, null);
            if (result[0] != null) {
                int id = nextTexId++;
                textures.put(id, result[0]);
                System.out.println("[Renderer] Loaded SVG: " + path + " (" + w + "x" + h + ")");
                return id;
            }
        } catch (Exception e) {
            System.err.println("[Renderer] Failed to load SVG: " + path + " - " + e.getMessage());
        }
        return 0;
    }

    private float clamp(float v) { return Math.max(0, Math.min(1, v)); }
}

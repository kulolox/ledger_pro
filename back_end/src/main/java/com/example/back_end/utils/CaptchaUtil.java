package com.example.back_end.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.imageio.ImageIO;

public final class CaptchaUtil {

    private CaptchaUtil() {}

    // 剔除容易混淆的字符：0, O, o, 1, I, l
    private static final char[] CHAR_POOL = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int WIDTH = 130;
    private static final int HEIGHT = 48;
    private static final int CODE_LENGTH = 4;

    public record CaptchaItem(String code, String imageBase64) {}

    public static CaptchaItem generateCaptcha() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 开启抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景底色：浅灰白
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制随机干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(getRandomColor(150, 210));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制噪点
        for (int i = 0; i < 40; i++) {
            g.setColor(getRandomColor(160, 220));
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }

        // 生成字符
        StringBuilder sb = new StringBuilder();
        int charWidth = (WIDTH - 20) / CODE_LENGTH;
        for (int i = 0; i < CODE_LENGTH; i++) {
            char c = CHAR_POOL[RANDOM.nextInt(CHAR_POOL.length)];
            sb.append(c);

            // 字符样式与旋转
            g.setColor(getRandomColor(20, 110));
            g.setFont(new Font("Arial", Font.BOLD, 28 + RANDOM.nextInt(6)));

            Graphics2D gChar = (Graphics2D) g.create();
            int x = 12 + i * charWidth + RANDOM.nextInt(6);
            int y = 32 + RANDOM.nextInt(6);

            // 轻微随机旋转 (-25° ~ +25°)
            double theta = (RANDOM.nextDouble() - 0.5) * 0.8;
            gChar.rotate(theta, x, y);
            gChar.drawString(String.valueOf(c), x, y);
            gChar.dispose();
        }

        g.dispose();

        // 转 Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }

        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        return new CaptchaItem(sb.toString(), base64);
    }

    private static Color getRandomColor(int min, int max) {
        int r = min + RANDOM.nextInt(max - min + 1);
        int g = min + RANDOM.nextInt(max - min + 1);
        int b = min + RANDOM.nextInt(max - min + 1);
        return new Color(r, g, b);
    }
}

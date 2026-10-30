package com.example.back_end.utils;

public class SanitizeUtils {

    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    // 防 CSV 公式注入：单元格以 = + - @ 开头时加 ' 前缀，防止 Excel 打开时执行恶意公式
    public static String sanitizeCsvCell(String value) {
        if (value == null || value.isEmpty()) return value;
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@') {
            return "'" + value;
        }
        return value;
    }
}

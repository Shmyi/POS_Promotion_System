package util;

/**
 * 提供文字對齊與寬度計算的公用工具類。
 * 適用於 POS 收據、表格列印、Console 對齊顯示。
 */
public class TextAlignUtil {

    /**
     * 右側補空白，確保中英文文字對齊。
     * @param text 原始文字
     * @param width 欄位寬度（字元數，中文字佔兩格）
     * @return 補齊後字串
     */
    public static String padRight(String text, int width) {
        if (text == null) text = "";
        int textLength = getDisplayWidth(text);
        int spaces = Math.max(width - textLength, 0);
        return text + " ".repeat(spaces);
    }

    /**
     * 將長文字縮短並加上省略符號 "..."
     * @param name 原文字
     * @param maxLength 最大長度
     * @return 縮短後字串
     */
    public static String shorten(String name, int maxLength) {
        if (name == null) return "";
        if (name.length() <= maxLength) return name;
        return name.substring(0, maxLength - 3) + "...";
    }

    /**
     * 右側對齊數字欄位（例如金額顯示）。
     * @param label 標籤文字
     * @param value 數值文字
     * @return 對齊後整行字串
     */
    public static String padRightAlign(String label, String value) {
        int labelWidth = getDisplayWidth(label);
        int totalWidth = 70; // 可依 console 寬度調整
        int spaces = Math.max(totalWidth - labelWidth - value.length(), 1);
        return label + " ".repeat(spaces) + value;
    }

    /**
     * 計算字串的實際寬度（中文字2格，英文1格）
     * @param text 文字內容
     * @return 實際寬度
     */
    public static int getDisplayWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += (c > 255) ? 2 : 1;
        }
        return width;
    }
}

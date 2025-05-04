package xj.enums.web;

/**
 * 对文字的编码形式
 */
public enum CharacterEncoding {

    UTF_8("UTF-8"), // UTF-8
    GBK("GBK"); // GBK

    private final String showStyle;

    CharacterEncoding(String showStyle) {
        this.showStyle = showStyle;
    }

    public String getShowStyle() {
        return showStyle;
    }
}

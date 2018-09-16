package org.lubei.bases.core.util;

@Deprecated
public class NamingUtils {

    /**
     * 下划线转骆驼命名
     *
     * @return String
     */
    public static String getCamelCaseString(final String inputString,
                                            final boolean firstCharacterUppercase) {
        StringBuilder sb = new StringBuilder();

        boolean nextUpperCase = false;
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);

            switch (c) {
                case '_':
                case '-':
                case '@':
                case '$':
                case '#':
                case ' ':
                case '/':
                case '&':
                    if (sb.length() > 0) {
                        nextUpperCase = true;
                    }
                    break;

                default:
                    if (nextUpperCase) {
                        sb.append(Character.toUpperCase(c));
                        nextUpperCase = false;
                    } else {
                        sb.append(Character.toLowerCase(c));
                    }
                    break;
            }
        }

        if (firstCharacterUppercase) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }

        return sb.toString();
    }

    /**
     * 骆驼命名 转下划线
     *
     * @return String
     */
    public static String camelCaseStringToColumnName(final String inputString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);
            if (isAsciiAlphaUpper(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 骆驼命名 转下划线
     *
     * @return String
     */
    public static String firstUppercase(final String inputString) {
        char c = inputString.charAt(0);
        if (isAsciiAlphaUpper(c)) {
            return inputString;
        } else {
            return Character.toUpperCase(c) + inputString.substring(1);
        }
    }

    public static boolean isAsciiAlphaUpper(final char ch) {
        return ch >= 'A' && ch <= 'Z';
    }
}

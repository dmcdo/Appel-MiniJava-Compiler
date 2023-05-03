package parser;

import java.util.HashMap;

public class TokenExtras {
    private static HashMap<Integer, String> names;

    public static String getPrintableImage(Token token) {
        if (token.kind == MiniParserConstants.EOF)
            return "(EOF)";

        if (token.image.length() == 0)
            return "(UNKNOWN)";

        if (Character.isISOControl(token.image.charAt(0))) {
            return "(" + Character.getName(token.image.charAt(0)) + ")";
        }

        return '"' + token.image + '"';
    }

    public static String getPrintableImage(int kind) {
        String image = MiniParserConstants.tokenImage[kind];

        if (image.startsWith("<") && image.endsWith(">"))
            return "(" + image.substring(1, image.length() - 1) + ")";

        if (Character.isISOControl(image.charAt(1)))
            return "(" + Character.getName(image.charAt(1)) + ")";

        return image;
    }

    public static String getName(Token token) {
        return getName(token.kind);
    }

    public static String getName(int kind) {
        if (names == null) {
            names = new HashMap<>();

            try {
                for (var field : Class.forName("parser.MiniParserConstants").getDeclaredFields()) {
                    if (field.getType() == int.class) {
                        names.put(field.getInt(null), field.getName());
                    }
                }
            } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException(ex.getClass().getCanonicalName());
            }
        }

        return kind == MiniParserConstants.EOF ? "EOF" : names.get(kind);
    }
}

package com.example.walletjournal.model;

/**
 * Fixed icon/color choices offered on the Add Category screen. A Category
 * stores an icon key (one of ICON_KEYS) and a colorIndex (into BASE_COLORS /
 * PASTEL_COLORS) rather than raw color values, so the palette stays centrally
 * controlled and consistent everywhere a category is rendered.
 */
public final class CategoryPalette {

    public static final int[] BASE_COLORS = {
            0xFFC98A2B, // brown / amber
            0xFFE2572B, // orange-red
            0xFF3B82D6, // blue
            0xFF1E7A5F, // green
            0xFF7B5EA7, // purple
            0xFFD6558C, // pink
            0xFF6E6A62, // gray
    };

    public static final int[] PASTEL_COLORS = {
            0xFFFBE8C6,
            0xFFFBDCD3,
            0xFFDCEAFB,
            0xFFD7F2E3,
            0xFFE8DFF5,
            0xFFFBDCE8,
            0xFFEDEAE1,
    };

    public static final String[] ICON_KEYS = {"bars", "bus", "transfer", "camera", "house", "card"};

    private CategoryPalette() {
    }

    public static int baseColor(int index) {
        return BASE_COLORS[clampIndex(index)];
    }

    public static int pastelColor(int index) {
        return PASTEL_COLORS[clampIndex(index)];
    }

    public static String emoji(String iconKey) {
        if (iconKey != null) {
            switch (iconKey) {
                case "bars":
                    return "‖";
                case "bus":
                    return "🚌";
                case "transfer":
                    return "⇄";
                case "camera":
                    return "📷";
                case "house":
                    return "🏠";
                case "card":
                    return "💳";
                default:
                    break;
            }
        }
        return "📌";
    }

    private static int clampIndex(int index) {
        return index < 0 || index >= BASE_COLORS.length ? 0 : index;
    }
}

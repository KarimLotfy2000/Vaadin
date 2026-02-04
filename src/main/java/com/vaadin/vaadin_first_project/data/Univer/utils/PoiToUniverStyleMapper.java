package com.vaadin.vaadin_first_project.data.Univer.utils;

import com.vaadin.vaadin_first_project.data.Univer.dto.styles.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public final class PoiToUniverStyleMapper {

    private PoiToUniverStyleMapper() {}

    public static UniverStyleData mapStyle(Workbook wb, CellStyle cs) {
        if (wb == null || cs == null) return null;

        // Font
        Font f = wb.getFontAt(cs.getFontIndex());
        String ff = nullIfBlank(f.getFontName());
        Integer fs = (int) f.getFontHeightInPoints();
        Integer bl = f.getBold() ? 1 : 0;
        Integer it = f.getItalic() ? 1 : 0;

        // Font color (XSSF only)
        UniverRgbColor cl = null;
        if (f instanceof XSSFFont xf) cl = colorFromXssf(xf.getXSSFColor());

        // Fill (XSSF only).
        UniverRgbColor bg = null;
        if (cs instanceof XSSFCellStyle xcs) {
            if (xcs.getFillPattern() != FillPatternType.NO_FILL) {
                bg = colorFromXssf(xcs.getFillForegroundXSSFColor());
            }
        }

        // Alignment + wrap
        Integer ht = mapHorizontal(cs.getAlignment());
        Integer vt = mapVertical(cs.getVerticalAlignment());
        Integer tb = cs.getWrapText() ? 3 : null; //

        UniverNumFmt n = null;
        String fmt = cs.getDataFormatString();
        if (fmt != null && !fmt.isBlank() && !"General".equalsIgnoreCase(fmt)) {
            n = new UniverNumFmt(fmt);
        }

        // Borders
        UniverBorder bd = mapBorders(cs);


        return new UniverStyleData(ff, fs, it, bl, bg, bd, cl, ht, vt, tb, n);
    }

    private static UniverBorder mapBorders(CellStyle cs) {
        if (cs == null) return null;

        UniverBorderSide t = mapBorderSide(cs, Side.TOP);
        UniverBorderSide b = mapBorderSide(cs, Side.BOTTOM);
        UniverBorderSide l = mapBorderSide(cs, Side.LEFT);
        UniverBorderSide r = mapBorderSide(cs, Side.RIGHT);

        if (t == null && b == null && l == null && r == null) return null;
        return new UniverBorder(t, b, l, r);
    }

    private enum Side { TOP, BOTTOM, LEFT, RIGHT }

    private static UniverBorderSide mapBorderSide(CellStyle cs, Side side) {
        BorderStyle bs = switch (side) {
            case TOP -> cs.getBorderTop();
            case BOTTOM -> cs.getBorderBottom();
            case LEFT -> cs.getBorderLeft();
            case RIGHT -> cs.getBorderRight();
        };

        if (bs == null || bs == BorderStyle.NONE) return null;

        Integer s = mapBorderStyle(bs);
        UniverRgbColor cl = getBorderColor(cs, side);

        return new UniverBorderSide(s, cl);
    }

    private static Integer mapBorderStyle(BorderStyle bs) {
        return switch (bs) {
            case THIN -> 1;
            case MEDIUM -> 2;
            case THICK -> 3;
            case DASHED, MEDIUM_DASHED -> 4;
            case DOTTED -> 5;
            case DOUBLE -> 6;
            default -> 1;
        };
    }

    private static UniverRgbColor getBorderColor(CellStyle cs, Side side) {
        if (!(cs instanceof XSSFCellStyle xcs)) return null;

        XSSFColor c = switch (side) {
            case TOP -> xcs.getTopBorderXSSFColor();
            case BOTTOM -> xcs.getBottomBorderXSSFColor();
            case LEFT -> xcs.getLeftBorderXSSFColor();
            case RIGHT -> xcs.getRightBorderXSSFColor();
        };
        return colorFromXssf(c);
    }

    public static Integer mapHorizontal(HorizontalAlignment a) {
        if (a == null) return null;
        return switch (a) {
            case LEFT, FILL, JUSTIFY, DISTRIBUTED -> 1;
            case CENTER, CENTER_SELECTION -> 2;
            case RIGHT -> 3;
            case GENERAL -> null;
        };
    }

    public static Integer mapVertical(VerticalAlignment a) {
        if (a == null) return null;
        return switch (a) {
            case TOP -> 1;
            case CENTER, JUSTIFY, DISTRIBUTED -> 2;
            case BOTTOM -> 3;
        };
    }

    public static UniverRgbColor colorFromXssf(XSSFColor c) {
        if (c == null) return null;
        byte[] rgb = c.getRGB();
        if (rgb == null) return null;

        int offset = (rgb.length == 4) ? 1 : 0; // handle ARGB
        if (rgb.length != 3 && rgb.length != 4) return null;

        int r = rgb[offset] & 0xFF;
        int g = rgb[offset + 1] & 0xFF;
        int b = rgb[offset + 2] & 0xFF;

        return new UniverRgbColor(String.format("#%02X%02X%02X", r, g, b));
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
    public static boolean hasRenderableStyle(UniverStyleData s) {
        if (s == null) return false;
        return s.bg() != null
                || s.bd() != null
                || s.cl() != null
                || s.ff() != null
                || s.fs() != null
                || s.bl() != null
                || s.it() != null
                || s.ht() != null
                || s.vt() != null
                || s.tb() != null
                || s.n() != null;
    }
}

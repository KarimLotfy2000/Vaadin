package com.vaadin.vaadin_first_project.data.Univer.utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverCell;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverWorkbookData;
import com.vaadin.vaadin_first_project.data.Univer.dto.styles.UniverBorderSide;
import com.vaadin.vaadin_first_project.data.Univer.dto.styles.UniverStyleData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.util.HashMap;
import java.util.Map;

public final class UniverToPoiStyleMapper {

    private UniverToPoiStyleMapper() {}

    private static final ObjectMapper OM = new ObjectMapper();

    /**
     * The Context exists because POI styles and fonts are workbook-scoped and limited.
     * Creating a new CellStyle or Font per cell will bloat or corrupt the Excel file.
     *
     * The Context caches styles and fonts so identical Univer styles are created once
     * and reused across all cells. This keeps the workbook valid, small, and fast.
     *
     */
    public static final class Context {
        private final Workbook wb;
        private final Map<String, CellStyle> styleCache = new HashMap<>(); // UniverStyle -> CellStyle
        private final Map<String, Font> fontCache = new HashMap<>(); // UniverFont -> Font

        public Context(Workbook wb) { this.wb = wb; }
    }


    public static void applyCellStyle(
            Context ctx,
            Cell poiCell,
            UniverWorkbookData snapshot,
            UniverCell univerCell
     ) {
        if (ctx == null || poiCell == null || snapshot == null || univerCell == null) return;

        Object s = univerCell.s();
        if (s == null) return;

        UniverStyleData resolved;
        if (s instanceof String styleId) {
            Map<String, UniverStyleData> styles = snapshot.styles();
            if (styles == null) return ;
            Object styleData = styles.get(styleId);
            resolved = OM.convertValue(styleData, UniverStyleData.class);
        } else {
             resolved = OM.convertValue(s, UniverStyleData.class);
        }

        if (resolved == null) return;

        CellStyle cs = getOrCreateCellStyle(ctx, resolved);
        if (cs != null) poiCell.setCellStyle(cs);
    }

    private static CellStyle getOrCreateCellStyle(Context ctx, UniverStyleData u) {
        try {
            // check cache to avoid duplicates
            String key = OM.writeValueAsString(u);
            CellStyle cached = ctx.styleCache.get(key);
            if (cached != null) return cached;

            CellStyle cs = ctx.wb.createCellStyle();

            // font
            Font font = getOrCreateFont(ctx, u);
            if (font != null) cs.setFont(font);

            // alignment
            if (u.ht() != null) cs.setAlignment(mapHt(u.ht()));
            if (u.vt() != null) cs.setVerticalAlignment(mapVt(u.vt()));

            // wrap
            if (u.tb() != null && u.tb() == 3) cs.setWrapText(true);


            // border styles
            applyBorderStylesOnly(cs, u);

            // colors and fill require XSSF for correct RGB
            if (ctx.wb instanceof XSSFWorkbook && cs instanceof XSSFCellStyle xcs) {
                applyFillAndBorderColors(xcs, u);
            }

            ctx.styleCache.put(key, cs);
            return cs;

        } catch (Exception e) {
            return null;
        }
    }

    private static Font getOrCreateFont(Context ctx, UniverStyleData u) {
        try {
            // only font-related identity for caching
            UniverStyleData fontOnly = new UniverStyleData(
                    u.ff(), u.fs(), u.it(), u.bl(),
                    null, null, u.cl(),
                    null, null, null, null
            );
            String key = OM.writeValueAsString(fontOnly);

            Font cached = ctx.fontCache.get(key);
            if (cached != null) return cached;

            Font f = ctx.wb.createFont();
            if (u.ff() != null) f.setFontName(u.ff());
            if (u.fs() != null) f.setFontHeightInPoints(u.fs().shortValue());
            if (u.bl() != null) f.setBold(u.bl() == 1);
            if (u.it() != null) f.setItalic(u.it() == 1);

            if (f instanceof XSSFFont xf && u.cl() != null && u.cl().rgb() != null) {
                XSSFColor c = toXssfColor(u.cl().rgb());
                if (c != null) xf.setColor(c);
            }

            ctx.fontCache.put(key, f);
            return f;

        } catch (Exception e) {
            return null;
        }
    }

    private static void applyFillAndBorderColors(XSSFCellStyle cs, UniverStyleData u) {
        // fill
        if (u.bg() != null && u.bg().rgb() != null) {
            XSSFColor fill = toXssfColor(u.bg().rgb());
            if (fill != null) {
                cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cs.setFillForegroundColor(fill);
            }
        }

        // border colors
        if (u.bd() == null) return;

        applyOneBorderColor(cs, Side.TOP, u.bd().t());
        applyOneBorderColor(cs, Side.BOTTOM, u.bd().b());
        applyOneBorderColor(cs, Side.LEFT, u.bd().l());
        applyOneBorderColor(cs, Side.RIGHT, u.bd().r());
    }

    private enum Side { TOP, BOTTOM, LEFT, RIGHT }

    private static void applyOneBorderColor(XSSFCellStyle cs, Side side, UniverBorderSide uSide) {
        if (uSide == null || uSide.cl() == null || uSide.cl().rgb() == null) return;
        XSSFColor c = toXssfColor(uSide.cl().rgb());
        if (c == null) return;

        switch (side) {
            case TOP -> cs.setTopBorderColor(c);
            case BOTTOM -> cs.setBottomBorderColor(c);
            case LEFT -> cs.setLeftBorderColor(c);
            case RIGHT -> cs.setRightBorderColor(c);
        }
    }

    private static void applyBorderStylesOnly(CellStyle cs, UniverStyleData u) {
        if (u.bd() == null) return;

        if (u.bd().t() != null) cs.setBorderTop(mapBorderStyle(u.bd().t().s()));
        if (u.bd().b() != null) cs.setBorderBottom(mapBorderStyle(u.bd().b().s()));
        if (u.bd().l() != null) cs.setBorderLeft(mapBorderStyle(u.bd().l().s()));
        if (u.bd().r() != null) cs.setBorderRight(mapBorderStyle(u.bd().r().s()));
    }

    private static HorizontalAlignment mapHt(int ht) {
        return switch (ht) {
            case 1 -> HorizontalAlignment.LEFT;
            case 2 -> HorizontalAlignment.CENTER;
            case 3 -> HorizontalAlignment.RIGHT;
            default -> HorizontalAlignment.GENERAL;
        };
    }

    private static VerticalAlignment mapVt(int vt) {
        return switch (vt) {
            case 1 -> VerticalAlignment.TOP;
            case 2 -> VerticalAlignment.CENTER;
            case 3 -> VerticalAlignment.BOTTOM;
            default -> VerticalAlignment.BOTTOM;
        };
    }

    private static BorderStyle mapBorderStyle(Integer s) {
        if (s == null) return BorderStyle.NONE;
        return switch (s) {
            case 2 -> BorderStyle.MEDIUM;
            case 3 -> BorderStyle.THICK;
            case 4 -> BorderStyle.DASHED;
            case 5 -> BorderStyle.DOTTED;
            case 6 -> BorderStyle.DOUBLE;
            default -> BorderStyle.THIN;
        };
    }

    private static XSSFColor toXssfColor(String rgb) {
        if (rgb == null) return null;
        String hex = rgb.startsWith("#") ? rgb.substring(1) : rgb;
        if (hex.length() != 6) return null;

        byte[] bytes = new byte[3];
        for (int i = 0; i < 3; i++) {
            int v = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
            bytes[i] = (byte) v;
        }
        return new XSSFColor(bytes, null);
    }
}


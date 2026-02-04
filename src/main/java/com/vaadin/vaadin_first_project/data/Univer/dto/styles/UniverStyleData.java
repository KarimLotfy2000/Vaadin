package com.vaadin.vaadin_first_project.data.Univer.dto.styles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UniverStyleData(
        String ff, // font family
        Integer fs, // font size
        Integer it, // italic
        Integer bl, // bold
        UniverRgbColor bg, // background color
        UniverBorder bd, // border
        UniverRgbColor cl, // color
        Integer ht, // horizontal text align
        Integer vt, // vertical text align
        Integer tb, // text wrap
        UniverNumFmt n // number format
) {}



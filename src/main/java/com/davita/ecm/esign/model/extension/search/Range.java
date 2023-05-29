package com.davita.ecm.esign.model.extension.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Range{
    private String gt;
    private String lt;
    private String max;
    private String min;
}
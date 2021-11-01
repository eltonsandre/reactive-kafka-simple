package com.github.eltonsandre.simple.reactivekafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Any {

    private String id;
    private String name;
    private String status;
    private boolean enabled;

}

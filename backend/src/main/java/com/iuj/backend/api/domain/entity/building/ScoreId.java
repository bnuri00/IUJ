package com.iuj.backend.api.domain.entity.building;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ScoreId implements Serializable {
    private Long id;
    private String type;
}

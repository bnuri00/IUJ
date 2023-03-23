package com.iuj.backend.api.domain.entity.infra;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class BookStore extends BaseInfra{
    @Column(length=45)
    private String name;

    @Column(length=45)
    private String type;
}

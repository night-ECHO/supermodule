package com.adflex.tracking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.adflex.tracking.enums.PackageType;

import java.math.BigDecimal;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
public class Package {

    @Id
    private String code;

    private String name;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private PackageType type;

    @Column(name = "min_package_level")
    private Integer minPackageLevel;  // 1: cả 2 gói, 2: chỉ gói cao cấp
}
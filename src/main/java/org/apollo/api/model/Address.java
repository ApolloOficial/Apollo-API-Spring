package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "address")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "street_name", nullable = false, length = 150)
    private String streetName;

    @Column(name = "number", nullable = false, length = 20)
    private String number;

    @Column(name = "additional_info", length = 100)
    private String additionalInfo;

    @Column(name = "neighborhood", nullable = false, length = 100)
    private String neighborhood;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    // BUG FIX (schema/model mismatch): the database column is CHAR(2) (fixed-length),
    // not VARCHAR(2). With ddl-auto=validate this mismatch would fail schema validation
    // at boot with a "wrong column type" error.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "state", nullable = false, length = 2)
    private String state;

    // zip_code tambem e CHAR(8) no banco (bpchar), nao VARCHAR.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "zip_code", nullable = false, length = 8)
    private String zipCode;
}
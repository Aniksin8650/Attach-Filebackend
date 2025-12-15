package com.example.attachfile.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ROLESNEW")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolesNew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // column name = persno (matches DB exactly, so no @Column needed)
    private String persno;

    // column name = role_name
    private String roleName;

    // column name = role_no
    private Integer roleNo;

    // column name = role_desc
    private String roleDesc;

    // column name = dte
    private String dte;

    // column name = div
    private String div;
}

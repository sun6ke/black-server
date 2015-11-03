package com.example.models;

import javax.persistence.*;
import java.sql.Blob;
import java.util.List;

@Entity
@Table(name = "tCollege")
public class College {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "int")
    private Integer id;
    @Column(columnDefinition = "varchar(30)")
    private String name;
    @Column(columnDefinition = "varchar(30)")
    private String nameExt;

    @Lob
    @Column(columnDefinition = "mediumblob")
    private Blob logo;
    @Column(columnDefinition = "varchar(200)")
    private String location;

    @OneToMany(mappedBy = "college")
    private List<Academy> academies;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameExt() {
        return nameExt;
    }

    public void setNameExt(String nameExt) {
        this.nameExt = nameExt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Blob getLogo() {
        return logo;
    }

    public void setLogo(Blob logo) {
        this.logo = logo;
    }
}

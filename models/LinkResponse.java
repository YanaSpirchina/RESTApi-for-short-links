package test.spring.restapi.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "link")
@Data
public class LinkResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "shortname")
    private String shortName;

    @Column(name = "hash")
    private String hash;

    @Column(name = "time_of_creating")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeOfCreating;
}

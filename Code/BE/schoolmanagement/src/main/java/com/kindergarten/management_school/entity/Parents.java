package com.kindergarten.management_school.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "parents")
@PrimaryKeyJoinColumn(name = "account_id")
public class Parents extends Account {

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "relationship", length = 50)
    private String relationship;

    @Column(name = "emergency_contact", length = 50)
    private String emergencyContact;

    @Column(name = "additional_phone", length = 15)
    private String additionalPhone;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Students> children;
}

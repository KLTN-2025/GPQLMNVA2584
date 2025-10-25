package com.kindergarten.management_school.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "teachers")
@PrimaryKeyJoinColumn(name = "account_id")
public class Teacher extends Account {

    @Column(name = "employee_code", nullable = false, unique = true, length = 20)
    private String employeeCode;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "join_date")
    private LocalDateTime joinDate;

    @Column(name = "emergency_contact", length = 50)
    private String emergencyContact;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<Classes> classes;

}

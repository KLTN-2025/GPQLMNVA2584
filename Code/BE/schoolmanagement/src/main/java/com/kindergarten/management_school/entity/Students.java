package com.kindergarten.management_school.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "account_id")
public class Students extends Account {

    @Column(name = "student_code", nullable = false, unique = true, length = 20)
    private String studentCode;

    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;

    @Column(name = "health_notes", length = 255)
    private String healthNotes;

    @Column(name = "address", length = 255)
    private String address;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private Classes clazz;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parents parent;
}

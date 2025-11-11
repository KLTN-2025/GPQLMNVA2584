package com.kindergarten.management_school.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "menu_items")
public class MenuItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menus menu;

    @Column(name = "day_of_week", length = 20, nullable = false)
    private String dayOfWeek;

    @Column(name = "meal_type", length = 30, nullable = false)
    private String mealType;

    @OneToMany(mappedBy = "menuItems", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuDish> dishes;
}

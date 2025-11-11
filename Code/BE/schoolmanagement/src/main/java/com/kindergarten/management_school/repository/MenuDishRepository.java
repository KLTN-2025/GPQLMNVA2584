package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.MenuDish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource
public interface MenuDishRepository extends JpaRepository<MenuDish, Long> {

}

package com.project.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.app.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    

}

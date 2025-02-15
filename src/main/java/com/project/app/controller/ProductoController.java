package com.project.app.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.app.model.Producto;
import com.project.app.model.dto.ProductoDto;
import com.project.app.repository.ProductoRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository repository;

    @GetMapping("")
    public String all(Model model) {
        List<Producto> productos = repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("productos", productos);
        return "productos/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        ProductoDto productoDto = new ProductoDto();
        model.addAttribute("productoDto", productoDto);
        return "productos/create";
    }

    @PostMapping("/create")
    public String store(@Valid @ModelAttribute ProductoDto dto, BindingResult result) {

        if (dto.getImageFileName().isEmpty()) {
            result.addError(new FieldError("dto", "imageFileName", "foto requerida!"));
        }

        if (result.hasErrors()) {

            return "productos/create";

        }

        // subir y guardar archivo en Spring boot
        MultipartFile file = dto.getImageFileName();
        Date fecha = new Date();
        String storageFileName = fecha.getTime() + "_" + file.getOriginalFilename();

        try {
            String uploadDir = "public/uploads/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception ex) {
            // TODO: handle exception
            System.out.println("Exception: " + ex.getMessage());
        }

        Producto p = new Producto();
        p.setNombre(dto.getNombre());
        p.setPrecio(dto.getPrecio());
        p.setImageFileName(storageFileName);
        p.setDescripcion(dto.getDescripcion());
        p.setCreatedAd(fecha);

        repository.save(p);

        return "redirect:/productos";
    }

    @GetMapping("/edit")
    public String edit(Model model, @RequestParam int id) {

        Producto producto = repository.findById(id).get();
        model.addAttribute("producto", producto);

        ProductoDto dto = new ProductoDto();
        dto.setNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setDescripcion(producto.getDescripcion());
        //dto.setImageFileName(producto.getImageFileName());
        model.addAttribute("dto", dto);


        return "productos/edit";
    }

    @PostMapping("/edit")
    public String update(@RequestParam int id, Model model, @Valid @ModelAttribute ProductoDto dto) {

        // if (dto.getImageFileName().isEmpty()) {
        //     return "productos/edit/2"; 
        // }

        try {
            Producto producto = repository.findById(id).get();
            model.addAttribute("producto", producto);

            if (!dto.getImageFileName().isEmpty()) {
                // borramos imagen anterior
                String uploadDir = "public/uploads/";
                Path imagenAnterior = Paths.get(uploadDir + producto.getImageFileName());

                try {
                    // borro imagen anterior
                    Files.delete(imagenAnterior);

                    // agrego la nueva imagen
                    MultipartFile file = dto.getImageFileName();
                    Date fecha = new Date();
                    String storageFileName = fecha.getTime() + "_" + file.getOriginalFilename();

                    try( InputStream inputStream = file.getInputStream() ) {
                        Files.copy(inputStream, Paths.get(uploadDir, storageFileName), StandardCopyOption.REPLACE_EXISTING);
                    }
                    
                    producto.setImageFileName(storageFileName);

                } catch (Exception e) {
                    System.out.println("Exception:" + e.getMessage());
                }
            }

                    producto.setNombre(dto.getNombre());
                    producto.setPrecio(dto.getPrecio());
                    producto.setDescripcion(dto.getDescripcion());
                    repository.save(producto);

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Excepetion:" + e.getMessage());
        }    
        
        return "redirect:/productos";

    }



    @GetMapping("/delete")
    public String destroy(@RequestParam int id) {

        try {
               Producto producto = repository.findById(id).get();

               Path pathFile = Paths.get("public/uploads/" + producto.getImageFileName());
            
               try {
                  // borro la imagen 
                  Files.delete(pathFile);
               } catch (Exception e) {
                  System.out.println("Exception:" + e.getMessage());
               }

               repository.delete(producto);

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Exception:" + e.getMessage());
        }

        return "redirect:/productos";
    }


}

package com.escom.tt2016.piccom;

/**
 * Created by ajmarmar on 7/10/13.
 */
public class Pictograma {
    private String nombre;
    private int id;
    private String imagen;
    private int categoria;
    private Integer recurso;

    public Pictograma(String nombre, int id, String imagen, int categoria, Integer recurso) {
        this.nombre = nombre;
        this.id = id;
        this.imagen = imagen;
        this.categoria = categoria;
        this.recurso = recurso;
    }

    public Integer getRecurso() {
        return recurso;
    }

    public void setRecurso(Integer recurso) {
        this.recurso = recurso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }
}
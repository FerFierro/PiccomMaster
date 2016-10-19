package com.escom.tt2016.piccom;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by ajmarmar on 6/10/13.
 */
public class GestionBD extends SQLiteOpenHelper {

    private Context ctx;

    public GestionBD(Context ctx) {
        super(ctx,"PicComDB",null,1);
        this.ctx = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Pictogramas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, idCategoria INTEGER, imagen TEXT)");
        try {
            db.execSQL("INSERT INTO Pictogramas (nombre, imagen, idCategoria) VALUES ('cat_people','cat_personas.png',0)");
            db.execSQL("INSERT INTO Pictogramas (nombre, imagen, idCategoria) VALUES ('cat_objects','cat_objetos.png',0)");
            db.execSQL("INSERT INTO Pictogramas (nombre, imagen, idCategoria) VALUES ('cat_verbs','cat_verbos.png',0)");
            db.execSQL("INSERT INTO Pictogramas (nombre, imagen, idCategoria) VALUES ('cat_places','cat_lugares.png',0)");
            db.execSQL("INSERT INTO Pictogramas (nombre, imagen, idCategoria) VALUES ('cat_moods','cat_estadosanimo.png',0)");
            db.execSQL("INSERT INTO Pictogramas (nombre, imagen, idCategoria) VALUES ('cat_social','cat_social.png',0)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public void addPictograma(int categoria, String nombre, String imagen){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Pictogramas (idCategoria,nombre,imagen) VALUES ("+categoria+",'"+nombre.trim()+"','"+imagen.trim()+"')");
        //db.close();
    }

    public ArrayList<Pictograma> getAllPictogramas(){
        ArrayList<Pictograma> array = new ArrayList<Pictograma>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor lista = db.rawQuery("SELECT id, nombre, imagen, idCategoria FROM Pictogramas WHERE idCategoria != 0 ORDER BY idCategoria", null);

        while (lista.moveToNext()){
            array.add(new Pictograma(lista.getString(1),lista.getInt(0),lista.getString(2),lista.getInt(3),null));
        }
        lista.close();

        return array;
    }

    public void delPictograma(int idPictograma){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM Pictogramas WHERE id = "+idPictograma);
        //db.close();
    }

    public Vector<Pictograma> getLista(int cat){
        Vector<Pictograma> pic = new Vector<Pictograma>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor lista = db.rawQuery("SELECT id, nombre, imagen FROM Pictogramas WHERE idCategoria = " + cat, null);

        while (lista.moveToNext()){
            pic.add(new Pictograma(lista.getString(1),lista.getInt(0),lista.getString(2),cat,null));
        }
        lista.close();
       // db.close();

        return pic;
    }

    public int contarPictograma(String imagen) {
        int c=0;
        SQLiteDatabase db = getReadableDatabase();

        Cursor lista = db.rawQuery("SELECT count(id) AS total FROM Pictogramas WHERE imagen = '" + imagen+"'", null);
        if (lista.moveToNext()){
            c = lista.getInt(0);
        }
        return c;
    }

    public Pictograma getPictograam(String texto){
        Pictograma pic = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor lista = db.rawQuery("SELECT id, nombre, imagen, idCategoria FROM Pictogramas WHERE nombre = '" + texto+"'", null);
        if (lista.moveToNext()){
            pic = new Pictograma(lista.getString(1),lista.getInt(0),lista.getString(2),lista.getInt(3),null);
        }
        return pic;
    }
}
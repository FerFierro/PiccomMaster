package com.escom.tt2016.piccom;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class AddPictogramExternal extends Activity {

    private Uri currImageURI;
    private int categoria;
    private EditText texto;
    private Button btnGuardar;
    private Button btnCancelar;
    private ImageView imagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addpictogramexternal);

        this.btnCancelar = (Button)findViewById(R.id.btnCancelar);
        this.btnGuardar = (Button)findViewById(R.id.btnGuardar);

        this.imagen = (ImageView)findViewById(R.id.imgPictograma);
        this.imagen.setImageURI(getIntent().getClipData().getItemAt(0).getUri());
        this.currImageURI = getIntent().getClipData().getItemAt(0).getUri();

        this.texto =(EditText)findViewById(R.id.textoPictograma);

        Spinner spCategoria = (Spinner)findViewById(R.id.spCategoria);
        this.categoria = 1;
        spCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setCategoria(position);
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });

        this.btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salir();
            }
        });

        this.btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarPictograma();
            }
        });
    }

    private void setCategoria(int id){
        this.categoria = id+1;
    }

    private void salir() {
        finish();
    }

    private void guardarPictograma()  {
        //http://stackoverflow.com/questions/10413659/how-to-resize-image-in-android
        File nuevaImagen;
        FileOutputStream ostream = null;
        Date d = new Date();
        File f = new File(getPath(currImageURI));
        File dir = this.getDir("imgPictogramas", Context.MODE_PRIVATE);
        GestionBD db;
        int dimension;

        dimension = getResources().getInteger(R.integer.dim_pictogramas);

        //Reescalamos
        /*
        Bitmap img = BitmapFactory.decodeFile(f.getAbsolutePath());
        Bitmap imgEscalada = Bitmap.createScaledBitmap(img, dimension, dimension, true);
        */
        Bitmap imgEscalada = Utilidades.rescalarImagen(dimension,f);

        //Creamos el fichero.
        nuevaImagen = new File(dir.getAbsolutePath()+"/"+d.getTime()+".png");
        try {
            Log.d("IMPORTPICTOGRAMA","Se va a crear la imagen en : "+nuevaImagen.getAbsoluteFile());
            nuevaImagen.createNewFile();
            ostream = new FileOutputStream(nuevaImagen);
            imgEscalada.compress(Bitmap.CompressFormat.PNG, 100, ostream);

            db = new GestionBD(this);
            db.addPictograma(this.categoria,this.texto.getText().toString(),nuevaImagen.getName());
            Log.d("IMPORTPICTOGRAMA","Se ha creado un nuevo registro");

        } catch (IOException e) {
            Log.e("IMPORTPICTOGRAMA","Error al intentar crear la imagen. "+e.getMessage());
            e.printStackTrace();
        } finally {
            if (ostream != null){
                try {
                    ostream.close();
                } catch (IOException e) {
                    Log.e("IMPORTPICTOGRAMA", "Error al intentar cerrar el fichero de  la imagen. " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        finish();
    }


    public String getPath(Uri uri) {
        String ruta=null;

        if (uri.getScheme().equals("content")){
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = managedQuery(uri, projection, null, null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            ruta=  cursor.getString(column_index);
            cursor.close();
        } else {
            ruta =uri.getEncodedPath();
        }
        return ruta;
    }

}

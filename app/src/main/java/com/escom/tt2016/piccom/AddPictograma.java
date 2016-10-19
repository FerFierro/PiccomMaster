package com.escom.tt2016.piccom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class AddPictograma extends Activity {

    private Button btnGuardar;
    private Button btnCancelar;
    private Button btnSeleccionar;
    private EditText texto;
    private ImageView pictograma;
    private Uri currImageURI;
    private int categoria;
    private boolean imagenSeleccionada;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addpictograma);

        this.imagenSeleccionada=false;

        this.categoria = getIntent().getExtras().getInt("categoria");

        this.pictograma = (ImageView)findViewById(R.id.imgPictograma);

        this.texto =(EditText)findViewById(R.id.textoPictograma);
        this.btnCancelar = (Button)findViewById(R.id.btnCancelar);
        this.btnGuardar = (Button)findViewById(R.id.btnGuardar);
        this.btnSeleccionar = (Button)findViewById(R.id.btnSeleccionarImagen);

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
        this.btnSeleccionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),1);
                */
               lanzarOpciones();
            }
        });
    }

    private void lanzarOpciones(){
        Intent i = new Intent(this, CamarayGaleria.class);
        startActivityForResult(i,1);
    }

    private void guardarPictograma()  {
        //http://stackoverflow.com/questions/10413659/how-to-resize-image-in-android
        File nuevaImagen;
        FileOutputStream ostream = null;
        Date d ;
        File f;
        File dir;
        GestionBD db;
        int dimension;

        if (this.imagenSeleccionada && !this.texto.getText().toString().equals("")) {
            d = new Date();
            f = new File(getPath(currImageURI));
            dir = this.getDir("imgPictogramas", Context.MODE_PRIVATE);

            dimension = getResources().getInteger(R.integer.dim_pictogramas);

            //Reescalamos
            /*
            Bitmap img = BitmapFactory.decodeFile(f.getAbsolutePath());
            Bitmap imgEscalada = Bitmap.createScaledBitmap(img, dimension, dimension, true);
            */
            Bitmap imgEscalada = Utilidades.rescalarImagen(dimension,f);

            // FileCopy(getPath(currImageURI),dir.getAbsolutePath()+"/"+d.getTime()+"."+extension);

            //Creamos el fichero.
            nuevaImagen = new File(dir.getAbsolutePath()+"/"+d.getTime()+".png");
            try {
                Log.d("ADDPICTOGRAMA","Se va a crear la imagen en : "+nuevaImagen.getAbsoluteFile());
                nuevaImagen.createNewFile();
                ostream = new FileOutputStream(nuevaImagen);
                imgEscalada.compress(Bitmap.CompressFormat.PNG, 100, ostream);

                db = new GestionBD(this);
                db.addPictograma(this.categoria,this.texto.getText().toString(),nuevaImagen.getName());
                Log.d("ADDPICTOGRAMA","Se ha creado un nuevo registro");

            } catch (IOException e) {
                Log.e("ADDPICTOGRAMA","Error al intentar crear la imagen. "+e.getMessage());
                e.printStackTrace();
            } finally {
                if (ostream != null){
                    try {
                        ostream.close();
                    } catch (IOException e) {
                        Log.e("ADDPICTOGRAMA","Error al intentar cerrar el fichero de  la imagen. "+e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this,getResources().getString(R.string.error_req_addpictograma),Toast.LENGTH_LONG).show();
        }
    }

    private void salir() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                // currImageURI is the global variable I'm using to hold the content:// URI of the image
                this.currImageURI = data.getData();
                this.pictograma.setImageURI(currImageURI);
                this.imagenSeleccionada=true;
            }
        }
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

    /*
    private void FileCopy(String source, String target){
        try {
            if ((source == null) || (target == null)) {
                return;

            }
            File fileSource = new File(source);
            if (!(fileSource.exists())) {
                return;
            }
            File fileTarget = new File(target);
            fileTarget.createNewFile();
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException iOException) {
           Toast.makeText(this,"Se ha producido un error al copiar fichero",Toast.LENGTH_LONG).show();
        }
    }
    */
}

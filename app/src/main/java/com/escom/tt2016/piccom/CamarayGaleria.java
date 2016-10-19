package com.escom.tt2016.piccom;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class CamarayGaleria extends Activity {
    private final int IMAGE_PICK     = 1;
    private final int IMAGE_CAPTURE  = 2;
    private final int PIC_CROP = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camaraygaleria);

        ImageButton btnCamara = (ImageButton) findViewById(R.id.btnCamara);
        ImageButton btnGaleria = (ImageButton) findViewById(R.id.btnGaleria);

        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mostrarCamara();
            }
        });

        btnGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarGaleria();
            }
        });
    }

    private void mostrarGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), IMAGE_PICK);
    }

    private void mostrarCamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, IMAGE_CAPTURE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri currImageURI=null;

        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case IMAGE_PICK:
                    currImageURI = data.getData();
                    break;
                case IMAGE_CAPTURE:
                    Bitmap captura = (Bitmap)data.getExtras().getParcelable("data");
                    File f = guardarImagen(captura);
                    currImageURI = Uri.fromFile(f);
                   // crop(currImageURI);
                    break;
            }

            Intent i = new Intent();
            i.setData(currImageURI);
            setResult(RESULT_OK,i);
            finish();
        }
    }

    private File guardarImagen( Bitmap img) {
        File nuevaImagen;
        FileOutputStream ostream = null;
        Date d = new Date();
        File dir = this.getDir("imgPictogramas", Context.MODE_PRIVATE);
        int dimension;

        dimension = getResources().getInteger(R.integer.dim_pictogramas);

        Bitmap imgEscalada = Bitmap.createScaledBitmap(img, dimension, dimension, true);

        //Creamos el fichero.
        nuevaImagen = new File(dir.getAbsolutePath()+"/"+d.getTime()+".png");
        try {
            Log.d("GUARDAR IMAGEN", "Se va a crear la imagen en : " + nuevaImagen.getAbsoluteFile());
            nuevaImagen.createNewFile();
            ostream = new FileOutputStream(nuevaImagen);
            imgEscalada.compress(Bitmap.CompressFormat.PNG, 100, ostream);

        } catch (IOException e) {
            Log.e(" Bitmap img","Error al intentar crear la imagen. "+e.getMessage());
            e.printStackTrace();
        } finally {
            if (ostream != null){
                try {
                    ostream.close();
                } catch (IOException e) {
                    Log.e(" Bitmap img","Error al intentar cerrar el fichero de  la imagen. "+e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return nuevaImagen;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.camaray_galeria, menu);
        return true;
    }
    
}

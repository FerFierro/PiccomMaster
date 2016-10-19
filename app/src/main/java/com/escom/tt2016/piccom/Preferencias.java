package com.escom.tt2016.piccom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by ajmarmar on 27/08/13.
 */
public class Preferencias extends PreferenceActivity {
    private final int CATEGORIA = 1;
    private String YO = "Yo";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);

        Preference customPref = (Preference) findPreference("data_contact_avatar");
        customPref.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {

                       lanzarOpciones();

                        return true;
                    }
                });

        //para la internacionalización,
        this.YO = getResources().getString(R.string.YO);
    }

    private void lanzarOpciones(){
        Intent i = new Intent(this, CamarayGaleria.class);
        startActivityForResult(i,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri currImageURI;
        File f;
        File dir;
        int dimension;
        Bitmap imgEscalada;
        GestionBD db;
        File nuevaImagen;
        FileOutputStream ostream = null;
        Date d = new Date();

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {

                // currImageURI is the global variable I'm using to hold the content:// URI of the image
                currImageURI = data.getData();

                //Guardamos la ruta.
                SharedPreferences shre = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit=shre.edit();
                edit.putString("data_contact_avatar",currImageURI.toString());
                edit.commit();

                f = new File(getPath(currImageURI));
                dir = this.getDir("imgPictogramas", Context.MODE_PRIVATE);

                dimension = getResources().getInteger(R.integer.dim_pictogramas);
                imgEscalada = Utilidades.rescalarImagen(dimension,f);

                nuevaImagen = new File(dir.getAbsolutePath()+"/"+d.getTime()+".png");
                try {
                    Log.d("PREFERENCIAS","Se va a crear la imagen en : "+nuevaImagen.getAbsoluteFile());
                    nuevaImagen.createNewFile();
                    ostream = new FileOutputStream(nuevaImagen);
                    imgEscalada.compress(Bitmap.CompressFormat.PNG, 100, ostream);

                    db = new GestionBD(this);
                    db.addPictograma(CATEGORIA,YO,nuevaImagen.getName());
                    Log.d("PREFERENCIAS","Se ha creado un nuevo registro");

                } catch (IOException e) {
                    Log.e("PREFERENCIAS","Error al intentar crear la imagen. "+e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (ostream != null){
                        try {
                            ostream.close();
                        } catch (IOException e) {
                            Log.e("PREFERENCIAS","Error al intentar cerrar el fichero de  la imagen. "+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
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
            ruta = uri.getEncodedPath();
        }
        return ruta;
    }
}
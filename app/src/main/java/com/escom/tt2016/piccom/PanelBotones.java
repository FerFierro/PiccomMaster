package com.escom.tt2016.piccom;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

public class PanelBotones extends Activity {
    private final int CODIGO_IMPORTAR=3;
    private final String URL_AYUDA="http://piccom.bitproservices.es";

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panelbotones);

        ImageButton btnConfigurar = (ImageButton) findViewById(R.id.btnConfiguracion);
        ImageButton btnMtoPicto = (ImageButton) findViewById(R.id.btnMtoPicto);
        ImageButton btnImportar = (ImageButton) findViewById(R.id.btnImportarPic);
        ImageButton btnExportar = (ImageButton) findViewById(R.id.btnExportarPic);
        ImageButton btnAyudar = (ImageButton) findViewById(R.id.btnAyuda);

        btnConfigurar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarPreferencias();
            }
        });

        btnMtoPicto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mtoPictograma();
            }
        });

        btnImportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importarPictogramas();
            }
        });

        btnExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportarPictogramas();
            }
        });

        btnAyudar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostarAyuda();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.panel_botones, menu);
        return true;
    }

    private void mostarAyuda(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(this.URL_AYUDA));
        startActivity(i);
        finish();
    }

    private void mostrarPreferencias(){
        Intent i = new Intent(this, Preferencias.class);
        startActivity(i);
        finish();
    }

    private void mtoPictograma() {
        //http://sintaxispragmatica.wordpress.com/2013/07/21/usando-la-libreria-texttospeech-texto-a-voz/
        Intent i = new Intent(this, Mtopictogramas.class);
        startActivity(i);
        finish();
    }

    private void importarPictogramas(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        boolean sdDisponible = false;

        //Comprobamos el estado de la memoria externa (tarjeta SD)
        String estado = Environment.getExternalStorageState();

        if (estado.equals(Environment.MEDIA_MOUNTED)) {
            sdDisponible = true;
        }
        else if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            sdDisponible = true;
        }
        else  {
            sdDisponible = false;
        }

        if (sdDisponible){
            intent.setType("*/*");
            // intent.setType("text/plain");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //intent.addCategory(Intent.CATEGORY_APP_BROWSER);

            try {
                startActivityForResult(
                        Intent.createChooser(intent, "Seleccione un Fichero"), CODIGO_IMPORTAR);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(this, "Por favor, Installe un gestor de ficheros",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,"La tarjeta externa no esta disponible",Toast.LENGTH_SHORT).show();
        }
        //finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case CODIGO_IMPORTAR:
                    Uri uri = data.getData();
                    importarPictogramas(uri);
                    break;
            }
        }
    }

    private void importarPictogramas(Uri uri) {
        // Get the Uri of the selected file
        //Uri uri = data.getData();
        Log.d("Importar Pictos", "File Uri: " + uri.toString());
        // Get the path
        String path = getPath(uri);
        Log.d("Importar Pictos", "File Path: " + path);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Cargando...");
        dialog.setTitle("Progreso");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);

        new ProcessCargarPictogramas().execute(path);
    }

    private String getPath(Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = this.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private void exportarPictogramas(){
        boolean sdDisponible = false;

        //Comprobamos el estado de la memoria externa (tarjeta SD)
        String estado = Environment.getExternalStorageState();

        if (estado.equals(Environment.MEDIA_MOUNTED)) {
            sdDisponible = true;
        }
        else if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            sdDisponible = true;
        }
        else  {
            sdDisponible = false;
        }

        if (sdDisponible){
            File dirExport = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/exportPicCom");
            if (!dirExport.exists()){
                dirExport.mkdir();
            }

            dialog = new ProgressDialog(this);
            dialog.setMessage("Exportando...");
            dialog.setTitle("Progreso");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);

            new ProcessExportarPictogramas().execute(dirExport.getAbsolutePath());

        } else {
            Toast.makeText(this,"La tarjeta externa no esta disponible",Toast.LENGTH_SHORT).show();
        }
        //finish();
    }

    private void guardarImagen(int categoria, String palabra, String imagen) {
        File f = new File(imagen);
        File nuevaImagen;
        FileOutputStream ostream = null;
        Date d = new Date();
        File dir = this.getDir("imgPictogramas", Context.MODE_PRIVATE);
        GestionBD db;

        //Reescalamos
        Bitmap img = BitmapFactory.decodeFile(f.getAbsolutePath());
        Bitmap imgEscalada = Bitmap.createScaledBitmap(img, 200, 200, true);

        //Creamos el fichero.
        nuevaImagen = new File(dir.getAbsolutePath()+"/"+d.getTime()+".png");
        try {
            Log.d("ImportarPICTOGRAMA","Se va a crear la imagen en : "+nuevaImagen.getAbsoluteFile());
            nuevaImagen.createNewFile();
            ostream = new FileOutputStream(nuevaImagen);
            imgEscalada.compress(Bitmap.CompressFormat.PNG, 100, ostream);

            db = new GestionBD(this);
            db.addPictograma(categoria,palabra,nuevaImagen.getName());
            Log.d("ImportarPICTOGRAMA","Se ha creado un nuevo registro");

        } catch (IOException e) {
            Log.e("ImportarPICTOGRAMA","Error al intentar crear la imagen. "+e.getMessage());
            e.printStackTrace();
        } finally {
            if (ostream != null){
                try {
                    ostream.close();
                } catch (IOException e) {
                    Log.e("ImportarPICTOGRAMA","Error al intentar cerrar el fichero de  la imagen. "+e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private class ProcessCargarPictogramas extends AsyncTask<String, Float, Integer> {
        private final int CATEGORIA = 0;
        private final int PALABRA=1;
        private final int IMAGEN=2;


        protected void onPreExecute() {
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.show(); //Mostramos el diálogo antes de comenzar
        }

        protected Integer doInBackground(String... url) {
            int totalFicheros=0;
            int i;
            BufferedReader fin = null;
            File fichero = new File(url[0]);
            String linea;
            String datos[];
            String dir = url[0];
            File d;
            GestionBD db;

            db = new GestionBD(PanelBotones.this);

            dir = dir.substring(0,dir.lastIndexOf("/"));
            d = new File(dir);
            totalFicheros = d.listFiles().length-1;

            try {
                fin =  new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(fichero)));
                i=0;
                linea = fin.readLine();
                while(linea != null){
                    linea = linea.trim();
                    datos = linea.split(";");

                    Log.d("IMPORTAR PICTOGRAMAS","Linea "+i+": "+linea);

                    if (datos != null && !linea.equals("") && datos.length == 3){

                        // guardarImagen(codigos.get(Integer.parseInt(datos[CATEGORIA])),datos[PALABRA],dir+"/"+datos[IMAGEN]);
                        guardarImagen(Integer.parseInt(datos[CATEGORIA]),datos[PALABRA],dir+"/"+datos[IMAGEN]);

                    } else {
                        Log.e("IMPORTAR PICTOGRAMAS","La linea '"+linea+"' tien un formato incorrecto");
                    }

                    linea = fin.readLine();
                    publishProgress(i/(float)totalFicheros); //Actualizamos los valores
                    i++;
                }
            } catch (Exception e) {
                Toast.makeText(PanelBotones.this,"Se ha producido un error al acceder al fichero. "+e.getMessage(),Toast.LENGTH_SHORT);
            } finally {
                if (fin != null){
                    try {
                        fin.close();
                    } catch (IOException e) {
                        Log.e("IMPORTAR PICTOGRAMAS", "No se pudo cerrar el fichero. " + e.getMessage());
                    }
                }
            }

            return totalFicheros;
        }

        protected void onProgressUpdate (Float... valores) {
            int p = Math.round(100*valores[0]);
            dialog.setProgress(p);
        }

        protected void onPostExecute(Integer bytes) {
            dialog.dismiss();
        }
    }
    private class ProcessExportarPictogramas extends AsyncTask<String, Float, Integer> {

        protected void onPreExecute() {
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.show(); //Mostramos el diálogo antes de comenzar
        }

        protected Integer doInBackground(String... url) {
            int totalFicheros=0;
            int i;
            ArrayList<Pictograma> lista;
            OutputStreamWriter fout = null;
            String linea;
            String dir = url[0];
            String fichero = dir+"/exportPicCom.txt";
            GestionBD db;
            File dirOrigen = PanelBotones.this.getDir("imgPictogramas", Context.MODE_PRIVATE);

            db = new GestionBD(PanelBotones.this);
            lista = db.getAllPictogramas();

            totalFicheros = lista.size();
            try {
                fout =
                        new OutputStreamWriter(
                                new FileOutputStream(fichero));

                i=0;
                for (Pictograma p: lista){
                    linea = p.getCategoria()+";"+p.getNombre()+";"+p.getImagen()+"\n";
                    fout.write(linea);

                    Utilidades.FileCopy(dirOrigen.getAbsolutePath() + "/" + p.getImagen(), dir + "/" + p.getImagen());

                    publishProgress(i/(float)totalFicheros); //Actualizamos los valores
                    i++;
                }
            } catch (Exception e) {
                Toast.makeText(PanelBotones.this,"Se ha producido un error al acceder al fichero. "+e.getMessage(),Toast.LENGTH_SHORT);
            } finally {
                if (fout != null){
                    try {
                        fout.flush();
                        fout.close();
                    } catch (IOException e) {
                        Log.e("EXPORTAR PICTOGRAMAS", "No se pudo cerrar el fichero. " + e.getMessage());
                    }
                }
            }

            return totalFicheros;
        }

        protected void onProgressUpdate (Float... valores) {
            int p = Math.round(100*valores[0]);
            dialog.setProgress(p);
        }

        protected void onPostExecute(Integer bytes) {
            dialog.dismiss();
        }
    }

}

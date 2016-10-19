package com.escom.tt2016.piccom;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
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
import java.util.Locale;

public class Mtopictogramas extends Activity implements TextToSpeech.OnInitListener {

    private final int CODIGO_DELPICTOGRAMA=2;
    private final int CODIGO_ADDPICTOGRAMA=1;
    private final int CODIGO_IMPORTAR=3;

    private int categoria;
    private Pictograma pictogramaSeleccionado;
    private GridView gv;

    private ImageButton addPic;
    private ImageButton delPic;
    private ImageButton bacPic;

    private boolean ttsEnable;
    private TextToSpeech tts;

    private ProgressDialog dialog;

    private String show_picto_text;
    private String school_font;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mtopictogramas);

        SharedPreferences pref = getSharedPreferences("com.bps.piccom_preferences", Context.MODE_PRIVATE);

        show_picto_text = pref.getString("data_pictogramas_show_text","0");
        school_font = pref.getString("data_pictogramas_type_fonts","0");

        this.categoria = 0;
        this.pictogramaSeleccionado = null;

        this.addPic = (ImageButton)findViewById(R.id.addPictogram);
        this.delPic = (ImageButton)findViewById(R.id.delPictogram);
        this.bacPic = (ImageButton)findViewById(R.id.backPictrograma);

        //La desactivamos por que no hay nada Selecionado. y si es la primera entrada son las categorias y por tanto no se puede borrar
        this.delPic.setEnabled(false);
        this.delPic.setVisibility(View.INVISIBLE);
        this.addPic.setEnabled(false);
        this.addPic.setVisibility(View.INVISIBLE);
        this.bacPic.setEnabled(false);
        this.bacPic.setVisibility(View.INVISIBLE);

        this.ttsEnable=false;
        tts = new TextToSpeech(this, this);

        this.addPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddPictograma();
            }
        });
        
        this.delPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDelPictograma();
            }
        });

        this.bacPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarPictogramas(0);
            }
        });

        this.gv = (GridView)findViewById(R.id.listaPic);
        this.gv.setAdapter(new PictogramasAdapter(this,R.layout.elementopic,show_picto_text.equals("0"), school_font.equals("1")));
        this.gv.setSelected(true);
        this.gv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Pictograma p = (Pictograma)gv.getItemAtPosition(position);

                if (Mtopictogramas.this.categoria == 0){
                    String texto = Utilidades.getTextoCategoria(Mtopictogramas.this, p.getNombre());
                    Toast.makeText(Mtopictogramas.this, texto, Toast.LENGTH_SHORT).show();
                    speakOut(texto);

                    cargarPictogramas(p.getId());
                } else {
                    Toast.makeText(Mtopictogramas.this, p.getNombre(), Toast.LENGTH_SHORT).show();
                    speakOut(p.getNombre());

                    seleccionarPic(p,position);
                }
            }
        });
    }

    private void seleccionarPic(Pictograma pic, int position){
        this.pictogramaSeleccionado = pic;
        gv.requestFocusFromTouch();
        gv.requestFocus();
        gv.setSelection(position);
        this.delPic.setEnabled(true);
        this.delPic.setVisibility(View.VISIBLE);
    }

    private void cargarPictogramas(int id) {
        Log.d("MTOPICTOGRAMAS","Se inicia la carga de pictogramas.");
        PictogramasAdapter adaptador = (PictogramasAdapter)this.gv.getAdapter();
        adaptador.cargarImagenes(id);
        adaptador.notifyDataSetChanged();
        this.delPic.setEnabled(false);
        this.addPic.setEnabled(id != 0);
        this.bacPic.setEnabled(id != 0);

        this.delPic.setVisibility(View.INVISIBLE);
        //this.delPic.setVisibility(id == 0?View.INVISIBLE:View.VISIBLE);
        this.addPic.setVisibility(id == 0?View.INVISIBLE:View.VISIBLE);
        this.bacPic.setVisibility(id == 0?View.INVISIBLE:View.VISIBLE);

        this.categoria = id;
        this.pictogramaSeleccionado = null;
        Log.d("MTOPICTOGRAMAS","Finaliza la carga de pictogramas.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mtopictogramas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.importPictograms:
                importPictogramas();
                break;
            case R.id.exportPictograms:
                exportPictogramas();
                break;
        }

        return true;
    }

    private void exportPictogramas() {
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
    }

    private void importPictogramas() {
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
    }


    private void openDelPictograma() {
        Intent i = new Intent(this,DelPictograma.class);
        startActivityForResult(i,CODIGO_DELPICTOGRAMA);
    }

    private void openAddPictograma() {
        Intent i = new Intent(this,AddPictograma.class);
        i.putExtra("categoria",this.categoria);
        //startActivity(i);
        startActivityForResult(i,CODIGO_ADDPICTOGRAMA);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK){
            switch (requestCode){
                case CODIGO_ADDPICTOGRAMA:
                    cargarPictogramas(this.categoria);
                    break;
                case CODIGO_DELPICTOGRAMA:
                    borrarPictograma(this.pictogramaSeleccionado);
                    break;
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

    private void borrarPictograma(Pictograma pic) {
        int cantidad;
        GestionBD db = new GestionBD(this);
        db.delPictograma(pic.getId());
        cantidad = db.contarPictograma(pic.getImagen());

        if (cantidad == 0){
            File dir = this.getDir("imgPictogramas", MODE_PRIVATE);
            File f = new File(dir.getAbsolutePath()+"/"+pic.getImagen());
            f.deleteOnExit();
        }
        cargarPictogramas(this.categoria);
        this.pictogramaSeleccionado = null;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Este lenguaje no esta soportado");
            } else {
                this.ttsEnable = true;
                speakOut("");
            }

        } else {
            Log.e("TTS", "Inicialización ha fallado");
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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

            db = new GestionBD(Mtopictogramas.this);
            
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
                Toast.makeText(Mtopictogramas.this,"Se ha producido un error al acceder al fichero. "+e.getMessage(),Toast.LENGTH_SHORT);
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
            File dirOrigen = Mtopictogramas.this.getDir("imgPictogramas", Context.MODE_PRIVATE);

            db = new GestionBD(Mtopictogramas.this);
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
                Toast.makeText(Mtopictogramas.this,"Se ha producido un error al acceder al fichero. "+e.getMessage(),Toast.LENGTH_SHORT);
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
}
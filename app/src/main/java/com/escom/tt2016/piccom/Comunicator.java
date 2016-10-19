package com.escom.tt2016.piccom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class Comunicator extends Activity implements TextToSpeech.OnInitListener, GestureOverlayView.OnGesturePerformedListener {

    private final double PUNTUACION_GESTURE=30;

    private GridView gvFrase;
    private GridView gvCategoria;

    private ImageButton btnVolverCat;
    private ImageButton btnBorrarFrase;
    private ImageButton btnPlayFrase;

    private boolean ttsEnable;
    private TextToSpeech tts;

    int categoria;

    private String show_picto_text;
    private String school_font;
    private boolean showI;
    private Pictograma pic_I;

    private GestureLibrary libreria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comunicator);

        SharedPreferences pref = getSharedPreferences("com.bps.piccom_preferences", Context.MODE_PRIVATE);
        //nombre.setText(pref.getString("data_contact_name",""));
        this.show_picto_text = pref.getString("data_pictogramas_show_text","0");
        this.school_font = pref.getString("data_pictogramas_type_fonts","0");
        this.showI= pref.getBoolean("data_pictogramas_show_I",true);

        if (showI){
            //buscamos el pictograma, en el caso de que no exista el pictograma, ponemos ShowI a false
            GestionBD db = new GestionBD(this);
            this.pic_I = db.getPictograam(getResources().getString(R.string.YO));
            showI = this.pic_I != null;
        }

        this.categoria=0;

        this.ttsEnable=false;
        tts = new TextToSpeech(this, this);

        this.btnBorrarFrase = (ImageButton)findViewById(R.id.delFrase);
        this.btnBorrarFrase.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarFrase();
            }
        });

        this.btnPlayFrase = (ImageButton)findViewById(R.id.playFrase);
        this.btnPlayFrase.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playFrase();
            }
        });

        this.btnVolverCat = (ImageButton)findViewById(R.id.btnVolverCat);
        this.btnVolverCat.setVisibility(View.INVISIBLE);
        this.btnVolverCat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //volvemos a las categorias despuesde añadir un nuevo picto.
                cargarPictogramasCat(0);
            }
        });

        this.gvFrase = (GridView)findViewById(R.id.gvFrase);
        this.gvFrase.setAdapter(new PictogramasAdapterFrase(this,show_picto_text.equals("0"),school_font.equals("1")));
        this.gvFrase.setSelected(true);
        this.gvFrase.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PictogramasAdapterFrase paf = (PictogramasAdapterFrase)gvFrase.getAdapter();
                paf.delPictograma(position);
                paf.notifyDataSetChanged();
            }
        });

        this.gvCategoria = (GridView)findViewById(R.id.gvCategoria);
        this.gvCategoria.setAdapter(new PictogramasAdapter(this,R.layout.elementopiccomunicator,show_picto_text.equals("0"),school_font.equals("1")));
        this.gvCategoria.setSelected(true);
        this.gvCategoria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Pictograma p = (Pictograma)gvCategoria.getItemAtPosition(position);
               // Toast.makeText(Comunicator.this, p.getNombre(), Toast.LENGTH_SHORT).show();

                //Solo cuando seleccionamos un pictograma de verdad.
                if (Comunicator.this.categoria != 0){
                    speakOut(p.getNombre());
                }

                if (Comunicator.this.categoria == 0){
                    cargarPictogramasCat(p.getId());
                } else {
                    seleccionarPic(p);
                }
            }
        });

        if (showI){
            PictogramasAdapterFrase paf = (PictogramasAdapterFrase)gvFrase.getAdapter();
            paf.addPictograma(pic_I);
            paf.notifyDataSetChanged();
        }

        //Gestures.
        boolean enableGestures = pref.getBoolean("data_pictogramas_enable_gestures",true);

        if (enableGestures) {
            this.libreria = GestureLibraries.fromRawResource(this, R.raw.gestures);
            if (!this.libreria.load()) {
                Toast.makeText(this,"Imposible cargar las Gestures",Toast.LENGTH_LONG).show();
                finish();
            }
        }
        GestureOverlayView gesturesView = (GestureOverlayView) findViewById(R.id.gestures);
        gesturesView.setEnabled(enableGestures);
        gesturesView.addOnGesturePerformedListener(this);

    }

    private void playFrase(){
        PictogramasAdapterFrase paf = (PictogramasAdapterFrase)gvFrase.getAdapter();
        speakOut(paf.getFrase());
    }

    private void borrarFrase(){
        PictogramasAdapterFrase paf = (PictogramasAdapterFrase)gvFrase.getAdapter();
        paf.delAll();
        if (showI){
            paf.addPictograma(pic_I);
        }
        paf.notifyDataSetChanged();
    }

    private void seleccionarPic(Pictograma pic){
        PictogramasAdapterFrase paf = (PictogramasAdapterFrase)gvFrase.getAdapter();
        paf.addPictograma(pic);
        paf.notifyDataSetChanged();

       //volvemos a las categorias despuesde añadir un nuevo picto.
        cargarPictogramasCat(0);
    }

    private void cargarPictogramasCat(int id) {
        Log.d("COMUNICATOR","Se inicia la carga de pictogramas.");

        PictogramasAdapter adaptador = (PictogramasAdapter)this.gvCategoria.getAdapter();
        adaptador.cargarImagenes(id);
        adaptador.notifyDataSetChanged();
        this.categoria = id;

        this.btnVolverCat.setVisibility(id == 0 ? View.INVISIBLE:View.VISIBLE);

        Log.d("COMUNICATOR","Finaliza la carga de pictogramas.");
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

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions =  this.libreria.recognize(gesture);

        String comando = predictions.get(0).name;

        //Toast.makeText(this,"Puntuacion: "+predictions.get(0).score,Toast.LENGTH_LONG).show();
        if(predictions.size()>0){
            if (predictions.get(0).score > PUNTUACION_GESTURE) {
                if (comando.equals("play")){
                    playFrase();
                } else if (comando.equals("remove")){
                    borrarFrase();
                }
            }
        }
    }
}
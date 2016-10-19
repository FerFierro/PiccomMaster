package com.escom.tt2016.piccom;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;


public class InfoContact extends Activity {

    ImageView avatar;
    TextView nombre;
    TextView anno;
    TextView direccion;
    TextView codPostal;
    TextView ciudad;
    TextView madre;
    TextView tlfMadre;
    TextView padre;
    TextView tlfPadre;
    TextView otrosTfl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infocontact);

        /////Inicializamos datos.
        avatar = (ImageView)findViewById(R.id.imgContact);
        nombre = (TextView)findViewById(R.id.infoCntName);
        anno = (TextView)findViewById(R.id.infoCntYearBirth);
        madre = (TextView)findViewById(R.id.infoCntMother);
        tlfMadre = (TextView)findViewById(R.id.infoCntTlfMother);
        padre = (TextView)findViewById(R.id.infoCntFather);
        tlfPadre = (TextView)findViewById(R.id.infoCntTlfFather);
        otrosTfl = (TextView)findViewById(R.id.infoCntOtherTlf);

        direccion = (TextView)findViewById(R.id.infoCntAddress);
        codPostal = (TextView)findViewById(R.id.infoCntCodPos);
        ciudad = (TextView)findViewById(R.id.infoCntCountry);

        cargarDatos();
    }

    private void cargarDatos() {
        SharedPreferences pref;
        //pref = context.getSharedPreferences(
        //        "com.bps.piccom_preferences", Context.MODE_PRIVATE);
        pref = getSharedPreferences("com.bps.piccom_preferences", Context.MODE_PRIVATE);
        nombre.setText(pref.getString("data_contact_name",""));
        anno.setText(pref.getString("data_contact_year_birth",""));
        madre.setText(pref.getString("data_contact_name_mother",""));
        tlfMadre.setText(pref.getString("data_contact_tlf_mother",""));
        padre.setText(pref.getString("data_contact_name_father",""));
        tlfPadre.setText(pref.getString("data_contact_tlf_father",""));
        direccion.setText(pref.getString("data_contact_address",""));

        codPostal.setText(pref.getString("data_contact_cod_post",""));
        ciudad.setText(pref.getString("data_contact_country",""));
        otrosTfl.setText(pref.getString("data_contact_tlf_other",""));

        avatar.setImageURI(Uri.parse(pref.getString("data_contact_avatar","")) );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.info_contact, menu);
        return true;
    }
    
}

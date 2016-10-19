package com.escom.tt2016.piccom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ///Definición de Botones.
        ImageButton btnInfoCnt = (ImageButton) findViewById(R.id.btn_InfoContacto);
        ImageButton btnLocalizacion = (ImageButton) findViewById(R.id.btn_Localizacion);
        ImageButton btnSalir = (ImageButton) findViewById(R.id.btn_Salir);
        ImageButton btnComunicador = (ImageButton) findViewById(R.id.btn_comunicar);
        ImageButton btnConfigurar = (ImageButton) findViewById(R.id.btnConfiguracion);

        btnSalir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                salir();
            }
        });
        btnInfoCnt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarInfoContacto();
            }
        });
        btnComunicador.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                comunicar();
            }
        });
        btnLocalizacion.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarUbicacionXMail();
            }
        });

        btnConfigurar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                otrasOpciones();
            }
        });
    }

    private void enviarUbicacionXMail(){
        SharedPreferences pref = getSharedPreferences("com.bps.piccom_preferences", Context.MODE_PRIVATE);
        String email = pref.getString("data_location_email","").trim();
        String usuario = pref.getString("data_location_from_email","").trim();
        String pwd = pref.getString("data_location_pwd_email","").trim();

        if (!email.equals("") && !usuario.equals("") && !pwd.equals("")){
            MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
                @Override
                public void gotLocation(Location location) {
                    if (location != null) {
                        Log.d("PicCom", "Latitud: " + location.getLatitude() + " Longitud: " + location.getLongitude());
                        //Toast.makeText(MainActivity.this,"Latitud: "+location.getLatitude()+" Longitud: "+location.getLongitude(),Toast.LENGTH_LONG).show();
                        enviarCorreo(location.getLatitude(), location.getLongitude(), location.getTime());
                    }
                }
            };
            MyLocation myLocation = new MyLocation();
            myLocation.getLocation(MainActivity.this, locationResult);
        } else {
            Toast.makeText(MainActivity.this,getResources().getText(R.string.email_not_set),Toast.LENGTH_LONG).show();
        }
    }


    public void otrasOpciones(){
        Intent i = new Intent(this, PanelBotones.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void comunicar() {
        Intent i = new Intent(this, Comunicator.class);
        startActivity(i);
    }

    private void salir(){
        finish();
    }

    private void mostrarInfoContacto(){
        Intent i = new Intent(this, InfoContact.class);
        startActivity(i);
    }

    private void enviarCorreo(double lat, double lon, long fecha) {
        String cuerpo;
        SharedPreferences pref;
        Intent itSend;
        String email;
        String usuario;
        String pwd;
        String asunto="Localización PicCom";


        pref = getSharedPreferences("com.bps.piccom_preferences", Context.MODE_PRIVATE);
        email = pref.getString("data_location_email","").trim();
        usuario = pref.getString("data_location_from_email","").trim();
        pwd = pref.getString("data_location_pwd_email","").trim();

        //if (!this.email.equals("") && !this.usuario.equals("") && !this.pwd.equals("")){
            cuerpo = "Última posición conocida más reciente a las "+(new Date(fecha)).toString()+" es: http://maps.google.es/?q="+lat+"%20"+lon;

           /*
            //es necesario un intent que levante la actividad deseada
            itSend = new Intent(android.content.Intent.ACTION_SEND);

            //vamos a enviar texto plano a menos que el checkbox esté marcado
            itSend.setType("plain/text");
            //colocamos los datos para el envío
            itSend.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ email});
            itSend.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getText(R.string.email_location_subject));
            itSend.putExtra(android.content.Intent.EXTRA_TEXT, cuerpo);

            //iniciamos la actividad
            startActivity(itSend);
            */

            new EnviarEmail().execute(usuario,pwd,email,asunto,cuerpo);

        //} else {
        //    Toast.makeText(MainActivity.this,getResources().getText(R.string.email_not_set),Toast.LENGTH_LONG).show();
        //}
    }

    class EnviarEmail extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "OK";
            Mail m = new Mail(params[0], params[1]);

            String[] toArr = {params[2]}; // This is an array, you can add more emails, just separate them with a coma
            m.setTo(toArr); // load array to setTo function
            m.setFrom(params[0]); // who is sending the email
            m.setSubject(params[3]);
            m.setBody(params[4]);

            try {

                if(m.send()) {
                    // success
                    Log.i("PicCom Main","Se ha enviado correctamente el correo.");
                } else {
                    // failure
                    Log.e("PicCom Main", "El Correo no se ha enviado por alguna razón");
                    result = "El Correo no se ha enviado por alguna razón";
                }
            } catch(Exception e) {
                // some other problem
                //e.printStackTrace();
                // Toast.makeText(this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                if (e.toString().equals("javax.mail.AuthenticationFailedException")){
                    result="Error en la autentificación del correo";
                } else {
                    if (e.getMessage() == null){
                        result = e.toString();
                    } else {
                        result = e.getMessage();
                    }
                }
                Log.e("PicCom Main","Error al intentar enviar el correo: "+result);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("OK")){
                Toast.makeText(MainActivity.this,"La ubicación se envio correctamente",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this,s,Toast.LENGTH_LONG).show();
            }
        }
    }
}
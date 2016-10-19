package com.escom.tt2016.piccom;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ajmarmar on 20/10/13.
 */
public class Utilidades {

    public static void FileCopy(String source, String target) throws IOException {

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
    }

    /*
    public static Drawable getBackground(int categoria, View v){
        Drawable d;

        switch (categoria){
            case 1:
                d = v.getResources().getDrawable(R.drawable.pic_border_personas);
                break;
            case 2:
                d = v.getResources().getDrawable(R.drawable.pic_border_objetos);
                break;
            case 3:
                d = v.getResources().getDrawable(R.drawable.pic_border_verbos);
                break;
            case 4:
                d = v.getResources().getDrawable(R.drawable.pic_border_lugares);
                break;
            case 5:
                d = v.getResources().getDrawable(R.drawable.pic_border_estados_animos);
                break;
            case 6:
                d = v.getResources().getDrawable(R.drawable.pic_border_sociales);
                break;
            default:
                d = v.getResources().getDrawable(R.drawable.pic_border);
                break;
        }

        return d;
    }
    */

    public static int getBackground(int categoria){
        int d;

        switch (categoria){
            case 1:
                d = R.drawable.pic_border_personas;
                break;
            case 2:
                d = R.drawable.pic_border_objetos;
                break;
            case 3:
                d = R.drawable.pic_border_verbos;
                break;
            case 4:
                d = R.drawable.pic_border_lugares;
                break;
            case 5:
                d = R.drawable.pic_border_estados_animos;
                break;
            case 6:
                d = R.drawable.pic_border_sociales;
                break;
            default:
                d = R.drawable.pic_border;
                break;
        }

        return d;
    }

    public static Bitmap rescalarImagen(int dimension, File f){

        Bitmap img;
        Bitmap imgEscalada;


        img = BitmapFactory.decodeFile(f.getAbsolutePath());
        imgEscalada = Bitmap.createScaledBitmap(img, dimension, dimension, true);

        //img.recycle();
        img = null;

        return imgEscalada;
    }

    public static String getTextoCategoria(Context cxt, String cat){
        String texto=null;

        if (cat.equals("cat_people")) {
            texto = cxt.getResources().getString(R.string.cat_people);
        } else if (cat.equals("cat_objects")){
            texto = cxt.getResources().getString(R.string.cat_objects);
        } else if (cat.equals("cat_verbs")){
            texto = cxt.getResources().getString(R.string.cat_verbs);
        } else if (cat.equals("cat_places")){
            texto = cxt.getResources().getString(R.string.cat_places);
        } else if (cat.equals("cat_moods")){
            texto = cxt.getResources().getString(R.string.cat_moods);
        } else { //Esta es la última categoria.
            texto = cxt.getResources().getString(R.string.cat_social);
        }

        return texto;
    }
}

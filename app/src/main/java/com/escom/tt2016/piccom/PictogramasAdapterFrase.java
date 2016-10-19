package com.escom.tt2016.piccom;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

/**
 * Created by ajmarmar on 24/09/13.
 */
public class PictogramasAdapterFrase extends BaseAdapter {

    private Context cxt;
    private Vector<Pictograma> lista;
    private static boolean show_picto_text;
    private static Typeface school_font;
    private static boolean type_font_schools;

    public PictogramasAdapterFrase(Context cxt) {
        this.cxt = cxt;
        this.lista = new Vector<Pictograma>();
        this.show_picto_text=true;
        this.type_font_schools=false;
    }

    public PictogramasAdapterFrase(Context cxt,boolean show_text, boolean type_font_schools) {
        this.cxt = cxt;
        this.lista = new Vector<Pictograma>();
        this.show_picto_text=show_text;
        this.type_font_schools=type_font_schools;
        this.school_font = Typeface.createFromAsset(cxt.getAssets(), "Escolar_N.TTF");
    }

    public void addPictograma(Pictograma pictograma){
        lista.add(pictograma);
    }

    public void delPictograma(int posicion){
        lista.remove(posicion);
    }

    public void delAll(){
        lista.removeAllElements();
    }

    public String getFrase(){
        String frase="";

        for (Pictograma p : lista){
            frase += " "+p.getNombre();
        }

        return frase.trim();
    }

    @Override
    public int getCount() {
        return this.lista.size();
    }

    @Override
    public Object getItem(int position) {
        return this.lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.lista.get(position).getId();
    }

    private static class PlaceHolder {
        Pictograma tag;
        TextView titulo;
        ImageView picto;

        public static PlaceHolder generate(View convertView) {
            PlaceHolder placeHolder = new PlaceHolder();
            placeHolder.titulo = (TextView)convertView.findViewById(R.id.textoPicElemento);
            placeHolder.picto = (ImageView)convertView.findViewById(R.id.imgPicElemento);

            //placeHolder.picto.setLayoutParams(new GridView.LayoutParams(200, 200));
            placeHolder.picto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //placeHolder.picto.setPadding(5, 5, 5, 5);

            placeHolder.titulo.setVisibility(PictogramasAdapterFrase.show_picto_text?View.VISIBLE:View.INVISIBLE);

            if (type_font_schools){
                placeHolder.titulo.setTypeface(PictogramasAdapterFrase.school_font);
            }

            return placeHolder;
        }

        public void setTag(Pictograma p){
            this.tag = p;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = null;
        File f = null;
        File dir = null;
        FileInputStream i = null;
        PlaceHolder placeHolder;



        if (convertView == null) {
            //convertView = View.inflate(this.cxt, R.layout.elementopic, null);
            convertView = View.inflate(this.cxt, R.layout.elementopiccomfrase, null);

            placeHolder = PlaceHolder.generate(convertView);
            convertView.setTag(placeHolder);
        } else {
            placeHolder = (PlaceHolder) convertView.getTag();
        }

        //Le ponemos el fondo según la categoria
        //convertView.setBackground(Utilidades.getBackground(lista.get(position).getCategoria(),convertView));
        convertView.setBackgroundResource(Utilidades.getBackground(lista.get(position).getCategoria()));

        placeHolder.titulo.setText(lista.get(position).getNombre());

      //TODO: Revisar este codigo
        if (position < lista.size()) {
            dir = this.cxt.getDir("imgPictogramas", Context.MODE_PRIVATE);
            placeHolder.picto.setImageBitmap(BitmapFactory.decodeFile(dir.getAbsolutePath()+"/"+lista.get(position).getImagen()));
            placeHolder.setTag(lista.get(position));
        }
        return convertView;
    }
}

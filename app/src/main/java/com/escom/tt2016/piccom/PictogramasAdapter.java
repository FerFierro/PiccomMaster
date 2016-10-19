package com.escom.tt2016.piccom;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Vector;

/**
 * Created by ajmarmar on 24/09/13.
 */
public class PictogramasAdapter extends BaseAdapter {

    private int cat;
    private Context cxt;
    private Vector<Pictograma> lista;
    private int layout;
    private static boolean show_picto_text;
    private static boolean type_font_schools;
    private static Typeface school_font;

    public PictogramasAdapter(Context cxt, int layout) {
        this.cxt = cxt;
        this.cat = 0;
        this.layout=layout;
        cargarImagenes(this.cat);
        this.show_picto_text=true;
        this.type_font_schools = false;
      //  this.school_font = Typeface.createFromAsset(cxt.getAssets(), "fonts/Escolar_N.TTF");
    }

    public PictogramasAdapter(Context cxt, int layout, boolean show_text, boolean type_font_schools) {
        this.cxt = cxt;
        this.cat = 0;
        this.layout=layout;
        cargarImagenes(this.cat);
        this.show_picto_text=show_text;
        this.type_font_schools = type_font_schools;
        this.school_font = Typeface.createFromAsset(cxt.getAssets(), "Escolar_N.TTF");
    }

    public void cargarImagenes(int cat) {
        GestionBD db = new GestionBD(this.cxt);
        lista = db.getLista(cat);
        this.cat = cat;
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

            placeHolder.titulo.setVisibility(PictogramasAdapter.show_picto_text?View.VISIBLE:View.INVISIBLE);
            if (type_font_schools){
                placeHolder.titulo.setTypeface(PictogramasAdapter.school_font);
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
            convertView = View.inflate(this.cxt, this.layout, null);
            placeHolder = PlaceHolder.generate(convertView);
            convertView.setTag(placeHolder);
        } else {
            placeHolder = (PlaceHolder) convertView.getTag();
        }

        //Le ponemos el fondo según la categoria
       // convertView.setBackground(Utilidades.getBackground(lista.get(position).getCategoria()==0?lista.get(position).getId():lista.get(position).getCategoria(),convertView));
       convertView.setBackgroundResource(Utilidades.getBackground(lista.get(position).getCategoria()==0?lista.get(position).getId():lista.get(position).getCategoria()));

        //placeHolder.titulo.setText(lista.get(position).getNombre());

      //TODO: Revisar este codigo
        if (position < lista.size()) {
            if (this.cat == 0){
                placeHolder.picto.setImageResource(getCategoriasRes(lista.get(position).getImagen()));
                placeHolder.titulo.setText(Utilidades.getTextoCategoria(this.cxt,lista.get(position).getNombre()));
            } else { //no es el nivel principal
                dir = this.cxt.getDir("imgPictogramas", Context.MODE_PRIVATE);
                placeHolder.picto.setImageBitmap(BitmapFactory.decodeFile(dir.getAbsolutePath()+"/"+lista.get(position).getImagen()));
                placeHolder.titulo.setText(lista.get(position).getNombre());
            }
            placeHolder.setTag(lista.get(position));
        }
        return convertView;
    }

    private Integer getCategoriasRes(String img){
        Integer i = null;

        if (img.equals("cat_personas.png")) {
            i = R.drawable.cat_personas;
        } else if (img.equals("cat_objetos.png")){
            i = R.drawable.cat_objetos;
        } else if (img.equals("cat_verbos.png")){
            i = R.drawable.cat_verbos;
        } else if (img.equals("cat_lugares.png")){
            i = R.drawable.cat_lugares;
        } else if (img.equals("cat_estadosanimo.png")){
            i = R.drawable.cat_estadosanimo;
        } else { //Esta es la última categoria.
            i = R.drawable.cat_social;
        }

        return i;
    }
}

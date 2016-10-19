package com.escom.tt2016.piccom;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class DelPictograma extends Activity {

    Button btnSI;
    Button btnNO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delpictograma);

        btnSI = (Button)findViewById(R.id.btnSi);
        btnNO = (Button)findViewById(R.id.btnNO);

        btnSI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accionSi();
            }
        });

        btnNO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accionNo();
            }
        });
    }


    private void accionSi(){
        setResult(RESULT_OK);
        finish();
    }
    private void accionNo(){
        setResult(RESULT_CANCELED);
        finish();
    }
}

package com.helios.appui;

import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebActivity extends AppCompatActivity {

    private String finalData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getPage("http://www.nita.ac.in/NITAmain/t--p/tphome.html");
        String data = getPageText();

        String formattedStr[] = formatData(data);
    }

    public String[] formatData(String data) {
        String formattedStr[] = data.split("\n");

        for(String elem : formattedStr) {
            if(!(elem.length() == 0))
                data += (elem.trim() + "\n");
        }
        return data.split("\n");
    }


    public void getPage(String url) {

        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Document doc = Jsoup.parse(response);

                doc.select("p").append("\\n\\n");
                doc.select("br").append("\\n");

                Element content = doc.getElementById("content");
                Elements head = content.getElementsByTag("h3");
                Element text = content.getElementById("text");

                String h3 = "";
                for(Element heading : head) {
                    h3 = heading.text();
                }

                Elements p = text.getElementsByTag("p");
                String names = "";
                for (Element para : p) {
                    names += (para.text() + "\n");
                }

                names = names.replaceAll("\\\\n", "\n");
                setPageText(h3 + "\n" + names);
            }
        },
        new Response.ErrorListener(){
            @Override
        public void onErrorResponse(VolleyError volleyError) {

            }
        });
    }

    public void setPageText(String data) {
        finalData = data;
    }

    public String getPageText() {
        return finalData;
    }

}

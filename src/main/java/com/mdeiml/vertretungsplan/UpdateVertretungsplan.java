package com.mdeiml.vertretungsplan;

import android.webkit.WebView;
import android.os.AsyncTask;
import android.net.Uri;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStream;
import android.util.Xml;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

public class UpdateVertretungsplan extends AsyncTask<URL, String, Exception> {

    private WebView webview;
    private XmlSerializer serializer;

    public UpdateVertretungsplan(WebView webview) {
        this.webview = webview;
    }

    @Override
    protected Exception doInBackground(URL... url) {
        //TODO: Das hier schreiben
        try {
            HttpURLConnection connection = (HttpURLConnection)url[0].openConnection();
            connection.setRequestProperty("Authorization", "Basic c2NodWVsZXI6d2ludGVyODYzMTY=");
            serializer = Xml.newSerializer();
            InputStream in = connection.getInputStream();
            Xml.parse(in, Xml.findEncodingByName("iso-8859-1"), new VertretungsplanHandler());
        }catch(IOException | SAXException e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        webview.loadData(Uri.encode(progress[0]), "text/html; charset=UTF-8", null);
    }

    private class VertretungsplanHandler implements ContentHandler {

        @Override
        public void characters(char[] ch, int start, int length) {

        }


        @Override
        public void endDocument() {

        }

        @Override
        public void endElement(String uri, String localName, String qName) {

        }

        @Override
        public void endPrefixMapping(String prefix) {

        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) {

        }

        @Override
        public void processingInstruction(String target, String data) {

        }

        @Override
        public void setDocumentLocator(Locator locator) {

        }

        @Override
        public void skippedEntity(String name) {

        }

        @Override
        public void startDocument() {

        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) {

        }

        @Override
        public void startPrefixMapping(String prefix, String uri) {

        }
    }
}

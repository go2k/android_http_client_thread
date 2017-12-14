package bitcoinumrechner.sabel.com.bitcoinumrechner;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity {

    private EditText et_euro, et_bitcoin, et_aktuellerkurs;
    private Button btn_umrechnen;
    private Button btn_aktualisieren;
    private double faktorBitcoinKursInEuro = 8919.0;
    private boolean euroLock;
    private boolean bitcoinLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_euro = findViewById(R.id.et_euro);
        et_bitcoin = findViewById(R.id.et_bitcoin);
        et_aktuellerkurs = findViewById(R.id.et_aktuellerkurs);
        btn_umrechnen = findViewById(R.id.btn_umrechnen);
        btn_aktualisieren = findViewById(R.id.btn_kursaktualisieren);

        euroLock = false;
        bitcoinLock = false;

        //et_bitcoin.setEnabled(false);
        et_euro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!euroLock) {
                    // System.out.println("Euro: Start: " + start + " Before: " + before + " Count: " + count);
                    try {
                        double euro = Double.parseDouble(charSequence.toString());
                        double ergebnis = euroBitcoinUmrechnen(euro);
                        bitcoinLock = true;
                        et_bitcoin.setText(String.valueOf(ergebnis));
                        bitcoinLock = false;
                    } catch (NumberFormatException e) {
                        bitcoinLock = true;
                        et_bitcoin.setText("");
                        bitcoinLock = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_bitcoin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //  System.out.println("Bitcoin: Start: " + start + " Before: " + before+ " Count: "+ count);
                if (!bitcoinLock) {
                    try {
                        double bitcoin = Double.parseDouble(charSequence.toString());
                        double ergebnis = bitcoinEuroUmrechnen(bitcoin);
                        euroLock = true;
                        et_euro.setText(String.valueOf(ergebnis));
                        euroLock = false;
                    } catch (NumberFormatException e) {
                        euroLock = true;
                        et_euro.setText("");
                        euroLock = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        btn_umrechnen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_euro.getText().toString().length() > 0) {
                    double euro = Double.parseDouble(et_euro.getText().toString());
                    double ergebnis = euroBitcoinUmrechnen(euro);
                    et_bitcoin.setText(String.valueOf(ergebnis));
                } else {
                    double bitcoin = Double.parseDouble(et_bitcoin.getText().toString());
                    double ergebnis = bitcoinEuroUmrechnen(bitcoin);
                    et_euro.setText(String.valueOf(ergebnis));
                }
            }
        });

        /**
         *
         */
        btn_aktualisieren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new MeinKursThread()).start();
                et_aktuellerkurs.setText(Double.toString(faktorBitcoinKursInEuro));
            }
        });

    }

    /**
     * Rechnet Euro in Bitcoin um.
     *
     * @param betragInEuro Betrag in Euro
     * @return betragInBitcoin
     */
    private double euroBitcoinUmrechnen(double betragInEuro) {
        return betragInEuro / faktorBitcoinKursInEuro;
    }

    /**
     * Rechnet Bitcoin in Euro um.
     *
     * @param betragInBitcoin Betrag in Bitcoin
     * @return betragInEuro
     */
    private double bitcoinEuroUmrechnen(double betragInBitcoin) {
        return faktorBitcoinKursInEuro * betragInBitcoin;
    }


    public class MeinKursThread implements Runnable {
        @Override
        public void run() {
            URLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL("https://bitaps.com/api/ticker/average");
                urlConnection = url.openConnection();
                inputStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String jsonZeile = null;
                String zeile = null;
                while ((zeile = br.readLine()) != null) {
                    jsonZeile = zeile;
                }
                JSONObject jsonObject = new JSONObject(jsonZeile);
                JSONObject fxrates = jsonObject.getJSONObject("fx_rates");
                faktorBitcoinKursInEuro = fxrates.getDouble("eur");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}

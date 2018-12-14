package com.a2stars.binaryrobot;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private LineChart mChart;
    private int winM;
    private double[] HistoryMean;
    private double[] HistoryMeanAux;
    private TimerTask task;
    private final Handler handler = new Handler();
    private Timer timerAtual = new Timer();
    private String sData = "";
    int mk = 0;
    final ArrayList<Entry> betDownValue = new ArrayList<>();
    final ArrayList<Entry> betUpValue = new ArrayList<>();
    private boolean betUpOne = true, betDownOne = true;
    private TextView amountYou, investmentTV, profitValue, profitPercent;
    private double balanceYou;
    private int investment;
    private double percent;
    private double profit;
    private double AssetValuePlus;
    private double AssetValueMinus;
    private double profitPlus;
    private double profitMinus;
    private double investmentPlus;
    private double investmentMinus;
    private Random genaratorPercent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        balanceYou = 10000;
        investment = 1;
        AssetValuePlus = 0;
        AssetValueMinus = 0;

        amountYou = findViewById(R.id.amountYou);
        investmentTV = findViewById(R.id.investmentTV);
        profitValue = findViewById(R.id.profitValue);
        profitPercent = findViewById(R.id.profitPercent);

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        String currency = format.format(balanceYou);
        amountYou.setText("V" + currency);

        genaratorPercent = new Random();
        percent = 70 + genaratorPercent.nextInt(25);
        profit = investment * ((percent + 100) / 100);
        currency = format.format(profit);
        profitValue.setText("+V" + currency);
        profitPercent.setText(percent + "%");

        client = new OkHttpClient();
        Request request = new Request.Builder().url("wss://ws.binaryws.com/websockets/v3?app_id=4257").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);

        // Real Value
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.getAxisRight().setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#F7F9F7"));

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setTextColor(Color.parseColor("#F7F9F7"));

        winM = 180;
        HistoryMean = new double[1];
        for (int i = 0; i < 1; i++) {
            HistoryMean[i] = 0;
        }

        task = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        update();
                    }
                });
            }
        };
        timerAtual.schedule(task, 10, 300);

        Button upbutton = findViewById(R.id.upbutton);
        upbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(investment != 0){
                    if (betUpOne && HistoryMeanAux.length < 120) {
                        if (investment < balanceYou) {
                            betUpValue.add(new Entry(-180, (float) HistoryMeanAux[HistoryMeanAux.length - 1]));
                            betUpValue.add(new Entry(0, (float) HistoryMeanAux[HistoryMeanAux.length - 1]));
                            betUpOne = false;
                            balanceYou = balanceYou - investment;
                            NumberFormat format2 = NumberFormat.getCurrencyInstance(Locale.US);
                            String currency2 = format2.format(balanceYou);
                            amountYou.setText("V" + currency2);
                            AssetValuePlus = HistoryMeanAux[HistoryMeanAux.length - 1];
                            profitPlus = profit;
                            investmentPlus = investment;
                        } else {
                            Toast.makeText(getApplicationContext(), "Insufficient funds", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Invest some value", Toast.LENGTH_LONG).show();
                }

            }
        });

        Button downbutton = findViewById(R.id.downbutton);
        downbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(investment != 0){
                    if (betDownOne && HistoryMeanAux.length < 120) {
                        if (investment < balanceYou) {
                            betDownValue.add(new Entry(-180, (float) HistoryMeanAux[HistoryMeanAux.length - 1]));
                            betDownValue.add(new Entry(0, (float) HistoryMeanAux[HistoryMeanAux.length - 1]));
                            betDownOne = false;
                            balanceYou = balanceYou - investment;
                            NumberFormat format2 = NumberFormat.getCurrencyInstance(Locale.US);
                            String currency2 = format2.format(balanceYou);
                            amountYou.setText("V" + currency2);
                            AssetValueMinus = HistoryMeanAux[HistoryMeanAux.length - 1];
                            profitMinus = profit;
                            investmentMinus = investment;
                        } else {
                            Toast.makeText(getApplicationContext(), "Insufficient funds", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Invest some value", Toast.LENGTH_LONG).show();
                }

            }
        });

        Button plusInvestment = findViewById(R.id.plusInvestment);
        plusInvestment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((investment + 5) <= balanceYou) {
                    if(investment == 1){
                        investment = investment + 4;
                    } else {
                        investment = investment + 5;
                    }
                    investmentTV.setText("" + investment);
                    profit = investment * ((percent / 100) + 1);
                    NumberFormat format2 = NumberFormat.getCurrencyInstance(Locale.US);
                    String currency2 = format2.format(profit);
                    profitValue.setText("+V" + currency2);
                    profitPercent.setText(percent + "%");
                }
            }
        });

        Button minusInvestment = findViewById(R.id.minusInvestment);
        minusInvestment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (investment > 1) {
                    if(investment == 5){
                        investment = investment - 4;
                    } else {
                        investment = investment - 5;
                    }
                    investmentTV.setText("" + investment);
                    profit = investment * ((percent / 100) + 1);
                    NumberFormat format2 = NumberFormat.getCurrencyInstance(Locale.US);
                    String currency2 = format2.format(profit);
                    profitValue.setText("+V" + currency2);
                    profitPercent.setText(percent + "%");
                }
            }
        });
    }

    private void update() {
        lerJson(sData);
    }

    public void lerJson(String json) {

        try {

            JSONObject bitJson = new JSONObject(json);
            JSONObject jsonQuery = bitJson.getJSONObject("tick");
            double comprar = jsonQuery.getDouble("quote");

            // Alimentando os vetores segundo o seu tamanho
            //Janela

            if (mk < 180) {
                HistoryMean[mk] = comprar;
                HistoryMeanAux = HistoryMean;
                mk++;
                HistoryMean = new double[mk + 1];
                for (int j = 0; j < mk; j++) {
                    HistoryMean[j] = HistoryMeanAux[j];
                }

            } else {
                mk = 59;
                HistoryMeanAux = HistoryMean;
                HistoryMean = new double[mk + 1];
                for (int j = 0; j < 59; j++) {
                    HistoryMean[j] = HistoryMeanAux[j + 119];
                }
                HistoryMean[mk] = comprar;
                HistoryMeanAux = HistoryMean;
                mk++;
                HistoryMean = new double[mk + 1];
                for (int j = 0; j < mk; j++) {
                    HistoryMean[j] = HistoryMeanAux[j];
                }
            }

            if(HistoryMeanAux.length == 120){
                if(AssetValuePlus == 0){
                    if(AssetValueMinus == 0){
                        mk = 59;
                        HistoryMeanAux = HistoryMean;
                        HistoryMean = new double[mk + 1];
                        for (int j = 0; j < 59; j++) {
                            HistoryMean[j] = HistoryMeanAux[j + 59];
                        }
                        HistoryMean[mk] = comprar;
                        HistoryMeanAux = HistoryMean;
                        mk++;
                        HistoryMean = new double[mk + 1];
                        for (int j = 0; j < mk; j++) {
                            HistoryMean[j] = HistoryMeanAux[j];
                        }

                        percent = 70 + genaratorPercent.nextInt(25);
                        profitPercent.setText(percent + "%");
                        profit = investment * ((percent + 100) / 100);
                        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                        String currency = format.format(profit);
                        profitValue.setText("+V" + currency);
                    }
                }
            }


            if (HistoryMeanAux.length == 180) {
                percent = 70 + genaratorPercent.nextInt(25);
                profitPercent.setText(percent + "%");
                profit = investment * ((percent + 100) / 100);
                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                String currency = format.format(profit);
                profitValue.setText("+V" + currency);
                if(AssetValuePlus != 0){
                    if(AssetValuePlus < HistoryMeanAux[HistoryMeanAux.length - 1]){
                        balanceYou = balanceYou + profitPlus;
                        AssetValuePlus = 0;
                        NumberFormat format2 = NumberFormat.getCurrencyInstance(Locale.US);
                        String currency2 = format2.format(balanceYou);
                        amountYou.setText("V" + currency2);
                    }
                    if(AssetValuePlus == HistoryMeanAux[HistoryMeanAux.length - 1]){
                        balanceYou = balanceYou + investmentPlus;
                        AssetValuePlus = 0;
                        NumberFormat format2 = NumberFormat.getCurrencyInstance(Locale.US);
                        String currency2 = format2.format(balanceYou);
                        amountYou.setText("V" + currency2);
                    }
                    AssetValuePlus = 0;
                }
                if(AssetValueMinus != 0) {
                    if (AssetValueMinus > HistoryMeanAux[HistoryMeanAux.length - 1]) {
                        balanceYou = balanceYou + profitMinus;
                        AssetValueMinus = 0;
                        NumberFormat format2 = NumberFormat.getCurrencyInstance(Locale.US);
                        String currency2 = format2.format(balanceYou);
                        amountYou.setText("V" + currency2);
                    }
                    if (AssetValueMinus == HistoryMeanAux[HistoryMeanAux.length - 1]) {
                        balanceYou = balanceYou + investmentMinus;
                        AssetValueMinus = 0;
                        NumberFormat format2 = NumberFormat.getCurrencyInstance(Locale.US);
                        String currency2 = format2.format(balanceYou);
                        amountYou.setText("V" + currency2);
                    }
                    AssetValueMinus = 0;
                }
                betUpValue.clear();
                betDownValue.clear();
                betUpOne = true;
                betDownOne = true;
            }

            //Plotar Gráficos
            int j = 0;
            ArrayList<Entry> MeanValue = new ArrayList<>();
            for (int i = (-winM + 1); i < (-winM + 1 + HistoryMeanAux.length); i++) {
                MeanValue.add(new Entry(i, (float) HistoryMeanAux[j]));
                j = j + 1;
            }

            double[] historySort = new double[HistoryMeanAux.length];
            for (int m = 0; m < HistoryMeanAux.length; m++) {
                historySort[m] = HistoryMeanAux[m];
            }
            Arrays.sort(historySort);
            ArrayList<Entry> limitInfValue = new ArrayList<>();
            limitInfValue.add(new Entry(-60, (float) historySort[historySort.length - 1]));
            limitInfValue.add(new Entry(-60, (float) historySort[0]));

            ArrayList<Entry> limitSupValue = new ArrayList<>();
            limitSupValue.add(new Entry(0, (float) historySort[historySort.length - 1]));
            limitSupValue.add(new Entry(0, (float) historySort[0]));

            ArrayList<Entry> limitSupValueAux = new ArrayList<>();
            limitSupValueAux.add(new Entry(1, (float) historySort[historySort.length - 1]));
            limitSupValueAux.add(new Entry(1, (float) historySort[0]));

            LineDataSet set1;
            set1 = new LineDataSet(MeanValue, "Asset value");
            set1.setColor(Color.WHITE);
            set1.setDrawCircles(false);
            set1.setValueTextColor(Color.WHITE);

            LineDataSet set2;
            set2 = new LineDataSet(limitInfValue, "Limit to bet");
            set2.setColor(Color.YELLOW);
            set2.setDrawCircles(false);
            set2.setValueTextColor(Color.WHITE);

            LineDataSet set3;
            set3 = new LineDataSet(limitSupValue, "Verification");
            set3.setColor(Color.RED);
            set3.setDrawCircles(false);
            set3.setValueTextColor(Color.WHITE);

            LineDataSet set4;
            set4 = new LineDataSet(limitSupValueAux, "");
            set4.setColor(Color.TRANSPARENT);
            set4.setDrawCircles(false);
            set4.setValueTextColor(Color.WHITE);

            LineDataSet set5;
            set5 = new LineDataSet(betUpValue, "");
            set5.setColor(Color.parseColor("#0FAA50"));
            set5.setDrawCircles(false);
            set5.setValueTextColor(Color.WHITE);

            LineDataSet set6;
            set6 = new LineDataSet(betDownValue, "");
            set6.setColor(Color.parseColor("#DF4738"));
            set6.setDrawCircles(false);
            set6.setValueTextColor(Color.WHITE);

            LineData data = new LineData(set1, set2, set3, set4, set5, set6);
            mChart.setData(data);
            mChart.setAutoScaleMinMaxEnabled(true);
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            mChart.setMaxVisibleValueCount(5);
            mChart.setBackgroundColor(Color.parseColor("#263147"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public OkHttpClient client;

    public final class EchoWebSocketListener extends WebSocketListener {
        public static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send("{\"ticks\": \"R_100\"}");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            sData = text;
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            Log.i("ws", "Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.e("ws", "Error : " + t.getMessage());
        }
    }
}

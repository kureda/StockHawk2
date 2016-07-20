package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.pojo.ClosePrice;
import com.sam_chordas.android.stockhawk.data.pojo.Quote;
import com.sam_chordas.android.stockhawk.data.pojo.Reply;
import com.sam_chordas.android.stockhawk.data.pojo.Results;
import com.sam_chordas.android.stockhawk.rest.StockApi;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ChartActivity extends AppCompatActivity {
    private static String LOG_TAG = Utils.class.getSimpleName();

    //parameters for reading historic stock data from site
    private static final String URL = "https://query.yahooapis.com";
    private static final String FORMAT = "json";
    private static final String ENV = "store://datatables.org/alltableswithkeys";
    private static final String CALLBACK = "";
    private static final String STOCK_NAME = "stock";
    public static final int TWO_MONTHS_AGO = 61;
    public static final int DISPLAY_ONE_OF_N_DATES = 6;
    public static final int LABELS_FONT_SIZE = 20;

    private int mMin, mMax, mStep;//for drawing axis

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//to display "home" on toolbar

        String stockName = getIntent().getStringExtra(STOCK_NAME);
        getSupportActionBar().setTitle(stockName.toUpperCase());
        drawHistoryGraph(stockName);
    }

    void drawHistoryGraph(String stockName) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        StockApi stockApi = retrofit.create(StockApi.class);
        Call<Reply> call = stockApi.getHistoryData(getQuery(stockName), FORMAT, ENV, CALLBACK);
        call.enqueue(new Callback<Reply>() {

            @Override
            public void onResponse(Call<Reply> call, Response<Reply> response) {
                List<Quote> quotes = getQuotes(response);
                List<ClosePrice> prices = toPrice(quotes);
                drawGraph(prices);
            }

            @Override
            public void onFailure(Call<Reply> call, Throwable t) {
                Log.e(LOG_TAG, t.toString(), t);
            }
        });
    }

    private List<ClosePrice> toPrice(List<Quote> quotes) {
        List<ClosePrice> result = new ArrayList<>();
        for (Quote quote : quotes) {
            result.add(new ClosePrice(quote.getClose(), quote.getDate()));
        }
        return result;
    }

    private List<Quote> getQuotes(Response<Reply> response) {
        List<Quote> quotes = new ArrayList<>();
        Results results = response.body().getQuery().getResults();
        if (results != null) { //non-existent stock name, for example
            quotes = results.getQuote();
        }
        return quotes;
    }

    private void drawGraph(List<ClosePrice> prices) {
        ChartView chartView = (ChartView) findViewById(R.id.linechart);
        Collections.reverse(prices);//to get them in chronological order
        LineSet points = new LineSet();

        points.setColor(ContextCompat.getColor(this, R.color.material_blue_500));//same as toobar
        int i=0;
        for (ClosePrice price : prices) {
            i++;
            String date = selectRoundDates(price.getDate(), i);
            points.addPoint(date, price.getValue());
        }
        chartView.addData(points);

        setMinMaxStep(prices);

        chartView.setXLabels(AxisController.LabelPosition.NONE);
        chartView.setXLabels(AxisController.LabelPosition.OUTSIDE);

        chartView.setAxisBorderValues(mMin, mMax, mStep);
        chartView.setAxisColor(Color.WHITE);
        chartView.setLabelsColor(Color.WHITE);
        chartView.setFontSize(LABELS_FONT_SIZE);
        chartView.show();
    }

    private String selectRoundDates(String date, int counter) {
        String result = date.substring(8)+"/"+date.substring(5,7);//2016-07-30 to 30/07
        if (counter % DISPLAY_ONE_OF_N_DATES != 0) { //display only one of N dates
            result = "";
        }
        return result;
    }


    //generate query for historical data
    private String getQuery(String stockName) {
        String start = getDate(TWO_MONTHS_AGO); //data for two months
        String end = getDate(1);//today
        String symbol = stockName;
        return "select * from yahoo.finance.historicaldata " +
                "where symbol = \"" + symbol + "\" " +
                "and startDate = \"" + start + "\" " +
                "and endDate = \"" + end + "\" ";
    }

    //generates date in format like "2016-05-24"
    private String getDate(int daysAgo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo);
        return dateFormat.format(cal.getTime());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMinMaxStep(List<ClosePrice> prices) {
        mMin = Math.round(Collections.min(prices).getValue());
        mMax = Math.round(Collections.max(prices).getValue());

        mStep = 1;
        int delta = mMax - mMin;
        while (delta > mStep) {
            mStep = mStep * 10;
        }
        mStep = mStep / 10;
        int max2 = mMin;
        while (max2 < mMax) {
            max2 = max2 + mStep;
        }

        //to have space at top and bottom
        mMax = max2 + mStep;
        mMin = mMin - mStep;
    }
}

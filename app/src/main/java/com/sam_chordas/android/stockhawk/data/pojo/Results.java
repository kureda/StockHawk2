package com.sam_chordas.android.stockhawk.data.pojo;

import java.util.ArrayList;
import java.util.List;

public class Results {

    private List<Quote> quote = new ArrayList<Quote>();

    public List<Quote> getQuote() {
        return quote;
    }

    public void setQuote(List<Quote> quote) {
        this.quote = quote;
    }

}

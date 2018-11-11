package com.rebataur.forexapp.data;

public class CurrencyData {
    public String name;
    public String rate;

    public CurrencyData(String name, String rate) {
        this.name = name;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}

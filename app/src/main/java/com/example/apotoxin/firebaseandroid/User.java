package com.example.apotoxin.firebaseandroid;

/**
 * Created by apotoxin on 10/12/2017 AD.
 */

public class User {
    private String name_;
    private String quote_;

    public User(String name_,String quote_){
        this.name_ = name_;
        this.quote_ = quote_;
    }

    public String getName_() {
        return name_;
    }

    public String getQuote_() {
        return quote_;
    }

    public void setName_(String name_) {
        this.name_ = name_;
    }

    public void setQuote_(String quote_) {
        this.quote_ = quote_;
    }
}

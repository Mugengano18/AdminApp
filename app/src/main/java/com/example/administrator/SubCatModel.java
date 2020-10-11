package com.example.administrator;

public class SubCatModel {
    private String id;
    private long name;


    public SubCatModel(String id, long name) {
        this.id = id;
        this.name = name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getName() {
        return name;
    }

    public void setName(long name) {
        this.name = name;
    }
}

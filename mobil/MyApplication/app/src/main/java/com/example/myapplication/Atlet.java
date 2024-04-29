package com.example.myapplication;

public class Atlet {
    private String name;
    private String country;

    private String age;

    private String disciplines;

    public Atlet(String name, String country, String age, String disciplines) {
        this.name = name;
        this.country = country;
        this.age = age;
        this.disciplines = disciplines;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }
    public String getAge() {
        return age;
    }

    public String getDisciplines() {
        return disciplines;
    }
}
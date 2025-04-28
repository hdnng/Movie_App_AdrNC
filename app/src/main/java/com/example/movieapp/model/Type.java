package com.example.movieapp.model;

import java.util.HashMap;

public class Type {
    public String idType;
    public String nameType;

    public Type() {
    }

    public Type(String idType, String nameType) {
        this.idType = idType;
        this.nameType = nameType;
    }
    //Phuong thuc xu ly du lieu thao tac voi FireBase
    public HashMap<String, Object> convertHashMap() {
        HashMap<String, Object> typelist = new HashMap<>();
        typelist.put("idType", idType);
        typelist.put("nameType", nameType);
        return typelist;
    }


    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getNameType() {
        return nameType;
    }

    public void setNameType(String nameType) {
        this.nameType = nameType;
    }

    // Trong lớp Type
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Type type = (Type) obj;
        return idType.equals(type.idType);
    }

    @Override
    public int hashCode() {
        return idType.hashCode();
    }
}

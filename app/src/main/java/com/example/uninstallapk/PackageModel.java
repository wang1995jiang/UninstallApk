package com.example.uninstallapk;

/**
 * Created by 王将 on 2018/7/30.
 */

public class PackageModel {
    private String packageName;
    private String packageLabel;
    private int id;

    public PackageModel(String packageName,String packageLabel,int id){
        this.packageName=packageName;
        this.packageLabel=packageLabel;
        this.id=id;
    }

    public int getId() {
        return id;
    }

    public String getPackageLabel() {
        return packageLabel;
    }

    public String getPackageName() {
        return packageName;
    }
}

package com.xy.shareme_tomcat.data;

import android.app.AlertDialog;
import android.content.Context;

import com.xy.shareme_tomcat.R;

import java.util.regex.Pattern;

public class Verifier {
    private Context c;

    private String lowerAccount = "8", upperAccount = "10";
    private String lowerPassword = "6", upperPassword = "15";
    private String lowerName = "2", upperName = "10";
    private String lowerTitle = "1", upperTitle = "50";
    private String lowerCondition = "1", upperCondition = "50";
    private String lowerPs = "0", upperPs = "50";

    private String ptnAccount = String.format("[a-zA-Z_0-9]{%s,%s}", lowerAccount, upperAccount);
    private String ptnPassword = String.format("[a-zA-Z_0-9]{%s,%s}", lowerPassword, upperPassword);
    private String ptnName = String.format("[\\u4e00-\\u9fa5a-zA-Z]{%s,%s}", lowerName, upperName);
    private String ptnEmail = "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$";
    private String ptnTitle = String.format("[\\u4e00-\\u9fa5a-zA-Z_0-9]{%s,%s}", lowerTitle, upperTitle);
    private String ptnPrice = "[0-9]";
    private String ptnCondition = String.format("[\\u4e00-\\u9fa5a-zA-Z_0-9]{%s,%s}", lowerCondition, upperCondition);;
    private String ptnPs = String.format("[\\u4e00-\\u9fa5a-zA-Z_0-9]{%s,%s}", lowerPs, upperPs);;


    public Verifier(Context context) {
        this.c = context;
    }

    public AlertDialog.Builder getDialog(String title, String msg) {
        return new AlertDialog.Builder(c)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("確定", null);
    }

    public String chkAccount(String s) {
        if (Pattern.matches(ptnAccount, s))
            return "";
        else
            return c.getString(R.string.chkAccount);
    }

    public String chkPassword(String pwd1, String pwd2) {
        if (!Pattern.matches(ptnPassword, pwd1))
            return c.getString(R.string.chkPassword);
        else if (!pwd1.equals(pwd2))
            return c.getString(R.string.chkPasswordMismatch);
        else
            return "";
    }

    public String chkName(String s) {
        if (Pattern.matches(ptnName, s))
            return "";
        else
            return c.getString(R.string.chkName);
    }

    public String chkEmail(String s) {
        if (Pattern.matches(ptnEmail, s))
            return "";
        else
            return c.getString(R.string.chkEmail);
    }

    public String chkTitle(String s) {
        if (Pattern.matches(ptnTitle, s))
            return "";
        else
            return c.getString(R.string.chkTitle);
    }

    public String chkPrice(String s) {
        if (Pattern.matches(ptnPrice, s))
            return "";
        else
            return c.getString(R.string.chkPrice);
    }

    public String chkCondition(String s) {
        if (Pattern.matches(ptnCondition, s))
            return "";
        else
            return c.getString(R.string.chkCondition);
    }

    public String chkPs(String s) {
        if (Pattern.matches(ptnPs, s))
            return "";
        else
            return c.getString(R.string.chkPs);
    }
}

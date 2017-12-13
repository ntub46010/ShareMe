package com.xy.shareme_tomcat;

import static com.xy.shareme_tomcat.MainActivity.board;
import static com.xy.shareme_tomcat.MainActivity.txtBarTitle;

public class DataHelper {
    public static boolean conFlag = false; //表示OKHttp的取資料工作結束，將流程交還原Activity進行後續操作
    public static String loginUserId = "";
    public static String myName = ""; //用於發送推播
    public static int myGender = -1; //用於發送icon，包含從信箱退出時的狀況
    public static String myAvatarUrl = ""; //用於發送推播
    public static String tmpToken = "token"; //登入時接收自己token(確認Firebase註冊成功)，或進交談室時接收對方token(確認可推播)。空字串代表無該帳號，token字串代表尚未取得

    public static final String KEY_STATUS = "Status";
    public static final String KEY_USER_ID = "UserId";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_NAME = "Name";
    //
    public static final String KEY_GENDER = "Gender";
    //
    public static final String KEY_AVATAR = "Avatar";
    //---
    public static final String KEY_PROFILE = "Profile";



    public static String getSpnDepCode (int position) {
        String depCode = "";
        switch (position) {
            case 0:
                depCode = "51";
                break;
            case 1:
                depCode = "52";
                break;
            case 2:
                depCode = "53";
                break;
            case 3:
                depCode = "54";
                break;
            case 4:
                depCode = "55";
                break;
            case 5:
                depCode = "56";
                break;
            case 6:
                depCode = "57";
                break;
            case 7:
                depCode = "41";
                break;
            case 8:
                depCode = "42";
                break;
            case 9:
                depCode = "43";
                break;
            case 10:
                depCode = "44";
                break;
            case 11:
                depCode = "45";
                break;
            case 12:
                depCode = "46";
                break;
            case 13:
                depCode = "47";
                break;
            case 14:
                depCode = "4A";
                break;
            case 15:
                depCode = "4B";
                break;
            case 16:
                depCode = "4C";
                break;
            case 17:
                depCode = "31";
                break;
            case 18:
                depCode = "32";
                break;
            case 19:
                depCode = "33";
                break;
            case 20:
                depCode = "34";
                break;
            case 21:
                depCode = "35";
                break;
            case 22:
                depCode = "36";
                break;
            case 23:
                depCode = "37";
                break;
            case 24:
                depCode = "3A";
                break;
            case 25:
                depCode = "3B";
                break;
            case 26:
                depCode = "3C";
                break;
        }
        return depCode;
    }

    public static void setBoardTitle() {
        switch (board) {
            case "-1":
                txtBarTitle.setText("全部");
                break;
            case "00":
                txtBarTitle.setText("通識");
                break;
            case "01":
                txtBarTitle.setText("會計資訊／會計統計");
                break;
            case "02":
                txtBarTitle.setText("財務金融");
                break;
            case "03":
                txtBarTitle.setText("財政稅務");
                break;
            case "04":
                txtBarTitle.setText("國際商務／國際貿易");
                break;
            case "05":
                txtBarTitle.setText("企業管理");
                break;
            case "06":
                txtBarTitle.setText("資訊管理");
                break;
            case "07":
                txtBarTitle.setText("應用外語");
                break;
            case "A":
                txtBarTitle.setText("商業設計管理");
                break;
            case "B":
                txtBarTitle.setText("商品創意經營");
                break;
            case "C":
                txtBarTitle.setText("數位多媒體設計");
                break;
        }
    }


}

package com.onthedeer.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.alibaba.fastjson.JSONObject;


public class ModifyJson{

    private static JSONObject jsonObject;

    private ModifyJson(){
		throw new IllegalStateException("Utility class");
    }
    
    static{
        String jsonRst = LoadJson.loadJsonFile("return.json","webapps/onthedeer/WEB-INF/return.json");
        jsonObject = JSONObject.parseObject(jsonRst);
    }

    public static boolean checkTimeFormat(String time){
        try{
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setLenient(false);
            format.parse(time);
        }catch(ParseException e){
            return false;
        }
        return true;
    }

    public static void switchCase(int result, int type, JSONObject back){

        String out =(result*7 + type)+"";

        back.put("message",jsonObject.getString(out));
    }
}
package br.com.rsa.folioreader.configuration;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rodrigo.almeida on 28/04/15.
 */
public class Configuration {
    private static Map<String, Object> object = new HashMap<String, Object>();

    public static Object getData(String key){
        return object.get(key);
    }

    public static void setData(String key, Object obj){
        object.put(key, obj);
    }

    public static void setButtonsBarColor(Color color) {
        object.put("folioreader-buttonsbarcolor", color);
    }

    public static void initConfiguration(){
        object.put("folioreader-buttonsbarcolor", Color.parseColor("#FF7900"));
    }
}

package br.com.rsa.folioreader.configuration;

import android.content.Context;
import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

import br.com.rsa.folioreader.R;

/**
 * Created by rodrigo.almeida on 28/04/15.
 */
public class Configuration {
    public static String COLOR_PANEL_BUTTONS = "folioreader-buttonsbarcolor";
    public static String COLOR_LIST_INDEX = "folioreader-list-indexcolor";
    public static String COLOR_LISTITEM_INDEX = "folioreader-listitem-indexcolor";
    /**
     * Keys to transfer data;
     */
    public static String PATH_DECOMPRESSED = "folioreader-pathpages";
    public static String BASE_URL = "folioreader-baseurl";

    private static Map<String, Object> object = new HashMap<String, Object>();

    public static Object getData(String key){
        return object.get(key);
    }

    public static void setData(String key, Object obj){
        object.put(key, obj);
    }

    public static void setButtonsBarColor(Color color) {
        object.put(COLOR_PANEL_BUTTONS, color);
    }

    public static void initConfiguration(Context context){
        object.put(COLOR_PANEL_BUTTONS, context.getResources().getColor(R.color.folioreader_panel_buttons));
        object.put(COLOR_LIST_INDEX, context.getResources().getColor(R.color.folioreader_list_index));
        object.put(COLOR_LISTITEM_INDEX, context.getResources().getColor(R.color.folioreader_list_item_index));
    }
}

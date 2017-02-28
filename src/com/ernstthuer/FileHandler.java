package com.ernstthuer;

import java.io.File;

/**
 * Created by ethur on 7/26/16.
 */
abstract class FileHandler {
    /**
     * Abstract class ??   maybe yes
     */


    private String locale;
    private String type;
    private String direction;
    private String feature;

    FileHandler(String locale, String type, String direction) {
        this.locale = locale;
        this.type = type;
        this.direction = direction;
    }

    FileHandler(String locale, String type, String direction, String feature) {
        this.locale = locale;
        this.type = type;
        this.direction = direction;
        this.feature = feature;
    }


    public String getLocale() {
        return locale;
    }

    public String getType() {
        return type;
    }

    String getDirection() {
        return direction;
    }

    public boolean isExistant() {
        File file = new File(this.locale);
        if (file.exists() && !file.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }


}
package com.ernstthuer;

/**
 * Created by ethur on 7/26/16.
 */
public class CSVHandler extends FileHandler {
    public CSVHandler(String locale, String type, String direction) {
        super(locale, type, direction);
    }

    public CSVHandler(String locale, String type, String direction, String feature) {
        super(locale, type, direction, feature);
    }
}

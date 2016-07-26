package com.ernstthuer;

/**
 * Created by ethur on 7/26/16.
 */
public class GFFHandler extends FileHandler {

    // GFF contain the gene positions, needed for fasta import, and classification



    public GFFHandler(String locale, String type, String direction, String feature) {
        super(locale, type, direction, feature);
    }
}

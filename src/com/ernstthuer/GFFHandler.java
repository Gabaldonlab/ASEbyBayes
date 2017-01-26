package com.ernstthuer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * Created by ethur on 7/26/16.
 */
public class GFFHandler extends FileHandler {

    // GFF contain the gene positions, needed for fasta import, and classification

    private String locale;
    private String type;
    private static String origin;
    private String direction;
    private String feature;
    private String[] lineList;
    private ArrayList<Gene> geneList = new ArrayList<>();
    //private ArrayList<String> lineList = new ArrayList<>();


    public GFFHandler(String locale, String type, String direction, String feature) {
        super(locale, type, direction);
        this.locale = locale;
        this.feature = feature;
        this.origin = null;
        this.direction = direction; // gff is always input anyways
        try {
            this.lineList = openGFF(locale);
        } catch (IOException e) {
            System.out.println("[ERROR] GFF file not found");
            System.out.println(e);
        }
        //System.out.println("Total features " + lineList.length);
        geneList = geneList(this.lineList);
        System.out.println("[STATUS] " + geneList.size() + "  " + this.feature + "s found " );// + geneList.get(0).getStart() + "  TEMP  " + geneList.get(0).getStop());
    }

    public String[] openGFF(String locale) throws IOException {
        ArrayList<String> outList = new ArrayList<String>();
        //System.out.println("This is where the file is " + direction);
        try (BufferedReader br = new BufferedReader(new FileReader(locale))) {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                outList.add(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] stockArr = new String[outList.size()];
        String[] linesOfFeatures = outList.toArray(stockArr);

        return linesOfFeatures;

    }


    public ArrayList<Gene> geneList(String[] featureList) {

        ArrayList<Gene> outList = new ArrayList<>();
        System.out.println("[STATUS] Parsing gff file for : " + this.feature + "s");
        for (int i = 0; i < featureList.length; i++) {
            if (!featureList[i].startsWith("#")) {
                String[] row = featureList[i].split("\t");
                if (row[2].equals(this.feature)) {

                    char orientation = row[6].charAt(0);
                    String description = descriptionParser(row[8]);
                    int start = parseInt(row[3]);  //start and stop position are read as String
                    int stop = parseInt(row[4]);

                    try {

                       /*orientation stored as + and -   always starts in lower half...
                       if (orientation == '+') {
                            orientation = "forward";
                        }
                        if (stop < start) {
                            // gene is on the reverse
                            orientation = "reverse";
                            int intermed = start;
                            start = stop;
                            stop = intermed;
                        }*/

                        //Gene newGene = new Gene(row[0], start, stop, description );

                        Gene gene = new Gene(row[0], start, stop, description);
                        gene.setOrientation(orientation);

                        outList.add(gene);

                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    //System.out.println(outList.size());//geneList.add(newGene);
                }
            }
            //System.out.println(outList.size());
        }
            return outList;

    }


    public String descriptionParser(String fullDescription) {

        String featureID;
        String[] desc = fullDescription.split(";");

        Pattern CGD = Pattern.compile("ID=*;");
        Pattern ENSEMBL = Pattern.compile("gene_id \"*\";");
        Pattern ENSEXP = Pattern.compile("hid=trf;");

        // Simple sniffer

        while (type == null) {
            for (String element : desc) {
                if (element.contains("ID")) {
                    type = "CGD";
                }
                if (element.contains("gene_id")) {
                    type = "ENSEMBL";
                }
                if (element.contains("gid")) {
                    type = "ENSEXP";
                }
                if (element.contains("geneID")) {
                    type = "ALTERNATIVE";
                    //System.out.println(" -> " + type);
                }
            }
        }
        System.out.println("[STATUS] Annotation file found type :" + type);

        switch (type) {
            case "CGD":
                for (String element : desc) {
                    if (element.contains("ID")) {
                        featureID = element.split("=")[1];
                        return featureID;

                    }
                }
                break;

            case "ENSEMBL":
                for (String element : desc) {
                    if (element.contains("gene_id")) {
                        featureID = element.split("\"")[1];
                        return featureID;
                    }
                }
                break;

            case "ALTERNATIVE":
                //System.out.println("casechoice = " + type);
                for (String element : desc) {
                    if (element.contains("ID")) {
                        featureID = element.split("\"")[1];
                        //System.out.println(featureID);
                        return featureID;
                    }
                }
                break;


        }
        return null;

    }

    public ArrayList<Gene> getGeneList() {
        return geneList;
    }
}



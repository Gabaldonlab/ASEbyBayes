package com.ernstthuer;

import org.biojava.nbio.core.sequence.DNASequence;
import org.slf4j.LoggerFactory;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by ethur on 7/26/16.
 */
public class FastaHandler extends FileHandler {

    private String locale;
    private String type;
    private String direction;
    private ArrayList<Gene> geneList;

    public FastaHandler(String locale, String type, String direction) {


        super(locale, type, direction);
        this.locale = locale;
        this.type = type;
        this.direction = direction;


    }

    public HashMap<String, DNASequence> readFasta(ArrayList<Gene> geneList) throws IOException {

        /**
         * Method supplied by biojava to read fasta file into hashMap
         *
         *I should read the nucleotide sequence into potential
         *
         */

        // check if chromosomes or genes were used as reference ....

        HashMap<String, DNASequence> fastaMap;


/** for testing purpose
        for (Gene gene : geneList) {
            System.out.println("inside Fasta parser : " + gene.getIdent() + "  " + gene.getStart());
        }
*/
        //fastaMap = FastaReaderHelper.readFastaDNASequence(file)

        try {
            File file = new File(this.locale);
            fastaMap = FastaReaderHelper.readFastaDNASequence(file);


            for (Gene gene : geneList) {
                if (fastaMap.containsKey(gene.getChromosome())) {
                    //it's on the chromosome
                    //System.out.println(" Found chromosome " +gene.getChromosome());
                    gene.loadSequence(fastaMap.get(gene.getChromosome()), true);
                    //System.out.println("populating genes");

                } else {
                    //System.out.println("not " + gene.getChromosome());

                    // full chromosome,  pass to gene for processing
                    gene.loadSequence(fastaMap.get(gene.getChromosome()),false);

                }
            }
        /*    for (Map.Entry<String, DNASequence> entry : fastaMap.entrySet()) {
                System.out.println(entry.getValue().getOriginalHeader());
            }*/
            return fastaMap;
        } catch (IOException e) {
            e.printStackTrace();
            fastaMap = null;
            return fastaMap;
        }
    }

}

package com.ernstthuer;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by ethur on 11/3/16.
 */
public class BamThreader {
    /**
     *
     *  Threading to a seperate Class,
     * should facilitate easier JUnit testing
     *
     */

    private ArrayList<BamHandler> bamfiles = new ArrayList<>();
    private ArrayList<Gene> geneArrayList;
    private ArrayList<ArrayList<Gene>> listOfGenesFromThreads = new ArrayList<>();

    public BamThreader(ArrayList<BamHandler> bamfiles, ArrayList<Gene> geneArrayList) {
        this.bamfiles = bamfiles;
        this.geneArrayList = geneArrayList;
        this.listOfGenesFromThreads = cloneGeneLists();
        loadBamDataviaThreading();

    }

    public ArrayList<ArrayList<Gene>> cloneGeneLists () {
        ArrayList<ArrayList<Gene>> localListOfGenesFromThreads = new ArrayList<>();
        for(int i = 0; i < bamfiles.size() ; i++) {
            ArrayList<Gene> individualGeneList = new ArrayList<Gene>(geneArrayList.size());
            for (Gene gene : geneArrayList) {
                // cloning the genes.  ToDo check for other possibilities of deep copies
                try {
                    Object clonedGene = gene.clone();
                    individualGeneList.add((Gene) clonedGene);
                } catch (CloneNotSupportedException e) {
                    System.out.println("[ERROR] Cloning of gene List for threading not supported,  run application with thread = 1 ");
                }
            }
            localListOfGenesFromThreads.add(individualGeneList);
        }
        return localListOfGenesFromThreads;
    }


    public void loadBamDataviaThreading(){
        int count = 0;
        for(BamHandler bhdlr:bamfiles){
            ArrayList<Gene> temporaryGeneList = listOfGenesFromThreads.get(count);
            bhdlr.setGeneList(temporaryGeneList);
            bhdlr.run();

            //Thread thread = new Thread(bhdlr); // using runnable instead
            //thread.start();
            //threads.add(thread);

            bhdlr.getGeneList();
            System.out.println("Bam Loading complete on file " + bhdlr.getLocale());
            count += 1 ;
        }


    }


    public ArrayList<Gene> unifyGeneLists () {


        return null;
    }


    /** ToDo implement Threading


    for (FileHandler file : parser.fileList) {
        if (file.getType().equals("Bam") && file.getDirection().equals("Input")) {
            //make them implement Runnable   ... later
            System.out.println("[STATUS] Loading BAM file " + file.getLocale());

            //Cloner cloner=new Cloner();
            try {
                if (fasta != null) {
                    BamHandler bhdlr = new BamHandler(file.getLocale(), "Bam", "Input");
                    // to loosen this for threading should create copies of the genelists
                    //bamList.add(bhdlr);

                    // deep clone this list
                    ArrayList<Gene> individualGeneList = new ArrayList<Gene>(geneList.size());
                    for(Gene gene:geneList) {
                        // cloning the genes.
                        Object clonedGene = gene.clone();
                        individualGeneList.add((Gene) clonedGene);
                    }

                    bhdlr.setGeneList(individualGeneList);

                    Thread thread = new Thread(bhdlr);
                    thread.start();
                    threads.add(thread);
                    bamList.add(bhdlr);


                    //bhdlr.readBam(fasta, parser.isExistingSNPinfo());
                }
            } catch (Exception e) {
                errorCaller(e);
                //fasta = null;
            }
        }
    }
    */
    /**  ToDo move everything related to the threads to the BamThreader ...

     for(Thread thread:threads){
     try {
     System.out.println(thread.getName() + " thread joined ");
     thread.join();
     }catch (InterruptedException e){
     errorCaller(e);
     }
     }

     // unify te obtained gene lists
     for(BamHandler bhdlr:bamList){
     ArrayList<Gene> individualGeneList = bhdlr.getGeneList();
     unifyGeneLists(individualGeneList);
     }

     /*

     /*
     for(List<Gene> geneList : listOfGeneLists) {
     System.out.println("found genes in geneLists " + geneList.size());
     for(Gene gene:geneList){
     System.out.println("Gene with " + gene.snpsOnGene.size());

     }
     }

     */
}

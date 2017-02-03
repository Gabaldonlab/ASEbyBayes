package com.ernstthuer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BamThreader {
    /**
     * Threading to a separate Class,
     * should facilitate easier JUnit testing
     */

    private ArrayList<BamHandler> bamfiles = new ArrayList<>();
    private ArrayList<Gene> geneArrayList;
    private ArrayList<Gene> outputGeneArrayList;
    private ArrayList<ArrayList<Gene>> listOfGenesFromThreads = new ArrayList<>();
    private int poolSize;

    public BamThreader(ArrayList<BamHandler> bamfiles, ArrayList<Gene> geneArrayList, int poolSize) {
        this.bamfiles = bamfiles;
        this.geneArrayList = geneArrayList;
        this.listOfGenesFromThreads = cloneGeneLists();
        this.poolSize = poolSize;

        try {
            loadBamDataViaThreading();
        } catch (InterruptedException e) {
            System.out.println("[ERROR] Thread loading interrupted");
            System.out.print(e);
        } catch (ExecutionException exec) {
            System.out.println("[ERROR] Execution of Threading disrupted ");
            System.out.print(exec);
        }

        outputGeneArrayList = unifyGeneLists();


    }

    public ArrayList<ArrayList<Gene>> cloneGeneLists() {
        ArrayList<ArrayList<Gene>> localListOfGenesFromThreads = new ArrayList<>();
        for (int i = 0; i < bamfiles.size(); i++) {
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


    public void loadBamDataViaThreading() throws ExecutionException, InterruptedException {
        int count = 0;
        //int poolSize = 10;
        ExecutorService service = Executors.newFixedThreadPool(poolSize);
        List<Future<ArrayList<Gene>>> futures = new ArrayList<>();

        for (BamHandler bhdlr : bamfiles) {
            ArrayList<Gene> temporaryGeneList = listOfGenesFromThreads.get(count);
            bhdlr.setGeneList(temporaryGeneList);
            Future<ArrayList<Gene>> f = service.submit(bhdlr);

            futures.add(f);

            System.out.println("[STATUS] Bam Loading complete on file " + bhdlr.getLocale());
            count += 1;
        }

        count = 0;
        for (Future<ArrayList<Gene>> f : futures) {

            try {
                ArrayList<Gene> geneListFromBamHandler = f.get();
                for (Gene gene : geneListFromBamHandler) {
                    gene.findORGCoverageOfSNPs();
                }
                this.listOfGenesFromThreads.set(count, geneListFromBamHandler);
                count += 1;
            } catch (NullPointerException nullpoint) {
                System.out.println(nullpoint);
            }

        }

        service.shutdownNow();

    }


    public ArrayList<Gene> unifyGeneLists() {

        ArrayList<Gene> outputGeneArrayList = new ArrayList<>();

        for (ArrayList<Gene> geneListSubset : listOfGenesFromThreads) {


            for (Gene gene : geneListSubset) {

                if (outputGeneArrayList.contains(gene)) {

                    int indexOf = outputGeneArrayList.indexOf(gene);
                    Gene orgGene = outputGeneArrayList.get(indexOf);

                    orgGene.unifySNPLists(gene.getSnpsOnGene());

                } else {
                    outputGeneArrayList.add(gene);
                }
            }
        }

        return outputGeneArrayList;
    }

    public ArrayList<Gene> getOutputGeneArrayList() {
        return outputGeneArrayList;
    }


}

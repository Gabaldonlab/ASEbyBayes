package com.ernstthuer;


import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


class FastaSilencer {
    /**
     * Invokes a set of methods to modify fasta sequences.
     * <p>
     * needs all SNIPs in Array,  and the fasta sequence
     * <p>
     * <p>
     * could also transfer SNPs to genes
     */

    FastaSilencer(ArrayList<SNP> snips, HashMap<String, DNASequence> fasta, String local) {

        // find positions on the fasta, then silence and write to output

        for (String i : fasta.keySet()) {

            DNASequence toSilence = fasta.get(i);
            StringBuilder buildStringFasta = new StringBuilder(toSilence.getSequenceAsString());

            // find all SNPs on that sequence in an ArrayList subset,  then change the positions to 'N'

            Iterator<SNP> snpIterator = snips.iterator();

            while (snpIterator.hasNext()) {
                SNP snp = snpIterator.next();
                try {
                    if (snp.getGene().getChromosome().equals(i)) {
                        int position = snp.getPosition();
                        buildStringFasta.setCharAt(position - 1, 'N');
                    }

                } catch (NullPointerException e) {
                    System.out.println(e);
                }
            }

            try {
                DNASequence dna = new DNASequence(buildStringFasta.toString());
                dna.setDescription(i);

                //System.out.println(dna.getLength());

                fasta.put(i, dna);

            } catch (CompoundNotFoundException e) {
                System.out.println(e);
            }

        }

        try {
            BufferedOutputStream outFast = new BufferedOutputStream(new FileOutputStream(local));
            FastaWriterHelper.writeNucleotideSequence(outFast, fasta.values());  //(outFast, i);
            System.out.println("[STATUS] Writing Silenced fasta sequence of SNPs to file : " + local);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}


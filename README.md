# ASEbyBayes

Due to restructuring, currently set back to pre-alpha version 0.2

ASEbyBayes is a flexible tool to estimate Allele Specific Expression from RNAseq data.
The analysis is based on the measurement of individual SNPs. SNPs can either be provided as a vcf file or are called directly by the software. The original interest in the development lay with the analysis of nonmodel yeast species. 

The process implemented by the software follows the assumption that the system is unregulated. The priors are modeled according to this expectation to be either weak bimodal to model the noise derived from RNAseq data, or strong central informative to model unregulated 0.5 ratio transcription of a hypothetical unregulated system. The true observations are evaluated against those assumptions.







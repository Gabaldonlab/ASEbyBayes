# ASEbyBayes

Due to restructuring, currently set back to pre-alpha version 0.22

ASEbyBayes is a flexible tool to estimate Allele Specific Expression from RNAseq data.
The analysis is based on the measurement of individual SNPs. SNPs can either be provided as a vcf file or are called directly by the software. The original interest in the development lay with the analysis of nonmodel yeast species. More specifically hybrids 
The process implemented by the software follows the assumption that the system is unregulated, and Allele specific expression is manifests as a pattern, that cannot be explained by the expected variation. The priors are modeled according to this expectation to be either weak bimodal to model the noise derived from RNAseq data, or strong central informative to model unregulated 0.5 ratio transcription of a hypothetical unregulated system. The true observations are evaluated against those assumptions.

Reads are assumed to follow a negative binomial distribution. To model our primers we used the conjugate Beta distribution. With preset weak bimodial primers for noise detection and strong central informative primers for the assumption of equal allelic expression. Classification occurs via evaluation of posteriors, by defining credible regions following the above assumptions.



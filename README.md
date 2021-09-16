# SIQ
Sequence Interrogation and Qualification is there to perform analysis on sequencing data on user-defined DNA

It is written in Java so that anyone with a computer can use this program to analyse their sequences.

We have designed and implemented this tool for anyone without a background in informatics to be able to analyse NGS data.

Contents
========

 * [Installation](#installation)
 * [Running SIQ](#running)
 * [Troubleshooting](#troubleshooting)

### Installation

* latest Java .jar file from this repository
* FLASH

Download the latest .jar file from this repository. You can double click it and it should work directly. The only thing you require is a working Java version (1.8 and up). 

If you also want to be able to merge NGS reads, please download FLASH (https://sourceforge.net/projects/flashpage/) for Windows or for Linux (https://github.com/dstreett/FLASH2).

### Running

when SIQ is started you should see the following screen:


SIQ can be used to analyze the following type of data:

* Sanger sequences (.ab1 files)
* Illumina single and paired-end sequence data (.fastq or .fastq.gz files)
* PacBio data (.fastq or .fastq.gz files)

When setting up your targeted sequencing experiments you generally use two primers to amplify your locus of interest. Additionally, you (optionally) have expected target sites for your experiments. This is for example the case if you use CRISPR\Cas9, Cas13, I-SceI or any other enzyme to target the DNA. We have also used SIQ to analyze different sites, such as transposon sites, sites of G-quadruplex sequences. SIQ uses these locations to 1) try to identify sequence alterations preferably at this location. 2) to ensure your PCR primers amplified from the expected location (see [Filters](#filters) below).

### Filters

### Troubleshooting









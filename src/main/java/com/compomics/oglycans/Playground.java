package com.compomics.oglycans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Playground {

    public static void main(String[] args) {

        try {
            FastaReader fastaReader = new FastaReader();
            fastaReader.readFasta(new File("/home/niels/Desktop/fastas/contaminants.fasta"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

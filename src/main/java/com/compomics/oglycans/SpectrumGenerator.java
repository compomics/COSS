package com.compomics.oglycans;

import com.compomics.ms2io.model.*;
import com.compomics.ms2io.controller.*;
import com.compomics.util.AtomMass;
import com.compomics.util.experiment.biology.proteins.Peptide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Genet
 */
public class SpectrumGenerator {

    private List<Integer> precursorCharges;
    private List<Integer> fragmentIonCharges;

    public SpectrumGenerator() {
        precursorCharges = new ArrayList<>();
        precursorCharges.add(2);
        precursorCharges.add(4);
        precursorCharges.add(5);

        fragmentIonCharges = new ArrayList<>();
        fragmentIonCharges.add(1);
        fragmentIonCharges.add(2);
    }

    public void generateSpectra(List<Peptide> peptides, File mgf_file) throws IOException {
        SpectraWriter spw = new MspWriter(mgf_file);

        ArrayList<Peak> peaks = new ArrayList<>();
        ArrayList<Peak> decoyPeaks = new ArrayList<>();
        for (Peptide peptide : peptides) {

            // get fragment ions of the peptide
            PeptideIons peptideIons = new PeptideIons(peptide, false);
            List<Double> ions = peptideIons.generateIons(fragmentIonCharges);

            // peak list
            for (Double d : ions) {
                Peak peak = new Peak(d, 600, "");
                peaks.add(peak);
            }

            String mod_str = "";
            for (Map.Entry<Integer, Modification> entry : peptideIons.getModification().entrySet()) {
                mod_str += entry.getValue().getModificationPosition() + "," + entry.getValue().getModifiedAA() + "," + entry.getValue().getModificationMassShift() + "\\";
            }
            if (mod_str != "") {
                mod_str = mod_str.substring(0, mod_str.length() - 1);
            } else {
                mod_str = "0";
            }

            // make a spectrum for each precursor charge
            for (Integer precursorCharge : precursorCharges) {
                Spectrum spectrum = new Spectrum();
                spectrum.setComment("Comment: " + "Parent=" + ((peptide.getMass() / precursorCharge) + precursorCharge * AtomMass.getAtomMass("H1")) + " " + "Mods=" + mod_str);
                spectrum.setMW(0);
                spectrum.setSequence(peptide.getSequence());
                spectrum.setPCMass(peptide.getMass());
                spectrum.setTitle(peptide.getSequence() + "/" + precursorCharge);
                spectrum.setNumPeaks(ions.size());
                spectrum.setPeakList(peaks);
                spectrum.setCharge(precursorCharge.toString());
                spw.write(spectrum);
            }


            // Generate decoy fragment ions from reverse sequence
            // reverse sequence except the last aa
            char[] tempSequence = peptide.getSequence().substring(0, peptide.getSequence().length() - 1).toCharArray();
            ArrayUtils.reverse(tempSequence);
            String reverseSequence = new String(tempSequence);
            //add last aa to reversed sequence
            reverseSequence += peptide.getSequence().charAt(peptide.getSequence().length() - 1);

            //decoy_pep=peptide;
            peptide.setSequence(reverseSequence);
            //decoy_pep = new Peptide(rev_sequence);
            // TODO ask Genet how to do this
            PeptideIons decoyPeptideIons = new PeptideIons(peptide, true);

            List<Double> decoyIons = decoyPeptideIons.generateIons(fragmentIonCharges);
            Collections.sort(decoyIons);

            //peak list of decoy
            for (Double dd : decoyIons) {
                Peak decoyPeak = new Peak(dd, 600, "");
                decoyPeaks.add(decoyPeak);
            }


            Spectrum decoySpectrum = new Spectrum();
            mod_str = "";
            for (Map.Entry<Integer, Modification> entry : decoyPeptideIons.getModification().entrySet()) {
                mod_str += entry.getValue().getModificationPosition() + "," + entry.getValue().getModifiedAA() + "," + entry.getValue().getModificationMassShift() + "\\";

            }
            if (mod_str != "") {
                mod_str = mod_str.substring(0, mod_str.length() - 1);
            } else {
                mod_str = "0";
            }

            for (Integer precursorCharge : precursorCharges) {
                decoySpectrum.setComment("Comment: " + "Parent=" + ((peptide.getMass() / precursorCharge) + precursorCharge * AtomMass.getAtomMass("H1")) + " " + "Mods=" + mod_str + " _decoy");
                decoySpectrum.setMW(0);
                decoySpectrum.setSequence(reverseSequence);
                decoySpectrum.setPCMass(peptide.getMass());
                decoySpectrum.setTitle(reverseSequence + "/" + precursorCharge);
                decoySpectrum.setNumPeaks(decoyIons.size());
                decoySpectrum.setPeakList(decoyPeaks);
                decoySpectrum.setCharge(precursorCharge.toString());
                spw.write(decoySpectrum);
            }

            peaks.clear();
            decoyPeaks.clear();
            ions.clear();
            decoyIons.clear();

        }
        spw.closeWriter();

    }

}

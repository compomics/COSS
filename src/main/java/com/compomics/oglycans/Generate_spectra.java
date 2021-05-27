package com.compomics.oglycans;

import com.compomics.ms2io.model.*;
import com.compomics.ms2io.controller.*;
import com.compomics.util.experiment.biology.proteins.Peptide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Genet
 */
public class Generate_spectra {

    public Generate_spectra() {
    }

    public void start(List<Peptide> peptides, File mgf_file) throws IOException {
        FragmentIon_glycan frag;
        FragmentIon_glycan frag_decoy;
        Spectrum spec = new Spectrum();
        Spectrum spec_decoy = new Spectrum();
        SpectraWriter spw = new MspWriter(mgf_file);
        Peak pk;
        ArrayList<Peak> peaks = new ArrayList<>();

        Peak pk_d;
        Peptide decoy_pep;
        ArrayList<Peak> peaks_d = new ArrayList<>();
        ArrayList<Double> mz_values;
        ArrayList<Double> mz_values_decoy;
        String mod_str;

        for (Peptide peptide : peptides) {
            //get fragment ion of the peptide

            frag = new FragmentIon_glycan(peptide, false);

            mz_values = frag.getFragmentIon();

            //peak list
            for (Double d : mz_values) {
                pk = new Peak(d, 600, "");
                peaks.add(pk);
            }

            mod_str = "";
            for (Map.Entry<Integer, Modification> entry : frag.getModification().entrySet()) {
                mod_str += entry.getValue().getModificationPosition() + "," + entry.getValue().getModifiedAA() + "," + entry.getValue().getModificationMassShift() + "\\";

            }
            if (mod_str != "") {
                mod_str = mod_str.substring(0, mod_str.length() - 1);
            } else {
                mod_str = "0";
            }

            spec = new Spectrum();
            spec.setComment("Comment: " + "Parent=" + peptide.getMass() + " " + "Mods=" + mod_str);
            spec.setMW(0);
            spec.setSequence(peptide.getSequence());
            spec.setPCMass(peptide.getMass());
            spec.setTitle(peptide.getSequence());
            spec.setNumPeaks(mz_values.size());
            spec.setPeakList(peaks);
            spw.write(spec);

            
            
            
            //Generatin decoy fragment ions from reverse sequence
            //reverse sequence except the last aa             
            char[] tempseq = peptide.getSequence().substring(0, peptide.getSequence().length() - 1).toCharArray();
            ArrayUtils.reverse(tempseq);
            String rev_sequence = new String(tempseq);
            //add last aa to reversed sequence
            rev_sequence += peptide.getSequence().charAt(peptide.getSequence().length() - 1);

            //decoy_pep=peptide;
            peptide.setSequence(rev_sequence);
            //decoy_pep = new Peptide(rev_sequence);
            // TODO ask Genet how to do this
            frag_decoy = new FragmentIon_glycan(peptide, true);
            
            mz_values_decoy = frag_decoy.getFragmentIon();
            //peak list of decoy
            for (Double dd : mz_values_decoy) {
                pk_d = new Peak(dd, 600, "");
                peaks_d.add(pk_d);
            }


            spec_decoy = new Spectrum();
            mod_str = "";
            for (Map.Entry<Integer, Modification> entry : frag_decoy.getModification().entrySet()) {
                mod_str += entry.getValue().getModificationPosition() + "," + entry.getValue().getModifiedAA() + "," + entry.getValue().getModificationMassShift() + "\\";

            }
            if (mod_str != "") {
                mod_str = mod_str.substring(0, mod_str.length() - 1);
            } else {
                mod_str = "0";
            }
            spec_decoy.setComment("Comment: " + "Parent=" + peptide.getMass() + " " + "Mods=" + mod_str + " _decoy");
            spec_decoy.setMW(0);
            spec_decoy.setSequence(rev_sequence);
            spec_decoy.setPCMass(peptide.getMass());
            spec_decoy.setTitle(rev_sequence);
            spec_decoy.setNumPeaks(mz_values_decoy.size());
            spec_decoy.setPeakList(peaks_d);
            spw.write(spec_decoy);

            peaks.clear();
            peaks_d.clear();
            mz_values.clear();
            mz_values_decoy.clear();

        }
        spw.closeWriter();

    }

}

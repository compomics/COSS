/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.preprocessing;


/**
 *
 * @author Genet
 */
public interface ITransform {
     /**
     * This method transforms intensities on a spectrum 

     * @param tr an enum Object of Transformations
     */
    public void transform(EnTransform tr);
    
}

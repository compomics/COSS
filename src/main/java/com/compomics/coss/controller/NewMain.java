/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller;

/**
 *
 * @author Genet
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("startRunning");
        String[] ip ={"23"};// {"C:/pandyDS/SpecA.msp", "C:/pandyDS/SpecB.msp"};
        MainConsolControler instance = new MainConsolControler();
        instance.startRunning(ip);
    }
    
}

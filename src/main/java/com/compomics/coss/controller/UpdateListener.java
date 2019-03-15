/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.controller;

/**
 *an interface to to listen to updates
 * @author Genet
 */
public interface UpdateListener {
    void updateprogress(int value, double percent);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

import java.io.Serializable;

/**
 *
 * @author Castor
 */
public class ActionEffectuer implements Serializable {
    public int numeroCaseChoisi;
    public int numeroCasePionCapturer = -1;
    public int numeroNouvelleCase;
    
    public ActionEffectuer(int numeroCaseChoisi, int numeroCasePionCapturer, int numeroNouvelleCase){
        this.numeroCaseChoisi = numeroCaseChoisi;
        this.numeroCasePionCapturer = numeroCasePionCapturer;
        this.numeroNouvelleCase = numeroNouvelleCase;
    }
    
    public ActionEffectuer(){ }
    
    @Override
    public String toString(){
        return "(" + this.numeroCaseChoisi + "," + this.numeroCasePionCapturer + "," + this.numeroNouvelleCase + ")";
    }
}

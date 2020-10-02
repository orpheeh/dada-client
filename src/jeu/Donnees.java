/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

/**
 *
 * @author Castor
 */
public class Donnees {
    public final int PLATEAU_X;
    public final int PLATEAU_Y;
    public final int TAILLE_PLATEAU;

    public Donnees(int x, int y, int taille){
        PLATEAU_X = x;
        PLATEAU_Y = y;
        TAILLE_PLATEAU = taille;
    }
}

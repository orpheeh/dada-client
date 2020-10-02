/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;
import dame.*;
import java.awt.Graphics2D;
/**
 *
 * @author Castor
 */
public abstract class Equipe {
    protected final Joueur joueur;
    protected int nombrePionCapture;
    
    public Equipe(int sensDeplacement, int nombrePions, char couleur, Damier damier){
        joueur = new Joueur(sensDeplacement, nombrePions, couleur, damier);
    }
    
    public Joueur getJoueur(){
        return joueur;
    }
    
    public int getNombrePionCapture(){
        return nombrePionCapture;
    }
    
    public abstract boolean jouer();
    public abstract void dessinerMarques(Graphics2D g2d);
}

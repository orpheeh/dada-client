/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

import dame.Damier;
import dame.Joueur;
import java.awt.Graphics2D;
import java.util.LinkedList;
import cacao.util.Utile;

/**
 *
 * @author Castor
 */
public class EquipeDistante extends Equipe {

    private LinkedList<ActionEffectuer> pileAction = new LinkedList<>();
    private int compteur;
    private boolean attend = true;
    private final Damier damier;
    private Joueur joueurAdverse;
    private int nbAction;
    
    public EquipeDistante(int sensDeplacement, int nombrePions, char couleur, Damier damier) {
        super(sensDeplacement, nombrePions, couleur, damier);
        this.damier = damier;
    }

    public void getJoueurAdverse(Joueur joueur) {
        this.joueurAdverse = joueur;
    }

    @Override
    public boolean jouer() {
        if (!attend) {
            System.out.println("CPU joue");
            Utile.sleepApplication(1000L);
            ActionEffectuer action = pileAction.get(nbAction);
            effectuerAction(action);
            nbAction++;
            if(nbAction >= pileAction.size()){
                attend = true;
                return false;
            }
        }
        return true;
    }

    @Override
    public void dessinerMarques(Graphics2D g2d) {
    }

    public void getAction(LinkedList<ActionEffectuer> pileAction) {
        this.pileAction = pileAction;
        attend = false;
        nbAction = 0;
    }

    private void effectuerAction(ActionEffectuer action) {
        joueur.deplacer(damier.getCase(action.numeroCaseChoisi).getNumeroPion(), damier.getCase(action.numeroNouvelleCase));
        if (action.numeroCasePionCapturer >= 0) {
            joueurAdverse.retirerPion(action.numeroCasePionCapturer);
            nombrePionCapture++;
        }
    }
}

package entity;

public final class CompteEpargne extends Compte {

    private final double tauxInteret;

    public CompteEpargne(Long id, String numero, double solde, Long idClient, double tauxInteret) {
        super(id, numero, solde, idClient);
        this.tauxInteret = tauxInteret;
    }

    public double getTauxInteret() {
        return tauxInteret;
    }

    public double calculerInterets() {
        return getSolde() * tauxInteret / 100;
    }
}
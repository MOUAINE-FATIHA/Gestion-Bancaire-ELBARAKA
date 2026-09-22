package entity;

public final class CompteCourant extends Compte {

    private final double decouvertAutorise;

    public CompteCourant(Long id, String numero, double solde, Long idClient, double decouvertAutorise) {
        super(id, numero, solde, idClient);
        this.decouvertAutorise = decouvertAutorise;
    }

    public double getDecouvertAutorise() {
        return decouvertAutorise;
    }

    public boolean estDecouvert() {
        return getSolde() < 0;
    }
}
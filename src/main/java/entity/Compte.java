package entity;

public sealed abstract class Compte permits CompteCourant, CompteEpargne {

    private final Long id;
    private final String numero;
    private double solde;
    private final Long idClient;

    protected Compte(Long id, String numero, double solde, Long idClient) {
        this.id = id;
        this.numero = numero;
        this.solde = solde;
        this.idClient = idClient;
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public Long getIdClient() {
        return idClient;
    }
}
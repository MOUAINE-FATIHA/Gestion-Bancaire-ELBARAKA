package service;
import dao.CompteDAO;
import dao.TransactionDAO;
import entity.Compte;
import entity.Transaction;
import enums.TypeTransaction;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionService {

    private static final double SEUIL_MONTANT_SUSPECT = 10_000.0;

    private final TransactionDAO transactionDAO;
    private final CompteDAO compteDAO;

    public TransactionService(TransactionDAO transactionDAO, CompteDAO compteDAO) {
        this.transactionDAO = transactionDAO;
        this.compteDAO = compteDAO;
    }

    public Transaction enregistrer(LocalDate date, double montant, TypeTransaction type, String lieu, Long idCompte) {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif.");
        }
        return transactionDAO.create(new Transaction(null, date, montant, type, lieu, idCompte));
    }

    public List<Transaction> listerParCompte(Long idCompte) {
        return transactionDAO.findByCompte(idCompte).stream()
                .sorted(Comparator.comparing(Transaction::date))
                .collect(Collectors.toList());
    }
    public List<Transaction> listerParClient(Long idClient) {
        List<Long> idsComptes = compteDAO.findByClient(idClient).stream()
                .map(Compte::getId)
                .toList();

        return transactionDAO.findAll().stream()
                .filter(t -> idsComptes.contains(t.idCompte()))
                .sorted(Comparator.comparing(Transaction::date))
                .collect(Collectors.toList());
    }
    public List<Transaction> filtrer(Double montantMin, TypeTransaction type, LocalDate date, String lieu) {
        return transactionDAO.findAll().stream()
                .filter(t -> montantMin == null || t.montant() >= montantMin)
                .filter(t -> type == null || t.type() == type)
                .filter(t -> date == null || t.date().equals(date))
                .filter(t -> lieu == null || t.lieu().equalsIgnoreCase(lieu))
                .collect(Collectors.toList());
    }

    public Map<TypeTransaction, List<Transaction>> grouperParType() {
        return transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::type));
    }

    public Map<YearMonth, List<Transaction>> grouperParPeriode() {
        return transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.date())));
    }

    public double moyenneParCompte(Long idCompte) {
        return transactionDAO.findByCompte(idCompte).stream()
                .mapToDouble(Transaction::montant)
                .average()
                .orElse(0.0);
    }

    public double totalParCompte(Long idCompte) {
        return transactionDAO.findByCompte(idCompte).stream()
                .mapToDouble(Transaction::montant)
                .sum();
    }

    public List<Transaction> detecterSuspectes() {
        List<Transaction> toutes = transactionDAO.findAll();

        List<Transaction> montantEleve = toutes.stream()
                .filter(t -> t.montant() > SEUIL_MONTANT_SUSPECT)
                .toList();

        List<Transaction> lieuInhabituel = toutes.stream()
                .filter(t -> estLieuInhabituel(t, toutes))
                .toList();

        List<Transaction> frequenceExcessive = detecterFrequenceExcessive(toutes);

        return java.util.stream.Stream.of(montantEleve, lieuInhabituel, frequenceExcessive)
                .flatMap(List::stream)
                .distinct()
                .sorted(Comparator.comparing(Transaction::date))
                .collect(Collectors.toList());
    }

    private boolean estLieuInhabituel(Transaction t, List<Transaction> toutes) {
        String lieuHabituel = toutes.stream()
                .filter(x -> x.idCompte().equals(t.idCompte()))
                .collect(Collectors.groupingBy(Transaction::lieu, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(t.lieu());
        return !t.lieu().equalsIgnoreCase(lieuHabituel);
    }
    private List<Transaction> detecterFrequenceExcessive(List<Transaction> toutes) {
        Map<Long, List<Transaction>> parCompte = toutes.stream()
                .collect(Collectors.groupingBy(Transaction::idCompte));

        return parCompte.values().stream()
                .flatMap(transactionsCompte -> transactionsCompte.stream()
                        .collect(Collectors.groupingBy(Transaction::date))
                        .values().stream()
                        .filter(groupe -> groupe.size() > 3)
                        .flatMap(List::stream))
                .toList();
    }
}
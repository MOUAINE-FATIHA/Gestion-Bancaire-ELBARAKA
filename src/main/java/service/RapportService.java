package service;

import dao.ClientDAO;
import dao.CompteDAO;
import dao.TransactionDAO;
import entity.Client;
import entity.Compte;
import entity.Transaction;
import enums.TypeTransaction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RapportService {

    private final ClientDAO clientDAO;
    private final CompteDAO compteDAO;
    private final TransactionDAO transactionDAO;
    private final TransactionService transactionService;

    public RapportService(ClientDAO clientDAO, CompteDAO compteDAO,
                          TransactionDAO transactionDAO, TransactionService transactionService) {
        this.clientDAO = clientDAO;
        this.compteDAO = compteDAO;
        this.transactionDAO = transactionDAO;
        this.transactionService = transactionService;
    }

    public List<Map.Entry<Client, Double>> top5ClientsParSolde() {
        List<Client> clients = clientDAO.findAll();

        return clients.stream()
                .collect(Collectors.toMap(
                        client -> client,
                        client -> compteDAO.findByClient(client.id()).stream()
                                .mapToDouble(Compte::getSolde)
                                .sum()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Client, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public Map<YearMonth, Map<TypeTransaction, Long>> rapportMensuelParType() {
        return transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.date()),
                        Collectors.groupingBy(Transaction::type, Collectors.counting())
                ));
    }

    public Map<YearMonth, Double> rapportMensuelVolumeTotal() {
        return transactionDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.date()),
                        Collectors.summingDouble(Transaction::montant)
                ));
    }

    public List<Transaction> transactionsSuspectes() {
        return transactionService.detecterSuspectes();
    }
    public List<Compte> comptesInactifs(int joursInactivite) {
        LocalDate limite = LocalDate.now().minusDays(joursInactivite);

        return compteDAO.findAll().stream()
                .filter(compte -> {
                    List<Transaction> transactions = transactionDAO.findByCompte(compte.getId());
                    return transactions.stream()
                            .map(Transaction::date)
                            .max(Comparator.naturalOrder())
                            .map(derniere -> derniere.isBefore(limite))
                            .orElse(true); // aucune transaction => considéré inactif
                })
                .collect(Collectors.toList());
    }
}
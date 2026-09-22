package entity;

import enums.TypeTransaction;

import java.time.LocalDate;

public record Transaction(Long id, LocalDate date, double montant, TypeTransaction type, String lieu, Long idCompte) {
}
package android.com.identificatecadministrador;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String serialNumber;
    private String name;
    private String matricula;
    private String academicProgram;
    private int nip;
    private int initialBalance;
    private ArrayList<Integer> deposits;
    private ArrayList<Integer> withdrawals;
    private Boolean isLost;
    private String foundBy;

    public User(String serialNumber, String name, String matricula, String academicProgram, int nip, int initialBalance, ArrayList<Integer> deposits, ArrayList<Integer> withdrawals, Boolean isLost, String foundBy) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.matricula = matricula;
        this.academicProgram = academicProgram;
        this.nip = nip;
        this.initialBalance = initialBalance;
        this.deposits = deposits;
        this.withdrawals = withdrawals;
        this.isLost = isLost;
        this.foundBy = foundBy;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getAcademicProgram() {
        return academicProgram;
    }

    public void setAcademicProgram(String academicProgram) {
        this.academicProgram = academicProgram;
    }

    public int getNip() {
        return nip;
    }

    public void setNip(int nip) {
        this.nip = nip;
    }

    public int getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(int initialBalance) {
        this.initialBalance = initialBalance;
    }

    public ArrayList<Integer> getDeposits() {
        return deposits;
    }

    public void setDeposits(ArrayList<Integer> deposits) {
        this.deposits = deposits;
    }

    public ArrayList<Integer> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(ArrayList<Integer> withdrawals) {
        this.withdrawals = withdrawals;
    }

    public Boolean getLost() {
        return isLost;
    }

    public void setLost(Boolean lost) {
        isLost = lost;
    }

    public String getFoundBy() {
        return foundBy;
    }

    public void setFoundBy(String foundBy) {
        this.foundBy = foundBy;
    }
}

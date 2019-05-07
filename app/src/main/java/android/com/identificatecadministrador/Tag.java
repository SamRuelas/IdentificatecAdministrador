package android.com.identificatecadministrador;

import java.util.ArrayList;

public class Tag {
    String matricula;
    int balance;
    boolean isLost;
    int numOfWithdrawal;
    ArrayList<Integer> withdrawals;

    public Tag(String matricula, int balance, boolean isLost, int numOfWithdrawal, ArrayList<Integer> withdrawals) {
        this.matricula = matricula;
        this.balance = balance;
        this.isLost = isLost;
        this.numOfWithdrawal = numOfWithdrawal;
        this.withdrawals = withdrawals;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public boolean isLost() {
        return isLost;
    }

    public void setLost(boolean lost) {
        isLost = lost;
    }

    public int getNumOfWithdrawal() {
        return numOfWithdrawal;
    }

    public void setNumOfWithdrawal(int numOfWithdrawal) {
        this.numOfWithdrawal = numOfWithdrawal;
    }

    public ArrayList<Integer> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(ArrayList<Integer> withdrawals) {
        this.withdrawals = withdrawals;
    }
}

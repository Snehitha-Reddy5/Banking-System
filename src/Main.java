import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

class Account {
    private int balance;
    private int accountId;
    String name;

    Account(String name, int initialBalance) {
        this.name = name;
        this.balance = initialBalance;
    }
    public int getAccountId() {
        return accountId;
    }

    public synchronized void deposit(int amount) {
            balance += amount;
            System.out.println("Deposited: " + amount + ", Balance: " + balance);
    }
    public synchronized void withdraw(int amount) {
        if (balance >= amount) {
                balance -= amount;
                System.out.println("Withdrawn: " + amount + ", Remaining balance: " + balance);
        }
        else {
            System.out.println("Insufficient balance. so withdrawing " + amount + " is not possible");
        }
    }
    public int getBalance() {
        return balance;
    }

}

class Transaction {
    private Account from;
    private Account to;
    private String type;
    private int amount;
    private BankService bank;

    Transaction(BankService bank, Account from, Account to, String type, int amount) {
        this.bank = bank;
        this.from = from;
        this.to = to;
        this.type = type;
        this.amount = amount;
    }

    public void process() {
        if (type.equalsIgnoreCase("transfer")) {
            bank.transfer(from, to, amount);
            FileManager.writeLog(
                    "TRANSFER : " + amount);
        }
        else if (type.equalsIgnoreCase("deposit")) {
            from.deposit(amount);
            FileManager.writeLog(
                    "DEPOSIT : " + amount);
        }
        else if (type.equalsIgnoreCase("withdraw")) {
            from.withdraw(amount);
            FileManager.writeLog(
                    "WITHDRAW : " + amount);
        }
    }
}
class BankService {
    public synchronized void transfer(Account from, Account to, int amount) {
        if (from.getBalance() >= amount) {
            from.withdraw(amount);
            to.deposit(amount);
            System.out.println("Transferred " + amount);
        }
        else {
            System.out.println("insufficient balance!");
        }
    }
}

class TransactionLogger implements Runnable {

    private Transaction transaction;

    TransactionLogger(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()
                + " started transaction");

        transaction.process();

        System.out.println(Thread.currentThread().getName()
                + " finished transaction");
    }
}

class FileManager {

    public static synchronized void writeLog(String message) {

        try (FileWriter writer = new FileWriter("transactions.txt", true)) {

            writer.write(message + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readLogs() {

        try (BufferedReader reader = new BufferedReader(new FileReader("transactions.txt"))) {

            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
public class Main {
    public static void main(String[] args) {
        Account account = new Account( "Cherry", 1000);
        Account account2 = new Account("Arjun", 2000);
        BankService bank = new BankService();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    Transaction t = new Transaction(bank, account, account2, "transfer", 200 );
                    t.process();
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread (() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    Transaction t = new Transaction(bank, account2, account, "withdraw", 400);
                    t.process();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t3 = new Thread (() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    bank.transfer(account, account2, 200);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });



        t1.start();
        t2.start();
        t3.start();


        try {
            t1.join();
            t2.join();
            t3.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n===== TRANSACTION LOGS =====");

        FileManager.readLogs();

        System.out.println("\n===== FINAL BALANCES =====");
        System.out.println("Remaining balance in  " + account.name + " is " + account.getBalance());
        System.out.println("Remaining balance in  " + account2.name + " is" + account2.getBalance());

    }
}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

/**
 * @author Klimentii Chistyakov
 */
public class Main {
    /**
     * Main method with all operations
     */
    public static void main(String[] args) {
        BankSystemProxyWithLogging proxy = BankSystemProxyWithLogging.getInstance();
        Scanner sc = new Scanner(System.in);
        final int N = sc.nextInt();
        ArrayList<String[]> commands = new ArrayList<>();
        sc.nextLine();
        for (int i = 0; i < N; i++) {
            commands.add(sc.nextLine().split("\\s+"));
        }
        //Loop for checking which command to execute
        for (int i = 0; i < N; i++) {
            String[] words = commands.get(i);
            switch (words[0]) {
                case "Create" -> {
                    String accountType = words[2];
                    String accountName = words[3];
                    String initialDeposit = words[4];
                    proxy.createAccount(accountName, Double.parseDouble(initialDeposit), accountType);
                }
                case "Deposit" -> {
                    String accountName = words[1];
                    String depositValue = words[2];
                    proxy.deposit(accountName, Double.parseDouble(depositValue));
                }
                case "Withdraw" -> {
                    String accountName = words[1];
                    String value = words[2];
                    proxy.withdraw(accountName, Double.parseDouble(value));
                }
                case "Transfer" -> {
                    String accountName = words[1];
                    String targetName = words[2];
                    String value = words[3];
                    proxy.transfer(accountName, targetName, Double.parseDouble(value));
                }
                case "View" -> {
                    String accountName = words[1];
                    proxy.viewAccount(accountName);
                }
                case "Activate" -> {
                    String accountName = words[1];
                    proxy.activateAccount(accountName);
                }
                case "Deactivate" -> {
                    String accountName = words[1];
                    proxy.deactivateAccount(accountName);
                }
            }
        }
    }
}

/**
 * Main singleton class for Bank System, that is hidden under proxy class
 *
 * @see BankSystemProxyWithLogging
 */
class BankSystem {
    private static BankSystem instance;

    private HashMap<String, Account> accounts = new HashMap<>();

    private BankSystem() {
    }

    public static BankSystem getInstance() {
        if (instance == null) {
            instance = new BankSystem();
        }
        return instance;
    }

    /**
     * Method for depositing money to the account
     *
     * @param name  Name of the account to deposit to
     * @param value Amount of money to deposit
     * @return Boolean value, to check if the operation needs to be logged
     */
    public boolean deposit(String name, double value) {
        if (accounts.containsKey(name)) {
            accounts.get(name).deposit(value);
            System.out.println(name + " successfully deposited $" + ValueFormat.format(value)
                    + ". New Balance: $" + ValueFormat.format(accounts.get(name).getBalance()) + ".");
            return true;
        } else {
            System.out.println("Error: Account " + name + " does not exist.");
            return false;
        }
    }

    /**
     * Method for withdrawing money from the account
     *
     * @param name  Name of the account to withdraw from
     * @param value Amount of money to withdraw
     * @return Boolean value, to check if the operation needs to be logged
     */
    public boolean withdraw(String name, double value) {
        if (accounts.containsKey(name)) {
            return accounts.get(name).withdraw(value);
        } else {
            System.out.println("Error: Account " + name + " does not exist.");
            return false;
        }

    }

    /**
     * Method for transferring money to a different account
     *
     * @param name   Name of the account to transfer from
     * @param target Name of the account to transfer to
     * @param value  Amount of money to transfer
     * @return Boolean value, to check if the operation needs to be logged
     */
    public boolean transfer(String name, String target, double value) {
        if (accounts.containsKey(name) && accounts.containsKey(target)) {
            return accounts.get(name).transfer(value, accounts.get(target));
        } else {
            if (!accounts.containsKey(name)) {
                System.out.println("Error: Account " + name + " does not exist.");
            } else {
                System.out.println("Error: Account " + target + " does not exist.");
            }
            return false;
        }
    }

    /**
     * Method for adding the operation into account history
     *
     * @param name   Name of the account
     * @param amount Amount of money involved into operation
     * @param type   Type of the operation (Deposit, Transfer, Withdraw, Initial Deposit)
     */
    public void addOperationInHistory(String name, double amount, String type) {
        accounts.get(name).addHistory(type + ValueFormat.format(amount));
    }

    /**
     * Method for creating new accounts
     *
     * @param type           Type of the account (Business, Savings, Checking)
     * @param name           Name of the owner
     * @param initialDeposit Initial balance value
     * @see Account
     * @see FeeCalculationStrategy
     * @see BusinessStrategy
     * @see CheckingStrategy
     * @see SavingsStrategy
     */
    public void createAccount(String type, String name, double initialDeposit) {
        accounts.put(name, new Account(name, initialDeposit));
        switch (type) {
            case "Savings" -> accounts.get(name).setStrategy(new SavingsStrategy());
            case "Checking" -> accounts.get(name).setStrategy(new CheckingStrategy());
            case "Business" -> accounts.get(name).setStrategy(new BusinessStrategy());
        }
    }

    /**
     * Method for showing account data
     *
     * @param name Name of the owner
     */
    public void viewAccount(String name) {
        if (accounts.containsKey(name)) {
            accounts.get(name).view();
        } else {
            System.out.println("Error: Account " + name + " does not exist.");
        }
    }

    /**
     * Method for activating an account
     *
     * @param name Name of the owner
     * @see AccountState
     * @see ActivatedState
     */
    public void activateAccount(String name) {
        if (accounts.containsKey(name)) {
            accounts.get(name).activate();
        } else {
            System.out.println("Error: Account " + name + " does not exist.");
        }

    }

    /**
     * Method for deactivating an account
     *
     * @param name Name of the owner
     * @see AccountState
     * @see DeactivatedState
     */
    public void deactivateAccount(String name) {
        if (accounts.containsKey(name)) {
            accounts.get(name).deactivate();
        } else {
            System.out.println("Error: Account " + name + " does not exist.");
        }

    }
}

/**
 * Class for formatting a double value into a string
 */
class ValueFormat {
    public static String format(double value) {
        return String.format(Locale.US, "%.3f", value);
    }
}

/**
 * Singleton proxy class for BankSystem
 *
 * @see BankSystem
 */
class BankSystemProxyWithLogging {
    /**
     * Field to get a BankSystem instance
     */
    private static final BankSystem bankSystem = BankSystem.getInstance();
    private static BankSystemProxyWithLogging instance;

    private BankSystemProxyWithLogging() {
    }

    public static BankSystemProxyWithLogging getInstance() {
        if (instance == null) {
            instance = new BankSystemProxyWithLogging();
        }
        return instance;
    }

    /**
     * Method for logging initial deposit in account history
     *
     * @param name  Name of the owner
     * @param value Amount of money involved
     * @see Account
     */
    private void logInitialDeposit(String name, double value) {
        bankSystem.addOperationInHistory(name, value, "Initial Deposit $");
    }

    /**
     * Method for logging the deposit in account history
     *
     * @param name  Name of the owner
     * @param value Amount of money involved
     * @see Account
     */
    private void logDeposit(String name, double value) {
        bankSystem.addOperationInHistory(name, value, "Deposit $");
    }

    /**
     * Method for logging the withdrawal in account history
     *
     * @param name  Name of the owner
     * @param value Amount of money involved
     * @see Account
     */
    private void logWithdrawal(String name, double value) {
        bankSystem.addOperationInHistory(name, value, "Withdrawal $");
    }

    /**
     * Method for logging the transfer in account history
     *
     * @param name  Name of the owner
     * @param value Amount of money involved
     * @see Account
     */
    private void logTransfer(String name, double value) {
        bankSystem.addOperationInHistory(name, value, "Transfer $");
    }

    /**
     * Method for depositing money
     *
     * @param name  Name of the owner
     * @param value Amount of money to deposit
     * @see BankSystem
     */
    public void deposit(String name, double value) {
        if (bankSystem.deposit(name, value)) {
            logDeposit(name, value);
        }

    }

    /**
     * Method for withdrawing money
     *
     * @param name  Name of the owner
     * @param value Amount of money to withdraw
     * @see BankSystem
     */
    public void withdraw(String name, double value) {
        if (bankSystem.withdraw(name, value)) {
            logWithdrawal(name, value);
        }
    }

    /**
     * Method for transferring money
     *
     * @param name       Name of the owner
     * @param targetName Name of the receiver
     * @param value      Amount of money to transfer
     * @see BankSystem
     */
    public void transfer(String name, String targetName, double value) {
        if (bankSystem.transfer(name, targetName, value)) {
            logTransfer(name, value);
        }
    }

    /**
     * Method for creating an account
     *
     * @param name           Name of the owner
     * @param initialDeposit Initial balance
     * @param type           Type of the account
     * @see BankSystem
     * @see Account
     * @see FeeCalculationStrategy
     */
    public void createAccount(String name, double initialDeposit, String type) {
        bankSystem.createAccount(type, name, initialDeposit);
        logInitialDeposit(name, initialDeposit);
        System.out.println("A new " + type + " account created for " + name + " with an initial balance of $" + ValueFormat.format(initialDeposit) + ".");
    }

    /**
     * Method for viewing an account details
     *
     * @param name Name of the owner
     */
    public void viewAccount(String name) {
        bankSystem.viewAccount(name);
    }

    /**
     * Method for activating an account
     *
     * @param name Name of the owner
     */
    public void activateAccount(String name) {
        bankSystem.activateAccount(name);
    }

    /**
     * Method for deactivating an account
     *
     * @param name Name of the owner
     */
    public void deactivateAccount(String name) {
        bankSystem.deactivateAccount(name);
    }

}

/**
 * Main account class that has:
 * Active and Inactive states.
 * Follows different strategies (Business, Checking and Savings account)
 *
 * @see AccountState
 * @see ActivatedState
 * @see DeactivatedState
 * @see FeeCalculationStrategy
 * @see BusinessStrategy
 * @see CheckingStrategy
 * @see SavingsStrategy
 */
class Account {
    private final String accountName;
    private double balance;
    private FeeCalculationStrategy strategy;
    private ArrayList<String> history = new ArrayList<>();
    private AccountState state;

    public Account(String accountName, double initialDeposit) {
        state = new ActivatedState();
        balance = initialDeposit;
        this.accountName = accountName;
    }

    /**
     * Method for setting account fee calculation strategy
     *
     * @param strategy Type of the account
     */
    public void setStrategy(FeeCalculationStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Method for depositing money
     *
     * @param value Amount of money to deposit
     */
    public void deposit(double value) {
        balance += value;
    }

    /**
     * Method for withdrawing money
     *
     * @param value Amount of money to withdraw
     * @return Boolean value to check if withdrawal is successful
     */
    public boolean withdraw(double value) {
        if (state.withdraw(accountName, balance, value, strategy)) {
            balance -= value;
            return true;
        }
        return false;
    }

    /**
     * Method for transferring money from this account to some other
     *
     * @param value  Amount of money to transfer
     * @param target Instance of target account
     * @return Boolean value to check if transfer is successful
     */
    public boolean transfer(double value, Account target) {
        return state.transfer(this, target, value);
    }

    /**
     * Method for adding an operation to history
     *
     * @param operation String formatted operation
     */
    public void addHistory(String operation) {
        history.add(operation);
    }

    /**
     * Method for activating an account
     */
    public void activate() {
        if (state.activate(accountName)) {
            state = new ActivatedState();
        }
    }

    /**
     * Method for deactivating an account
     */
    public void deactivate() {
        if (state.deactivate(accountName)) {
            state = new DeactivatedState();
        }
    }

    /**
     * Method for viewing account details
     */
    public void view() {
        System.out.print(accountName + "'s Account: Type: " + strategy.strategyName() + ", Balance: $"
                + ValueFormat.format(balance) + ", State: " + state.stateName() + ", Transactions: [");
        printHistory();
    }

    /**
     * Supporting method for view(), that prints history of operations
     */
    private void printHistory() {
        for (int i = 0; i < history.size(); i++) {
            System.out.print(history.get(i));
            if (history.size() - 1 != i) {
                System.out.print(", ");
            } else {
                System.out.print("].");
            }
        }
        System.out.print('\n');
    }

    public double getBalance() {
        return balance;
    }

    public String getAccountName() {
        return accountName;
    }

    public FeeCalculationStrategy getStrategy() {
        return strategy;
    }
}

/**
 * Interface for account type strategies
 *
 * @see Account
 * @see SavingsStrategy
 * @see CheckingStrategy
 * @see BusinessStrategy
 */
interface FeeCalculationStrategy {
    /**
     * Method for calculating fee, depending on type of the account
     *
     * @param value Amount of money to calculate fee with
     * @return Value of fee
     */
    double calculateFee(double value);

    /**
     * @return Strategy name
     */
    String strategyName();

    /**
     * @return String value of fee in percent
     */
    String feeValue();
}

/**
 * Class for savings strategy. Transaction Fee: 1.5% per transaction. This lower fee reflects the encouragement
 * of saving and less frequent transactions compared to other account types.
 *
 * @see FeeCalculationStrategy
 */
class SavingsStrategy implements FeeCalculationStrategy {

    @Override
    public double calculateFee(double value) {
        return value * 0.015;
    }

    @Override
    public String strategyName() {
        return "Savings";
    }

    @Override
    public String feeValue() {
        return "1.5%";
    }
}

/**
 * Class for checking strategy. Transaction Fee: 2% per transaction. The slightly higher fee is due to the
 * higher volume of transactions and the convenience of easy access and frequent use.
 *
 * @see FeeCalculationStrategy
 */
class CheckingStrategy implements FeeCalculationStrategy {

    @Override
    public double calculateFee(double value) {
        return value * 0.02;
    }

    @Override
    public String strategyName() {
        return "Checking";
    }

    @Override
    public String feeValue() {
        return "2.0%";
    }
}

/**
 * Class for business strategy. Transaction Fee: 2.5% per transaction. The highest fee among the accounts,
 * justified by the higher transaction volumes and additional features provided for business purposes.
 *
 * @see FeeCalculationStrategy
 */
class BusinessStrategy implements FeeCalculationStrategy {

    @Override
    public double calculateFee(double value) {
        return value * 0.025;
    }

    @Override
    public String strategyName() {
        return "Business";
    }

    @Override
    public String feeValue() {
        return "2.5%";
    }
}

/**
 * Interface for account states, which are Active and Inactive
 *
 * @see ActivatedState
 * @see DeactivatedState
 */
interface AccountState {
    /**
     * Method for withdrawing money from the account
     *
     * @param accountName Name of the owner
     * @param balance     Current balance
     * @param value       Amount of money to withdraw
     * @param strategy    Strategy used to calculate fee
     * @return Boolean value to check if withdraw is successful. Always returns false if account is inactive.
     */
    boolean withdraw(String accountName, double balance, double value, FeeCalculationStrategy strategy);

    /**
     * Method for transferring money to another account
     *
     * @param thisAccount Instance of owner account
     * @param target      Instance of target account
     * @param value       Amount of money to transfer
     * @return Boolean value to check if transfer is successful. Always returns false if account is inactive.
     */
    boolean transfer(Account thisAccount, Account target, double value);

    /**
     * Method for activating an account
     *
     * @param name Name of the owner
     * @return Boolean value to check if activation is successful. Always returns false, if account is already active
     */
    boolean activate(String name);

    /**
     * Method for deactivating an account
     *
     * @param name Name of the owner
     * @return Boolean value to check if deactivation is successful.
     * Always returns false, if account is already inactive
     */
    boolean deactivate(String name);

    /**
     * @return String name of the state (Active, Inactive)
     */
    String stateName();
}

/**
 * Class for handling operations while active
 *
 * @see AccountState
 */
class ActivatedState implements AccountState {

    @Override
    public boolean withdraw(String accountName, double balance, double value, FeeCalculationStrategy strategy) {
        if (balance - value >= 0) {
            balance -= value;
            double fee = strategy.calculateFee(value);
            System.out.println(accountName + " successfully withdrew $" + ValueFormat.format(value - fee)
                    + ". New Balance: $" + ValueFormat.format(balance) + ". Transaction Fee: $" + ValueFormat.format(fee)
                    + " (" + strategy.feeValue() + ") in the system.");
            return true;
        }
        System.out.println("Error: Insufficient funds for " + accountName + ".");
        return false;
    }

    @Override
    public boolean transfer(Account thisAccount, Account target, double value) {
        if (thisAccount.getBalance() - value >= 0) {
            thisAccount.deposit(value * -1);
            double fee = thisAccount.getStrategy().calculateFee(value);
            target.deposit(value - fee);
            System.out.println(thisAccount.getAccountName() + " successfully transferred $"
                    + ValueFormat.format(value - fee) + " to " + target.getAccountName() + ". New Balance: $"
                    + ValueFormat.format(thisAccount.getBalance()) + ". Transaction Fee: $"
                    + ValueFormat.format(fee) + " (" + thisAccount.getStrategy().feeValue() + ") in the system.");
            return true;
        }
        System.out.println("Error: Insufficient funds for " + thisAccount.getAccountName() + ".");
        return false;
    }

    @Override
    public boolean activate(String name) {
        System.out.println("Error: Account " + name + " is already activated.");
        return false;
    }

    @Override
    public boolean deactivate(String name) {
        System.out.println(name + "'s account is now deactivated.");
        return true;
    }

    @Override
    public String stateName() {
        return "Active";
    }
}

/**
 * Class for handling operations while inactive
 *
 * @see AccountState
 */
class DeactivatedState implements AccountState {

    @Override
    public boolean withdraw(String accountName, double balance, double value, FeeCalculationStrategy strategy) {
        System.out.println("Error: Account " + accountName + " is inactive.");
        return false;
    }

    @Override
    public boolean transfer(Account thisAccount, Account target, double value) {
        System.out.println("Error: Account " + thisAccount.getAccountName() + " is inactive.");
        return false;
    }

    @Override
    public boolean activate(String name) {
        System.out.println(name + "'s account is now activated.");
        return true;
    }

    @Override
    public boolean deactivate(String name) {
        System.out.println("Error: Account " + name + " is already deactivated.");
        return false;
    }

    @Override
    public String stateName() {
        return "Inactive";
    }
}
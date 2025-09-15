import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import javax.swing.*;

public class ExpenseCalculatorGUI extends JFrame{

    private java.util.List<JTextField> friendFields = new ArrayList<>();
    private java.util.List<ExpenseRow> expenseRows = new ArrayList<>();

    private JPanel friendsPanel;
    private JPanel expensesPanel;

    
    public ExpenseCalculatorGUI(){
        setTitle("Expense Calculator");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ==== FRIENDS SECTION ====
        friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
        friendsPanel.setBorder(BorderFactory.createTitledBorder("Friends"));

        JButton addFriendBtn = new JButton("Add Friend");
        addFriendBtn.addActionListener(e -> addFriendRow("Friend "));

        JPanel friendSection = new JPanel(new BorderLayout());
        friendSection.add(new JScrollPane(friendsPanel), BorderLayout.CENTER);
        friendSection.add(addFriendBtn, BorderLayout.SOUTH);

        // ==== EXPENSE SECTION ====
        expensesPanel = new JPanel();
        expensesPanel.setLayout(new BoxLayout(expensesPanel, BoxLayout.Y_AXIS));

        JButton addExpenseBtn = new JButton("Add Expense");
        addExpenseBtn.addActionListener(e -> addExpenseRow());

        JPanel expenseSection = new JPanel(new BorderLayout());
        expenseSection.add(new JScrollPane(expensesPanel), BorderLayout.CENTER);
        expenseSection.add(addExpenseBtn, BorderLayout.SOUTH);


        // Main Layout
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.add(friendSection);
        topPanel.add(expenseSection);
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        // Split Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);

        add(splitPane);
    }

    private void addFriendRow(String defaultName){
        JTextField field = new JTextField(defaultName, 15);
        friendFields.add(field);
        friendsPanel.add(field);
        friendsPanel.revalidate();
        friendsPanel.repaint();

        updateExpenseDropDowns();
    }

    private void addExpenseRow(){
        ExpenseRow row = new ExpenseRow(friendFields);
        expenseRows.add(row);
        expensesPanel.add(row);
        expensesPanel.revalidate();
        expensesPanel.repaint();
    }

    private void updateExpenseDropDowns(){
        List<String> names = getFriendNames();
        for(ExpenseRow row : expenseRows){
            row.updateDropDown(names);
        }
    }

    private List<String> getFriendNames(){
        List<String> names = new ArrayList<>();
        for(JTextField f : friendFields){
            String name = f.getText().trim();
            if(!name.isEmpty()){
                names.add(name);
            }
        }
        return names;
    }



    // TODO: Pick up values from GUI
    private void calculateExpenses(){   // to be modified 

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of friends: ");
        int numFriends = sc.nextInt();
        sc.nextLine();

        List<String> friends = new ArrayList<>(); // Empty arraylist
        for(int i = 0; i < numFriends; i++){
            System.out.print("Enter friend " + (i + 1) + " name: ");
            friends.add(sc.nextLine().trim());
        }

        System.out.print("Enter number of expense records: ");
        int numRecords = sc.nextInt();  // number of purchase entries
        sc.nextLine();

        String[] payers = new String[numRecords];
        double[] amounts = new double[numRecords];

        for (int i = 0; i < numRecords; i++) {
            System.out.println("Record " + (i + 1) + ":");
            System.out.print("  Who paid? ");
            payers[i] = sc.nextLine().trim();
            System.out.print("  How much? ");
            amounts[i] = sc.nextDouble();
            sc.nextLine(); // consume newline
        }

        HashMap<String, Double> balances = new HashMap<>();
        for(String friend : friends){
            balances.put(friend, 0.0);
        }
        System.out.println(balances);

        for(int day = 0; day < numRecords; day++){
            // retrieve the total and the payer for each day
            String payer = payers[day];
            double total = amounts[day]; 
            double share = total / friends.size();

            for(String friend : friends){
                if(friend.equals(payer)){
                    balances.put(friend, balances.get(friend) + (total - share));
                }
                else{
                    balances.put(friend, balances.get(friend) - share);
                }
            }
        }

        System.out.println("Net Balances: ");
        for(String friend : friends){
            System.out.println(friend + " " + balances.get(friend));
        }

        System.out.println("Simplified Debts");
        simplifyDebts(balances);
    }

    private static void simplifyDebts(HashMap <String, Double> balances){
        // Find the largest creditor, and smallest debtor and pay out (repeat)
        PriorityQueue <Person> creditors = new PriorityQueue<>((a,b) -> Double.compare(b.amount, a.amount)); 
        PriorityQueue <Person> debtors = new PriorityQueue<>((a,b) -> Double.compare(a.amount, b.amount));
        
        // Entry - Key/value pair
        for(HashMap.Entry<String, Double> entry: balances.entrySet()){
            String name = entry.getKey();
            double amount = entry.getValue();
            if(amount > 0){
                creditors.offer(new Person(name, amount));
            }
            else if(amount < 0){
                debtors.offer(new Person(name, -amount));
            }
        }

        while(!creditors.isEmpty() && !debtors.isEmpty()){
            Person creditor = creditors.poll();
            Person debtor = debtors.poll();

            double min = Math.min(creditor.amount, debtor.amount);
            System.out.printf("%s owes %s: $%.2f\n", debtor.name, creditor.name, min);

            if(creditor.amount > min){
                creditors.offer(new Person(creditor.name, creditor.amount - min));
            }
            if(debtor.amount > min){
                debtors.offer(new Person(debtor.name, debtor.amount - min));
            }
        }

    }

    // Inner Classes

    static class Person{
        String name;
        double amount;
        Person(String name, double amount){
            this.name = name;
            this.amount = amount;
        }
    }

    class ExpenseRow extends JPanel{
        JTextField description;
        JComboBox<String> payerDropDown;
        JTextField amountField;

        ExpenseRow(java.util.List<JTextField> friends){
            setLayout(new FlowLayout(FlowLayout.LEFT));
            description = new JTextField("Expense ");
            payerDropDown = new JComboBox<>();
            amountField = new JTextField(7);

            add(new JLabel("Desc: "));
            add(description);
            add(new JLabel("Payer: "));
            add(payerDropDown);
            add(new JLabel("Amount: "));
            add(amountField);

            updateDropDown(getFriendNames());
        }

        void updateDropDown(java.util.List<String> names){
            payerDropDown.removeAllItems();
            for(String n : names){
                payerDropDown.addItem(n);
            }
        }

        String getPayer(){
            return (String) payerDropDown.getSelectedItem();
        }

        double getAmount(){
            try {
                return Double.parseDouble((amountField.getText().trim()));
            } catch (Exception e) {
                return 0.0;
            }
        }

    }

}

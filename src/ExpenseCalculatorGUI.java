import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import javax.swing.*;

public class ExpenseCalculatorGUI extends JFrame{

    private java.util.List<JTextField> friendFields = new ArrayList<>();
    private java.util.List<ExpenseRow> expenseRows = new ArrayList<>();

    private JPanel friendsPanel;
    private JPanel expensesPanel;
    private JTextArea outputArea;

    
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


        // ==== OUTPUT SECTION
        JButton calculateBtn = new JButton("Calculate Balance");
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        outputArea.setLineWrap(false);

        JScrollPane outputScroll = new JScrollPane(outputArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        outputScroll.setPreferredSize(new Dimension(800, 300));

        // Main Layout
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.add(friendSection);
        topPanel.add(expenseSection);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(calculateBtn, BorderLayout.NORTH);
        bottomPanel.add(outputScroll, BorderLayout.CENTER);

        // Split Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(350);

        add(splitPane, BorderLayout.CENTER);

        calculateBtn.addActionListener(e -> calculateExpenses());

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



    private void calculateExpenses(){   // to be modified 
        List<String> friends = getFriendNames();
        if(friends.isEmpty()){
            outputArea.setText("Please add at least one friend");
            return;
        }

        // Initialize Balances
        HashMap<String, Double> balances = new HashMap<>();
        for(String friend : friends){
            balances.put(friend, 0.0);
        }

        // Processe each expense
        for(ExpenseRow row : expenseRows){
            // retrieve the total and the payer for each day
            String payer = row.getPayer();
            double total = row.getAmount(); 
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

        StringBuilder sb = new StringBuilder();
        sb.append("==== NET BALANCES ====\n");
        for(String friend : friends){
            sb.append(String.format("%s: %.2f\n", friend, balances.get(friend)));
        }

        // System.out.println("Simplified Debts");
        // simplifyDebts(balances);

        sb.append("==== Simplified Debts ====\n");
        sb.append(simplifyDebts(balances));

        outputArea.setText(sb.toString());
    }

    private String simplifyDebts(HashMap <String, Double> balances){
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

        StringBuilder sb = new StringBuilder();
        while(!creditors.isEmpty() && !debtors.isEmpty()){
            Person creditor = creditors.poll();
            Person debtor = debtors.poll();

            double min = Math.min(creditor.amount, debtor.amount);
            sb.append(String.format("%s owes %s: $%.2f\n", debtor.name, creditor.name, min));

            if(creditor.amount > min){
                creditors.offer(new Person(creditor.name, creditor.amount - min));
            }
            if(debtor.amount > min){
                debtors.offer(new Person(debtor.name, debtor.amount - min));
            }
        }

        return sb.toString();

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


    // Description, Payer, $100


}

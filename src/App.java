import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            new ExpenseCalculatorGUI().setVisible(true);
        });
    }
}

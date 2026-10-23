
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Giao dien Java");

        JLabel label = new JLabel("Nhap ten cua ban:");
        JTextField textField = new JTextField(15);
        JButton button = new JButton("Xin chao");

        JLabel result = new JLabel("");

        frame.setLayout(new FlowLayout());

        frame.add(label);
        frame.add(textField);
        frame.add(button);
        frame.add(result);

        button.addActionListener(e -> {
            String name = textField.getText();
            result.setText("Xin chao, " + name + "!");
        });

        frame.setSize(350, 180);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

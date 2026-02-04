import javax.swing.*;
import java.awt.*;

public class BaseFrame extends JFrame {

    public BaseFrame(String title) {
        setTitle(title);
        setSize(900, 600);
        setLocationRelativeTo(null); // center screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // set background color
        getContentPane().setBackground(UIColors.BACKGROUND);

        // layout
        setLayout(new BorderLayout());
    }
}
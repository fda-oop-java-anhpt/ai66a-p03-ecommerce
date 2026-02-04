import javax.swing.*;

public class UIButton extends JButton {

    public UIButton(String text) {
        super(text);
        setFont(UIFonts.BUTTON);
        setBackground(UIColors.PRIMARY);
        setForeground(UIColors.BUTTON_TEXT);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }
}
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author liuml
 * @explain
 * @time 2019-06-04 15:02
 */
public class ClientFrame extends Frame {

    public TextArea mTextArea = new TextArea();
    public TextField mTextField = new TextField();
    public static ClientFrame sClientFrame = new ClientFrame();

    private Client mClient;

    private ClientFrame() {

        this.setSize(600, 400);
        this.setLocation(100, 200);
        //BorderLayout布置容器的边框布局,它可以对容器组件进行安排,并调整其大小,使其符合下列五个区域:北、南、东、西、中,
        this.add(mTextArea, BorderLayout.CENTER);
        this.add(mTextField, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                System.exit(0);
            }
        });

        mTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mClient.sendMsg(mTextField.getText());
                mTextField.setText("");
            }
        });
        this.setVisible(true);
    }

    public void updateText(String msgAccepted) {
        this.mTextArea.append(msgAccepted + System.getProperty("line.separator"));
    }

    public static void main(String[] args) {
        sClientFrame.connectToServer();
    }

    private void connectToServer() {
        mClient = new Client();
        mClient.connect();
    }


}


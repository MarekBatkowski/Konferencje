package konferencje;

import SQLhandling.DBConnection;
import SQLhandling.QueryHandler;
import SQLhandling.Selector;
//import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class LoginForm
{
    private static JFrame frame;
    private JPanel MainPanel;
    private JPanel Cards;

    private JPanel StartScreen;
    private JLabel ImageTruck;
    private JButton logInButton;
    private JButton signInButton;
    private JButton WyjdzButton;

    private JPanel LogIn;
    private JLabel ImageKey;
    private JTextField LogInEmail;
    private JPasswordField LogInPassword;
    private JButton WsteczButton;
    private JButton ZalogujSieButton;

    private JPanel SignIn;
    private JLabel ImageAdd;
    private JTextField SignUpName;
    private JTextField SignUpSurname;
    private JTextField SignUpEmail;
    private JPasswordField SignUpPassword;
    private JButton WsteczButton1;
    private JButton ZalozKontoButton;

    private static final Pattern emailRegEx = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
    private static final Pattern passwordRegEx = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");
    private static final Pattern nameRegEx = Pattern.compile("^[A-Z]+[a-z]{2,}");
    private static final Object[] confirmOptions = {"     Tak     ","     Nie     "};

    MessageDigest digest = null;
    final Logger logger = LogManager.getLogger(LoginForm.class);

    public LoginForm()
    {
        try
        {
            digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

    //    System.out.println("Creating LoginForm");

        Cards.add(StartScreen, "StartScreen");
        Cards.add(LogIn, "LogIn");
        Cards.add(SignIn, "SignIn");

        CardLayout cards = (CardLayout) (Cards.getLayout());
        cards.show(Cards, "StartScreen");

        Selector selector = new Selector();
        QueryHandler queryHandler = new QueryHandler();
        Uzytkownicy.setLogin() = null;

        try
        {
            DBConnection.GetInstance().getConnection();
    //        System.out.println("getting instance");
        }
        catch (SQLException e)
        {
            logger.error("Couldn't get connection to database");
            logger.trace("Application closed with exit code 1");

            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Nie można połaczyć z bazą danych", "Błąd", JOptionPane.PLAIN_MESSAGE);
            System.exit(1);
        }

            logger.trace("Connected to Database");


        //// StartScreen ////

        logInButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.setSize(340, 350);
                frame.setTitle("Zaloguj się");
                cards.show(Cards, "LogIn");
                LogInEmail.requestFocus();
            }
        });

        signInButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.setSize(340, 400);
                frame.setTitle("Zarejestruj się");
                cards.show(Cards, "SignIn");
                SignUpEmail.requestFocus();
            }
        });

        WyjdzButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(JOptionPane.showOptionDialog(frame, "Czy chcesz wyjść?", "Potwierdź operację",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1])==JOptionPane.YES_OPTION)
                {
                    logger.trace("Application closed with exit code 0");
                    System.exit(0);
                }
            }
        });


        //// LogIn ////

        ZalogujSieButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String email = LogInEmail.getText();
                String password = String.valueOf(LogInPassword.getPassword());
                String SHApassword = String.format("%032x", new BigInteger(1, digest.digest(password.getBytes(StandardCharsets.UTF_8))));

                ArrayList<ArrayList<String>> result = selector.select("SELECT * FROM Dostawca " +
                                                                        "WHERE email = '" + email + "' and haslo = '" + SHApassword + "';");

                if (result.size() == 1)
                {
                    Uzytkownicy.setLogin() = result.get(0);
                    new MainWindow().createWindow();
                    frame.dispose();

                    logger.trace("User " + CurrentUser.Values.get(3) + " logged");
                }
                else
                {
                    if(selector.select("SELECT * FROM Dostawca WHERE email = '" + email + "';").size()==0)
                    {
                        JOptionPane.showMessageDialog(frame, "Niewłaściwy email!", "Błąd", JOptionPane.PLAIN_MESSAGE);
                        logger.warn("Incorrect email provided when attempting to login: " + email);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(frame, "Niewłaściwe hasło!", "Błąd", JOptionPane.PLAIN_MESSAGE);
                        logger.warn("Incorrect password provided when attempting to login with email: " + email);
                    }
                }
            }
        });

        WsteczButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.setSize(340, 380);
                frame.setTitle("Zaloguj lub zarejestruj się");
                cards.show(Cards, "StartScreen");
                logInButton.requestFocus();
            }
        });


        // SignIn

        ZalozKontoButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<ArrayList<String>> Emails = selector.select("SELECT email FROM Dostawca");

                if(!nameRegEx.matcher(SignUpName.getText()).matches())
                {
                    JOptionPane.showMessageDialog(frame, "Imię jest nieprawidłowe!", "Błąd", JOptionPane.PLAIN_MESSAGE);
                    return;
                }

                if(!nameRegEx.matcher(SignUpSurname.getText()).matches())
                {
                    JOptionPane.showMessageDialog(frame, "Nazwisko jest nieprawidłowe!", "Błąd", JOptionPane.PLAIN_MESSAGE);
                    return;
                }

                if(!emailRegEx.matcher(SignUpEmail.getText()).matches())
                {
                    JOptionPane.showMessageDialog(frame, "Email jest nieprawidłowy!", "Błąd", JOptionPane.PLAIN_MESSAGE);
                    return;
                }

                for(ArrayList<String> iter : Emails)
                {
                    if (iter.get(0).equals(SignUpEmail.getText()))
                    {
                        JOptionPane.showMessageDialog(frame, "Email jest już zajęty!", "Błąd", JOptionPane.PLAIN_MESSAGE);
                        return;
                    }
                }

                if(!passwordRegEx.matcher(String.valueOf(SignUpPassword.getPassword())).matches())
                {
                    JOptionPane.showMessageDialog(frame,
                            "Hasło nieprawidłowe!\n\n" +
                                    "Hasło musi:\n" +
                                    "-zawierać co najmniej jedną cyfrę, jedną wielką i jedną małą literę\n" +
                                    "-mieć co najmniej 8 znaków.", "Błąd", JOptionPane.PLAIN_MESSAGE);
                    return;
                }

                String name = SignUpName.getText();
                String surname = SignUpSurname.getText();
                String email = SignUpEmail.getText();
                String password = String.valueOf(SignUpPassword.getPassword());
                String SHApassword = String.format("%032x", new BigInteger(1, digest.digest(password.getBytes(StandardCharsets.UTF_8))));

                queryHandler.execute("INSERT INTO dostawca(imie, nazwisko, email, haslo) VALUES ('" + name + "', '" + surname + "', '" + email + "', '" + SHApassword + "');");
                JOptionPane.showMessageDialog(frame, "Dodano nowego użytownika!", "Operacja Pomyślna", JOptionPane.PLAIN_MESSAGE);
                cards.show(Cards, "StartScreen");

                logger.trace("Created new user " + email);
            }
        });

        WsteczButton1.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.setSize(340, 380);
                frame.setTitle("Zaloguj lub zarejestruj się");
                cards.show(Cards, "StartScreen");
                logInButton.requestFocus();
            }
        });
    }

    void createWindow()
    {
        frame = new JFrame("Zaloguj lub zarejestruj się");
        frame.setContentPane(this.MainPanel);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(340, 380);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent we)
            {
                if(JOptionPane.showOptionDialog(frame, "Czy chcesz wyjść?", "Potwierdź operację",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1])==JOptionPane.YES_OPTION)
                {
                    logger.trace("Application closed with exit code 0");
                    System.exit(0);
                }
            }
        });
    }

    private void createUIComponents()
    {
        ImageTruck = new JLabel(new ImageIcon("TruckDelivery.png"));
        ImageKey = new JLabel(new ImageIcon("Key.png"));
        ImageAdd = new JLabel(new ImageIcon("Add.png"));
    }
}


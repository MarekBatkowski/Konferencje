package GUI;

import SQLhandling.DBConnection;
import SQLhandling.QueryHandler;
import SQLhandling.Selector;

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
    private CardLayout cards;

    private JPanel StartScreen;
    private JLabel ImageConference;
    private JButton logInButton;
    private JButton signInButton;
    private JButton WyjdźButton;

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
    private JTextField SignUpLogin;
    private JPasswordField SignUpPassword;
    private JButton WsteczButton1;
    private JButton ZałóżKontoButton;
    private JTextField SignUpPesel;
    private JTextField SignUpAddress;
    private JTextField SignUpInterests;
    private JPasswordField SignUpRepeadPassword;

    private static final Pattern emailRegEx = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
    private static final Pattern loginRegEx = Pattern.compile("[0-9a-zA-Z]{8,25}");
    private static final Pattern passwordRegEx = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,25}$");
    private static final Pattern nameRegEx = Pattern.compile("^[A-Z]+[a-z]{2,}");
    private static final Pattern peselRegEx = Pattern.compile("[0-9]{11}");
    private static final Object[] confirmOptions = {"     Tak     ","     Nie     "};

    private static final int StartScreenHeight = 420;
    private static final int LogInHeight = 420;
    private static final int SignInHeight = 600;

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

        Cards.add(StartScreen, "StartScreen");
        Cards.add(LogIn, "LogIn");
        Cards.add(SignIn, "SignIn");

        cards = (CardLayout) (Cards.getLayout());
        cards.show(Cards, "StartScreen");

        final Selector selector = new Selector();
        final QueryHandler queryHandler = new QueryHandler();
        CurrentUser.Values = null;

        try
        {
            DBConnection.GetInstance().getConnection();
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
                frame.setSize(340, LogInHeight);
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
                frame.setSize(340, SignInHeight);
                frame.setTitle("Zarejestruj się");
                cards.show(Cards, "SignIn");
                SignUpEmail.requestFocus();
            }
        });

        WyjdźButton.addActionListener(new ActionListener()
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
                String login = LogInEmail.getText();
                String password = String.valueOf(LogInPassword.getPassword());
                String SHApassword = String.format("%032x", new BigInteger(1, digest.digest(password.getBytes(StandardCharsets.UTF_8))));

                ArrayList<ArrayList<String>> result = selector.select
                ("SELECT * FROM SysUser " + "WHERE login = '" + login + "' and passwd = '" + SHApassword + "';");

                if (result.size() == 1)
                {
                    CurrentUser.Values = result.get(0);
                    System.out.println("logged succesfully as: " + CurrentUser.Values.get(6));
                    //new MainWindow().createWindow();
                    logger.trace("User " + CurrentUser.Values.get(6) + " logged");

                    JOptionPane.showMessageDialog(frame,"Zalogowano jako " + CurrentUser.Values.get(6),"Operacja pomyślna", JOptionPane.INFORMATION_MESSAGE);

                    frame.dispose();
                    System.exit(0);
                }
                else
                {
                    if(selector.select("SELECT * FROM SysUser WHERE login = '" + login + "';").size()==0)
                    {
                        JOptionPane.showMessageDialog(frame, "Niewłaściwy login!", "Błąd", JOptionPane.PLAIN_MESSAGE);
                        logger.warn("Incorrect email provided when attempting to login: " + login);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(frame, "Niewłaściwe hasło!", "Błąd", JOptionPane.PLAIN_MESSAGE);
                        logger.warn("Incorrect password provided when attempting to login as: " + login);
                    }
                }
            }
        });

        WsteczButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.setSize(340, StartScreenHeight);
                frame.setTitle("Zaloguj lub zarejestruj się");
                cards.show(Cards, "StartScreen");
                logInButton.requestFocus();
            }
        });

        // SignIn //

        ZałóżKontoButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<ArrayList<String>> Emails = selector.select("SELECT email FROM sysuser");
                ArrayList<ArrayList<String>> Logins = selector.select("SELECT login FROM sysuser");
                ArrayList<ArrayList<String>> Pesels = selector.select("SELECT pesel FROM sysuser");

                boolean errors = false;
                String errormessage = "";

                if(!loginRegEx.matcher(SignUpLogin.getText()).matches())        // check login
                {
                    errors = true;
                    errormessage+="Login nieprawidłowy!\n" +
                        "Login musi:\n" +
                        "- składać się z cyfr oraz małych i wielkich liter\n" +
                        "- mieć 8-25 znaków.\n\n";
                }

                for(ArrayList<String> iter : Logins)
                {
                    String Login = SignUpLogin.getText();
                    if(iter.get(0).equalsIgnoreCase(Login))
                    {
                        errors = true;
                        errormessage+="Login jest już zajęty!\n\n";
                        break;
                    }
                }

                if(!passwordRegEx.matcher(String.valueOf(SignUpPassword.getPassword())).matches())      // check password
                {
                    errors = true;
                    errormessage+="Hasło nieprawidłowe!\n" +
                            "Hasło musi:\n" +
                            "- zawierać co najmniej jedną cyfrę, jedną wielką i jedną małą literę\n" +
                            "- mieć 8-25 znaków.\n\n";
                }
                else
                {
                    if (!String.valueOf(SignUpPassword.getPassword()).equals(String.valueOf(SignUpRepeadPassword.getPassword())))    // check repeat password
                    {
                        errors = true;
                        errormessage += "Hasła się nie zgadzają!\n\n";
                    }
                }

                if(!nameRegEx.matcher(SignUpName.getText()).matches())      // check name
                {
                    errors = true;
                    errormessage+="Imię jest nieprawidłowe!\n\n";
                }

                if(!nameRegEx.matcher(SignUpSurname.getText()).matches())   // check surname
                {
                    errors = true;
                    errormessage+="Nazwisko jest nieprawidłowe!\n\n";
                }

                if(!emailRegEx.matcher(SignUpEmail.getText()).matches())    // check email
                {
                    errors = true;
                    errormessage+="Email jest nieprawidłowy!\n\n";
                }

                for(ArrayList<String> iter : Emails)
                {
                    String Email = SignUpEmail.getText();
                    if (iter.get(0).equalsIgnoreCase(Email))
                    {
                        errors = true;
                        errormessage+="Email jest już zajęty!\n\n";
                        break;
                    }
                }

                if(SignUpPesel.getText().length()!=0 && !peselRegEx.matcher(SignUpPesel.getText()).matches())   // check surname
                {
                    errors = true;
                    errormessage+="Pesel jest nieprawidłowy!\n\n";
                }

                if(SignUpPesel.getText().length()!=0)
                {
                    for (ArrayList<String> iter : Pesels)
                    {
                        String Pesel = SignUpPesel.getText();
                        if (iter.get(0)!=null && iter.get(0).equals(Pesel))
                        {
                            errors = true;
                            errormessage+="Pesel jest już zajęty!\n\n";
                            break;
                        }
                    }
                }

                if(errors)
                {
                    JOptionPane.showMessageDialog(frame, errormessage, "Błąd", JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                else
                {
                    String login = SignUpLogin.getText();
                    String password = String.valueOf(SignUpPassword.getPassword());
                    String SHApassword = String.format("%032x", new BigInteger(1, digest.digest(password.getBytes(StandardCharsets.UTF_8))));

                    String name = SignUpName.getText();
                    String surname = SignUpSurname.getText();
                    String email = SignUpEmail.getText();

                    queryHandler.execute("INSERT INTO SysUser (name, surname, email, login, passwd) VALUES " +
                            "('" + name + "', '" + surname + "', '" + email + "', '" + login + "', '" + SHApassword + "');");

                    if(SignUpPesel.getText().length()!=0)
                        queryHandler.execute("UPDATE SysUser SET pesel = " + SignUpPesel.getText() + " WHERE login = '" + login + "';");

                    if(SignUpAddress.getText().length()!=0)
                        queryHandler.execute("UPDATE SysUser SET address = '" + SignUpAddress.getText() + "' WHERE login = '" + login + "';");

                    if(SignUpInterests.getText().length()!=0)
                        queryHandler.execute("UPDATE SysUser SET interests = '" + SignUpInterests.getText() + "' WHERE login = '" + login + "';");

                    JOptionPane.showMessageDialog(frame, "Dodano nowego użytownika!", "Operacja Pomyślna", JOptionPane.PLAIN_MESSAGE);

                    SignUpLogin.setText("");
                    SignUpPassword.setText("");
                    SignUpRepeadPassword.setText("");
                    SignUpName.setText("");
                    SignUpSurname.setText("");
                    SignUpEmail.setText("");
                    SignUpPesel.setText("");
                    SignUpAddress.setText("");
                    SignUpInterests.setText("");

                    logger.trace("Created new user " + login);

                    frame.setSize(340, StartScreenHeight);
                    frame.setTitle("Zaloguj lub zarejestruj się");
                    cards.show(Cards, "StartScreen");
                    logInButton.requestFocus();
                }
            }
        });

        WsteczButton1.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.setSize(340, StartScreenHeight);
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
        frame.setSize(340, StartScreenHeight);
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
        ImageConference = new JLabel(new ImageIcon("Conference.png"));
        ImageKey = new JLabel(new ImageIcon("Key.png"));
        ImageAdd = new JLabel(new ImageIcon("Add.png"));
    }
}
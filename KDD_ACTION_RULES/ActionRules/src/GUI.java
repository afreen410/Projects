import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author
 * KDD Group 1
 * Anchal Atlani
 * Afreen
 * Ankita Kumari
 * Rishika Ganga Shetty
 * Satish Kumar
 *
 *  GUI
 */
public class GUI extends JFrame {


    private GenerateRules loadData;
    private File dataFile ;
    private JTextField attributeFileField;
    private JList<String> stableAttributesList;
    private JComboBox<String> decisionStartValueComboBox;
    private File attributeFile;
    private JFrame firstFrame;
    private JTextField dataFileField;
    private JTextField minSupportTf;
    private JTextField minConfTextField;
    private JComboBox<String> decisionAttributeComboBox;
    private JComboBox<String> decisionEndValueComboBox;




    public GUI(){
        initialize();
    }

    public void initialize(){
        firstFrame= new JFrame();
        firstFrame.setBounds(200, 200, 800, 400);
        firstFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        firstFrame.getContentPane().setLayout(null);

        firstFrame.setTitle("Action Rules");
        firstFrame.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
        firstFrame.setSize(750,400);
        firstFrame.setLocationRelativeTo(null);
        firstFrame.setResizable(true);



        dataFileField = new JTextField();
        dataFileField.setEditable(false);
        dataFileField.setBounds(65, 5, 320, 22);
        firstFrame.getContentPane().add(dataFileField);
        dataFileField.setColumns(10);

        JButton attributeFileBtn = new JButton("Select attribute file");
        attributeFileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser selectedFile = new JFileChooser();
                int returnVal = selectedFile.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    attributeFile =selectedFile.getSelectedFile();
                    attributeFileField.setText(attributeFile.getPath());
                }else{
                    System.out.println("Error while Selecting the File");
                }
            }
        });

        attributeFileBtn.setBounds(400, 33, 183, 25);
        firstFrame.getContentPane().add(attributeFileBtn);

        JButton dataFileBtn = new JButton("Select data file");

        dataFileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser selectFile = new JFileChooser();
                int returnVal = selectFile.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    dataFile =  selectFile.getSelectedFile();
                    dataFileField.setText(dataFile.getPath());
                }else{
                    System.out.println("Error while Selecting the File");
                }
            }
        });

        dataFileBtn.setBounds(400, 5, 128, 25);
        firstFrame.getContentPane().add(dataFileBtn);


        attributeFileField = new JTextField();
        attributeFileField.setEditable(false);

        attributeFileField.setColumns(10);
        attributeFileField.setBounds(65, 33, 320, 22);
        firstFrame.getContentPane().add(attributeFileField);


        JButton loadFileBtn = new JButton("Load Data");
        loadFileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {

                loadData= new GenerateRules();
                loadData.readFile(attributeFile, dataFile);
                setDecisionAttributes();
                setStableAttributes();

            }
        });



        decisionAttributeComboBox = new JComboBox<String>();
        decisionAttributeComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                if(arg0.getStateChange() == ItemEvent.SELECTED) {
                    decisionStartValueComboBox.removeAllItems();
                    decisionEndValueComboBox.removeAllItems();

                    HashSet<String> distinctValues = loadData.getUniqueValues((String)arg0.getItem());

                    if(distinctValues==null || distinctValues.size()==0){

                    }else{
                        for(String value : distinctValues) {
                            decisionStartValueComboBox.addItem(value);
                            decisionEndValueComboBox.addItem(value);
                    }
                        decisionStartValueComboBox.setEnabled(true);
                        decisionEndValueComboBox.setEnabled(true);
                    }
                }else {
                    decisionStartValueComboBox.setEnabled(false);
                    decisionEndValueComboBox.setEnabled(false);
                }
            }
        });
        decisionAttributeComboBox.setBounds(5, 125, 153, 22);
        firstFrame.getContentPane().add(decisionAttributeComboBox);

        JLabel decisionAttributeLabel = new JLabel("Select Decision Attribute: ");
        decisionAttributeLabel.setBounds(5, 100, 300, 16);
        firstFrame.getContentPane().add(decisionAttributeLabel);


        decisionStartValueComboBox= new JComboBox<String>();
        decisionStartValueComboBox.setBounds(265, 125, 88, 22);
        firstFrame.getContentPane().add(decisionStartValueComboBox);

        decisionEndValueComboBox = new JComboBox<String>();
        decisionEndValueComboBox.setBounds(420, 125, 88, 22);
        firstFrame.getContentPane().add(decisionEndValueComboBox);


        JLabel stableAttLabel = new JLabel("Select Stable attributes:");
        stableAttLabel.setBounds(5, 150, 300, 16);
        firstFrame.getContentPane().add(stableAttLabel);


        JLabel decisionAttStart = new JLabel("Initial Value:");
        decisionAttStart.setBounds(265, 100, 82, 16);
        firstFrame.getContentPane().add(decisionAttStart);

        JLabel decisionAttEnd = new JLabel("Final Value:");
        decisionAttEnd.setBounds(420, 100, 82, 16);
        firstFrame.getContentPane().add(decisionAttEnd);

        JScrollPane stableAttscrollPane = new JScrollPane();
        stableAttscrollPane.setBounds(5, 170, 128, 142);
        firstFrame.getContentPane().add(stableAttscrollPane);

        stableAttributesList = new JList<String>();
        stableAttscrollPane.setViewportView(stableAttributesList);

        JLabel minSupportlabel = new JLabel("Minimum Support:");
        minSupportlabel.setBounds(195, 170, 120, 16);
        firstFrame.getContentPane().add(minSupportlabel);

        JLabel minimumConfidenceLabel = new JLabel("Minimum Confidence:");
        minimumConfidenceLabel.setBounds(410, 170, 150, 16);
        firstFrame.getContentPane().add(minimumConfidenceLabel);

        minSupportTf = new JTextField();
        minSupportTf.setBounds(325, 170, 44, 22);
        firstFrame.getContentPane().add(minSupportTf);
        minSupportTf.setColumns(10);

        minConfTextField = new JTextField();
        minConfTextField.setColumns(10);
        minConfTextField.setBounds(560, 170, 44, 22);
        firstFrame.getContentPane().add(minConfTextField);

        JLabel percentLabel = new JLabel("%");
        percentLabel.setBounds(610, 172, 10, 10);
        firstFrame.getContentPane().add(percentLabel);

        loadFileBtn.setBounds(150, 65, 109, 25);
        firstFrame.getContentPane().add(loadFileBtn);



        JButton generateActionRulesBtn = new JButton("Generate Action Rules");
        generateActionRulesBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                boolean isValid = true;


                HashSet<String> stable = new HashSet<String>();
                stable.addAll(stableAttributesList.getSelectedValuesList());
                loadData.setFlexibleAttributes(stable);

                if(stable.contains((String)decisionAttributeComboBox.getSelectedItem())){
                    JOptionPane.showMessageDialog(null, "Decision attribute cannot be stable.",
                            "Decision attribute error", JOptionPane.ERROR_MESSAGE);
                    isValid = false;
                }

                try {
                    if(Integer.parseInt(minSupportTf.getText()) <= 0 ||
                            Integer.parseInt(minConfTextField.getText()) < 0) {
                        isValid = false;
                        JOptionPane.showMessageDialog(null, "Support and confidence values must be greater than 0",
                                "Value error", JOptionPane.ERROR_MESSAGE);
                    }
                }catch(NullPointerException err) {
                    isValid = false;
                    JOptionPane.showMessageDialog(null, "Must enter support and confidence values",
                            "Value missing", JOptionPane.ERROR_MESSAGE);
                }catch(NumberFormatException err) {
                    isValid = false;
                    JOptionPane.showMessageDialog(null, "Support and confidence values must be integers",
                            "Value error", JOptionPane.ERROR_MESSAGE);
                }

                if(isValid) {
                    loadData.setSupportAndConfidence(Integer.parseInt(minSupportTf.getText()),
                            Integer.parseInt(minConfTextField.getText()));

                    String decisionName = (String)decisionAttributeComboBox.getSelectedItem();

                    loadData.setDecisionAttributes(decisionName + ((String)decisionStartValueComboBox.getSelectedItem()),
                            decisionName + (String)decisionEndValueComboBox.getSelectedItem());

                    loadData.generateRules();
                    JOptionPane.showMessageDialog(null, "Action Rules and Certain Rules are generated, please open output.txt for complete output",
                            "Execution Result", JOptionPane.INFORMATION_MESSAGE);

                }

            }
        });

        generateActionRulesBtn.setBounds(300, 283, 200, 25);
        firstFrame.getContentPane().add(generateActionRulesBtn);
        firstFrame.setVisible(true);
    }

    public static void main(String[] args){
        GUI uf= new GUI();

    }
    private void setDecisionAttributes() {
        List<String> attributeNames = loadData.attributeNames;

        decisionAttributeComboBox.removeAllItems();
        decisionAttributeComboBox.addItem("");
        for(String name : attributeNames) {
            decisionAttributeComboBox.addItem(name);
        }
    }

    private void setStableAttributes(){
        ArrayList<String> attributes= loadData.attributeNames;
        String[] tempArray=attributes.toArray(new String[attributes.size()]);
        stableAttributesList.setListData(tempArray);
    }
}

package org.im.dc.client.ui;

public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbUser = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        cbState = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtWord = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtText = new javax.swing.JTextField();
        labelSelected = new javax.swing.JLabel();
        btnSearch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableArticles = new javax.swing.JTable();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableIssues = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableNews = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        btnSettings = new javax.swing.JButton();
        btnStat = new javax.swing.JButton();
        btnUsers = new javax.swing.JButton();
        btnAddWords = new javax.swing.JButton();
        btnAddArticle = new javax.swing.JButton();
        btnValidateFull = new javax.swing.JButton();
        btnPreview = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("MainFrame.title")); // NOI18N

        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("hor"); // NOI18N

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(bundle.getString("MainFrame.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel1, gridBagConstraints);

        cbUser.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(cbUser, gridBagConstraints);

        jLabel4.setText(bundle.getString("MainFrame.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel4, gridBagConstraints);

        cbState.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(cbState, gridBagConstraints);

        jLabel2.setText(bundle.getString("MainFrame.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel2, gridBagConstraints);

        txtWord.setColumns(15);
        txtWord.setText(bundle.getString("MainFrame.txtWord.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(txtWord, gridBagConstraints);

        jLabel3.setText(bundle.getString("MainFrame.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel3, gridBagConstraints);

        txtText.setColumns(15);
        txtText.setText(bundle.getString("MainFrame.txtText.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(txtText, gridBagConstraints);

        labelSelected.setText(bundle.getString("MainFrame.labelSelected.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(labelSelected, gridBagConstraints);

        btnSearch.setText(bundle.getString("MainFrame.btnSearch.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(btnSearch, gridBagConstraints);

        jPanel2.add(jPanel3, java.awt.BorderLayout.NORTH);

        tableArticles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Артыкул", "Стан", "Памылкі"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableArticles.setName("list"); // NOI18N
        tableArticles.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane1.setViewportView(tableArticles);
        if (tableArticles.getColumnModel().getColumnCount() > 0) {
            tableArticles.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("MainFrame.list.columnModel.title0")); // NOI18N
            tableArticles.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("MainFrame.list.columnModel.title1")); // NOI18N
        }

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel2);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);
        jSplitPane2.setName("ver"); // NOI18N

        tableIssues.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {}
            },
            new String [] {

            }
        ));
        tableIssues.setToolTipText(bundle.getString("MainFrame.tableIssues.toolTipText")); // NOI18N
        tableIssues.setName("issues"); // NOI18N
        tableIssues.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(tableIssues);

        jSplitPane2.setTopComponent(jScrollPane2);

        tableNews.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {}
            },
            new String [] {

            }
        ));
        tableNews.setToolTipText(bundle.getString("MainFrame.tableNews.toolTipText")); // NOI18N
        tableNews.setName("news"); // NOI18N
        tableNews.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(tableNews);

        jSplitPane2.setRightComponent(jScrollPane3);

        jSplitPane1.setRightComponent(jSplitPane2);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnSettings.setText(bundle.getString("MainFrame.btnSettings.text")); // NOI18N
        jPanel1.add(btnSettings);

        btnStat.setText(bundle.getString("MainFrame.btnStat.text")); // NOI18N
        jPanel1.add(btnStat);

        btnUsers.setText(bundle.getString("MainFrame.btnUsers.text")); // NOI18N
        jPanel1.add(btnUsers);

        btnAddWords.setText(bundle.getString("MainFrame.btnAddWords.text")); // NOI18N
        jPanel1.add(btnAddWords);

        btnAddArticle.setText(bundle.getString("MainFrame.btnAddArticle.text")); // NOI18N
        jPanel1.add(btnAddArticle);

        btnValidateFull.setText(bundle.getString("MainFrame.btnValidateFull.text")); // NOI18N
        jPanel1.add(btnValidateFull);

        btnPreview.setText(bundle.getString("MainFrame.btnPreview.text")); // NOI18N
        jPanel1.add(btnPreview);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton btnAddArticle;
    public javax.swing.JButton btnAddWords;
    public javax.swing.JButton btnPreview;
    public javax.swing.JButton btnSearch;
    public javax.swing.JButton btnSettings;
    public javax.swing.JButton btnStat;
    public javax.swing.JButton btnUsers;
    public javax.swing.JButton btnValidateFull;
    public javax.swing.JComboBox<String> cbState;
    public javax.swing.JComboBox<String> cbUser;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JLabel jLabel4;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JPanel jPanel3;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JScrollPane jScrollPane3;
    public javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JSplitPane jSplitPane2;
    public javax.swing.JLabel labelSelected;
    public javax.swing.JTable tableArticles;
    public javax.swing.JTable tableIssues;
    public javax.swing.JTable tableNews;
    public javax.swing.JTextField txtText;
    public javax.swing.JTextField txtWord;
    // End of variables declaration//GEN-END:variables
}

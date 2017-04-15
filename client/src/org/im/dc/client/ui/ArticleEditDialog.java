package org.im.dc.client.ui;

public class ArticleEditDialog extends javax.swing.JDialog {

    /**
     * Creates new form ArticleEditDialog
     */
    public ArticleEditDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
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

        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        txtWords = new javax.swing.JTextField();
        txtState = new javax.swing.JTextField();
        txtUsers = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        lblHasProposedChanges = new javax.swing.JLabel();
        lblWatched = new javax.swing.JLabel();
        lblPreview = new javax.swing.JLabel();
        lblValidationError = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel8 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableHistory = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();
        jPanel9 = new javax.swing.JPanel();
        panelLinkedFrom = new javax.swing.JPanel();
        panelLinkedExternal = new javax.swing.JPanel();
        panelEditor = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        btnChangeState = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnAddIssue = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("ArticleEditDialog.title")); // NOI18N
        setPreferredSize(new java.awt.Dimension(800, 600));

        jPanel5.setLayout(new java.awt.BorderLayout());

        txtWords.setEditable(false);
        txtWords.setToolTipText(bundle.getString("ArticleEditDialog.txtWords.toolTipText")); // NOI18N
        jPanel6.add(txtWords);

        txtState.setEditable(false);
        txtState.setToolTipText(bundle.getString("ArticleEditDialog.txtState.toolTipText")); // NOI18N
        jPanel6.add(txtState);

        txtUsers.setEditable(false);
        txtUsers.setToolTipText(bundle.getString("ArticleEditDialog.txtUsers.toolTipText")); // NOI18N
        jPanel6.add(txtUsers);

        jPanel5.add(jPanel6, java.awt.BorderLayout.WEST);

        lblHasProposedChanges.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/im/dc/client/ui/images/proposed-off.png"))); // NOI18N
        lblHasProposedChanges.setToolTipText(bundle.getString("ArticleEditDialog.lblHasProposedChanges.toolTipText")); // NOI18N
        jPanel7.add(lblHasProposedChanges);

        lblWatched.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/im/dc/client/ui/images/watch-off.png"))); // NOI18N
        lblWatched.setToolTipText(bundle.getString("ArticleEditDialog.lblWatched.toolTipText")); // NOI18N
        jPanel7.add(lblWatched);

        lblPreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/im/dc/client/ui/images/show-text.png"))); // NOI18N
        lblPreview.setToolTipText(bundle.getString("ArticleEditDialog.lblPreview.toolTipText")); // NOI18N
        jPanel7.add(lblPreview);

        jPanel5.add(jPanel7, java.awt.BorderLayout.EAST);

        lblValidationError.setForeground(new java.awt.Color(255, 0, 0));
        lblValidationError.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel5.add(lblValidationError, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel5, java.awt.BorderLayout.NORTH);

        jSplitPane1.setDividerLocation(400);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("hor"); // NOI18N

        jPanel8.setLayout(new java.awt.BorderLayout());

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);

        tableHistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tableHistory.setToolTipText(bundle.getString("ArticleEditDialog.tableHistory.toolTipText")); // NOI18N
        tableHistory.setName("history"); // NOI18N
        tableHistory.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(tableHistory);

        jSplitPane2.setBottomComponent(jScrollPane1);

        txtNotes.setColumns(20);
        txtNotes.setRows(5);
        txtNotes.setToolTipText(bundle.getString("ArticleEditDialog.txtNotes.toolTipText")); // NOI18N
        jScrollPane2.setViewportView(txtNotes);

        jSplitPane2.setLeftComponent(jScrollPane2);

        jPanel8.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.Y_AXIS));

        panelLinkedFrom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        jPanel9.add(panelLinkedFrom);

        panelLinkedExternal.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        jPanel9.add(panelLinkedExternal);

        jPanel8.add(jPanel9, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(jPanel8);
        jSplitPane1.setLeftComponent(panelEditor);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        btnChangeState.setText(bundle.getString("ArticleEditDialog.btnChangeState.text")); // NOI18N
        jPanel4.add(btnChangeState);

        jPanel2.add(jPanel4, java.awt.BorderLayout.WEST);

        btnAddIssue.setText(bundle.getString("ArticleEditDialog.btnAddIssue.text")); // NOI18N
        jPanel3.add(btnAddIssue);

        btnSave.setText(bundle.getString("ArticleEditDialog.btnSave.text")); // NOI18N
        jPanel3.add(btnSave);

        jPanel2.add(jPanel3, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

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
            java.util.logging.Logger.getLogger(ArticleEditDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ArticleEditDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ArticleEditDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ArticleEditDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ArticleEditDialog dialog = new ArticleEditDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton btnAddIssue;
    public javax.swing.JButton btnChangeState;
    public javax.swing.JButton btnSave;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JPanel jPanel3;
    public javax.swing.JPanel jPanel4;
    public javax.swing.JPanel jPanel5;
    public javax.swing.JPanel jPanel6;
    public javax.swing.JPanel jPanel7;
    public javax.swing.JPanel jPanel8;
    public javax.swing.JPanel jPanel9;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JSplitPane jSplitPane2;
    public javax.swing.JLabel lblHasProposedChanges;
    public javax.swing.JLabel lblPreview;
    public javax.swing.JLabel lblValidationError;
    public javax.swing.JLabel lblWatched;
    public javax.swing.JScrollPane panelEditor;
    public javax.swing.JPanel panelLinkedExternal;
    public javax.swing.JPanel panelLinkedFrom;
    public javax.swing.JTable tableHistory;
    public javax.swing.JTextArea txtNotes;
    public javax.swing.JTextField txtState;
    public javax.swing.JTextField txtUsers;
    public javax.swing.JTextField txtWords;
    // End of variables declaration//GEN-END:variables
}

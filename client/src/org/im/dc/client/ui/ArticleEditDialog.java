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
        lblAddComment = new javax.swing.JLabel();
        lblHasProposedChanges = new javax.swing.JLabel();
        lblWatched = new javax.swing.JLabel();
        lblPreview = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel9 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("ArticleEditDialog.title")); // NOI18N

        jPanel5.setLayout(new java.awt.BorderLayout());

        txtWords.setEditable(false);
        txtWords.setColumns(2);
        txtWords.setText(bundle.getString("ArticleEditDialog.txtWords.text")); // NOI18N
        jPanel6.add(txtWords);

        txtState.setEditable(false);
        txtState.setColumns(2);
        txtState.setText(bundle.getString("ArticleEditDialog.txtState.text")); // NOI18N
        jPanel6.add(txtState);

        txtUsers.setEditable(false);
        txtUsers.setColumns(2);
        txtUsers.setText(bundle.getString("ArticleEditDialog.txtUsers.text")); // NOI18N
        jPanel6.add(txtUsers);

        jPanel5.add(jPanel6, java.awt.BorderLayout.WEST);

        lblAddComment.setText(bundle.getString("ArticleEditDialog.lblAddComment.text")); // NOI18N
        jPanel7.add(lblAddComment);

        lblHasProposedChanges.setText(bundle.getString("ArticleEditDialog.lblHasProposedChanges.text")); // NOI18N
        jPanel7.add(lblHasProposedChanges);

        lblWatched.setText(bundle.getString("ArticleEditDialog.lblWatched.text")); // NOI18N
        jPanel7.add(lblWatched);

        lblPreview.setText(bundle.getString("ArticleEditDialog.lblPreview.text")); // NOI18N
        jPanel7.add(lblPreview);

        jPanel5.add(jPanel7, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel5, java.awt.BorderLayout.NORTH);

        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setToolTipText(bundle.getString("ArticleEditDialog.jSplitPane1.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 341, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);
        jSplitPane2.setToolTipText(bundle.getString("ArticleEditDialog.jSplitPane2.toolTipText")); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jSplitPane2.setBottomComponent(jScrollPane1);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jSplitPane2.setLeftComponent(jScrollPane2);

        jPanel8.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
        flowLayout1.setAlignOnBaseline(true);
        jPanel9.setLayout(flowLayout1);

        jLabel5.setText(bundle.getString("ArticleEditDialog.jLabel5.text")); // NOI18N
        jPanel9.add(jLabel5);

        jPanel8.add(jPanel9, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(jPanel8);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jButton1.setText(bundle.getString("ArticleEditDialog.jButton1.text")); // NOI18N
        jPanel4.add(jButton1);

        jPanel2.add(jPanel4, java.awt.BorderLayout.WEST);

        jButton2.setText(bundle.getString("ArticleEditDialog.jButton2.text")); // NOI18N
        jPanel3.add(jButton2);

        jButton3.setText(bundle.getString("ArticleEditDialog.jButton3.text")); // NOI18N
        jPanel3.add(jButton3);

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
    public javax.swing.JButton jButton1;
    public javax.swing.JButton jButton2;
    public javax.swing.JButton jButton3;
    public javax.swing.JLabel jLabel5;
    public javax.swing.JPanel jPanel1;
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
    public javax.swing.JTable jTable1;
    public javax.swing.JTextArea jTextArea1;
    public javax.swing.JLabel lblAddComment;
    public javax.swing.JLabel lblHasProposedChanges;
    public javax.swing.JLabel lblPreview;
    public javax.swing.JLabel lblWatched;
    public javax.swing.JTextField txtState;
    public javax.swing.JTextField txtUsers;
    public javax.swing.JTextField txtWords;
    // End of variables declaration//GEN-END:variables
}

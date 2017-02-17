/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.im.dc.client.ui;

/**
 *
 * @author alex
 */
public class AddWordsDialog extends javax.swing.JDialog {

    /**
     * Creates new form AddWordsDialog
     */
    public AddWordsDialog(java.awt.Frame parent, boolean modal) {
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
        java.awt.GridBagConstraints gridBagConstraints;

        panelUsers = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cbInitialState = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        words = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        btnOk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("AddWordsDialog.title")); // NOI18N

        panelUsers.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(bundle.getString("AddWordsDialog.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelUsers.add(jLabel1, gridBagConstraints);

        jLabel2.setText(bundle.getString("AddWordsDialog.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelUsers.add(jLabel2, gridBagConstraints);

        jLabel3.setText(bundle.getString("AddWordsDialog.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelUsers.add(jLabel3, gridBagConstraints);

        cbInitialState.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelUsers.add(cbInitialState, gridBagConstraints);

        getContentPane().add(panelUsers, java.awt.BorderLayout.NORTH);

        words.setColumns(20);
        words.setRows(5);
        jScrollPane1.setViewportView(words);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnOk.setText(bundle.getString("AddWordsDialog.btnOk.text")); // NOI18N
        jPanel2.add(btnOk);

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
            java.util.logging.Logger.getLogger(AddWordsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddWordsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddWordsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddWordsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddWordsDialog dialog = new AddWordsDialog(new javax.swing.JFrame(), true);
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
    public javax.swing.JButton btnOk;
    public javax.swing.JComboBox<String> cbInitialState;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JPanel panelUsers;
    public javax.swing.JTextArea words;
    // End of variables declaration//GEN-END:variables
}

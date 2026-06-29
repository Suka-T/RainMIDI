package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import plg.I18n;
import plg.Utility;

public class BgImageSelectDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JPanel imagePanel;
    private JButton btnImageLoadButton;
    private JCheckBox chckbxImageValidCheckBox;
    
    private String path;
    
    private BufferedImage image = null;
    
    private boolean isCommit = false;
    private JComboBox<String> comboBoxEffect;
    private JLabel lblEffectLabel;
    
    
    public class ImagePanel extends JPanel {

        // 画像を設定するためのコンストラクタやセッター
        public ImagePanel() {
        }

        // パネルの描画処理をオーバーライド
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // コンポーネント自体の背景などの描画をクリア
            
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            if (image != null && chckbxImageValidCheckBox.isSelected()) {
                
                
                // パネルの (0, 0) の位置に画像を等倍で描画
                //g.drawImage(image, 0, 0, this);
                
                // もしパネルのサイズに合わせて画像を拡大縮小したい場合はこちら：
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                
                if (comboBoxEffect.getSelectedItem().toString().equalsIgnoreCase("Circle Vignette")) {
                    Utility.drawEffectCircleVignette(g, getWidth(), getHeight());
                }
                else if (comboBoxEffect.getSelectedItem().toString().equalsIgnoreCase("Top Vignette")) {
                    Utility.drawEffectTopVignette(g, getWidth(), getHeight());
                }
            }
        }

        // 動的に画像を変更したい場合のメソッド
        public void setImage(BufferedImage image) {
            repaint(); // 再描画を要求
        }
    }

    /**
     * Create the dialog.
     */
    public BgImageSelectDialog(Frame owner) {
        super(owner);
        setResizable(false);
        setTitle("Background Image Select");
        setModal(true);
        setBounds(100, 100, 475, 380);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);
        this.setTransferHandler(new DropFileHandler());
        
        imagePanel = new ImagePanel();
        imagePanel.setBounds(12, 10, 426, 256);
        contentPanel.add(imagePanel);
        
        btnImageLoadButton = new JButton("Load");
        btnImageLoadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAndLoadImage();
                updateImage();
            }
        });
        btnImageLoadButton.setBounds(12, 271, 91, 21);
        contentPanel.add(btnImageLoadButton);
        
        chckbxImageValidCheckBox = new JCheckBox("Valid");
        chckbxImageValidCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imagePanel.repaint();
            }
        });
        chckbxImageValidCheckBox.setBounds(111, 271, 103, 21);
        contentPanel.add(chckbxImageValidCheckBox);
        
        comboBoxEffect = new JComboBox<String>();
        comboBoxEffect.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateImage();
            }
        });
        comboBoxEffect.setModel(new DefaultComboBoxModel<String>(new String[] {"None", "Circle Vignette", "Top Vignette"}));
        comboBoxEffect.setBounds(344, 271, 103, 21);
        contentPanel.add(comboBoxEffect);
        
        lblEffectLabel = new JLabel("Effect");
        lblEffectLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        lblEffectLabel.setBounds(283, 275, 49, 13);
        contentPanel.add(lblEffectLabel);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        isCommit = true;
                        setVisible(false);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
                
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        isCommit = false;
                        setVisible(false);
                    }
                });
                buttonPane.add(cancelButton);
            }
        }
    }
    
    public void showDialog(boolean valid, String path, String effect) {
        this.path = path;
        this.image = null;
        
        if (effect.equals("circle_vignette")) {
            comboBoxEffect.setSelectedItem("Circle Vignette");
        }
        else if (effect.equals("top_vignette")) {
            comboBoxEffect.setSelectedItem("Top Vignette");
        }
        else {
            comboBoxEffect.setSelectedItem("None");
        }
        
        chckbxImageValidCheckBox.setSelected(valid);
        
        updateImage();
        
        this.setTitle(I18n.t("tab.bgImageSelect"));
        this.lblEffectLabel.setText(I18n.t("label.effect"));
        this.btnImageLoadButton.setText(I18n.t("button.load"));
        this.chckbxImageValidCheckBox.setText(I18n.t("chckbx.valid"));
        
        isCommit = false;
        setVisible(true);
    }
    
    private void updateImage() {
        File file = new File(path);
        System.out.println(file.getAbsolutePath());
        
        try {
            image = ImageIO.read(file);
            
        } catch (IOException e) {
            image = null;
            e.printStackTrace();
        }
        
        imagePanel.repaint();
    }
    
    private void selectAndLoadImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image File (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif"
        );
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            this.path = selectedFile.getAbsolutePath();
        }
    }
    
    public String getEffect() {
        String selected = comboBoxEffect.getSelectedItem().toString();
        if (selected.equals("Circle Vignette")) {
            return "circle_vignette";
        }
        else if (selected.equals("Top Vignette")) {
            return "top_vignette";
        }
        else {
            return "none";
        }
    }
    
    public String getPath() {
        return path;
    }
    
    public boolean getValidState() {
        return chckbxImageValidCheckBox.isSelected();
    }

    public boolean isCommit() {
        return isCommit;
    }
    

    /**
     *
     * ドラッグ＆ドロップハンドラー
     *
     */
    public class DropFileHandler extends TransferHandler {
        /**
         * ドロップされたものを受け取るか判断 (アイテムのときだけ受け取る)
         */
        @Override
        public boolean canImport(TransferSupport support) {
            if (support.isDrop() == false) {
                // ドロップ操作でない場合は受け取らない
                return false;
            }

            if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) == false) {
                // ファイルでない場合は受け取らない
                return false;
            }

            return true;
        }

        /**
         * ドロップされたアイテムを受け取る
         */
        @Override
        public boolean importData(TransferSupport support) {
            // ドロップアイテム受理の確認
            if (canImport(support) == false) {
                return false;
            }

            // ドロップ処理
            Transferable t = support.getTransferable();
            try {
                // ドロップアイテム取得
                catchLoadItem(t.getTransferData(DataFlavor.javaFileListFlavor));
                return true;
            }
            catch (Exception e) {
                /* 受け取らない */
            }
            return false;
        }
    }

    public void catchLoadItem(Object item) {
        @SuppressWarnings("unchecked")
        List<File> files = (List<File>) item;
        // 一番先頭のファイルを取得
        if ((files != null) && (files.size() > 0)) {
            this.path = files.get(0).getAbsolutePath();
            updateImage();
        }
    }
}

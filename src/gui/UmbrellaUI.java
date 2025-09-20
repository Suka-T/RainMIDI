package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import jlib.core.JMPCoreAccessor;
import plg.SystemProperties;

public class UmbrellaUI implements MouseListener, MouseMotionListener {
    
    public static enum UmbFunction {
        None,
        PlayStop,
        InitPlay,
        FileLoad,
        OpenControl,
    }
    
    public class UmbDiv {
        public int sector = 0;
        public UmbFunction func = UmbFunction.None;
        public String title = "";
        
        public UmbDiv(int sector, UmbFunction func, String title){
            this.sector = sector;
            this.func = func;
            this.title = title;
        };
    }
    
    private List<UmbDiv> umbDivMap;

    private boolean isVisible = false;
    private int pressX = -1;
    private int pressY = -1;
    private int mouseX = -1;
    private int mouseY = -1;
    
    
    // 半径
    private final int radius = 100;
    private final int inradius = 20;

    public UmbrellaUI() {
        umbDivMap = new ArrayList<UmbDiv>();
        umbDivMap.add(new UmbDiv(0, UmbFunction.InitPlay, "Init Pos"));
        umbDivMap.add(new UmbDiv(1, UmbFunction.PlayStop, "Play/Stop"));
        umbDivMap.add(new UmbDiv(2, UmbFunction.FileLoad, "Load"));
        umbDivMap.add(new UmbDiv(3, UmbFunction.OpenControl, "Control"));
    }
    
    public void execUmb(UmbDiv div) {
        if (div == null) return;
        
        switch (div.func) {
            case InitPlay: {
                JMPCoreAccessor.getSoundManager().stop();
                JMPCoreAccessor.getSoundManager().initPosition();
                break;
            }
            case OpenControl: {
                JMPCoreAccessor.getWindowManager().getMainWindow().showWindow();
                break;
            }
            case PlayStop: {
                JMPCoreAccessor.getSoundManager().togglePlayStop();
                break;
            }
            case FileLoad: {
                JFileChooser chooser = new JFileChooser();

                // 複数選択を許可
                chooser.setMultiSelectionEnabled(true);
                
                String path = SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_FILE_DEFAULT_PATH).getDataString();
                File dir = new File(path);
                if (path.isEmpty() == false) {
                    if (dir.exists() && dir.isDirectory()) {
                        chooser.setCurrentDirectory(dir);
                    }
                }

                // ダイアログを開く
                int result = chooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles(); // 複数ファイル
                    
                    SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_FILE_DEFAULT_PATH).setObject(files[0].getParent());
                    
                    JMPCoreAccessor.getSoundManager().stop();
                    if (files.length >= 2) {
                        JMPCoreAccessor.getFileManager().loadDualFileToPlay(files[0], files[1]);
                    }
                    else {
                        JMPCoreAccessor.getFileManager().loadFileToPlay(files[0]);
                    }
                }
                break;
            }
            case None: 
            default:
                break;
        }
    }
    
    protected UmbDiv getSectorUmb(int sector) {
        for (UmbDiv div : umbDivMap) {
            if (sector == div.sector) {
                return div;
            }
        }
        return null;
    }
    
    public int getCurrentSector() {
        int centerX = pressX;
        int centerY = pressY;
        int sector = -1;
        
        double dx = Math.abs(mouseX - centerX);
        double dy = Math.abs(mouseY - centerY);
        double distance = Math.hypot(dx, dy);
        
        if (inradius < distance && distance <= radius && centerX != -1 && centerY != -1) {
            // 点1から点2への角度 (ラジアン)
            double angleRad = Math.atan2(mouseY - centerY, mouseX - centerX);

            // ラジアンを度に変換
            double angleDeg = Math.toDegrees(angleRad) - 90;

            // 0～360度の範囲に正規化
            if (angleDeg < 0) {
                angleDeg += 360;
            }
            
            if (angleDeg >= 315 || angleDeg < 45) {
                sector = 3;
            } else if (angleDeg >= 45 && angleDeg < 135) {
                sector = 2;
            } else if (angleDeg >= 135 && angleDeg < 225) {
                sector = 1;
            } else {
                sector = 0;
            }
        }
        return sector;
    }

    public void paint(Graphics g) {
        if (isVisible == true) {
            int centerX = pressX;
            int centerY = pressY;
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(2));
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));

            if (centerX != -1 && centerY != -1) {
                
                // 6つのピースを個別に描画
                int curSector = getCurrentSector();
                for (int i = 0; i < 4; i++) {
                    int startAngle = i * 90 - 45;
                    
                    // 特定のピースの色を変更
                    if (i == curSector) { // 例：最初のピースを赤色に
                        g2d.setColor(new Color(255, 140, 20, 128));
                    } else {
                        g2d.setColor(new Color(255, 255, 255, 128));
                    }

                    // 扇形を塗りつぶす
                    g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, 90);
                }
                
                for (int i = 0; i < 4; i++) {
                    UmbDiv div = getSectorUmb(i);
                    if (div != null) {
                        int strX, strY;
                        String text = div.title;
                        FontMetrics fm = g.getFontMetrics();
                        if (i == curSector) { // 例：最初のピースを赤色に
                            g2d.setColor(Color.WHITE);
                        }
                        else {
                            g2d.setColor(Color.BLACK);
                        }
                        int ubrad = radius + 10;
                        int strW = fm.stringWidth(text);
                        if (i == 1) {
                            strX = centerX - strW / 2;
                            strY = centerY - ubrad / 2;
                        }
                        else if (i == 0) {
                            strX = centerX + (ubrad - strW) / 2;
                            strY = centerY + 7;
                        }
                        else if (i == 3) {
                            strX = centerX - strW / 2;
                            strY = centerY + ubrad / 2;
                        }
                        else if (i == 2) {
                            strX = centerX - ubrad + (ubrad - strW) / 2;
                            strY = centerY + 7;
                        }
                        else {
                            strX = centerX;
                            strY = centerY;
                        }
                        g2d.drawString(text, strX, strY);
                    }
                }
                
                g2d.setColor(new Color(30, 30, 30, 128));
                // 外周の線
                g2d.fillOval(centerX - inradius, centerY - inradius, inradius * 2, inradius * 2);

                // 分割線と外周の線（見た目を整えるために重ねて描画）
                g2d.setColor(Color.WHITE);
                // 外周の線
                g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

                // 分割線
                for (int i = 0; i < 4; i++) {
                    double angle = Math.toRadians(i * 90 - 45);
                    int endX = (int) (centerX + radius * Math.cos(angle));
                    int endY = (int) (centerY + radius * Math.sin(angle));
                    g2d.drawLine(centerX, centerY, endX, endY);
                }
                
//                g2d.setColor(Color.MAGENTA);
//                g2d.drawLine(centerX, centerY, mouseX, mouseY);
//                g2d.setColor(Color.CYAN);
//                double angleDeg = (Math.toDegrees(Math.atan2(mouseY - centerY, mouseX - centerX)) - 90);
//                if (angleDeg < 0) {
//                    angleDeg += 360;
//                }
//                g2d.drawString("dig=" + angleDeg, 30, 30);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressX != -1 && pressY != -1) {
            mouseX = e.getX();
            mouseY = e.getY();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (pressX != -1 && pressY != -1) {
            mouseX = e.getX();
            mouseY = e.getY();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
            if (isVisible == false) {
                pressX = e.getX();
                pressY = e.getY();
                mouseX = pressX;
                mouseY = pressY;
            }
            isVisible = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            isVisible = false;
            
            int sector = getCurrentSector();
            if (sector != -1) {
                UmbDiv div = getSectorUmb(sector);
                execUmb(div);
            }
            
            pressX = -1;
            pressY = -1;
            mouseX = -1;
            mouseY = -1;
        }
    }

}

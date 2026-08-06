//Para compilar:
//  Linux: javac -cp .:jSerialComm-1.3.11.jar LeerSerial.java
//  Windows: javac -cp .;jSerialComm-1.3.11.jar LeerSerial.java
//Para ejecutar:
//  Linux: java -cp .:jSerialComm-1.3.11.jar LeerSerial
//  Windows: java -cp .;jSerialComm-1.3.11.jar LeerSerial

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import com.fazecast.jSerialComm.*;

public class LeerSerial extends JFrame implements ActionListener {
    private int BAUDRATE = 115200;

    private JPanel panel;
    private JProgressBar barraLuz; 
    private JLabel lblMedida;
    private JTextField txtMedida;
    private JLabel lblEstadoIluminacion;
    private JButton btnIniciar;
    private JButton btnDetener;
    private JButton btnFase1;
    private JButton btnFase2;
    private JButton btnFase3;
    private JButton btnFase4;

    private Monitor monitor;
    private Thread t;
    private SerialPort comPort;

    
    private volatile boolean corriendo = false;

    
    private final Color COLOR_FONDO = Color.decode("#e1c7e6");       // Morado pastel muy claro
    private final Color COLOR_TEXTO = Color.decode("#311B92");       // Morado oscuro profundo
    private final Color COLOR_BOTON_IN = Color.decode("#7E57C2");    // Morado vibrante (Iniciar)
    private final Color COLOR_BOTON_DET = Color.decode("#c9b8e8");   // Morado claro (Detener)
    private final Color COLOR_APAGADO = Color.decode("#E1BEE7");     // Violeta apagado
    private final Color COLOR_ENCENDIDO = Color.decode("#4A148C");   // Morado intenso brillante

    public LeerSerial() {
        configurarPuerto();

        this.setTitle("Monitoreo de luz");
       
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(COLOR_FONDO);

        Font fuenteEtiquetas = new Font("SansSerif", Font.BOLD, 14);
        Font fuenteCampos = new Font("SansSerif", Font.BOLD, 16);
        Font fuenteBotones = new Font("SansSerif", Font.BOLD, 12);

        // Barra de Nivel Vertical 
        barraLuz = new JProgressBar(JProgressBar.VERTICAL, 0, 100);
        barraLuz.setBounds(250, 150, 30, 150);
        barraLuz.setStringPainted(true); // Muestra el porcentaje 
        barraLuz.setFont(fuenteBotones);
        barraLuz.setForeground(COLOR_ENCENDIDO); 
        barraLuz.setBackground(Color.WHITE);
        barraLuz.setBorder(BorderFactory.createLineBorder(COLOR_BOTON_DET, 1));
        panel.add(barraLuz);

       
        lblMedida = new JLabel("Medida:");
        lblMedida.setBounds(75, 25, 70, 30);
        lblMedida.setFont(fuenteEtiquetas);
        lblMedida.setForeground(COLOR_TEXTO);
        panel.add(lblMedida);

        txtMedida = new JTextField();
        txtMedida.setBounds(155, 25, 100, 30);
        txtMedida.setEditable(false);
        txtMedida.setFont(fuenteCampos);
        txtMedida.setHorizontalAlignment(JTextField.CENTER);
        txtMedida.setBackground(Color.WHITE);
        txtMedida.setForeground(COLOR_ENCENDIDO);
        txtMedida.setBorder(BorderFactory.createLineBorder(COLOR_BOTON_DET, 2));
        panel.add(txtMedida);

        JLabel lblUnidades = new JLabel("%");
        lblUnidades.setBounds(265, 25, 40, 30);
        lblUnidades.setFont(fuenteEtiquetas);
        lblUnidades.setForeground(COLOR_TEXTO);
        panel.add(lblUnidades);

        lblEstadoIluminacion = new JLabel("Estado: :(");
        lblEstadoIluminacion.setBounds(305, 25, 150, 30);
        lblEstadoIluminacion.setFont(fuenteEtiquetas);
        lblEstadoIluminacion.setForeground(COLOR_TEXTO);
        panel.add(lblEstadoIluminacion);

        btnIniciar = new JButton("Iniciar");
        btnIniciar.setBounds(75, 75, 110, 35);
        btnIniciar.setFont(fuenteBotones);
        btnIniciar.setBackground(COLOR_BOTON_IN);
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFocusPainted(false);
        btnIniciar.setBorder(BorderFactory.createRaisedBevelBorder());
        btnIniciar.addActionListener(this);
        panel.add(btnIniciar);

        btnDetener = new JButton("Detener");
        btnDetener.setBounds(200, 75, 110, 35);
        btnDetener.setFont(fuenteBotones);
        btnDetener.setBackground(COLOR_BOTON_DET);
        btnDetener.setForeground(COLOR_TEXTO);
        btnDetener.setFocusPainted(false);
        btnDetener.setBorder(BorderFactory.createRaisedBevelBorder());
        btnDetener.addActionListener(this);
        btnDetener.setEnabled(false);
        panel.add(btnDetener);

        btnFase1 = new JButton("Fase 1");
        btnFase1.setBounds(110, 150, 80, 30);
        btnFase1.setFont(fuenteBotones);
        btnFase1.setBackground(COLOR_APAGADO);
        btnFase1.setForeground(COLOR_TEXTO);
        btnFase1.setEnabled(false);
        panel.add(btnFase1);

        btnFase2 = new JButton("Fase 2");
        btnFase2.setBounds(110, 190, 80, 30);
        btnFase2.setFont(fuenteBotones);
        btnFase2.setBackground(COLOR_APAGADO);
        btnFase2.setForeground(COLOR_TEXTO);
        btnFase2.setEnabled(false);
        panel.add(btnFase2);

        btnFase3 = new JButton("Fase 3");
        btnFase3.setBounds(110, 230, 80, 30);
        btnFase3.setFont(fuenteBotones);
        btnFase3.setBackground(COLOR_APAGADO);
        btnFase3.setForeground(COLOR_TEXTO);
        btnFase3.setEnabled(false);
        panel.add(btnFase3);

        btnFase4 = new JButton("Fase 4");
        btnFase4.setBounds(110, 270, 80, 30);
        btnFase4.setFont(fuenteBotones);
        btnFase4.setBackground(COLOR_APAGADO);
        btnFase4.setForeground(COLOR_TEXTO);
        btnFase4.setEnabled(false);
        panel.add(btnFase4);

        this.add(panel);
        this.setSize(700, 450); 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void configurarPuerto() {
        comPort = SerialPort.getCommPort("COM6");
        comPort.setBaudRate(BAUDRATE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnIniciar) {
            if (comPort != null && comPort.openPort()) {
               
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {}
               
                if (comPort.bytesAvailable() > 0) {
                    byte[] bufferBasura = new byte[comPort.bytesAvailable()];
                    comPort.readBytes(bufferBasura, bufferBasura.length); // Limpia el buffer
                }

                corriendo = true;
                btnIniciar.setEnabled(false);
                btnDetener.setEnabled(true);
                monitor = new Monitor();
                t = new Thread(monitor);
                t.start();
            } else {
                JOptionPane.showMessageDialog(this, "Error al abrir el puerto serial.");
            }
        }
        if (e.getSource() == btnDetener) {
            corriendo = false;
            if (comPort != null) {
                comPort.closePort();
            }
            btnIniciar.setEnabled(true);
            btnDetener.setEnabled(false);
           
            txtMedida.setText("");
            barraLuz.setValue(0); // Limpia la barra al detenerse
            lblEstadoIluminacion.setText("Estado: :(");
            btnFase1.setBackground(COLOR_APAGADO); btnFase1.setForeground(COLOR_TEXTO); btnFase1.setEnabled(false);
            btnFase2.setBackground(COLOR_APAGADO); btnFase2.setForeground(COLOR_TEXTO); btnFase2.setEnabled(false);
            btnFase3.setBackground(COLOR_APAGADO); btnFase3.setForeground(COLOR_TEXTO); btnFase3.setEnabled(false);
            btnFase4.setBackground(COLOR_APAGADO); btnFase4.setForeground(COLOR_TEXTO); btnFase4.setEnabled(false);
        }
    }

    private void actualizarInterfaz(int porcentajeLuz) {
        txtMedida.setText(String.valueOf(porcentajeLuz));
        barraLuz.setValue(porcentajeLuz); // Modifica el nivel de la barra en tiempo real

       
        btnFase1.setEnabled(true);
        btnFase2.setEnabled(true);
        btnFase3.setEnabled(true);
        btnFase4.setEnabled(true);

        if (porcentajeLuz < 25) {
            lblEstadoIluminacion.setText("Estado: Umbra");
            btnFase1.setBackground(COLOR_ENCENDIDO); btnFase1.setForeground(Color.WHITE);
            btnFase2.setBackground(COLOR_APAGADO);   btnFase2.setForeground(COLOR_TEXTO);
            btnFase3.setBackground(COLOR_APAGADO);   btnFase3.setForeground(COLOR_TEXTO);
            btnFase4.setBackground(COLOR_APAGADO);   btnFase4.setForeground(COLOR_TEXTO);
        }
        else if (porcentajeLuz < 50) {
            lblEstadoIluminacion.setText("Estado: Penumbra");
            btnFase1.setBackground(COLOR_ENCENDIDO); btnFase1.setForeground(Color.WHITE);
            btnFase2.setBackground(COLOR_ENCENDIDO); btnFase2.setForeground(Color.WHITE);
            btnFase3.setBackground(COLOR_APAGADO);   btnFase3.setForeground(COLOR_TEXTO);
            btnFase4.setBackground(COLOR_APAGADO);   btnFase4.setForeground(COLOR_TEXTO);
        }
        else if (porcentajeLuz < 75) {
            lblEstadoIluminacion.setText("Estado: Luminoso");
            btnFase1.setBackground(COLOR_ENCENDIDO); btnFase1.setForeground(Color.WHITE);
            btnFase2.setBackground(COLOR_ENCENDIDO); btnFase2.setForeground(Color.WHITE);
            btnFase3.setBackground(COLOR_ENCENDIDO); btnFase3.setForeground(Color.WHITE);
            btnFase4.setBackground(COLOR_APAGADO);   btnFase4.setForeground(COLOR_TEXTO);
        }
        else {
            lblEstadoIluminacion.setText("Estado: Resplandor");
            btnFase1.setBackground(COLOR_ENCENDIDO); btnFase1.setForeground(Color.WHITE);
            btnFase2.setBackground(COLOR_ENCENDIDO); btnFase2.setForeground(Color.WHITE);
            btnFase3.setBackground(COLOR_ENCENDIDO); btnFase3.setForeground(Color.WHITE);
            btnFase4.setBackground(COLOR_ENCENDIDO); btnFase4.setForeground(Color.WHITE);
        }
    }

    private class Monitor implements Runnable {
        @Override
        public void run() {
            byte[] peticion = new byte[] { 114 }; 
           
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
            comPort.writeBytes(peticion, 1); // Lanzamos la primera solicitud
           
            // Para evitar bloqueos infinitos de hilos
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(comPort.getInputStream()))) {
               
                while (corriendo) {
                    if (!reader.ready()) {
                        Thread.sleep(40);
                        continue;
                    }

                    String v = reader.readLine();
                    if (v == null) break;
                    v = v.trim();
                   
                    if (v.isEmpty()) {
                        comPort.writeBytes(peticion, 1);
                        continue;
                    }

                    try {
                        int v_int = Integer.parseInt(v);

                     
                        int porcentajeLuz = (int) ((v_int / 1023.0) * 100);

                        if (porcentajeLuz > 100) porcentajeLuz = 100;
                        if (porcentajeLuz < 0) porcentajeLuz = 0;

                        final int finalPorcentaje = porcentajeLuz;
                       
                       
                        SwingUtilities.invokeLater(() -> actualizarInterfaz(finalPorcentaje));
                       
                    } catch (NumberFormatException nfe) {
                       
                    }
                   
                  
                    if (corriendo) {
                        comPort.writeBytes(peticion, 1);
                    }
                }
            } catch (Exception ex) {
                if (corriendo) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LeerSerial());
    }
}
package main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Controller {

    @FXML
    private Label battery;
    @FXML
    private Label adapter;
    @FXML
    private Label time;
    @FXML
    private Label timeLock;
    @FXML
    private Button apply;
    @FXML
    private Slider slider;

    private double currentLockTime;

    private RefreshThread refreshThread = new RefreshThread();

    public Controller() {
        refreshThread.start();
        refreshThread.addListeners(() -> updateLabels());
        Platform.runLater(() -> {
            currentLockTime = getCurrentLockTime();
            slider.setValue(currentLockTime);
            timeLock.setText(Integer.toString((int)currentLockTime) + " seconds");
            slider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                    timeLock.setText(Integer.toString((int)slider.getValue()) + " seconds");
                    apply.setDisable(false);
                }
            });
        });
    }

    private void updateLabels() {
        Platform.runLater(() -> {
            battery.setText(refreshThread.getList().get(0) + ", "
                    + refreshThread.getList().get(1));
            time.setText(refreshThread.getList().get(2));
            String adapterInfo = refreshThread.getAdapterInfo();
            if(adapterInfo.equals("online")) {
                slider.setDisable(true);
            } else {
                slider.setDisable(false);
            }
            adapter.setText(adapterInfo);
        });
    }

    private double getCurrentLockTime() {
        try {
            Process process = Runtime.getRuntime().exec("gsettings get org.gnome.desktop.session idle-delay");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int result = Integer.parseInt(reader.readLine().split(" ")[1]);
            process.destroy();
            reader.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void clickApply() {
        try {
            Runtime.getRuntime().exec("gsettings set org.gnome.desktop.session idle-delay " +
                    Integer.toString((int)slider.getValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        apply.setDisable(true);
    }

    public void shutdown() {
        refreshThread.shutdown();
    }

}

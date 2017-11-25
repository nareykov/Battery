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

    private int currentLockTime;
    private int initialLockTime;
    private int counter = 0;
    private double previousLevel = 100;
    private String remainingTime;

    private RefreshThread refreshThread = new RefreshThread(2);

    public Controller() {
        initialLockTime = getCurrentLockTime();
        refreshThread.start();
        refreshThread.addListeners(new DataListener() {
            @Override
            public void refreshData() {
                updateLabels();
            }
        });
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
            battery.setText(refreshThread.getChargeLevel());
            String adapterInfo = refreshThread.getAdapterInfo();
            if(adapterInfo.equals("online")) {
                setLockTime(initialLockTime);
                timeLock.setText(Integer.toString(initialLockTime) + " seconds");
                slider.setValue(initialLockTime);
                slider.setDisable(true);
            } else {
                slider.setDisable(false);
            }
            adapter.setText(adapterInfo);
            time.setText(getEstimateTime());
        });
    }

    private int getCurrentLockTime() {
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
        setLockTime((int)slider.getValue());
        apply.setDisable(true);
    }

    private void setLockTime(int time) {
        try {
            Runtime.getRuntime().exec("gsettings set org.gnome.desktop.session idle-delay " +
                    Integer.toString(time));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getEstimateTime() {

        if (counter == getMultiplier()) {
            if (previousLevel != 0) {
                Double currentLevel = Double.parseDouble(refreshThread.getChargeLevel().split("%")[0]);
                Double speed = (previousLevel - currentLevel) / (getMultiplier() * refreshThread.getSleepTime());
                Double time = currentLevel / speed;
                counter = 0;
                remainingTime =  Integer.toString((int)(time / 3600)) + ":"
                        + Integer.toString((int)((time % 3600) / 60)) + ":"
                        + Integer.toString((int)(time % 60));
                previousLevel = currentLevel;
            }
        }
        counter++;
        return remainingTime;
    }

    private int getMultiplier() {
        if(refreshThread.getSleepTime() >= 1) {
            return 5;
        } else {
            return 10;
        }
    }

    public void close() {
        setLockTime(initialLockTime);
        refreshThread.close();
    }

    public int getInitialLockTime() {
        return initialLockTime;
    }
}

package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RefreshThread extends Thread {

    private Process process = null;
    private BufferedReader reader;
    private String chargeLevel;
    private String adapterInfo;
    private List<DataListener> listeners = new ArrayList<>();

    public RefreshThread() {
        super("RefreshThread");
    }

    @Override
    public void run() {
        while (true) {
            try {
                process = Runtime.getRuntime().exec("acpitool");
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String[] batteryInfo = reader.readLine().split(" :")[1].trim().split(", ");
                chargeLevel = batteryInfo[1];
                adapterInfo = reader.readLine().split(" :")[1].trim();
                process.destroy();
                reader.close();
                notifyListeners();
                sleep(2000);
            } catch (IOException e) {
                if(process != null) process.destroy();
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyListeners() {
        for (DataListener listener: listeners){
            listener.refreshData();
        }
    }

    public void addListeners(DataListener listener) {
        this.listeners.add(listener);
    }

    public String getChargeLevel() {
        return chargeLevel;
    }

    public String getAdapterInfo() {
        return adapterInfo;
    }
}

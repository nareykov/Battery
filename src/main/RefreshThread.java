package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RefreshThread extends Thread {

    private Process process = null;
    private BufferedReader reader;
    private ArrayList<String> list = new ArrayList<>(3);
    private String adapterInfo;
    private boolean work = true;
    private List<DataListener> listeners = new ArrayList<>();

    public RefreshThread() {
        super("RefreshThread");
    }

    @Override
    public void run() {
        while (work) {
            try {
                process = Runtime.getRuntime().exec("acpitool");
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String[] batteryInfo = reader.readLine().split(" :")[1].trim().split(", ");
                list.clear();
                list.addAll(Arrays.asList(batteryInfo));
                if (batteryInfo.length == 2 ) list.add("---");
                if(batteryInfo.length == 3 && batteryInfo[0].equals("Full")) list.set(2, "---");
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

    public ArrayList<String> getList() {
        return list;
    }

    public String getAdapterInfo() {
        return adapterInfo;
    }

    public void shutdown(){
        work = false;
    }
}

package com.example.if570_lab_uts_valentinomahardhikaputra_00000074857;

// AttendanceEntry.java
public class AttendanceEntry {
    public String date;
    public String timeIn;
    public String timeHome;
    public String imageUrlMasuk;
    public String imageUrlPulang;

    public AttendanceEntry(String date, String time, String imageUrlMasuk, String timeHome, String imageUrlPulang) {
        this.date = date;
        this.timeIn = time;
        this.timeHome = timeHome;
        this.imageUrlMasuk = imageUrlMasuk;
        this.imageUrlPulang = imageUrlPulang;
    }
}

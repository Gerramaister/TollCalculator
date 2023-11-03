package src.main.java.se.layerten.tollfeecalculator.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import src.main.java.se.layerten.tollfeecalculator.models.VehicleInterface;

/**
 * The `TollCalculator` class is responsible for calculating toll fees for vehicles based on
 * their entry timestamps and vehicle type.
 *
 * This class provides a method to calculate the total toll fee for a list of entry timestamps
 * and a specific vehicle type. It takes into account toll-free vehicles and dates, as well as
 * different time-based toll fee rates.
 *
 * @author Jonathan
 * @version 1.0
 */
public class TollCalculator {

    /**
     * Calculates the total toll fee for a vehicle's entries on specific dates and times.
     *
     * @param vehicle   The vehicle for which the toll fee is calculated.
     * @param dateList  A list of entry timestamps for the vehicle.
     * @return The total toll fee for the vehicle's entries.
     */
    public int getTollFee(VehicleInterface vehicle, List<LocalDateTime> dateList) {
        if (isTollFreeVehicle(vehicle)) {
            return 0;
        }

        dateList.sort(null);

        List<LocalDateTime> feeList = new ArrayList<>();

        for (LocalDateTime date : dateList) {

            LocalTime localTime = LocalTime.from(date);

            if (isTollFreeDate(date)) {
                continue;
            }
            if (getTollFeeByTime(localTime) == 0) {
                continue;
            }
            feeList.add(date);

        }

        LocalDateTime startDateAndTime = feeList.get(0);
        int currentFee = 0;
        int hourFee = 0;
        int dayFee = 0;
        int totalFee = 0;

        for (LocalDateTime currentDateAndTime : feeList) {

            LocalTime currentTime = LocalTime.from(currentDateAndTime);
            LocalDate currentDate = LocalDate.from(currentDateAndTime);

            currentFee = getTollFeeByTime(currentTime);

            if (currentDate.equals(LocalDate.from(startDateAndTime))) { // Same day
                
                if (currentDateAndTime.isBefore(startDateAndTime.plusMinutes(60))) { // Same day and same 60min
                    
                    if (currentFee > hourFee) {
                        hourFee = currentFee;
                    }
                } else { // New hour same day
                    
                    dayFee = dayFee + hourFee;
                    hourFee = currentFee;

                    startDateAndTime = currentDateAndTime;
                }

            } else { // New day
                
                dayFee = dayFee + hourFee;
                if (dayFee > 60) {
                    dayFee = 60;
                }
                totalFee = totalFee + dayFee;
                dayFee = 0;
                startDateAndTime = currentDateAndTime;

                hourFee = currentFee;

            }

        }
        totalFee = totalFee + currentFee + dayFee;
        return totalFee;
    }

    /**
     * Retrieves the toll fee based on the time of entry.
     *
     * @param time The time of entry.
     * @return The toll fee corresponding to the entry time.
     */
    private int getTollFeeByTime(LocalTime time) {

        if ((time.isAfter(LocalTime.of(5, 59, 59, 9999)) && time.isBefore(LocalTime.of(6, 30))) || // 06:00 - 06:29
                (time.isAfter(LocalTime.of(8, 29, 59, 9999)) && time.isBefore(LocalTime.of(15, 00))) || // 08:30 – 14:59
                (time.isAfter(LocalTime.of(17, 59, 59, 9999)) && time.isBefore(LocalTime.of(18, 30))) // 18:00 – 18:29
        ) {
            return 9;
        }

        if ((time.isAfter(LocalTime.of(6, 29, 59, 9999)) && time.isBefore(LocalTime.of(7, 00))) || // 06:30–06:59
                (time.isAfter(LocalTime.of(7, 59, 59, 9999)) && time.isBefore(LocalTime.of(8, 30))) || // 08:00–08:29
                (time.isAfter(LocalTime.of(14, 59, 59, 9999)) && time.isBefore(LocalTime.of(15, 30))) || // 15:00–15:29
                (time.isAfter(LocalTime.of(16, 59, 59, 9999)) && time.isBefore(LocalTime.of(18, 0))) // 17:00–17:59
        ) {
            return 16;
        }

        if ((time.isAfter(LocalTime.of(6, 59, 59, 9999)) && time.isBefore(LocalTime.of(8, 00))) || // 07:00–07:59
                (time.isAfter(LocalTime.of(15, 29, 59, 9999)) && time.isBefore(LocalTime.of(17, 00))) // 15:30–16:59
        ) {
            return 22;
        }

        return 0;
    }

    /**
     * Checks if a specific date and time is a toll-free date and time.
     *
     * @param date The date and time to check.
     * @return `true` if the date and time are toll-free; otherwise, `false`.
     */
    private boolean isTollFreeDate(LocalDateTime date) {

        int day = date.getDayOfMonth();
        int month = date.getMonthValue();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return true;
        }

        switch (month) {
            case 1: // January
                return day == 5 || day == 6;
            case 2: // February
                return false;
            case 3: // March
                return false;
            case 4: // April
                return day == 6 || day == 7 || day == 10;
            case 5: // May
                return day == 1 || day == 17 || day == 18;
            case 6: // June
                return day == 5 || day == 6 || day == 23;
            case 7: // July
                return true;
            case 8: // August
                return false;
            case 9: // September
                return false;
            case 10: // October
                return false;
            case 11: // November
                return day == 3;
            case 12: // December
                return day == 25 || day == 26;
            default:
                System.err.println("Invalid month");
                break;
        }
        return false;
    }

    private boolean isTollFreeVehicle(VehicleInterface vehicle) {
        if (vehicle == null)
            return false;
        String vehicleType = vehicle.getType();
        return vehicleType.equals(TollFreeVehicles.MOTORBIKE.getType()) ||
                vehicleType.equals(TollFreeVehicles.TRACTOR.getType()) ||
                vehicleType.equals(TollFreeVehicles.EMERGENCY.getType()) ||
                vehicleType.equals(TollFreeVehicles.DIPLOMAT.getType()) ||
                vehicleType.equals(TollFreeVehicles.FOREIGN.getType()) ||
                vehicleType.equals(TollFreeVehicles.MILITARY.getType());
    }

    private enum TollFreeVehicles {
        MOTORBIKE("Motorbike"),
        TRACTOR("Tractor"),
        EMERGENCY("Emergency"),
        DIPLOMAT("Diplomat"),
        FOREIGN("Foreign"),
        MILITARY("Military");

        private final String type;

        private TollFreeVehicles(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}

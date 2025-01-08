package com.project12.Frontend;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.project12.Backend.*;
import com.project12.Backend.TimeCalculators.TimeCalculator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AppControler {
    @FXML
    private TextField startPostCode;
    @FXML
    private TextField destinationPostCode;
    @FXML
    private ComboBox<String> vehicleChooser;
    @FXML
    private TextField distanceWalk1;
    @FXML
    private TextField distanceBus;
    @FXML
    private TextField distanceWalk2;
    @FXML
    private TextField timeWalk1;
    @FXML
    private TextField timeBus;
    @FXML
    private TextField timeWalk2;
    @FXML
    private TextField busNumber;
    @FXML
    private TextField exitStop;
    @FXML
    private TextField busDeparture;
    @FXML
    private VBox mapBox;
    @FXML
    private CheckBox showAllRoutes;
    @FXML
    private Label tourismLabel;
    @FXML
    private Label generalScoreLabel;
    @FXML
    private Label accesibilityPostCode;
    @FXML
    private TextField accesibilityInput;
    @FXML
    private Slider maxWalkingDistance;
    @FXML
    private Pane infoPane;
    @FXML
    private CheckBox toggleCheckBox;
    private final DataReader reader = new DataReader();
    private final ActualMap map = new ActualMap();
    private MapView mapView;
    private final DBConnectionSingleton dbConnection = DBConnectionSingleton.getDbConnection();
    private List<List<double[]>> allPossibleRoutes;
    private String routeShortName = "";
    private List<double[]> stops1;
    private List<double[]> stops2;
    private Coordinates[] coords = new Coordinates[]{};
    private double[] distances = new double[3];
    private Accessibility_Tourism accessibilityTourism = new Accessibility_Tourism();
    private AccessibilityScoreCalculator accessibilityScoreCalculator = new AccessibilityScoreCalculator();

    @FXML
    public void initialize() {
//        vehicleChooser.getItems().addAll("Choose Vehicle", "Walking", "Bicycle", "Car", "Helicopter", "Bus");
        vehicleChooser.getItems().addAll("Choose Vehicle", "Bus");
        distanceWalk1.setDisable(true);
        distanceBus.setDisable(true);
        distanceWalk2.setDisable(true);
        timeWalk1.setDisable(true);
        timeBus.setDisable(true);
        timeWalk2.setDisable(true);
        exitStop.setDisable(true);
        busNumber.setDisable(true);
        busDeparture.setDisable(true);
        initializeMap();
        allPossibleRoutes = findAllRoutes();

        infoPane.setVisible(toggleCheckBox.isSelected());

        // Listener for checkbox state changes
        toggleCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            infoPane.setVisible(newValue);
        });
    }

    private void initializeMap() {
        mapView = map.getMapView();
        mapBox.getChildren().add(mapView);
        VBox.setVgrow(mapView, Priority.ALWAYS);
    }

    @FXML
    public void searchButtonHandling() throws SQLException {
        coords = fetchCoordinates();
        double distance = calculateDistance(coords[0], coords[1]);
        updateDistanceUI(distance);
        handleVehicleSelection(distance);
        refreshMapView();
        if(vehicleChooser.getValue().equals((String)"Bus")) {
            processBusRoute(coords[0], coords[1]);
        } else {
            ErrorMessage.window("Error", "No vehicle chosen", "Please select a vehicle option and try again");
        }
    }

    private Coordinates[] fetchCoordinates() throws SQLException {
        String[] coords1Str = reader.search(PostcodeToDefault.PostcodeStandardised(startPostCode.getText()));
        String[] coords2Str = reader.search(PostcodeToDefault.PostcodeStandardised(destinationPostCode.getText()));
        Coordinates startCoords = new Coordinates(Double.parseDouble(coords1Str[0]), Double.parseDouble(coords1Str[1]));
        Coordinates endCoords = new Coordinates(Double.parseDouble(coords2Str[0]), Double.parseDouble(coords2Str[1]));
        return new Coordinates[]{startCoords, endCoords};
    }

    double calculateDistance(Coordinates start, Coordinates end) {
        return DistanceCalculator.calculateDistance(start, end);
    }

    private void updateDistanceUI(double distance) {
        String formattedDistance = String.format("%.2f km", distance);
        distanceBus.setText(formattedDistance);
    }

    private void refreshMapView() {
        mapBox.getChildren().remove(mapView);
        mapView = map.getMapView();
        mapBox.getChildren().add(mapView);
        VBox.setVgrow(mapView, Priority.ALWAYS);
    }

    private void handleVehicleSelection(double distance) throws SQLException {
        try {
            if (vehicleChooser.getValue().equals((String)"Choose Vehicle")) {
                ErrorMessage.window("Error!", "Vehicle Unselected", "Please choose a vehicle and try again.");
                return;
            }
        } catch (Exception e) {
            ErrorMessage.window("Error!", "Vehicle Unselected", "Please choose a vehicle and try again.");
                return;
        }
        
    }


    private void processBusRoute(Coordinates start, Coordinates end) throws SQLException {
        List<String> nearestStartStops = findNearestStop(start);
        List<String> nearestEndStops = findNearestStop(end);

        List<String> tripsFromStart = findRoutesFromStops(nearestStartStops);

        double minDistance1 = Integer.MAX_VALUE;
        double minDistance2 = Integer.MAX_VALUE;

        String bestRoute1 = "0 0 0";
        String bestStop1 = "0 0";

        String bestRoute2 = "0 0 0";
        String bestStop2 = "0 0";

        String bestTrip = "";
        boolean oneBusRoute = false;

        int i = 0;

        for (var trips : tripsFromStart) {
            i++;
            List<String> stopsForRoute   = dbConnection.readFromDB(
            "SELECT DISTINCT stop_lat, stop_lon, stop_id, trip_id, stop_sequence FROM all_route_stops WHERE trip_id = " + trips.split(" ")[0] + ";"
            );

            System.out.println(trips.split(" ")[0] + "\t" + i + "/" + tripsFromStart.size());

            for (var stop : stopsForRoute) {
                Coordinates stopCoords = new Coordinates(Double.parseDouble(stop.split(" ")[0]), Double.parseDouble(stop.split(" ")[1]));
                double distanceToEnd    = DistanceCalculator.calculateDistance(stopCoords, end);

                if(distanceToEnd < minDistance1){
                    minDistance1 = distanceToEnd;
                    bestRoute1   = trips;
                    bestTrip = stop.split(" ")[3];
                    bestStop1    = stop;
                }
            }

            if(minDistance1 < 0.8){
                oneBusRoute = true;
            }
        }

        String busStop1     = bestRoute1.split(" ")[1];
        String busStop2     = bestStop1;
        String busRoute12   = bestRoute1.split(" ")[0];

        System.out.println("busStop1 = " + busStop1);
        double distanceToStart = Integer.MAX_VALUE;
        minDistance1 = Integer.MAX_VALUE;
        var stopsForRoute   = dbConnection.readFromDB(
                "SELECT DISTINCT stop_lat, stop_lon, stop_id, trip_id, stop_sequence FROM all_route_stops WHERE trip_id = " + busRoute12 + ";"
                );
        System.out.println("Start");

        double distanceToEnd = Integer.MAX_VALUE;
        minDistance1 = Integer.MAX_VALUE;

        for (String row : stopsForRoute) {
            Coordinates stopCoords = new Coordinates(Double.parseDouble(row.split(" ")[0]), Double.parseDouble(row.split(" ")[1]));
            distanceToEnd  = DistanceCalculator.calculateDistance(stopCoords, end);
            
            System.out.println("Stop = " + row.split(" ")[2] 
                + "\t" + "distanceToStart = " + distanceToEnd 
                + "\tstopCoords = " + Double.parseDouble(row.split(" ")[0]) + " " + Double.parseDouble(row.split(" ")[1]) 
                + "\tstart" + start.getLat() + " " + start.getLon());

            if(minDistance1 > distanceToEnd){
                System.out.println("new stop found");
                busStop2 = row;
                minDistance1 = distanceToEnd;
            }
        }
        System.out.println("busStop1 = " + busStop1);
        System.out.println("busStop2 = " + busStop2);

        List<String> startStop1_coordsList = dbConnection.readFromDB(
                "SELECT stop_lat, stop_lon FROM all_route_stops WHERE stop_id = " + busStop1 + ";"
        );
        String startStop1_coords = startStop1_coordsList.get(0);

        double distanceOnlyWalk = DistanceCalculator.calculateDistance(start, end);
        double distanceWithBus  = DistanceCalculator.calculateDistance(start, 
                                                                        new Coordinates(Double.parseDouble(startStop1_coords.split(" ")[0]),
                                                                                        Double.parseDouble(startStop1_coords.split(" ")[1])))
                                + DistanceCalculator.calculateDistance(new Coordinates(Double.parseDouble(busStop2.split(" ")[0]), 
                                                                                        Double.parseDouble(busStop2.split(" ")[1])), 
                                                                        end);
        System.out.println("Dist: " + distanceOnlyWalk + " " + distanceWithBus + " " + DistanceCalculator.calculateDistance(start, 
        new Coordinates(Double.parseDouble(startStop1_coords.split(" ")[0]),
        Double.parseDouble(startStop1_coords.split(" ")[1]))) + " " + DistanceCalculator.calculateDistance(new Coordinates(Double.parseDouble(busStop2.split(" ")[0]), 
        Double.parseDouble(busStop2.split(" ")[1])),end));
        
        /*if(distanceOnlyWalk < distanceWithBus){
            displayStraightRoute(fetchCoordinates());
            return;
        }*/
        //=============================================================================================================================================================

        List<String> nearestBStops = findNearestStop(new Coordinates(Double.parseDouble(bestStop1.split(" ")[0]), Double.parseDouble(bestStop1.split(" ")[1])));
        List<String> tripsFromB = findRoutesFromStops(nearestBStops);

        distanceToStart = Integer.MAX_VALUE;
        distanceToEnd   = Integer.MAX_VALUE;
        
        String busStop3     = "";
        String busStop4     = "";
        if(oneBusRoute == false){

            for (var trips : tripsFromB) {
                i++;
                List<String> stopsForRoute2   = dbConnection.readFromDB(
                "SELECT DISTINCT stop_lat, stop_lon, stop_id, trip_id, stop_sequence FROM all_route_stops WHERE trip_id = " + trips.split(" ")[0] + ";"
                );

                System.out.println(trips.split(" ")[0] + "\t" + i + "/" + tripsFromStart.size());

                for (var stop : stopsForRoute2) {
                    Coordinates stopCoords = new Coordinates(Double.parseDouble(stop.split(" ")[0]), Double.parseDouble(stop.split(" ")[1]));
                    distanceToEnd    = DistanceCalculator.calculateDistance(stopCoords, end);

                    if(distanceToEnd < minDistance2 && Double.parseDouble(stop.split(" ")[4]) > Double.parseDouble(bestRoute2.split(" ")[2])){
                        minDistance2 = distanceToEnd;
                        bestRoute2   = trips;
                        bestTrip = stop.split(" ")[3];
                        bestStop2    = stop;
                    }
                }
            }

            busStop3     = bestRoute2.split(" ")[1];
            busStop4     = bestStop2;
            String busRoute34   = bestRoute2.split(" ")[0];

            System.out.println("busStop1 = " + busStop1);
            distanceToStart = Integer.MAX_VALUE;
            minDistance2 = Integer.MAX_VALUE;
            var stopsForRoute2   = dbConnection.readFromDB(
                    "SELECT DISTINCT stop_lat, stop_lon, stop_id, trip_id, stop_sequence FROM all_route_stops WHERE trip_id = " + busRoute34 + ";"
                    );
            System.out.println("Start");

            distanceToEnd = Integer.MAX_VALUE;
            minDistance2 = Integer.MAX_VALUE;

            for (String row : stopsForRoute2) {
                Coordinates stopCoords = new Coordinates(Double.parseDouble(row.split(" ")[0]), Double.parseDouble(row.split(" ")[1]));
                distanceToEnd  = DistanceCalculator.calculateDistance(stopCoords, end);
                
                System.out.println("Stop = " + row.split(" ")[2] 
                    + "\t" + "distanceToStart = " + distanceToEnd 
                    + "\tstopCoords = " + Double.parseDouble(row.split(" ")[0]) + " " + Double.parseDouble(row.split(" ")[1]) 
                    + "\tstart" + start.getLat() + " " + start.getLon());

                if(minDistance2 > distanceToEnd){
                    System.out.println("new stop found");
                    busStop4 = row;
                    minDistance2 = distanceToEnd;
                }
            }
        }

        if(busStop1 == "0" && busStop2 == "0" && busStop3 == "0" && busStop4 == "0"){
            ErrorMessage.window("Error", "No Direct Routs", "No direct bus routs to your destination");
            return;
        }
        
        System.out.println("     busStop1 = "    + busStop1 +
                        "\n     busStop2 = "     + busStop2  +   
                        "\n     bestRoute1 = "    + bestRoute1 +
                        "\n     bestStop1 = "     + bestStop1  +
                         "\n     bestRoute2 = "    + bestRoute2 +
                         "\n     bestStop2 = "     + bestStop2);
        
        
        List<String> commonRoutes = new ArrayList<>();
        // route_id startStop endStop

        if(!bestStop1.equals("0 0") && !bestStop2.equals("0 0") && !busStop1.equals(busStop2.split(" ")[2]) && (!bestRoute2.split(" ")[1].equals(bestStop2.split(" ")[2]))){
            System.out.println("my dick smells 1");
            commonRoutes.add(busRoute12 + " " + busStop1 + " " + busStop2.split(" ")[2]);
            commonRoutes.add(bestRoute2.split(" ")[0] + " " + bestRoute2.split(" ")[1] + " " + bestStop2.split(" ")[2]);
            if(!commonRoutes.isEmpty())
                System.out.println("my dick smells ggod");
                updateBusUI(commonRoutes.get(0) , commonRoutes.get(1));
            return;
        }
        
        if(!bestStop1.equals("0 0") && !busStop1.equals(busStop2.split(" ")[2])){
            System.out.println("my dick smells 2");
            commonRoutes.add(busRoute12.split(" ")[0] + " " + busStop1 + " " + busStop2.split(" ")[2]);
            System.out.println("com routes: " + commonRoutes.get(0));
            if(!commonRoutes.isEmpty())
                updateBusUI(commonRoutes.get(0));
            return;
        }
        System.out.println("my dick smells bbad");
        displayStraightRoute(fetchCoordinates());
    }


    private void updateBusUI(String commonRoute) throws SQLException {
        String[] routeDetails = commonRoute.split(" ");
        String trip_id = routeDetails[0];
        String startStop = routeDetails[1];
        String endStop = routeDetails[2];

        List<String> allStops = dbConnection.readFromDB(
                "SELECT DISTINCT stop_lat, stop_lon, stop_id, trip_id FROM all_route_stops WHERE trip_id = " + trip_id + ";"
        );
        stops1 = selectStops(startStop, endStop, allStops);
        System.out.println(stops1.size());
        String[] time = calculateTime(stops1).split(" ");
        Coordinates[] postCodes = fetchCoordinates();

        List<String> startStop_coordsList = dbConnection.readFromDB(
                "SELECT stop_lat, stop_lon FROM all_route_stops WHERE stop_id = " + startStop + ";"
        );
        String startStop_coords = startStop_coordsList.get(0);

        List<String> endStop_coordsList = dbConnection.readFromDB(
                "SELECT stop_lat, stop_lon FROM all_route_stops WHERE stop_id = " + endStop + ";"
        );
        String endStop_coords = endStop_coordsList.get(0);

        double walkingDistance1 = DistanceCalculator.calculateDistance(new Coordinates(postCodes[0].getLat(), postCodes[0].getLon()), new Coordinates(Double.parseDouble(startStop_coords.split(" ")[0]) , Double.parseDouble(startStop_coords.split(" ")[1])));
        String timeByFoot1 = timeToString(walkingDistance1, 5);
        double walkingDistance2 = DistanceCalculator.calculateDistance(new Coordinates(Double.parseDouble(endStop_coords.split(" ")[0]), Double.parseDouble(endStop_coords.split(" ")[1])), new Coordinates(postCodes[1].getLat(), postCodes[1].getLon()));
        String timeByFoot2 = timeToString(walkingDistance2, 5);

        String totalFootTime = sumTimes(timeByFoot1, timeByFoot2, "00:00:00");

        System.out.println("timeByFoot = " + totalFootTime);

        String timeWithBus = sumTimes("00:00:00", timeToString(distances[1], 5), timeToString(distances[2], 5));

        String totalTime = sumTimes(totalFootTime, timeWithBus, "00:00:00");

        System.out.println("Total time: " + totalTime);

        if (compareTimes(timeWithBus, totalFootTime) > 0 || stops1.size() == 0) {
            System.out.println(time[0]);
            System.out.println(timeToString(walkingDistance1, 5));
            System.out.println(timeToString(walkingDistance2, 5));
            displayStraightRoute(postCodes);
            return;
        }

        timeWalk1.setText(timeToString(distances[1], 5));
        timeBus.setText(time[0]);
        timeWalk2.setText(timeToString(distances[2], 5));
        busDeparture.setText(time[1]);

        //String totalTime = sumTimes(timeWalk1, timeBus, timeWalk2);

        //System.out.println("Total Time: " + totalTime);

        //routeShortName = dbConnection.readFromDB(
        //        "SELECT route_short_name FROM trips WHERE trip_id = " + trip_id + ";"
        //).get(0);

        displayBusRouteOne();
    }

    private void updateBusUI(String commonRoute1, String commonRoute2) throws SQLException {
        System.out.println("updateBusUI2");
        String[] routeDetails1  = commonRoute1.split(" ");
        String trip_id1         = routeDetails1[0];
        String startStop1       = routeDetails1[1];
        String endStop1         = routeDetails1[2];

        String[] routeDetails2  = commonRoute2.split(" ");
        String trip_id2         = routeDetails2[0];
        String startStop2       = routeDetails2[1];
        String endStop2         = routeDetails2[2];

        List<String> allStops1 = dbConnection.readFromDB(
                "SELECT DISTINCT stop_lat, stop_lon, stop_id, trip_id FROM all_route_stops WHERE trip_id = " + trip_id1 + ";"
        );

        List<String> allStops2 = dbConnection.readFromDB(
                "SELECT DISTINCT stop_lat, stop_lon, stop_id, trip_id FROM all_route_stops WHERE trip_id = " + trip_id2 + ";"
        );

        stops1 = selectStops(startStop1, endStop1, allStops1);
        stops2 = selectStops(startStop2, endStop2, allStops2);

        String[] time1 = calculateTime(stops1).split(" ");
        String[] time2 = calculateTime(stops2).split(" ");

        Coordinates[] postCodes = fetchCoordinates();

        List<String> startStop1_coordsList = dbConnection.readFromDB(
                "SELECT stop_lat, stop_lon FROM all_route_stops WHERE stop_id = " + startStop1 + ";"
        );
        String startStop1_coords = startStop1_coordsList.get(0);

        List<String> endStop1_coordsList = dbConnection.readFromDB(
                "SELECT stop_lat, stop_lon FROM all_route_stops WHERE stop_id = " + endStop1 + ";"
        );
        String endStop1_coords = endStop1_coordsList.get(0);

        List<String> startStop2_coordsList = dbConnection.readFromDB(
            "SELECT stop_lat, stop_lon FROM all_route_stops WHERE stop_id = " + startStop2 + ";"
        );
        String startStop2_coords = startStop2_coordsList.get(0);

        List<String> endStop2_coordsList = dbConnection.readFromDB(
            "SELECT stop_lat, stop_lon FROM all_route_stops WHERE stop_id = " + endStop2 + ";"
        );
        String endStop2_coords = endStop2_coordsList.get(0);

        double walkingDistance1 = DistanceCalculator.calculateDistance(new Coordinates(postCodes[0].getLat(), postCodes[0].getLon()),
                                                                    new Coordinates(Double.parseDouble(startStop1_coords.split(" ")[0]),
                                                                    Double.parseDouble(startStop1_coords.split(" ")[1])));
        System.out.println("Wlakingdistance1 = "+ walkingDistance1);

        double walkingDistance2 = DistanceCalculator.calculateDistance(new Coordinates(Double.parseDouble(endStop1_coords.split(" ")[0]),
                                                                                        Double.parseDouble(endStop1_coords.split(" ")[1])),
                                                                    new Coordinates(Double.parseDouble(startStop2_coords.split(" ")[0]),
                                                                                     Double.parseDouble(startStop2_coords.split(" ")[1])));

        double walkingDistance3 = DistanceCalculator.calculateDistance(new Coordinates(Double.parseDouble(endStop2_coords.split(" ")[0]), 
                                                                                        Double.parseDouble(endStop2_coords.split(" ")[1])), 
                                                                    new Coordinates(postCodes[1].getLat(), postCodes[1].getLon()));
        System.out.println("walking distance3 = " + walkingDistance3);


        String timeByFoot1 = timeToString(walkingDistance1, 5);
        String timeByFoot2 = timeToString(walkingDistance2, 5);
        String timeByFoot3 = timeToString(walkingDistance3, 5);
        String timeWithBus1 = sumTimes(time1[0], timeToString(distances[1], 5), timeToString(distances[2], 5));//TODO: check
        String timeWithBus2 = sumTimes(time2[0], timeToString(distances[1], 5), timeToString(distances[2], 5));

        String totalTime = sumTimes(timeWithBus1, timeWithBus2, sumTimes(timeByFoot1, timeByFoot2, timeByFoot3));

        System.out.println("Total Time: " + totalTime);

        timeWalk1.setText(timeToString(distances[1], 5));//TODO: check
        timeBus.setText(time1[0] + time2[0]);
        timeWalk2.setText(timeToString(distances[2], 5));
        busDeparture.setText(time1[1]);



        //routeShortName = dbConnection.readFromDB(
        //        "SELECT route_short_name FROM trips WHERE trip_id = " + trip_id + ";"
        //).get(0);

        displayBusRouteTwo();
    }

    public static int compareTimes(String time1, String time2) {
        LocalTime t1 = LocalTime.parse(correctTimeFormat(time1), DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime t2 = LocalTime.parse(correctTimeFormat(time2), DateTimeFormatter.ofPattern("HH:mm:ss"));

        return t1.compareTo(t2);
    }

    private static String correctTimeFormat(String time) {
        String[] parts = time.split(":");
        return String.format("%02d:%02d:%02d",
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]));
    }

    private String timeToString(double distance, double speed) {
        double timeInHours = distance / speed;

        int totalSeconds = (int) (timeInHours * 3600);

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        System.out.println(hours + " " + " " + minutes + " " + seconds);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private double[] getRouteDistance(List<double[]> stops) {
        double busDistance = 0;
        double distanceToStart = DistanceCalculator.calculateDistance(coords[0], new Coordinates(stops.get(0)[0], stops.get(0)[1]));
        double distanceToEnd = DistanceCalculator.calculateDistance(coords[0], new Coordinates(stops.get(stops.size() - 1)[0], stops.get(stops.size() - 1)[1]));

        if (distanceToStart < distanceToEnd) {
            distanceToEnd = DistanceCalculator.calculateDistance(coords[1], new Coordinates(stops.get(stops.size() - 1)[0], stops.get(stops.size() - 1)[1]));
        } else {
            distanceToStart = DistanceCalculator.calculateDistance(coords[1], new Coordinates(stops.get(0)[0], stops.get(0)[1]));
        }

        for (int i = 0; i < stops.size() - 1; i++) {
            double[] firstStop = stops.get(i);
            double[] secondStop = stops.get(i + 1);
            busDistance += DistanceCalculator.calculateDistance(new Coordinates(firstStop[0], firstStop[1]), new Coordinates(secondStop[0], secondStop[1]));
        }

        return new double[] {busDistance, distanceToStart, distanceToEnd};
    }

    String calculateTime(List<double[]> stops) {
//        String actualTime = getActualTime(); //TODO
        String actualTime = "11:00:00";
        double[] startStop = stops.get(0);
        String startTripId = String.valueOf((int) startStop[3]);
        String startStopId = String.valueOf((int) startStop[2]);

        double[] endStop = stops.get(stops.size() - 1);
        String endTripId = String.valueOf((int) endStop[3]);
        String endStopId = String.valueOf((int) endStop[2]);

        LocalTime startTime = LocalTime.parse(getDepartureTime(startTripId, startStopId).split(" ")[0]);
        LocalTime endTime = LocalTime.parse(getDepartureTime(endTripId, endStopId).split(" ")[0]);

        long secondsBetween = ChronoUnit.SECONDS.between(startTime, endTime);

        long hours = secondsBetween / 3600;
        long minutes = (secondsBetween % 3600) / 60;
        long seconds = secondsBetween % 60;

        System.out.println(startTripId + " " + startStopId + " " + actualTime);
        String departureTime = dbConnection.readFromDB("SELECT departure_time " +
                "FROM stop_times " +
                "WHERE stop_id=" + startStopId + " AND departure_time > '" + actualTime + "' " +
                "ORDER BY departure_time;").get(0);

        return hours + ":" + minutes + ":" + seconds + " " + departureTime;
    }

    public static String sumTimes(String time1, String time2, String time3) {
        LocalTime t1 = LocalTime.parse(correctTimeFormat(time1), DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime t2 = LocalTime.parse(correctTimeFormat(time2), DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime t3 = LocalTime.parse(correctTimeFormat(time3), DateTimeFormatter.ofPattern("HH:mm:ss"));

        Duration totalDuration = Duration.ofSeconds(t1.toSecondOfDay())
                .plusSeconds(t2.toSecondOfDay())
                .plusSeconds(t3.toSecondOfDay());

        long secondsInDay = totalDuration.getSeconds() % (24 * 3600);
        LocalTime summedTime = LocalTime.ofSecondOfDay(secondsInDay);

        return summedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private String getDepartureTime(String tripId, String stopId) {
        String departureTime = dbConnection.readFromDB("SELECT departure_time " +
                "FROM stop_times " +
                "WHERE trip_id=" + tripId + " AND stop_id=" + stopId + ";").get(0);
        return departureTime;
    }

    private String getActualTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime now = LocalTime.now();
        return now.format(formatter);
    }

    private List<List<double[]>> findAllRoutes() {
        List<String> allShapeIds = dbConnection.readFromDB("SELECT DISTINCT shape_id " +
                "FROM maastricht_shapes_buses;");
        List<List<double[]>> allPossibleRoutes = new ArrayList<>();
        for (int i = 0; i < allShapeIds.size(); i++) {
            String shapeId = allShapeIds.get(i);
            List<double[]> oneRoute = new ArrayList<>();
            List<String> allRoutes = dbConnection.readFromDB("SELECT shape_pt_lat, shape_pt_lon " +
                    "FROM maastricht_shapes " +
                    "WHERE shape_id =" + shapeId + ";");

            for (var route : allRoutes) {
                String[] data = route.split(" ");
                oneRoute.add(new double[] {Double.parseDouble(data[0]), Double.parseDouble(data[1])});
            }
            allPossibleRoutes.add(oneRoute);
        }
        return allPossibleRoutes;
    }

    private void displayStraightRoute(Coordinates[] coords) {
        MapPoint startPoint = new MapPoint(coords[0].getLat(), coords[0].getLon());
        MapPoint endPoint = new MapPoint(coords[1].getLat(), coords[1].getLon());

        String timeByFoot = timeToString(DistanceCalculator.calculateDistance(new Coordinates(coords[0].getLat(), coords[0].getLon()), new Coordinates(coords[1].getLat(), coords[1].getLon())), 5);

        String totalTime = timeByFoot;
        System.out.println("Total time: " + totalTime);

        map.setStartPoint(startPoint, Color.BLUE, 5);
        map.setPath(startPoint, endPoint, Color.BLACK, true);
        map.setDestinationPoint(endPoint, Color.BLACK, 3);
    }

    private void displayBusRouteOne() throws SQLException {
        double firstStopLat = 0;
        double firstStopLon = 0;
        double lastStopLat = 0;
        double lastStopLon = 0;

        Coordinates[] postcodes = fetchCoordinates();
        busNumber.setText(routeShortName);
        exitStop.setText(String.valueOf(stops1.size() - 1));

        Coordinates firstStop = new Coordinates(stops1.get(stops1.size() - 1)[0], stops1.get(stops1.size() - 1)[1]);
        double distanceCloserToTheBus1 = DistanceCalculator.calculateDistance(postcodes[0], firstStop);
        Coordinates lastStop = new Coordinates(stops1.get(0)[0], stops1.get(0)[1]);
        double distanceCloserToTheBus2 = DistanceCalculator.calculateDistance(postcodes[0], lastStop);
        if (distanceCloserToTheBus1 < distanceCloserToTheBus2){
            firstStopLat = stops1.get(stops1.size() - 1)[0];
            firstStopLon = stops1.get(stops1.size() - 1)[1];
            lastStopLat = stops1.get(0)[0];
            lastStopLon = stops1.get(0)[1];

            // First Bus:
            for (int i = 0; i < stops1.size() - 1; i++) {

                MapPoint startPoint2    = new MapPoint(stops1.get(  i  )[0], stops1.get(  i  )[1]);
                MapPoint endPoint2      = new MapPoint(stops1.get(i + 1)[0], stops1.get(i + 1)[1]);

                map.setStartPoint(startPoint2, Color.RED, 3);
                map.setDestinationPoint(endPoint2, Color.RED, 3);
                map.setPath(startPoint2, endPoint2, Color.RED, false);
            }

        } else if (distanceCloserToTheBus2 < distanceCloserToTheBus1) {
            firstStopLat = stops1.get(0)[0];
            firstStopLon = stops1.get(0)[1];
            lastStopLat = stops1.get(stops1.size() - 1)[0];
            lastStopLon = stops1.get(stops1.size() - 1)[1];

            // First Bus:
            for (int i = 0; i < stops1.size() - 1; i++) {

                MapPoint startPoint2    = new MapPoint(stops1.get(  i  )[0], stops1.get(  i  )[1]);
                MapPoint endPoint2      = new MapPoint(stops1.get(i + 1)[0], stops1.get(i + 1)[1]);

                map.setStartPoint(startPoint2, Color.RED, 3);
                map.setDestinationPoint(endPoint2, Color.RED, 3);
                map.setPath(startPoint2, endPoint2, Color.RED, false);
            }
        }


        // First Walk:
        MapPoint startPoint1 = new MapPoint(postcodes[0].getLat(), postcodes[0].getLon());
        MapPoint endPoint1   = new MapPoint(firstStopLat,firstStopLon);
        map.setStartPoint(startPoint1, Color.BLUE, 2);
        map.setDestinationPoint(endPoint1, Color.BLACK, 1);
        map.setPath(startPoint1, endPoint1, Color.BLACK,true);
        
        // Second Walk:
        MapPoint startPoint3 = new MapPoint(lastStopLat, lastStopLon);
        MapPoint endPoint3   = new MapPoint(postcodes[1].getLat(), postcodes[1].getLon());

        map.setStartPoint(startPoint3, Color.BLACK, 1);
        map.setDestinationPoint(endPoint3, Color.GREEN, 2);
        map.setPath(startPoint3, endPoint3, Color.BLACK,true);

    }

    private void displayBusRouteTwo() throws SQLException {
        double firstStopLat1 = 0;
        double firstStopLon1 = 0;
        double lastStopLat1 = 0;
        double lastStopLon1 = 0;

            Coordinates[] postcodes = fetchCoordinates();
            busNumber.setText(routeShortName);
            exitStop.setText(String.valueOf(stops2.size() - 1));

        Coordinates firstStop1 = new Coordinates(stops1.get(stops1.size() - 1)[0], stops1.get(stops1.size() - 1)[1]);
        double distanceCloserToTheBus1 = DistanceCalculator.calculateDistance(postcodes[0], firstStop1);
        Coordinates lastStop1 = new Coordinates(stops1.get(0)[0], stops1.get(0)[1]);
        double distanceCloserToTheBus2 = DistanceCalculator.calculateDistance(postcodes[0], lastStop1);
        if (distanceCloserToTheBus1 < distanceCloserToTheBus2){
            firstStopLat1 = stops1.get(stops1.size() - 1)[0];
            firstStopLon1 = stops1.get(stops1.size() - 1)[1];
            lastStopLat1 = stops1.get(0)[0];
            lastStopLon1 = stops1.get(0)[1];

            for (int i = 0; i < stops1.size() - 1; i++) {

                MapPoint startPoint2    = new MapPoint(stops1.get(  i  )[0], stops1.get(  i  )[1]);
                MapPoint endPoint2      = new MapPoint(stops1.get(i + 1)[0], stops1.get(i + 1)[1]);

                map.setStartPoint(startPoint2, Color.RED, 3);
                map.setDestinationPoint(endPoint2, Color.RED, 3);
                map.setPath(startPoint2, endPoint2, Color.RED, false);
            }

        } else if (distanceCloserToTheBus2 < distanceCloserToTheBus1) {
            firstStopLat1 = stops1.get(0)[0];
            firstStopLon1 = stops1.get(0)[1];
            lastStopLat1 = stops1.get(stops1.size() - 1)[0];
            lastStopLon1 = stops1.get(stops1.size() - 1)[1];

            for (int i = 0; i < stops1.size() - 1; i++) {

                MapPoint startPoint2    = new MapPoint(stops1.get(  i  )[0], stops1.get(  i  )[1]);
                MapPoint endPoint2      = new MapPoint(stops1.get(i + 1)[0], stops1.get(i + 1)[1]);

                map.setStartPoint(startPoint2, Color.RED, 3);
                map.setDestinationPoint(endPoint2, Color.RED, 3);
                map.setPath(startPoint2, endPoint2, Color.RED, false);
            }

        }

        double firstStopLat2 = 0;
        double firstStopLon2 = 0;
        double lastStopLat2 = 0;
        double lastStopLon2 = 0;

        Coordinates firstStop2 = new Coordinates(stops2.get(stops2.size() - 1)[0], stops2.get(stops2.size() - 1)[1]);
        double distanceCloserToTheBus3 = DistanceCalculator.calculateDistance(lastStop1, firstStop2);
        Coordinates lastStop2 = new Coordinates(stops2.get(0)[0], stops2.get(0)[1]);
        double distanceCloserToTheBus4 = DistanceCalculator.calculateDistance(lastStop1, lastStop2);
        if (distanceCloserToTheBus3 < distanceCloserToTheBus4){
            firstStopLat2 = stops2.get(stops2.size() - 1)[0];
            firstStopLon2 = stops2.get(stops2.size() - 1)[1];
            lastStopLat2 = stops2.get(0)[0];
            lastStopLon2 = stops2.get(0)[1];

            // Second Bus:
            for (int i = 0; i < stops2.size() - 1; i++) {

                MapPoint startPoint4    = new MapPoint(stops2.get(  i  )[0], stops2.get(  i  )[1]);
                MapPoint endPoint4      = new MapPoint(stops2.get(i + 1)[0], stops2.get(i + 1)[1]);

                map.setStartPoint(startPoint4, Color.BLUE, 3);
                map.setDestinationPoint(endPoint4, Color.BLUE, 3);
                map.setPath(startPoint4, endPoint4, Color.BLUE, false);
            }

        } else if (distanceCloserToTheBus4 < distanceCloserToTheBus3) {
            firstStopLat2 = stops2.get(0)[0];
            firstStopLon2 = stops2.get(0)[1];
            lastStopLat2 = stops2.get(stops2.size() - 1)[0];
            lastStopLon2 = stops2.get(stops2.size() - 1)[1];

            // Second Bus:
            for (int i = stops2.size() - 1; i >= 1; i--) {

                MapPoint startPoint4    = new MapPoint(stops2.get(  i  )[0], stops2.get(  i  )[1]);
                MapPoint endPoint4      = new MapPoint(stops2.get( i - 1 )[0], stops2.get( i - 1 )[1]);

                map.setStartPoint(startPoint4, Color.BLUE, 3);
                map.setDestinationPoint(endPoint4, Color.BLUE, 3);
                map.setPath(startPoint4, endPoint4, Color.BLUE, false);
            }

        }

            // First Walk:
            MapPoint startPoint1 = new MapPoint(postcodes[0].getLat(), postcodes[0].getLon());
            MapPoint endPoint1   = new MapPoint(firstStopLat1, firstStopLon1);

            map.setStartPoint(startPoint1, Color.BLUE, 2);
            map.setDestinationPoint(endPoint1, Color.BLACK, 1);
            map.setPath(startPoint1, endPoint1, Color.BLACK,true);
            
            // Second Walk:
            MapPoint startPoint3 = new MapPoint(lastStopLat1, lastStopLon1);
            MapPoint endPoint3   = new MapPoint(firstStopLat2, firstStopLon2);

            map.setStartPoint(startPoint3, Color.BLACK, 1);
            map.setDestinationPoint(endPoint3, Color.BLACK, 1);
            map.setPath(startPoint3, endPoint3, Color.BLACK,true);

            // Third Walk:
            MapPoint startPoint5 = new MapPoint(lastStopLat2, lastStopLon2);
            MapPoint endPoint5   = new MapPoint(postcodes[1].getLat(), postcodes[1].getLon());

            map.setStartPoint(startPoint5, Color.BLACK, 1);
            map.setDestinationPoint(endPoint5, Color.GREEN, 2);
            map.setPath(startPoint5, endPoint5, Color.BLACK,true);
    }

    private void displayAllRoutes(List<List<double[]>> allRoutes) {
        for (var route : allRoutes) {
            Color color = getRandomColor();
            for (int i = 0; i < route.size() - 1; i++) {
                MapPoint startPoint = new MapPoint(route.get(i)[0], route.get(i)[1]);
                MapPoint endPoint = new MapPoint(route.get(i + 1)[0], route.get(i + 1)[1]);
                map.setStartPoint(startPoint, color, 1);
                map.setDestinationPoint(endPoint, color, 1);
                map.setPath(startPoint, endPoint, color,false);
            }
        }
    }

    private Color getRandomColor() {
        Random rand = new Random();

        double red = rand.nextDouble();
        double green = rand.nextDouble();
        double blue = rand.nextDouble();

        return new Color(red, green, blue, 1.0);
    }


    List<double[]> selectStops(String startStop, String endStop, List<String> allStops) {
        List<double[]> stops = new ArrayList<>();
        boolean foundStop = false;
        for (var i : allStops) {
            String[] data = i.split(" ");

            if (foundStop) {
                stops.add(new double[] {Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3])});
            }

            if ((data[2].equals(startStop) || data[2].equals(endStop)) && !foundStop) {
                foundStop = true;
                stops.add(new double[] {Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3])});
                continue;
            }

            if (data[2].equals(startStop) || data[2].equals(endStop)) {
                break;
            }
        }
        return stops;
    }

    private List<String> findRoutesFromStops(List<String> stops) throws SQLException {
        List<String> routes = new ArrayList<>();
        for (String stopId : stops) {
            List<String> foundRoutes = dbConnection.readFromDB(
                    "SELECT MIN(trip_id), MIN(stop_id) AS stop_id, MIN(stop_sequence) AS stop_sequence, route_id FROM all_route_stops WHERE stop_id = '" + stopId + "' GROUP BY route_id;"
            );
            routes.addAll(foundRoutes);
        }
        return routes;
    }

    /*private List<String> findCommonRoutes(List<String> routesFromStart, List<String> routesFromEnd) {
        Map<String, String> startRouteToStopMap = new HashMap<>();
        for (String routeStopPair : routesFromStart) {
            String[] parts = routeStopPair.split(" ");
            startRouteToStopMap.put(parts[0], parts[1]);
        }

        List<String> commonRoutesWithStops = new ArrayList<>();
        for (String routeStopPair : routesFromEnd) {
            String[] parts = routeStopPair.split(" ");
            String routeId = parts[0];
            String stopIdEnd = parts[1];

            if (startRouteToStopMap.containsKey(routeId)) {
                String stopIdStart = startRouteToStopMap.get(routeId);
                commonRoutesWithStops.add(routeId + " " + stopIdStart + " " + stopIdEnd);
            }
        }

        return commonRoutesWithStops;
    }*/


    private List<String> findNearestStop(Coordinates coords) {
        List<String> stops = dbConnection.readFromDB("SELECT DISTINCT stop_lat, stop_lon, stop_id FROM maastricht_stops");
        List<String[]> stopsWithDistance = new ArrayList<>();
        List<String> bestStops = new ArrayList<>();

        for (int i = 0; i < stops.size(); i++) {
            String[] stopData = stops.get(i).split(" ");
            Coordinates stopCoordinates = new Coordinates(Double.parseDouble(stopData[0]), Double.parseDouble(stopData[1]));
            double distance = DistanceCalculator.calculateDistance(stopCoordinates, coords);
            stopsWithDistance.add(new String[] {stopData[0], stopData[1], stopData[2], String.valueOf(distance)});
        }

        stopsWithDistance.sort(new Comparator<String[]>() {
            public int compare(String[] s1, String[] s2) {
                return Double.compare(Double.parseDouble(s1[3]), Double.parseDouble(s2[3]));
            }
        });

        for (var i : stopsWithDistance) {
            if (Double.parseDouble(i[3]) < maxWalkingDistance.getValue()) {
                bestStops.add(i[2]);
            }
        }

        return bestStops;
    }

    @FXML
    public void onAccesibilitySearch() throws SQLException {
        String postCode = accesibilityInput.getText();
        accesibilityPostCode.setText(postCode);
        tourismLabel.setText(accessibilityTourism.getScore(postCode));
        generalScoreLabel.setText(accessibilityScoreCalculator.getTotalScore(postCode));
    }

    @FXML
    public void showRoutesHandling() throws SQLException {
        refreshMapView();
        if (showAllRoutes.isSelected() && allPossibleRoutes != null) {
            displayAllRoutes(allPossibleRoutes);
            busNumber.setText("");
            exitStop.setText("");
            vehicleChooser.setValue("Choose Vehicle");
        } else if (!startPostCode.getText().isEmpty() && !destinationPostCode.getText().isEmpty()){
            displayBusRouteOne();
        }
    }

    @FXML
    public void clearButtonHandling() {
        map.clearPoints();
        startPostCode.setText("");
        destinationPostCode.setText("");
        distanceWalk1.setText("");
        distanceBus.setText("");
        distanceWalk2.setText("");
        timeWalk1.setText("");
        timeBus.setText("");
        timeWalk2.setText("");
        busNumber.setText("");
        exitStop.setText("");
        refreshMapView();
        vehicleChooser.setValue("Choose Vehicle");
    }
}
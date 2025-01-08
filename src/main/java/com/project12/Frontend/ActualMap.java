package com.project12.Frontend;


import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.project12.Frontend.MapMarkers.CircleMarker;
import com.project12.Frontend.MapMarkers.PathMarker;
import javafx.scene.paint.Color;

public class ActualMap {

    private CircleMarker startMarker;
    private CircleMarker destinationMarker;
    private MapPoint startPoint;
    private MapPoint endPoint;
    private PathMarker line;
    private final MapPoint maastrichtMarkt = new MapPoint(50.8513, 5.6908);
    private MapView mapView;

    private MapView createMapView() {
        mapView = new MapView();
        mapView.setPrefSize(500, 400);
        mapView.setZoom(12);
        mapView.flyTo(0, maastrichtMarkt, 0.1);
        return mapView;
    }

    public MapView getMapView() {
        return createMapView();
    }

    public void setStartPoint(MapPoint point, Color color, int size) {
        this.startPoint = point;
        this.startMarker = new CircleMarker(point, color, size);
        mapView.addLayer(startMarker);
    }

    public void setDestinationPoint(MapPoint point, Color color, int size) {
        this.endPoint = point;
        this.destinationMarker = new CircleMarker(point, color, size);
        mapView.addLayer(destinationMarker);
    }

    public void setPath(MapPoint start, MapPoint end, Color color,Boolean byFoot) {
        this.line = new PathMarker(start, end, color, byFoot);
        mapView.addLayer(line);
        mapView.flyTo(0, getMiddlePoint(start, end), 0.1);
    }

    public MapPoint getMiddlePoint(MapPoint start, MapPoint end) {
        double middleX = (start.getLatitude() + end.getLatitude()) / 2.0;
        double middleY = (start.getLongitude() + end.getLongitude()) / 2.0;
        return new MapPoint(middleX, middleY);
    }

    public void clearPoints(){
        mapView.flyTo(0, maastrichtMarkt, 0.1);
        mapView.removeLayer(startMarker);
        mapView.removeLayer(destinationMarker);
        mapView.removeLayer(line);
    }
}

package com.project12.Frontend.MapMarkers;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CircleMarker extends MapLayer {

    private final Node marker;
    private final MapPoint point;

    public CircleMarker(MapPoint point, Color color, int size) {
        marker = new Circle(size, color);
        getChildren().add(marker);
        this.point = point;
    }

    @Override
    protected void layoutLayer() {
        Point2D mapPoint = getMapPoint(point.getLatitude(), point.getLongitude());
        marker.setVisible(true);
        marker.setTranslateX(mapPoint.getX());
        marker.setTranslateY(mapPoint.getY());
    }
}

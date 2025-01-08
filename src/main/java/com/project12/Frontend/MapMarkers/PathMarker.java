package com.project12.Frontend.MapMarkers;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class PathMarker extends MapLayer {
    private final Line line;
    private final MapPoint start;
    private final MapPoint end;

    public PathMarker(MapPoint start, MapPoint end, Color color, boolean byFoot) {
        this.line = new Line();
        this.line.setStroke(color);
        this.line.setStrokeWidth(2.5);
        if (byFoot) {
            this.line.getStrokeDashArray().addAll(10.0, 5.0);
        }
        this.start = start;
        this.end = end;
        getChildren().add(line);
    }

    @Override
    protected void layoutLayer() {
        Point2D startPoint = getMapPoint(start.getLatitude(), start.getLongitude());
        Point2D endPoint = getMapPoint(end.getLatitude(), end.getLongitude());
        this.line.setStartX(startPoint.getX());
        this.line.setStartY(startPoint.getY());
        this.line.setEndX(endPoint.getX());
        this.line.setEndY(endPoint.getY());
        this.line.setVisible(true);
    }
}

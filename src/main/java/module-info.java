module com.example.project12fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.gluonhq.maps;
    requires java.sql;
    requires javafx.graphics;
    requires commons.math3;


    opens com.project12.Frontend to javafx.fxml;
    exports com.project12.Frontend;
}
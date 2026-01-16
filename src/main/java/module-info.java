module com.tlcsdm.figma2json {
    requires com.dlsc.preferencesfx;
    requires com.google.gson;
    requires java.prefs;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.net.http;

    opens com.tlcsdm.figma2json to javafx.fxml;
    opens com.tlcsdm.figma2json.ui to javafx.fxml;
    opens com.tlcsdm.figma2json.api to com.google.gson;

    exports com.tlcsdm.figma2json;
}
module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires org.json;
    
    opens org.main.quiz to javafx.fxml;
    exports org.main.quiz;
}